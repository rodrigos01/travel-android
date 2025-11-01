package travel.vola.android.ui.trip.creation.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripDetailsToolbar(
    modifier: Modifier = Modifier,
    types: List<AddPlanType> = AddPlanType.entries,
    selectedType: AddPlanType? = null,
    onTypeSelected: (AddPlanType) -> Unit = {},
) {
    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier,
        expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab
    ) {
        types.forEach {
            ToolbarItem(
                selected = it == selectedType,
                onSelected = { onTypeSelected(it) },
                addPlanType = it,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolbarItem(
    selected: Boolean,
    onSelected: () -> Unit,
    addPlanType: AddPlanType,
) {
    TonalToggleButton(
        checked = selected, onCheckedChange = { onSelected() },
        colors = ToggleButtonDefaults.tonalToggleButtonColors(
            containerColor = FloatingToolbarDefaults.vibrantFloatingToolbarColors().toolbarContainerColor
        ),
    ) {
        AnimatedContent(selected, transitionSpec = {
            expandHorizontally(expandFrom = Alignment.Start).togetherWith(shrinkHorizontally(shrinkTowards = Alignment.Start))
        }) { expanded ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = addPlanType.icon(), contentDescription = addPlanType.label
                )

                if (expanded) {
                    Text(addPlanType.label)
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TripDetailsToolbarPreview() {
    var selectedType by remember { mutableStateOf<AddPlanType?>(null) }
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            TripDetailsToolbar(
                selectedType = selectedType,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                onTypeSelected = {
                    selectedType = it
                })
        }
    }
}