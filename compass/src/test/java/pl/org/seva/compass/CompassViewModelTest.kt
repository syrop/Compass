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

package pl.org.seva.compass

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.*
import org.mockito.Mockito.`when`

import org.mockito.Mockito.mock
import pl.org.seva.compass.compass.CompassViewModel
import pl.org.seva.compass.compass.DestinationModel
import pl.org.seva.compass.compass.DirectionModel
import pl.org.seva.compass.location.LocationChannelFactory
import pl.org.seva.compass.rotation.RotationChannelFactory
import kotlin.coroutines.EmptyCoroutineContext

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class CompassViewModelTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun distanceTest() = runBlocking {

        var distance = 0.0
        var lastDistance = distance

        suspend fun progress() {
            delay(INTERVAL)
            assertTrue(distance > lastDistance)
            lastDistance = distance
        }

        val mockLocationChannelFactory: LocationChannelFactory = mock(LocationChannelFactory::class.java)
        val mockRotationChannelFactory: RotationChannelFactory = mock(RotationChannelFactory::class.java)

        val locationChannel = Channel<LatLng>(Channel.CONFLATED)
        val rotationChannel = Channel<Float>(Channel.CONFLATED)

        val job = launch(Dispatchers.IO) {
            locationChannel.send(HOME)
            var lat = HOME.latitude
            while (true) {
                delay(INTERVAL)
                lat += LATITUDE_STEP
                locationChannel.send(LatLng(lat, HOME.longitude))
            }
        }

        `when`(mockLocationChannelFactory.getLocationChannel()).thenReturn(locationChannel)
        `when`(mockRotationChannelFactory.getRotationChannel()).thenReturn(rotationChannel)

        val liveDataJob = Job()

        val vm = CompassViewModel(
                mockRotationChannelFactory,
                mockLocationChannelFactory,
                EmptyCoroutineContext + liveDataJob)
        vm.setDestination(DestinationModel(HOME, ""))

        val destination = vm.direction

        val distanceObserver = Observer<DirectionModel> {
            distance = it.distance
        }
        destination.observeForever(distanceObserver)
        delay(STABILIZE_DELAY)
        progress()
        progress()
        assertFalse(locationChannel.isClosedForReceive)
        assertFalse(locationChannel.isClosedForSend)
        liveDataJob.cancel()
        delay(STABILIZE_DELAY)
        assertTrue(locationChannel.isClosedForReceive)
        assertTrue(locationChannel.isClosedForSend)

        job.cancel()
    }

    companion object {
        val HOME = LatLng(52.233333, 21.016667)
        private const val LATITUDE_STEP = 0.001
        private const val STABILIZE_DELAY = 50L
        private const val INTERVAL = 100L
    }
}
