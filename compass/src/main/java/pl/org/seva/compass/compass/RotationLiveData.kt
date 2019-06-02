package pl.org.seva.compass.compass

import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import pl.org.seva.compass.rotation.rotationChannelFactory

class RotationLiveData(private val scope: CoroutineScope) : LiveData<Float>() {

    private var rotationChannel: ReceiveChannel<Float>? = null
    private var rotationJob: Job? = null

    @ExperimentalCoroutinesApi
    override fun onActive() {
        rotationChannel = rotationChannelFactory.getRotationChannel()
        rotationJob = scope.launch {
            while (true) {
                postValue(rotationChannel!!.receive())
            }
        }
    }

    override fun onInactive() {
        rotationJob?.cancel()
        rotationChannel?.cancel()
    }
}
