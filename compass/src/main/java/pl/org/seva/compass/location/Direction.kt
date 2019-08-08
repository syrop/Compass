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

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlin.math.*

suspend fun LatLng.toDirection(destination: LatLng) = withContext(Dispatchers.Default) {
    val distance = distance(this@toDirection, destination)
    val bearing = bearing(this@toDirection, destination)
    DirectionModel(distance.await(), bearing.await())
}

private fun CoroutineScope.distance(location: LatLng, destination: LatLng) = async {
    val dLat = Math.toRadians(destination.latitude - location.latitude)
    val dLon = Math.toRadians(destination.longitude - location.longitude)
    val radLatLoc = Math.toRadians(location.latitude)
    val radLatDest = Math.toRadians(destination.latitude)
    val a = sin(dLat / 2).pow(2) +
            sin(dLon / 2) * sin(dLon / 2) * cos(radLatLoc) * cos(radLatDest)
    val c = 2 * asin(sqrt(a))
    RADIUS_KM * c
}

private fun CoroutineScope.bearing(location: LatLng, destination: LatLng) = async {
    val longDiff = destination.longitude - location.longitude
    val y = sin(longDiff) * cos(destination.latitude)
    val x = cos(location.latitude) *
            sin(destination.latitude) - sin(location.latitude) *
            cos(destination.latitude) * cos(longDiff)
    ((Math.toDegrees(atan2(y, x)) + 360 ) % 360).toFloat()
}

private const val RADIUS_KM = 6371.0