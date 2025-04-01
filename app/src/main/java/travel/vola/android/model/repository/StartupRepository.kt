package travel.vola.android.model.repository

import io.ktor.http.HttpStatusCode
import travel.vola.android.model.data.ServerStatus
import travel.vola.android.model.network.get

class StartupRepository {
    suspend fun checkStatus(): ServerStatus {
        val response = get("/")
        return if (response.status == HttpStatusCode.OK) {
            ServerStatus.OK
        } else {
            ServerStatus.UNAVAILABLE
        }
    }
}