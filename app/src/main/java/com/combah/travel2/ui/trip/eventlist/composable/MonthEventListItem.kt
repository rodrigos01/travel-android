package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.theme.AppTheme
import java.text.DateFormatSymbols

@Composable
fun MonthEventListItem(event: MonthEvent) {
    ListItem(headlineContent = {
        Text(
            text = stringResource(
                R.string.month_event_title,
                DateFormatSymbols.getInstance().months[event.month],
                event.year
            )
        )
    })
}

@Composable
@Preview
fun MonthEventListItemPreview() {
    AppTheme {
        MonthEventListItem(event = MonthEvent(10, 2021))
    }
}