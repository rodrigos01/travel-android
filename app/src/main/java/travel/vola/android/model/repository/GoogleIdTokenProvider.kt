package travel.vola.android.model.repository

interface GoogleIdTokenProvider {
    suspend fun getIdToken(): String
}
