package pl.org.seva.compass.rotation

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking
import pl.org.seva.compass.main.init.instance

val rotationChannelFactory by instance<RotationChannelFactory>()

class RotationChannelFactory(ctx: Context) {

    private val manager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magneticSensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelSensor: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    @ExperimentalCoroutinesApi
    fun getRotationChannel(): ReceiveChannel<Float> = Channel<Float>(Channel.CONFLATED).also { channel ->
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
