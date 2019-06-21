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

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import kotlinx.android.synthetic.main.fr_destination_picker.*
import pl.org.seva.compass.R
import pl.org.seva.compass.compass.CompassViewModel
import pl.org.seva.compass.compass.DestinationModel
import pl.org.seva.compass.main.extension.*
import pl.org.seva.compass.main.init.KodeinVMFactory

class DestinationPickerFragment : Fragment(R.layout.fr_destination_picker) {

    private val viewModel
            by navGraphViewModels<CompassViewModel>(R.id.nav_graph) { KodeinVMFactory }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun onLocationChanged(l: DestinationModel?) {
            address set (l?.address ?: "")
            if (l != null) delete_location_fab.show() else delete_location_fab.hide()
        }

        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        delete_location_fab { viewModel.setDestination(null) }
        (viewModel.destination to this) { onLocationChanged(it) }

        createInteractiveMapHolder(R.id.map) {
            prefs = prefs(SHARED_PREFERENCES_TAG)
            onLocationSet = { viewModel.setDestination(it) }
        }.also { holder ->
            (holder.liveMap to this) { map ->
                enableMyLocationOnResume(map)
                (viewModel.destination to this) { holder.markPosition(it?.location) }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_ok -> back()
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.location_picker, menu)
    }

    companion object {
        private const val SHARED_PREFERENCES_TAG = "fragment_location_picker_preferences"
    }
}
