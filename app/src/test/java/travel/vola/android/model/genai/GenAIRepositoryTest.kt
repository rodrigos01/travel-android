package travel.vola.android.model.genai

import com.google.firebase.ai.Chat
import com.google.firebase.ai.type.GenerateContentResponse
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify


class GenAIRepositoryTest {

    private val logger: GenAIRepository.Logger = mock()
    private val chat: Chat = mock()

    private val subject: GenAIRepository = GenAIRepository(logger, chatFactory = { chat })

    @Test
    fun `null parameters should trigger retry with non null type`() = runTest {
        val response: GenerateContentResponse = mock {
            on { functionCalls } doReturn emptyList()
        }
        chat.stub {
            onBlocking { sendMessage(any<String>()) } doReturn response
        }
        val result = subject.genInitialParametersOptions(
            GenAIData.BasicInformation(
                "",
                "",
                "",
                GenAIData.GroupType.SOLO,
                1,
            )
        )
        verify(logger, times(3)).error(any(), any(), any())
        verify(chat, times(3)).sendMessage(any<String>())
        assertThat(result).isNull()
    }

    @Test
    fun `null parameters should not trigger retry with null type`() = runTest {
        val response: GenerateContentResponse = mock {
            on { functionCalls } doReturn emptyList()
        }
        chat.stub {
            onBlocking { sendMessage(any<String>()) } doReturn response
        }
        val result = subject.genInitialParametersFollowUpQuestions(
            parameters = GenAIData.InitialParametersOptions(
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                ""
            )
        )
        assertThat(result).isNull()
        verify(logger, times(0)).error(any(), any(), any())
        verify(chat, mode = times(1)).sendMessage(any<String>())
    }

}