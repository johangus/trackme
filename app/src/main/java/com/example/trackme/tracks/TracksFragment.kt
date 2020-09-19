package com.example.trackme.tracks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trackme.R
import com.example.trackme.data.Track
import com.example.trackme.tracking.TrackingFragment
import com.example.trackme.tracks.ui.TracksScreen

class TracksFragment : Fragment() {

    private val viewModel: TracksViewModel by viewModels { TracksViewModelFactory(requireActivity().application) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val tracks by viewModel.tracks.observeAsState()

                TracksScreen(
                    tracks = tracks ?: emptyList(),
                    onTrackClick = this@TracksFragment::showTrack
                )
            }
        }
    }

    private fun showTrack(track: Track) {
        val params = Bundle().apply {
            putLong(TrackingFragment.TRACK_ID_EXTRA, track.id)
        }
        findNavController().navigate(R.id.tracking_route, params)
    }
}