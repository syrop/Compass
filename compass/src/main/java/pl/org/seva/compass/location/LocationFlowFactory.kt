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

package pl.org.seva.compass.location

import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

open class LocationFlowFactory(ctx: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(ctx)
    private val request = LocationRequest.create().apply {
        interval = INTERVAL
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private var lastLocation: LatLng? = null

    @ExperimentalCoroutinesApi
    open fun getLocationFlow() = callbackFlow<LatLng> {
        val lastLocationJob = GlobalScope.launch {
            lastLocation = getLastLocation()?.also { sendBlocking(it) }
        }
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastLocationJob.cancel()
                lastLocation = result.lastLocation.toLatLng().also { sendBlocking(it) }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.myLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }.buffer(Channel.CONFLATED)

    private suspend fun getLastLocation(): LatLng? = lastLocation ?:
            suspendCancellableCoroutine { continuation ->
                client.lastLocation.addOnSuccessListener {
                    continuation.resume(it?.toLatLng())
                }
            }

    private fun Location.toLatLng() = LatLng(latitude, longitude)

    companion object {
        private const val INTERVAL = 1000L
    }
}
