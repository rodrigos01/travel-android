package com.combah.travel2.ui.trip.eventlist.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.R

@Composable
fun AddPlan(addTransportationClickListener: () -> Unit, onClickClose: () -> Unit) {
    Column {
        Row {
            Text(
                text = stringResource(R.string.add_plan_title),
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .weight(1f),
                style = MaterialTheme.typography.subtitle1
            )
            Icon(
                Icons.Default.Close,
                contentDescription = "",
                modifier = Modifier
                    .clickable(onClick = onClickClose)
                    .padding(all = 16.dp)
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            PlanOption(
                imageRes = R.drawable.ic_flight_24dp,
                titleRes = R.string.add_transportation_option,
                modifier = Modifier
                    .clickable(onClick = addTransportationClickListener)
                    .padding(end = 8.dp)
                    .weight(1f)
            )
            PlanOption(
                imageRes = R.drawable.ic_hotel_black_24dp,
                titleRes = R.string.add_accommodation_option,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            )
            PlanOption(
                imageRes = R.drawable.ic_activity_24dp,
                titleRes = R.string.add_activity_option,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }
    }
}

@Composable
fun PlanOption(
    @DrawableRes imageRes: Int,
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Image(painter = painterResource(id = imageRes), contentDescription = "")
        Text(stringResource(titleRes), style = MaterialTheme.typography.caption)
    }
}

@Composable
@Preview
fun AddPlanPreview() {
    MaterialTheme {
        AddPlan(addTransportationClickListener = {}, onClickClose = {})
    }
}