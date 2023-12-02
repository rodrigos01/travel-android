package com.combah.travel2.ui.trip.eventlist.composable

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.combah.travel2.R
import com.combah.travel2.ui.theme.AppTheme

@Composable
fun MonthEventListItem(month: String, year: String) {
    ListItem(headlineContent = {
        Text(
            text = stringResource(
                R.string.month_event_title,
                month,
                year,
            )
        )
    })
}

@Composable
@Preview
fun MonthEventListItemPreview() {
    AppTheme {
        MonthEventListItem("May", "2024")
    }
}