package travel.vola.android.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import travel.vola.android.di.factoryDependencies
import travel.vola.android.extensions.viewModelFactory
import travel.vola.android.model.repository.AuthRepository
import travel.vola.android.ui.home.HomeScreenDestination

class SignInViewModel private constructor(
    private val navController: NavController,
    private val authRepository: AuthRepository,
) : ViewModel() {

    enum class Mode { SIGN_IN, SIGN_UP }

    data class UiState(
        val email: String = "",
        val password: String = "",
        val mode: Mode = Mode.SIGN_IN,
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            val newMode = if (it.mode == Mode.SIGN_IN) Mode.SIGN_UP else Mode.SIGN_IN
            it.copy(mode = newMode, errorMessage = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        runCatchingAuth {
            when (state.mode) {
                Mode.SIGN_IN -> authRepository.signIn(state.email, state.password)
                Mode.SIGN_UP -> authRepository.signUp(state.email, state.password)
            }
        }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        runCatchingAuth { authRepository.signInWithGoogleIdToken(idToken) }
    }

    fun onGoogleSignInFailed(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun runCatchingAuth(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                block()
                navController.navigate(HomeScreenDestination.ROUTE) {
                    popUpTo(SignInScreenDestination.ROUTE) { inclusive = true }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    class Factory : ViewModelProvider.Factory by viewModelFactory(initializer = {
        SignInViewModel(factoryDependencies.navController, factoryDependencies.authRepository)
    })
}
