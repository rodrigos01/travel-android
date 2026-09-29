package travel.vola.android.model.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import travel.vola.android.R

// CredentialManager.getCredential() wants an Activity context to anchor its
// picker UI to, not applicationContext - callers must pass one that's alive
// for at most as long as the call.
class CredentialManagerGoogleIdTokenProvider(private val context: Context) : GoogleIdTokenProvider {

    override suspend fun getIdToken(): String {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build(),
            )
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context = context, request = request)
            .credential
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential type: ${credential.type}"
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
