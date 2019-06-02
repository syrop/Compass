package pl.org.seva.compass.main.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

operator fun <T> LiveData<T>.invoke(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner) { observer(it) }

fun <T> LiveData<T>.observe(owner: LifecycleOwner, f: (T) -> Unit) =
        observe(owner, Observer<T> { f(it) })
