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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import pl.org.seva.compass.main.extension.invoke
import kotlinx.android.synthetic.main.fr_compass.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.org.seva.compass.R
import pl.org.seva.compass.main.extension.nav
import pl.org.seva.compass.main.extension.set
import pl.org.seva.compass.main.permissions
import pl.org.seva.compass.main.requestLocationPermission

class CompassFragment : Fragment(R.layout.fr_compass) {

    private val viewModel by navGraphViewModels<CompassViewModel>(R.id.nav_graph)

    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var lastBearing: Float? = null
        var lastRotation: Float? = null
        address { nav(R.id.action_compassFragment_to_destinationPickerFragment) }
        (viewModel.destination to this) { destination ->
            address set (destination?.address)
            if (destination == null) {
                distance_layout.visibility = View.GONE
                arrow.visibility = View.GONE
                viewModel.direction.removeObservers(this)
                lastBearing = null
            }
            else {
                requestLocationPermission {
                    distance_layout.visibility = View.VISIBLE
                    arrow.visibility = View.VISIBLE
                    (viewModel.direction to this) { direction ->
                        distance set direction.distance.toString()
                        lastRotation?.let { arrow.rotate(it - direction.degrees ) }
                        lastBearing = direction.degrees
                    }
                }
            }
        }
        (viewModel.rotation to this) { rotation ->
            lastBearing?.let { arrow.rotate(rotation - it) }
            compass.rotate(rotation)
            lastRotation = rotation
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray) =
            permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
}
