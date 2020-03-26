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

import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import pl.org.seva.compass.location.DestinationModel
import pl.org.seva.compass.location.LocationFlowFactory
import pl.org.seva.compass.location.toDirection
import pl.org.seva.compass.main.flowLiveData
import pl.org.seva.compass.rotation.RotationFlowFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CompassViewModel(
        rotationFlowFactory: RotationFlowFactory,
        locationFlowFactory: LocationFlowFactory,
        liveDataContext: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {

    private val mutableDestination by lazy { MutableLiveData<DestinationModel?>() }
    @ExperimentalCoroutinesApi
    val rotation by flowLiveData(liveDataContext) { rotationFlowFactory.getRotationFlow() }

    @ExperimentalCoroutinesApi
    val direction by flowLiveData(liveDataContext) {
            locationFlowFactory.getLocationFlow().map { it.toDirection(destinationLocation) }
    }
    val destination get() = mutableDestination as LiveData<DestinationModel?>

    private lateinit var destinationLocation: LatLng

    fun setDestination(destination: DestinationModel?) {
        if (destination != null) {
            destinationLocation = destination.location
        }
        mutableDestination.value = destination
    }
}
