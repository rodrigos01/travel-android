package travel.vola.android.common.ui.preview

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "1 - Portrait Tablet",
    group = "Tablet",
    device = "spec:parent=pixel_tablet,orientation=portrait",
)
@Preview(name = "2 - Landscape Tablet", group = "Tablet", device = "id:pixel_tablet")
annotation class TabletPreview