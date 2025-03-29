package travel.vola.android.common.ui.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", showSystemUi = true, device = "id:pixel_9_pro")
@Preview(
    name = "Dark",
    showSystemUi = true,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    device = "id:pixel_9_pro",
)
annotation class PreviewLightDarkSystemUI
