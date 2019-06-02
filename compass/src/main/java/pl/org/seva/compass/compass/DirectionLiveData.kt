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

package pl.org.seva.compass.compass

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import pl.org.seva.compass.location.locationChannelFactory
import kotlin.math.*

class DirectionLiveData(private val scope: CoroutineScope) : LiveData<DirectionModel>() {

    private var locationChannel: ReceiveChannel<LatLng>? = null
    private var locationJob: Job? = null
    lateinit var destinationLocation: LatLng

    @ExperimentalCoroutinesApi
    override fun onActive() {
        locationChannel = locationChannelFactory.getLocationChannel()
        locationJob = scope.launch {
            while (true) {
                val location = locationChannel!!.receive()
                val distance = distance(location)
                val bearing = bearing(location)
                postValue(DirectionModel(distance.await(), bearing.await()))
            }
        }
    }

    override fun onInactive() {
        locationJob?.cancel()
        locationChannel?.cancel()
    }

    private fun CoroutineScope.distance(location: LatLng) = async {
        val dLat = Math.toRadians(destinationLocation.latitude - location.latitude)
        val dLon = Math.toRadians(destinationLocation.longitude - location.longitude)
        val radLatLoc = Math.toRadians(location.latitude)
        val radLatDest = Math.toRadians(destinationLocation.latitude)
        val a = sin(dLat / 2).pow(2) +
                sin(dLon / 2) * sin(dLon / 2) * cos(radLatLoc) * cos(radLatDest)
        val c = 2 * asin(sqrt(a))
        RADIUS_KM * c
    }

    private fun CoroutineScope.bearing(location: LatLng) = async {
        val longDiff = destinationLocation.longitude - location.longitude
        val y = sin(longDiff) * cos(destinationLocation.latitude)
        val x = cos(location.latitude) *
                sin(destinationLocation.latitude) - sin(location.latitude) *
                cos(destinationLocation.latitude) * cos(longDiff)
        ((Math.toDegrees(atan2(y, x)) + 360 ) % 360).toFloat()
    }

    companion object {
        const val RADIUS_KM = 6371.0
    }
}
