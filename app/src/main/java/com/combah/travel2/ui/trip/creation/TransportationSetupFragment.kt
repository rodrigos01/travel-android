package com.combah.travel2.ui.trip.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import com.combah.travel2.ui.extensions.setContent
import com.combah.travel2.ui.trip.creation.composable.TransportationSetup
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransportationSetupFragment : Fragment() {

    private val viewModel: TransportationSetupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = AutoTransition()
        sharedElementReturnTransition = AutoTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return setContent {
            TransportationSetup(viewModel)
        }
    }
}
