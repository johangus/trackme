package com.example.trackme

import android.app.Application
import androidx.lifecycle.*
import com.example.trackme.data.AppDatabase
import com.example.trackme.data.Track
import com.example.trackme.data.asLocation
import java.time.LocalDateTime

class TrackingViewModel(
    private val database: AppDatabase,
) : ViewModel() {
    private val activeTrack = MutableLiveData<Track?>(null)
    private var _trackStartedAt = MutableLiveData<LocalDateTime?>(null)
    val trackStartedAt: LiveData<LocalDateTime?> = _trackStartedAt
    val totalDistance: LiveData<Float> =
        Transformations.map(
            Transformations
                .switchMap(activeTrack) { track ->
                    track?.let {
                        database.trackEntryDao().getAllAndObserve(track.id)
                    } ?: MutableLiveData()
                }
        ) {
            it?.map { entry -> entry.asLocation() }
                ?.zipWithNext { l1, l2 -> l1.distanceTo(l2) }
                ?.sum() ?: 0F
        }

    suspend fun newTrack(): Track {
        val trackId = database.trackDao().insert(Track()).first()
        val track = database.trackDao().get(trackId)
        activeTrack.postValue(track)
        return track
    }

    fun startTracking() {
        _trackStartedAt.postValue(LocalDateTime.now())
    }

    fun stopTracking() {
        _trackStartedAt.postValue(null)
    }
}

@Suppress("UNCHECKED_CAST")
class TrackingViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val database = AppDatabase.getInstance(application)
        return TrackingViewModel(database) as T
    }
}

