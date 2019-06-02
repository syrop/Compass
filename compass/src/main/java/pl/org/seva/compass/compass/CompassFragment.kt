package pl.org.seva.compass.compass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import pl.org.seva.compass.main.extension.invoke
import kotlinx.android.synthetic.main.fr_compass.*
import pl.org.seva.compass.R
import pl.org.seva.compass.main.extension.nav
import pl.org.seva.compass.main.extension.set
import pl.org.seva.compass.main.permissions
import pl.org.seva.compass.main.requestLocationPermission


class CompassFragment : Fragment(R.layout.fr_compass) {

    private val viewModel by navGraphViewModels<CompassViewModel>(R.id.nav_graph)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var bearing: Float? = null
        address { nav(R.id.action_compassFragment_to_destinationPickerFragment) }
        (viewModel.destination to this) { destination ->
            address set (destination?.address)
            if (destination == null) {
                distance_layout.visibility = View.GONE
                arrow.visibility = View.GONE
                viewModel.direction.removeObservers(this)
            }
            else {
                requestLocationPermission {
                    distance_layout.visibility = View.VISIBLE
                    arrow.visibility = View.VISIBLE
                    (viewModel.direction to this) { direction ->
                        distance set direction.distance.toString()
                        bearing = direction.degrees
                    }
                }
            }
        }
        (viewModel.rotation to this) { rotation ->
            bearing?.let { arrow.rotate(rotation - it) }
            compass.rotate(rotation)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            requests: Array<String>,
            grantResults: IntArray) =
            permissions.onRequestPermissionsResult(this, requestCode, requests, grantResults)
}
