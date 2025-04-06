package travel.vola.android.common.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil.compose.rememberAsyncImagePainter
import travel.vola.android.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageGallery(
    models: List<String>,
    modifier: Modifier = Modifier,
    selectedInitially: String? = null,
) {
    var selectedModel by rememberSaveable { mutableStateOf(selectedInitially) }
    val galleryScrollState = rememberScrollState()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val maxItemsPerLine = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 8
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 5
        else -> 3
    }

    AnimatedContent(selectedModel, contentKey = { it != null }) { selected ->
        if (selected == null) {
            ContextualFlowRow(
                models.size,
                maxItemsInEachRow = maxItemsPerLine,
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Top)
                    .verticalScroll(galleryScrollState)
                    .then(modifier)
                    .padding(horizontal = 16.dp),
            ) { index ->
                val width = maxWidthInLine / (maxItemsPerLine - indexInLine) - 8.dp
                GalleryItem(
                    model = models[index],
                    modifier = Modifier
                        .size(width)
                        .clickable { selectedModel = models[index] },
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxHeight()
                    .then(modifier)
            ) {
                TextButton(
                    onClick = { selectedModel = null },
                    modifier = Modifier
                        .padding(start = 8.dp),
                    colors = if (!isSystemInDarkTheme()) {
                        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inverseOnSurface)
                    } else {
                        ButtonDefaults.textButtonColors()
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                    Text(text = "All Photos", style = MaterialTheme.typography.labelLarge)
                }
                Image(
                    painter = rememberAsyncImagePainter(
                        selectedModel,
                        contentScale = ContentScale.Fit
                    ),
                    contentDescription = "Lodging Image Description",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxWidth()
                        .then(
                            if (LocalInspectionMode.current) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                            } else {
                                Modifier
                            }
                        )
                )
                val scrollState = rememberLazyListState()
                LazyRow(
                    state = scrollState,
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(models) { model ->
                        GalleryItem(
                            model = model,
                            modifier = Modifier
                                .size(96.dp)
                                .clickable { selectedModel = model }
                                .then(
                                    if (model == selected) Modifier.border(
                                        border = BorderStroke(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                        ), shape = MaterialTheme.shapes.large
                                    ) else Modifier
                                ),
                            colorFilter = if (model == selected) {
                                ColorFilter.tint(
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.3F),
                                    blendMode = BlendMode.SrcAtop,
                                )
                            } else {
                                null
                            }
                        )
                    }
                }
                val itemHalfSize = with(LocalDensity.current) { 48.dp.roundToPx() }
                val padding = with(LocalDensity.current) { 8.dp.roundToPx() }
                LaunchedEffect(selected) {
                    models.indexOf(selected).takeIf { it >= 0 }?.let { index ->
                        scrollState.animateScrollToItem(
                            index,
                            scrollOffset = -(scrollState.layoutInfo.viewportEndOffset / 2) + itemHalfSize + padding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItem(model: String, modifier: Modifier = Modifier, colorFilter: ColorFilter? = null) {
    Image(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        painter = rememberAsyncImagePainter(
            model,
            contentScale = ContentScale.Crop
        ),
        contentDescription = "Lodging Image Description",
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter,
    )
}

@Composable
@Preview
@TabletPreview
fun ImageGalleryPreview() {
    val models = List(46, { index ->
        "https://photo.hotellook.com/image_v2/limit/h374703_${index % 23}/1024/768.auto"
    })
    AppTheme {
        Surface {
            Overlay {
                Box {
                    ImageGallery(
                        models = models,
//                        selectedInitially = models.first(),
                        modifier = Modifier.padding(top = 96.dp)
                    )
                }
            }
        }
    }
}
