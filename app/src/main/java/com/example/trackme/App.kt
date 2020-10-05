package com.example.trackme

import android.os.Parcelable
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.viewinterop.viewModel
import androidx.ui.tooling.preview.Devices
import androidx.ui.tooling.preview.Preview
import com.example.trackme.ui.common.Navigation
import com.example.trackme.ui.tracking.TrackingViewModel
import com.example.trackme.ui.tracking.TrackingViewModelFactory
import com.example.trackme.ui.tracking.TrackingScreen
import com.example.trackme.ui.tracks.TracksViewModel
import com.example.trackme.ui.tracks.TracksViewModelFactory
import com.example.trackme.ui.tracks.TracksScreen
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Composable
fun App(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    requestLocationTracking: (cb: (newTrackId: Long) -> Unit) -> Unit,
    stopLocationTracking: () -> Unit
) {
    val navigator: Navigation<Destination> =
        rememberSavedInstanceState(saver = Navigation.saver(onBackPressedDispatcher)) {
            Navigation(onBackPressedDispatcher, Destination.Tracks)
        }

    Providers(NavigationAmbient provides navigator) {
        Crossfade(current = navigator.current) {
            when (it) {
                Destination.Tracks -> TracksScreenWrapper(requestLocationTracking)
                is Destination.Tracking -> TrackingScreenWrapper(it.trackId, stopLocationTracking)
            }
        }
    }
}

@Composable
@Preview(device = Devices.PIXEL_3, showDecoration = true, showBackground = true)
fun AppPreview() {
    App(OnBackPressedDispatcher(), {}, {})
}

@Composable
fun TrackingScreenWrapper(trackId: Long, stopLocationTracking: () -> Unit) {
    val viewModel = viewModel(
        modelClass = TrackingViewModel::class.java,
        factory = TrackingViewModelFactory(ContextAmbient.current.applicationContext)
    )
    onCommit(trackId) {
        viewModel.setTrackId(trackId)
    }

    val totalDistance by viewModel.totalDistance.observeAsState(0f)
    val trackStartedAt by viewModel.trackStartedAt.observeAsState()
    val activeTrack by viewModel.activeTrack.observeAsState()
    val trackEntries by viewModel.activeTrackEntries.observeAsState(emptyList())

    val startedAt = activeTrack?.let { track ->
        if (track.active) trackStartedAt ?: LocalDateTime.now()
        else null
    }

    TrackingScreen(
        onStopClick = stopLocationTracking,
        startedAt = startedAt,
        totalLength = totalDistance,
        currentSpeed = 0f,
        trackEntries = trackEntries,
    )
}

@Composable
fun TracksScreenWrapper(requestLocationTracking: (cb: (newTrackId: Long) -> Unit) -> Unit) {
    val viewModel =
        viewModel(
            modelClass = TracksViewModel::class.java,
            factory = TracksViewModelFactory(ContextAmbient.current.applicationContext)
        )
    val tracks by viewModel.tracks.observeAsState()
    val navigator = NavigationAmbient.current
    TracksScreen(
        tracks = tracks ?: emptyList(),
        onTrackClick = { track -> navigator.push(Destination.Tracking(track.id)) },
        onNewClick = {
            requestLocationTracking { newTrackId: Long ->
                navigator.push(Destination.Tracking(newTrackId))
            }
        }
    )
}

internal val NavigationAmbient = staticAmbientOf<Navigation<Destination>> {
    error("No navigation created")
}

sealed class Destination : Parcelable {

    @Parcelize
    object Tracks : Destination()

    @Parcelize
    class Tracking(val trackId: Long) : Destination()
}


