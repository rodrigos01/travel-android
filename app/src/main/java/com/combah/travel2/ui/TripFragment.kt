package com.combah.travel2.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.widget.Text

class TripFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val tripId = TripFragmentArgs.fromBundle(arguments).tripId

        val componentContext = ComponentContext(context)
        val component = Text.create(componentContext)
            .text(tripId)
            .build()

        return LithoView.create(componentContext, component)
    }


}
