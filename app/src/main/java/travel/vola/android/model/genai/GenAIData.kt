package travel.vola.android.model.genai

import kotlinx.serialization.Serializable

object GenAIData {

    enum class GroupType {
        SOLO,
        FAMILY,
        FRIENDS,
        COWORKERS,
        COUPLE
    }

    @Serializable
    data class BasicInformation(
        val destination: String,
        val date: String,
        val duration: String,
        val groupType: GroupType,
        val travelers: Int,
    )

    @Serializable
    data class InitialParametersOptions(
        val occasions: List<String>,
        val interests: List<String>,
        val vibe: List<String>,
        val focus: List<String>,
        val mustHave: List<String>,
        val duration: List<String>,
    )
}