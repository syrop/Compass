package pl.org.seva.compass.location

import android.location.Geocoder
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import pl.org.seva.compass.compass.DestinationModel
import pl.org.seva.compass.main.init.instance
import java.lang.Exception

class InteractiveMapHolder : MapHolder() {

    private val geocoder by instance<Geocoder>()
    lateinit var onLocationSet: (DestinationModel) -> Unit

    override fun withMap(map: GoogleMap) {
        fun onMapLongClick(latLng: LatLng) {
            val address = try {
                with(geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1)[0]) {
                    getAddressLine(maxAddressLineIndex)
                }
            } catch (ex: Exception) {
                DEFAULT_ADDRESS
            }
            onLocationSet(DestinationModel(latLng, address))
        }

        super.withMap(map)
        map.setOnMapLongClickListener { onMapLongClick(it) }
    }

    companion object {
        const val DEFAULT_ADDRESS = ""
    }
}

fun Fragment.createInteractiveMapHolder(@IdRes map: Int, block: InteractiveMapHolder.() -> Unit = {}) =
        InteractiveMapHolder().apply(block).also {
            withMapHolder(it to map)
        }
