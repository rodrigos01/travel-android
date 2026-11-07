package travel.vola.android.model.genai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerateContentResponse
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import travel.vola.android.extensions.zonedDateTime

class GenAIRepositoryTest {

    private val logger: GenAIRepository.Logger = mock()

    private fun repositoryAnswering(text: String?): GenAIRepository {
        val response: GenerateContentResponse = mock { on { this.text } doReturn text }
        val model: GenerativeModel = mock {
            onBlocking { generateContent(any<String>()) } doReturn response
        }
        return GenAIRepository(logger, dailyItineraryModelFactory = { model })
    }

    private suspend fun GenAIRepository.generate() = genDailyItinerary(
        basicInformation = GenAIData.BasicInformation("Paris", "", null, GenAIData.GroupType.SOLO, 1),
        parameters = GenAIData.InitialParametersOptions(
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            "",
        ),
        followUpQuestions = emptyList(),
        itinerary = GenAIData.Itinerary(
            "Paris",
            "",
            GenAIData.DateResult(1, 6, 2025),
            GenAIData.DateResult(3, 6, 2025),
            emptyList(),
            emptyList(),
        ),
        lodgings = emptyList(),
        existingPlaces = emptyList(),
        itineraryType = GenAIData.ItineraryType.OPEN_ENDED,
        dates = listOf(zonedDateTime("2025-06-01T00:00:00+02:00")),
    )

    @Test
    fun `daily itinerary is parsed from the model's JSON`() = runTest {
        val result = repositoryAnswering("""{"numDays":1,"days":[{"date":"2025-06-01","sections":[]}]}""").generate()
        assertThat(result?.numDays).isEqualTo(1)
        assertThat(result?.days?.single()?.date).isEqualTo("2025-06-01")
    }

    @Test
    fun `no text from the model means no itinerary`() = runTest {
        assertThat(repositoryAnswering(null).generate()).isNull()
    }
}
