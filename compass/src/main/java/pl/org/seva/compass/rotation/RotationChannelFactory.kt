/*
 * Copyright (C) 2019 Wiktor Nizio
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you like this program, consider donating bitcoin: bc1qncxh5xs6erq6w4qz3a7xl7f50agrgn3w58dsfp
 */

package pl.org.seva.compass.rotation

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking

open class RotationChannelFactory(private val ctx: Context) {

    @Suppress("UNCHECKED_CAST")
    @ExperimentalCoroutinesApi
    open fun getRotationChannel(): ReceiveChannel<Float> =
            Channel<Float>(Channel.CONFLATED).also { channel ->
                val manager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val magneticSensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                val accelSensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                if (magneticSensor == null || accelSensor == null) return@also
                var valuesMagnet: FloatArray? = null
                var valuesAccel: FloatArray? = null
                val rotationMatrix = FloatArray(9)
                val orientation = FloatArray(3)

                val callback = object : SensorEventCallback() {
                    override fun onSensorChanged(event: SensorEvent) {
                        when (event.sensor.type) {
                            Sensor.TYPE_MAGNETIC_FIELD -> valuesMagnet = event.values
                            Sensor.TYPE_ACCELEROMETER -> valuesAccel = event.values
                        }

                        SensorManager.getRotationMatrix(
                                rotationMatrix,
                                null,
                                valuesAccel ?: return,
                                valuesMagnet ?: return)

                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = orientation[0]
                        channel.sendBlocking((azimuth / Math.PI * 180.0).toFloat())
                    }
                }
                manager.registerListener(callback, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
                manager.registerListener(callback, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)
                channel.invokeOnClose { manager.unregisterListener(callback) }
            }
}
