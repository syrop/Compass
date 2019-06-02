@file:Suppress("EXPERIMENTAL_API_USAGE")

package pl.org.seva.compass.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.selects.select
import pl.org.seva.compass.R
import pl.org.seva.compass.main.extension.checkPermission
import pl.org.seva.compass.main.init.instance

val permissions by instance<Permissions>()

class Permissions {

    fun onRequestPermissionsResult(
            fragment: Fragment,
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {

        val vm = fragment.navGraphViewModels<ViewModel>(R.id.nav_graph).value
        val granted = vm.granted
        val denied = vm.denied

        infix fun String.onGranted(requestCode: Int) =
                granted.sendBlocking(PermissionResult(requestCode, this))

        infix fun String.onDenied(requestCode: Int) =
                denied.sendBlocking(PermissionResult(requestCode, this))

        if (grantResults.isEmpty()) {
            permissions.forEach { it onDenied requestCode }
        } else repeat(permissions.size) { id ->
            if (grantResults[id] == PackageManager.PERMISSION_GRANTED) {
                permissions[id] onGranted requestCode
            } else {
                permissions[id] onDenied requestCode
            }
        }
    }

    class ViewModel : androidx.lifecycle.ViewModel() {
        val granted by lazy { BroadcastChannel<PermissionResult>(DEFAULT_CAPACITY) }
        val denied by lazy { BroadcastChannel<PermissionResult>(DEFAULT_CAPACITY) }

        private fun CoroutineScope.watch(code: Int, request: PermissionRequest) = launch (Dispatchers.IO) {
            suspend fun PermissionResult.ifMatching(block: () -> Unit) {
                if (requestCode == code && permission == request.permission) {
                    withContext(Dispatchers.Main) {
                        block()
                    }
                }
            }
            val grantedSubscription = granted.openSubscription()
            val deniedSubscription = denied.openSubscription()

            try {
                while (true) {
                    select<Unit> {
                        grantedSubscription.onReceive {
                            it.ifMatching { request.onGranted() }
                        }
                        deniedSubscription.onReceive {
                            it.ifMatching { request.onDenied() }
                        }
                    }
                }
            } finally {
                grantedSubscription.cancel()
                deniedSubscription.cancel()
            }
        }

        suspend fun watch(code: Int, requests: Array<PermissionRequest>) = coroutineScope {
            for (req in requests) {
                watch(code, req)
            }
        }

        companion object {
            private const val DEFAULT_CAPACITY = 10
        }
    }

    companion object {
        const val DEFAULT_PERMISSION_REQUEST_ID = 0
    }

    data class PermissionResult(val requestCode: Int, val permission: String)

    class PermissionRequest(
            val permission: String,
            val onGranted: () -> Unit = {},
            val onDenied: () -> Unit = {})
}

fun Fragment.requestLocationPermission(onGranted: () -> Unit) {
    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) onGranted()
    else request(
            Permissions.DEFAULT_PERMISSION_REQUEST_ID,
            arrayOf(Permissions.PermissionRequest(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    onGranted = onGranted)))
}

private fun Fragment.request(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    watch(requestCode, requests)
    requestPermissions(requests.map { it.permission }.toTypedArray(), requestCode)
}

private fun Fragment.watch(requestCode: Int, requests: Array<Permissions.PermissionRequest>) {
    lifecycleScope.launch {
        navGraphViewModels<Permissions.ViewModel>(R.id.nav_graph).value.watch(requestCode, requests)
    }
}
