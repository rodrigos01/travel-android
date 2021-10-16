package com.combah.travel2.ui.triplist


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.combah.travel2.ui.extensions.setContent
import com.combah.travel2.ui.triplist.composable.TripList
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalMaterialApi
class TripListFragment : Fragment() {

    private val viewModel: TripListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return setContent {
            val trips by viewModel.trips.observeAsState(emptyList())
            TripList(trips = trips, onItemClick = {
                val action =
                    TripListFragmentDirections.actionTripListFragmentToTripFragment(it.id)
                findNavController().navigate(action)
            })
        }
    }


}
