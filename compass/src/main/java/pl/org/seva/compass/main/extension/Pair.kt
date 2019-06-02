package pl.org.seva.compass.main.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

operator fun <T> Pair<LiveData<T>, LifecycleOwner>.invoke(observer: (T) -> Unit) = first(second, observer)
