package travel.vola.android.common.ui.state

data class MarkerViewState(
    val position: Pair<Double, Double>,
    val name: String,
    val type: MarkerType,
)

enum class MarkerType {
    City,
    Lodging,
}