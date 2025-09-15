package travel.vola.android.common.ui.state

data class MarkerViewState(
    val position: Pair<Double, Double>,
    val name: String,
    val type: MarkerType,
    val selected: Boolean = false,
)

enum class MarkerType {
    City,
    Lodging,
    Place,
    Restaurant,
}