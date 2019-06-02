package pl.org.seva.compass.location

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.org.seva.compass.main.init.instance
import kotlin.coroutines.resume

val locationChannelFactory by instance<LocationChannelFactory>()

class LocationChannelFactory(ctx: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(ctx)
    private val request = LocationRequest.create().apply {
        interval = INTERVAL
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var lastLocation: LatLng? = null

    @ExperimentalCoroutinesApi
    fun getLocationChannel(): ReceiveChannel<LatLng> = Channel<LatLng>(Channel.CONFLATED).also { channel ->
        val lastLocationJob = GlobalScope.launch {
            lastLocation = getLastLocation()?.also { channel.sendBlocking(it) }
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastLocationJob.cancel()
                lastLocation = result.lastLocation.toLatLng().also { channel.sendBlocking(it) }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.myLooper())
        channel.invokeOnClose { client.removeLocationUpdates(callback) }
    }

    private suspend fun getLastLocation(): LatLng? = lastLocation ?:
            suspendCancellableCoroutine { continuation ->
                client.lastLocation.addOnSuccessListener(OnSuccessListener {
                    continuation.resume(it?.toLatLng())
                })
            }

    private fun Location.toLatLng() = LatLng(latitude, longitude)

    companion object {
        private const val INTERVAL = 1000L
    }
}
