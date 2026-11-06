package travel.vola.android.test

import com.google.firebase.firestore.util.CustomClassMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull
import travel.vola.android.model.data.Trip
import travel.vola.android.model.firebase.FirebaseData
import travel.vola.android.model.firebase.toAppDataModel

/**
 * Trip documents as the backend writes them: the entities as the app stored them, plus the
 * `itinerary` travel-node's `buildItinerary` made of them. The files in `resources/trips` are
 * the trips of travel-node's `api/trips/itinerary/corpus.js`; regenerate them from there when
 * the builder's output changes:
 *
 *     const { buildItinerary } = await import("./api/trips/itinerary/builder.js");
 *     const { corpus } = await import("./api/trips/itinerary/corpus.js");
 *     JSON.stringify({ ownerId: "owner", ...corpus.europe, itinerary: buildItinerary(corpus.europe) }, null, 2)
 *
 * They are read the way the app reads Firestore documents: through Firestore's own object mapper.
 */
object BackendTrips {

    fun document(name: String): FirebaseData.Trip {
        val text = checkNotNull(javaClass.getResource("/trips/$name.json")) { "missing trip $name" }.readText()

        @Suppress("UNCHECKED_CAST")
        val map = Json.parseToJsonElement(text).toPlain() as Map<String, Any?>
        return CustomClassMapper.convertToCustomClass(map, FirebaseData.Trip::class.java, null)
            .copy(id = name)
    }

    fun trip(name: String): Trip = document(name).toAppDataModel()

    // What Firestore hands the mapper: Long for whole numbers, Double otherwise.
    private fun JsonElement.toPlain(): Any? = when (this) {
        is JsonNull -> null
        is JsonObject -> mapValues { it.value.toPlain() }
        is JsonArray -> map { it.toPlain() }
        is JsonPrimitive -> when {
            isString -> content
            else -> booleanOrNull ?: longOrNull ?: content.toDouble()
        }
    }
}
