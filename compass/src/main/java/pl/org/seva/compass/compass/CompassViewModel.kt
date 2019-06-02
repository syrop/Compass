package pl.org.seva.compass.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus

class CompassViewModel : ViewModel() {

    private val mutableDestination by lazy { MutableLiveData<DestinationModel?>() }
    val rotation by lazy { RotationLiveData(viewModelScope + Dispatchers.Default) }
    val direction by lazy { DirectionLiveData(viewModelScope + Dispatchers.Default) }
    val destination get() = mutableDestination as LiveData<DestinationModel?>

    fun setDestination(destination: DestinationModel?) {
        if (destination != null) {
            direction.destinationLocation = destination.location
        }
        mutableDestination.value = destination
    }
}
