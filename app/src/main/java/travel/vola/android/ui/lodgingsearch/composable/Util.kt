package travel.vola.android.ui.lodgingsearch.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import travel.vola.android.R

@Composable
fun Int.reviewCountString() = if (this < 1000) {
    pluralStringResource(R.plurals.review_count, this, this)
} else if (this < 1000000) {
    stringResource(R.string.review_count_thousands, this.toFloat() / 1000)
} else {
    stringResource(R.string.review_count_millions, this.toFloat() / 1000000)
}