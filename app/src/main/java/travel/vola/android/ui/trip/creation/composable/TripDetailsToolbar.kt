package travel.vola.android.ui.trip.creation.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import travel.vola.android.extensions.Time
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.eventlist.composable.AddPlanContent
import travel.vola.android.ui.trip.eventlist.composable.NoOpActionHandler
import travel.vola.android.ui.trip.eventlist.composable.uiType
import travel.vola.android.ui.trip.state.AddFlightItemState
import travel.vola.android.ui.trip.state.AddPlanItemState
import travel.vola.android.ui.trip.state.ManualAddPlanState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripDetailsToolbar(
    modifier: Modifier = Modifier,
    types: List<AddPlanType> = AddPlanType.entries,
    addPlanState: AddPlanItemState? = null,
    onTypeSelected: (AddPlanType) -> Unit = {},
) {
    val expanded = addPlanState != null
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .then(
                if (expanded) {
                    Modifier
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                } else {
                    Modifier.wrapContentSize()
                }
            ),
    ) {
        if (expanded) {
            AddPlanContent(state = addPlanState, actionHandler = NoOpActionHandler)
        }
        HorizontalFloatingToolbar(
            expanded = expanded,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            collapsedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
            expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevation,
            shape = if (expanded) RoundedCornerShape(0.dp) else FloatingToolbarDefaults.ContainerShape,
            modifier = if (expanded) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
            }
        ) {
            types.forEach {
                ToolbarItem(
                    selected = it == addPlanState?.uiType,
                    onSelected = { onTypeSelected(it) },
                    addPlanType = it,
                )
            }
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
            expandHorizontally(expandFrom = Alignment.Start).togetherWith(
                shrinkHorizontally(
                    shrinkTowards = Alignment.Start
                )
            )
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
    val timestamp = Time("2025-10-17T18:25 +0200")
    val state = AddFlightItemState(
        id = "flight",
        timestamp = timestamp,
        startState = ManualAddPlanState(
            dateTime = timestamp,
            minDateTime = null,
            dateSelectionEnabled = true,
            isTimeSet = false,
            locationText = null,
            searchResults = emptyList(),
        ),
        endState = ManualAddPlanState(
            dateTime = null,
            minDateTime = null,
            dateSelectionEnabled = true,
            isTimeSet = false,
            locationText = null,
            searchResults = emptyList(),
        ),
        typeSelectionEnabled = false,
        saveButtonEnabled = true,
        deleteButtonEnabled = false,
    )
    var currentState by remember { mutableStateOf<AddPlanItemState?>(null) }
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            Surface(onClick = { currentState = null }, modifier = Modifier.fillMaxSize()) {}
            TripDetailsToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                addPlanState = currentState,
                onTypeSelected = {
                    currentState = state
                })
        }
    }
}