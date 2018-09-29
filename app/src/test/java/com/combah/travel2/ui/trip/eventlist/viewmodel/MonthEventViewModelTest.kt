package com.combah.travel2.ui.trip.eventlist.viewmodel

import com.combah.travel2.R
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.widget.ResolvingString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*


class MonthEventViewModelTest {

    @Before
    fun setup() {
        Locale.setDefault(Locale("en", "US"))
    }

    @Test
    fun titleShouldShowMonthAndYear() {
        val event = MonthEvent(9, 2018)
        val viewModel = MonthEventViewModel(event)

        assertEquals(ResolvingString(R.string.month_event_title, "October", 2018), viewModel.title)
    }
}