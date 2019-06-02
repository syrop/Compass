package pl.org.seva.compass.main.extension

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap

fun Fragment.nav(@IdRes resId: Int): Boolean {
    findNavController().navigate(resId)
    return true
}

fun Fragment.back() = findNavController().popBackStack()

fun Fragment.prefs(name: String): SharedPreferences =
        context!!.getSharedPreferences(name, Context.MODE_PRIVATE)

fun Fragment.enableMyLocationOnResume(map: GoogleMap) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_RESUME && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                map.isMyLocationEnabled = true
            }
        }
    })
}

fun Fragment.checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(context!!, permission) == PackageManager.PERMISSION_GRANTED

