package travel.vola.android.ui.trip.creation.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TonalToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.common.ui.components.toPx
import travel.vola.android.extensions.zonedDateTime
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.usecase.AddPlanItemActionHandler
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
    addPlanActionHandler: AddPlanItemActionHandler,
    onTypeSelected: (AddPlanType) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        val spaceBetweenInPx = 8.dp.toPx().toInt()
        var toolbarSize by remember { mutableStateOf(IntSize.Zero) }
        val offset by animateDpAsState(
            if (addPlanState != null) 0.dp else FloatingToolbarDefaults.ContainerSize + 8.dp,
            animationSpec = if (addPlanState != null) {
                spring(visibilityThreshold = Dp.VisibilityThreshold)
            } else {
                tween(
                    delayMillis = 100,
                )
            },
        )
        AnimatedContent(
            targetState = addPlanState,
            transitionSpec = {
                (
                    fadeIn() + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) { height ->
                        height + spaceBetweenInPx
                    }
                    ) togetherWith (
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) { height ->
                        height + spaceBetweenInPx
                    } + fadeOut()
                    ) using SizeTransform { initialSize, targetSize ->
                    if (targetSize.height > initialSize.height) {
                        // enter
                        keyframes {
                            toolbarSize at 0
                            toolbarSize at durationMillis / 3
                            targetSize at durationMillis using EaseOutElastic
                        }
                    } else {
                        keyframes {
                            initialSize at 0
                            toolbarSize at durationMillis * 2 / 3 using EaseOutElastic
                            toolbarSize at durationMillis
                        }
                    }
                }
            },
            contentAlignment = Alignment.BottomCenter,
            contentKey = { it != null },
            modifier = Modifier
                .offset(x = 0.dp, y = offset)
                .padding(horizontal = 16.dp)
                .shadow(
                    FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
                    shape = MaterialTheme.shapes.extraLarge,
                )
                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(vertical = 16.dp),
        ) { state ->
            if (state != null) {
                Column {
                    AddPlanContent(state = state, actionHandler = addPlanActionHandler)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 8.dp, end = 16.dp)
                            .align(Alignment.End),
                    ) {
                        TextButton(
                            onClick = { addPlanActionHandler.cancelEdit(state.id) },
                            colors = ButtonDefaults.textButtonColors(),
                        ) {
                            Text(stringResource(R.string.action_cancel))
                        }
                        TextButton(onClick = { addPlanActionHandler.save(state.id) }, enabled = state.saveButtonEnabled) {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            }
        }
        HorizontalFloatingToolbar(
            expanded = true,
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
            modifier = Modifier.onGloballyPositioned {
                if (toolbarSize == IntSize.Zero) {
                    toolbarSize = it.size
                }
            },
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
        checked = selected,
        onCheckedChange = { onSelected() },
        colors = ToggleButtonDefaults.tonalToggleButtonColors(
            containerColor = FloatingToolbarDefaults.vibrantFloatingToolbarColors().toolbarContainerColor,
        ),
    ) {
        AnimatedContent(selected, transitionSpec = {
            expandHorizontally(expandFrom = Alignment.Start).togetherWith(
                shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                ),
            )
        }) { expanded ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = addPlanType.icon(),
                    contentDescription = addPlanType.label,
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
    val timestamp = zonedDateTime("2025-10-17T18:25 +0200")
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
    var currentState by remember { mutableStateOf<AddPlanItemState?>(state) }
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            Surface(onClick = { currentState = null }, modifier = Modifier.fillMaxSize()) {}
            TripDetailsToolbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                addPlanState = currentState,
                addPlanActionHandler = NoOpActionHandler,
                onTypeSelected = {
                    currentState = state
                },
            )
        }
    }
}
