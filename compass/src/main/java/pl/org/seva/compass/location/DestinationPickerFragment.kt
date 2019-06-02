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

class DestinationPickerFragment : Fragment(R.layout.fr_destination_picker) {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun onLocationChanged(l: DestinationModel?) {
            address set (l?.address ?: "")
            if (l != null) delete_location_fab.show() else delete_location_fab.hide()
        }

        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        val viewModel = navGraphViewModels<CompassViewModel>(R.id.nav_graph).value
        delete_location_fab { viewModel.setDestination(null) }
        (viewModel.destination to this) { onLocationChanged(it) }

        @Suppress("ReplaceSingleLineLet")
        createInteractiveMapHolder(R.id.map) {
            prefs = prefs(SHARED_PREFERENCES_TAG)
            onLocationSet = { viewModel.setDestination(it) }
        }.let { holder ->
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
