package com.combah.travel2.ui

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.combah.travel2.di.ViewModelFactory
import com.combah.travel2.ui.component.TripListSection
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.sections.SectionContext
import com.facebook.litho.sections.widget.RecyclerCollectionComponent
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var factory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProviders.of(this, factory)
            .get(TripListViewModel::class.java)

        val componentContext = ComponentContext(this)
        val component = RecyclerCollectionComponent.create(componentContext)
            .section(TripListSection.create(SectionContext(componentContext))
                .tripsLiveData(viewModel.trips)
                .lifecycleOwner(this)
                .build())
            .build()
        setContentView(LithoView.create(componentContext, component))

    }
}
