package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R
import com.combah.travel2.ui.data.MonthEvent
import com.combah.travel2.ui.theme.AppTheme
import java.text.DateFormatSymbols

@Composable
fun MonthEventListItem(event: MonthEvent) {
    Text(
        text = stringResource(
            R.string.month_event_title,
            DateFormatSymbols.getInstance().months[event.month],
            event.year
        ),
        modifier = Modifier.padding(all = 16.dp)
    )
}

@Composable
@Preview
fun MonthEventListItemPreview() {
    AppTheme {
        MonthEventListItem(event = MonthEvent(10, 2021))
    }
}