package pl.org.seva.compass.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.channels.ReceiveChannel

fun <T> channelLiveData(block: () -> ReceiveChannel<T>): Lazy<LiveData<T>> = lazy {
    liveData {
        val channel = block()
        try {
            while (true) {
                emit(channel.receive())
            }
        }
        finally {
            channel.cancel()
        }
    }
}
