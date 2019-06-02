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
