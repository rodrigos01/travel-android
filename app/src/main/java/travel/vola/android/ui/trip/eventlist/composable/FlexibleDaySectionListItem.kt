package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import travel.vola.android.R
import travel.vola.android.common.ui.components.OutlinedInlinedTextField
import travel.vola.android.common.ui.components.SearchBoxDialog
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.common.ui.components.rememberPlaceholderPainter
import travel.vola.android.ui.theme.AppTheme
import travel.vola.android.ui.trip.creation.composable.ConfirmationDialog
import travel.vola.android.ui.trip.state.TripItemState
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlexibleDaySectionListItem(
    state: TripItemState.FlexibleDaySectionState,
    modifier: Modifier = Modifier,
    highlightDate: Boolean = false,
    position: EventItemPosition = EventItemPosition.SINGLE,
    startExpanded: Boolean = false,
    onEditTapped: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onCategoryAdded: (String) -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int, Int) -> Unit,
    onNoteAdded: (Int, Int, String) -> Unit,
    onSuggestionConfirmed: () -> Unit,
    onSuggestionDismissed: () -> Unit,
) {
    val containerColor by animateColorAsState(if (state.isGenerated) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.secondaryContainer)
    val contentColor by animateColorAsState(if (state.isGenerated) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer)
    EventItem(
        showDate = state.showDate,
        highlightDate = highlightDate,
        dayOfMonthString = state.dayOfMonth,
        dayOfWeekString = state.dayOfWeek,
        position = position,
        style = if (state.isGenerated) EventItemStyle.Outlined else EventItemStyle.Filled,
        containerColor = containerColor,
        contentColor = contentColor,
    ) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        if (showDeleteConfirmation) {
            ConfirmationDialog(
                onConfirm = if (state.isGenerated) onSuggestionDismissed else onDeleteConfirmed,
                onDismiss = { showDeleteConfirmation = false },
                confirmButtonLabel = if (state.isGenerated) stringResource(R.string.action_dismiss) else stringResource(R.string.action_delete),
                confirmButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                dismissButtonLabel = stringResource(R.string.action_cancel),
            ) {
                if (state.isGenerated) {
                    Text(stringResource(R.string.dialog_dismiss_suggestion_confirmation, state.name))
                } else {
                    Text(stringResource(R.string.dialog_delete_item_confirmation, state.name))
                }
            }
        }
        var expanded by remember { mutableStateOf(startExpanded) }
        AnimatedContent(expanded) { isExpanded ->
            if (isExpanded) {
                ExpandedSection(
                    state,
                    onSuggestionConfirmed,
                    onEditTapped,
                    onDeleteTapped = { showDeleteConfirmation = true },
                    onCategoryAdded,
                    onLocationSearchTextChanged,
                    onLocationSearchResultSelected,
                    onNoteAdded,
                    onCollapseTapped = { expanded = false },
                    modifier = modifier,
                )
            } else {
                CollapsedSection(state, onTap = { expanded = true })
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ExpandedSection(
    state: TripItemState.FlexibleDaySectionState,
    onSuggestionConfirmed: () -> Unit,
    onEditTapped: () -> Unit,
    onDeleteTapped: () -> Unit,
    onCategoryAdded: (String) -> Unit,
    onLocationSearchTextChanged: (CharSequence) -> Unit,
    onLocationSearchResultSelected: (Int, Int) -> Unit,
    onNoteAdded: (Int, Int, String) -> Unit,
    onCollapseTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 8.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable(onClick = onCollapseTapped)
                    .weight(1f),
            ) {
                Text(
                    state.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    tint = LocalContentColor.current,
                    contentDescription = "collapse",
                )
            }
            IconButton(
                onClick = if (state.isGenerated) onSuggestionConfirmed else onEditTapped,
            ) {
                Icon(
                    if (state.isGenerated) Icons.Rounded.Check else Icons.Rounded.Edit,
                    contentDescription = null,
                )
            }
            IconButton(
                onClick = onDeleteTapped,
            ) {
                Icon(
                    if (state.isGenerated) Icons.Rounded.Close else Icons.Filled.Delete,
                    contentDescription = null,
                )
            }
        }
        var selectedCategoryIndex by remember { mutableIntStateOf(0) }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            itemsIndexed(state.categories) { index, category ->
                ToggleButton(
                    checked = index == selectedCategoryIndex,
                    onCheckedChange = {
                        selectedCategoryIndex = index
                    },
                    shapes = when (index) {
                        0 -> {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        }

                        state.categories.lastIndex -> {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        }

                        else -> {
                            ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                    },
                ) {
                    Text(category.name)
                }
            }
            if (!state.isGenerated) {
                item {
                    OutlinedInlinedTextField(onDone = onCategoryAdded) {
                        Text(stringResource(R.string.action_add_category))
                    }
                }
            }
        }
        val options = state.categories.getOrNull(selectedCategoryIndex)?.items ?: emptyList()
        var selectedPlaceIndex by remember(selectedCategoryIndex) {
            mutableIntStateOf(-1)
        }
        val selectedPlace = options.getOrNull(selectedPlaceIndex)
        val itemsExpanded = selectedPlace == null
        val itemScrollState = rememberLazyListState()
        LaunchedEffect(selectedPlaceIndex) {
            if (selectedPlaceIndex != -1) {
                itemScrollState.animateScrollToItem(selectedPlaceIndex)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            state = itemScrollState,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            itemsIndexed(options) { index, option ->
                val modifier = Modifier.clickable {
                    selectedPlaceIndex = index
                }
                AnimatedContent(itemsExpanded) { expand ->
                    if (expand) {
                        CategoryItem(option, modifier = modifier)
                    } else {
                        CollapsedCategoryItem(
                            option,
                            selected = option == selectedPlace,
                            modifier = modifier.animateItem(),
                        )
                    }
                }
            }
            if (!state.isGenerated && state.categories.isNotEmpty()) {
                item {
                    var showAddPlaceDialog by remember { mutableStateOf(false) }
                    AnimatedVisibility(visible = showAddPlaceDialog) {
                        SearchBoxDialog(
                            onDismiss = { showAddPlaceDialog = false },
                            searchResults = state.searchResults.map {
                                SearchResult(
                                    it.title,
                                    it.subtitle,
                                )
                            },
                            onLocationSearchTextChanged,
                            onLocationSearchResultSelected = {
                                onLocationSearchResultSelected(
                                    it,
                                    selectedCategoryIndex,
                                )
                            },
                        )
                    }

                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
                        if (itemsExpanded) {
                            OutlinedCard(
                                onClick = { showAddPlaceDialog = true },
                                modifier = Modifier.size(width = 96.dp, height = 144.dp),
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxHeight(),
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "add option",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1F),
                                    )
                                    Text(
                                        stringResource(R.string.action_add_place),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.CenterHorizontally),
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showAddPlaceDialog = true },
                                modifier = Modifier.height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                ),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = "add option",
                                    )
                                    Text(
                                        stringResource(R.string.action_add_place),
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        selectedPlace?.let { selected ->
            PlaceDetailsListItem(
                title = selected.title,
                subtitle = selected.subtitle,
                imageUrl = selected.imageUrl,
                note = selected.note,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                onNoteAdded = { onNoteAdded(selectedPlaceIndex, selectedCategoryIndex, it) },
            )
        }
    }
}

@Composable
private fun CollapsedSection(
    state: TripItemState.FlexibleDaySectionState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = LocalContentColor.current,
            supportingColor = LocalContentColor.current,
            leadingIconColor = LocalContentColor.current,
            overlineColor = LocalContentColor.current,
        ),
        leadingContent = {
            val icon = if (state.isGenerated) {
                Icons.Rounded.AutoFixHigh
            } else {
                Icons.Rounded.Explore
            }
            Icon(
                icon,
                tint = LocalContentColor.current,
                contentDescription = state.name,
            )
        },
        headlineContent = {
            Text(
                state.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        supportingContent = {
            if (state.subtitle.isNotBlank()) {
                Text(state.subtitle)
            }
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                tint = LocalContentColor.current,
                contentDescription = "expand",
            )
        },
        modifier = modifier.clickable(onClick = onTap),
    )
}

@Composable
private fun CategoryItem(option: TripItemState.SectionOption, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .size(width = 96.dp, height = 144.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
            ),
    ) {
        CategoryItemImage(
            option,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F),
        )
        Column(
            modifier = Modifier.padding(
                top = 0.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 8.dp,
            ),
        ) {
            Text(
                option.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                option.subtitle,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CollapsedCategoryItem(
    option: TripItemState.SectionOption,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val (containerColor, contentColor) = if (selected) {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(44.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(containerColor)
                .then(modifier),
        ) {
            CategoryItemImage(option, modifier = Modifier.size(44.dp))
            Text(
                option.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun CategoryItemImage(option: TripItemState.SectionOption, modifier: Modifier = Modifier) {
    AsyncImage(
        model = option.imageUrl,
        placeholder = rememberPlaceholderPainter(),
        contentScale = ContentScale.Crop,
        contentDescription = option.title,
        modifier = modifier
            .padding(4.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiary),
    )
}

@Composable
@Preview(group = "Generated")
fun FlexibleDaySectionListItemGeneratedCollapsedPreview() {
    FlexibleDaySectionListItemPreview(expanded = false, isGenerated = true)
}

@Composable
@Preview(group = "Generated")
fun FlexibleDaySectionListItemGeneratedPreview() {
    FlexibleDaySectionListItemPreview(expanded = true, isGenerated = true)
}

@Composable
@Preview(group = "Organic")
fun FlexibleDaySectionListItemCollapsedPreview() {
    FlexibleDaySectionListItemPreview(expanded = false)
}

@Composable
@Preview(group = "Organic")
fun FlexibleDaySectionListItemPreview(expanded: Boolean = true, isGenerated: Boolean = false) {
    AppTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            FlexibleDaySectionListItem(
                state = TripItemState.FlexibleDaySectionState(
                    id = "1",
                    timestamp = ZonedDateTime.now(),
                    dayOfMonth = "21",
                    dayOfWeek = "Wed",
                    showDate = true,
                    name = "Södermalm (SoFo) exploration",
                    subtitle = "Drop Cofee, Herr Judit, RAINS",
                    categories = listOf(
                        TripItemState.DaySectionCategory(
                            name = "☕ Work-base coffee shops",
                            items = listOf(
                                TripItemState.SectionOption(
                                    id = "1",
                                    title = "Drop Coffee",
                                    subtitle = "Wollmar Yxkullsgatan 10, 118 50 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
                                    note = "Popular laptop-friendly coffee shop near your hotel",
                                ),
                                TripItemState.SectionOption(
                                    id = "3",
                                    title = "Johan & Nyström - Swedenborgsgatan",
                                    subtitle = "Södermannagatan 23, 116 40 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
                                    note = "",
                                ),
                            ),
                        ),
                        TripItemState.DaySectionCategory(
                            name = "\uD83D\uDECD\uFE0F Shopping",
                            items = listOf(
                                TripItemState.SectionOption(
                                    id = "1",
                                    title = "Herr Judit",
                                    subtitle = "Hornsgatan 65, 118 49 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
                                    note = "",
                                ),
                                TripItemState.SectionOption(
                                    id = "2",
                                    title = "RAINS",
                                    subtitle = "Götgatan 42, 118 26 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
                                    note = "",
                                ),
                            ),
                        ),
                        TripItemState.DaySectionCategory(
                            name = "Other",
                            items = emptyList(),
                        ),
                    ),
                    searchResults = emptyList(),
                    isGenerated = isGenerated,
                ),
                startExpanded = expanded,
                highlightDate = true,
                onEditTapped = {},
                onDeleteConfirmed = {},
                onCategoryAdded = {},
                onLocationSearchTextChanged = {},
                onLocationSearchResultSelected = { _, _ -> },
                onNoteAdded = { _, _, _ -> },
                onSuggestionConfirmed = {},
                onSuggestionDismissed = {},
            )
        }
    }
}
