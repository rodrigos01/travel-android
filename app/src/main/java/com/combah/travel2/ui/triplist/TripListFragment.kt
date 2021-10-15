package com.combah.travel2.ui.triplist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.combah.travel2.databinding.FragmentTripListBinding
import com.combah.travel2.ui.triplist.composable.TripListItem
import org.koin.androidx.viewmodel.ext.android.viewModel

class TripListFragment : Fragment() {

    private val viewModel: TripListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentTripListBinding.inflate(inflater, container, false)

        binding.composeContainer.setContent {
            val trips by viewModel.trips.observeAsState(emptyList())
            LazyColumn {
                items(trips) { trip ->
                    TripListItem(name = trip.name, coverImageUrl = trip.coverImage, onClick = {
                        val action =
                            TripListFragmentDirections.actionTripListFragmentToTripFragment(trip.id)
                        findNavController().navigate(action)
                    })
                }
            }
        }

        return binding.root
    }


}
