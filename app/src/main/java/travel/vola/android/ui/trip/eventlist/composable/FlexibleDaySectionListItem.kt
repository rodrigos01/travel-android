package travel.vola.android.ui.trip.eventlist.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import travel.vola.android.common.ui.components.OutlinedInlinedTextField
import travel.vola.android.common.ui.components.SearchBoxDialog
import travel.vola.android.common.ui.components.SearchResult
import travel.vola.android.ui.genai.theme.GenAITheme
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
    onEditTapped: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
    onCategoryAdded: (String) -> Unit = {},
    onLocationSearchTextChanged: (CharSequence) -> Unit = {},
    onLocationSearchResultSelected: (Int, Int) -> Unit = { _, _ -> },
) {
    val colorScheme = if (state.isGenerated) {
        GenAITheme.colorScheme
    } else {
        MaterialTheme.colorScheme
    }
    EventItem(
        showDate = state.showDate,
        highlightDate = highlightDate,
        dayOfMonthString = state.dayOfMonth,
        dayOfWeekString = state.dayOfWeek,
        position = position,
        containerColor = colorScheme.secondaryContainer,
        contentColor = colorScheme.onSecondaryContainer,
    ) {
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        if (showDeleteConfirmation) {
            ConfirmationDialog(
                onConfirm = onDeleteConfirmed,
                onDismiss = { showDeleteConfirmation = false },
                confirmButtonLabel = "Delete",
                confirmButtonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                dismissButtonLabel = "Cancel"
            ) {
                Text("Delete ${state.name}?")
            }
        }
        MaterialTheme(colorScheme) {
            var expanded by remember { mutableStateOf(startExpanded) }
            AnimatedContent(expanded) { isExpanded ->
                if (isExpanded) {
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                bottom = 8.dp
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .clickable(onClick = { expanded = false })
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
                                onClick = onEditTapped,
                            ) {
                                Icon(
                                    if (state.isGenerated) Icons.Rounded.Check else Icons.Rounded.Edit,
                                    contentDescription = null
                                )
                            }
                            IconButton(
                                onClick = { showDeleteConfirmation = true },
                            ) {
                                Icon(
                                    if (state.isGenerated) Icons.Rounded.Close else Icons.Filled.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                        var selectedCategoryIndex by remember { mutableIntStateOf(0) }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            itemsIndexed(state.categories) { index, category ->
                                ToggleButton(
                                    checked = index == selectedCategoryIndex,
                                    onCheckedChange = { selectedCategoryIndex = index },
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
                                        Text("Add Category")
                                    }
                                }
                            }
                        }
                        val options =
                            state.categories.getOrNull(selectedCategoryIndex)?.items ?: emptyList()
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            items(options) { option ->
                                CategoryItem {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = option.imageUrl,
                                        ),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = option.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1F)
                                            .padding(4.dp)
                                            .clip(MaterialTheme.shapes.large)
                                            .background(MaterialTheme.colorScheme.tertiary),
                                    )
                                    Column(
                                        modifier = Modifier.padding(
                                            top = 0.dp,
                                            start = 8.dp,
                                            end = 8.dp,
                                            bottom = 8.dp
                                        )
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
                            if (!state.isGenerated && state.categories.isNotEmpty()) {
                                item {
                                    var showAddPlaceDialog by remember { mutableStateOf(false) }
                                    AnimatedVisibility(visible = showAddPlaceDialog) {
                                        SearchBoxDialog(
                                            onDismiss = { showAddPlaceDialog = false },
                                            searchResults = state.searchResults.map {
                                                SearchResult(
                                                    it.title,
                                                    it.subtitle
                                                )
                                            },
                                            onLocationSearchTextChanged,
                                            onLocationSearchResultSelected = {
                                                onLocationSearchResultSelected(
                                                    it,
                                                    selectedCategoryIndex
                                                )
                                            },
                                        )
                                    }
                                    OutlinedCard(
                                        onClick = { showAddPlaceDialog = true },
                                        modifier = Modifier.size(width = 96.dp, height = 144.dp)
                                    ) {
                                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
                                            Column(
                                                verticalArrangement = Arrangement.SpaceEvenly,
                                                modifier = Modifier.fillMaxHeight()
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Add,
                                                    contentDescription = "add option",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(1F)
                                                )
                                                Text(
                                                    "Add Place",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                } else {
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
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
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
                        modifier = modifier.clickable(onClick = { expanded = true })
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .size(width = 96.dp, height = 144.dp)
            .background(
                containerColor,
                shape = MaterialTheme.shapes.large
            ),
    ) {
        content()
    }
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
                .fillMaxWidth()
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
                                ),
                                TripItemState.SectionOption(
                                    id = "3",
                                    title = "Johan & Nyström - Swedenborgsgatan",
                                    subtitle = "Södermannagatan 23, 116 40 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
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
                                ),
                                TripItemState.SectionOption(
                                    id = "2",
                                    title = "RAINS",
                                    subtitle = "Götgatan 42, 118 26 Stockholm, Sweden",
                                    imageUrl = "https://picsum.photos/200/300",
                                ),
                            )
                        ),
                        TripItemState.DaySectionCategory(
                            name = "Other",
                            items = emptyList()
                        ),
                    ),
                    searchResults = emptyList(),
                    isGenerated = isGenerated,
                ),
                startExpanded = expanded,
                highlightDate = true,
            )
        }
    }
}