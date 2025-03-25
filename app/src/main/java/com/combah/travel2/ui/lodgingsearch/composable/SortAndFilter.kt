package com.combah.travel2.ui.lodgingsearch.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.combah.travel2.ui.lodgingsearch.viewmodel.LodgingSearchViewModel
import java.text.NumberFormat
import kotlin.math.roundToInt

@Composable
fun SortOptionSelector(
    state: LodgingSearchViewModel.SortAndFilterState,
    onSortOptionSelected: (LodgingSearchViewModel.SortOption) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .selectableGroup()
            .padding(16.dp)
    ) {
        LodgingSearchViewModel.SortOption.entries.forEach { option ->
            val selected = state.sortOption == option
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.selectable(
                    selected = selected,
                    onClick = {
                        onSortOptionSelected(option)
                    },
                )
            ) {
                RadioButton(selected = selected, onClick = null)
                val label = when (option) {
                    LodgingSearchViewModel.SortOption.BEST -> "Best"
                    LodgingSearchViewModel.SortOption.RATING -> "Rating"
                    LodgingSearchViewModel.SortOption.PRICE_LOW_TO_HIGH -> "Price: Low to High"
                    LodgingSearchViewModel.SortOption.PRICE_HIGH_TO_LOW -> "Price: High to Low"
                }
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

class FilterOptionsState(
    minRating: Double,
    minStars: Int,
    priceRange: ClosedFloatingPointRange<Double>,
) {
    var minRating: Float by mutableStateOf(minRating.toFloat())
    var minStars: Int by mutableStateOf(minStars)
    var priceRange: ClosedFloatingPointRange<Float> by mutableStateOf(priceRange.start.toFloat()..priceRange.endInclusive.toFloat())
}

@Composable
fun FilterOptions(
    state: FilterOptionsState,
    valueRange: ClosedFloatingPointRange<Double>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(16.dp),
    ) {
        ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
            Text("Rating")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val range = 0F..10F
                Slider(
                    value = state.minRating,
                    valueRange = range,
                    steps = range.steps(0.5F),
                    onValueChange = { state.minRating = it },
                    modifier = Modifier.weight(1F),
                )
                Text("%.1f+".format(state.minRating))
            }
            Text("Stars")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                LodgingSearchViewModel.StarOption.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            LodgingSearchViewModel.StarOption.entries.size
                        ),
                        selected = option.stars == state.minStars,
                        onClick = { state.minStars = option.stars },
                    ) {
                        val label = when (option) {
                            LodgingSearchViewModel.StarOption.ANY -> "Any"
                            LodgingSearchViewModel.StarOption.THREE -> "3+ stars"
                            LodgingSearchViewModel.StarOption.FOUR -> "4+ stars"
                            LodgingSearchViewModel.StarOption.FIVE -> "5 stars"
                        }
                        Text(text = label)
                    }
                }
            }
            Text("Price Range")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val currencyFormatter = NumberFormat.getCurrencyInstance()
                Text(currencyFormatter.format(state.priceRange.start))
                Text(currencyFormatter.format(state.priceRange.endInclusive))
            }
            val floatValueRange = valueRange.toFloatRange()
            RangeSlider(
                value = state.priceRange,
                valueRange = floatValueRange,
                steps = floatValueRange.steps(50F),
                onValueChange = { state.priceRange = it },
                colors = SliderDefaults.colors(
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                )
            )
        }
    }
}

private fun ClosedFloatingPointRange<Double>.toFloatRange(): ClosedFloatingPointRange<Float> =
    start.toFloat()..endInclusive.toFloat()

private fun ClosedFloatingPointRange<Float>.steps(increment: Float): Int =
    ((endInclusive - start) / increment).roundToInt() - 1

@Preview
@Composable
fun SortOptionSelectorPreview() {
    Surface {
        SortOptionSelector(LodgingSearchViewModel.SortAndFilterState(), onSortOptionSelected = {})
    }
}

@Preview
@Composable
fun FilterOptionsPreview() {
    Surface {
        FilterOptions(
            FilterOptionsState(
                0.0,
                0,
                0.0..6000.0
            ),
            valueRange = 0.0..6000.0,
        )
    }
}