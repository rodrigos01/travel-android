package travel.vola.android.ui.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import travel.vola.android.R
import travel.vola.android.common.ui.preview.PreviewLightDarkSystemUI
import travel.vola.android.extensions.viewModel
import travel.vola.android.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInScreen(
    state: SignInViewModel.UiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignInTapped: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryFixed,
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.vola_logo),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.app_name_vola),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryFixed,
            )
            Column(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.shapes.extraLarge.copy(
                            bottomEnd = CornerSize(0), bottomStart = CornerSize(0)
                        )
                    )
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(PaddingValues(horizontal = 24.dp, vertical = 40.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = { Text(stringResource(R.string.sign_in_email_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    label = { Text(stringResource(R.string.sign_in_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
                state.errorMessage?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    colors = ButtonDefaults.buttonColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            stringResource(
                                if (state.mode == SignInViewModel.Mode.SIGN_IN) {
                                    R.string.action_sign_in
                                } else {
                                    R.string.action_sign_up
                                },
                            ),
                        )
                    }
                }
                TextButton(onClick = onToggleMode, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(
                            if (state.mode == SignInViewModel.Mode.SIGN_IN) {
                                R.string.sign_in_toggle_to_sign_up
                            } else {
                                R.string.sign_in_toggle_to_sign_in
                            },
                        ),
                    )
                }
                OutlinedButton(
                    onClick = onGoogleSignInTapped,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_sign_in_with_google))
                }
            }
        }
    }
}

@Composable
fun SignInScreen() {
    val viewModel: SignInViewModel = viewModel(factory = SignInViewModel.Factory())
    val uiState by viewModel.uiState.collectAsState()
    SignInScreen(
        state = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onToggleMode = viewModel::toggleMode,
        onSubmit = viewModel::submit,
        onGoogleSignInTapped = viewModel::onGoogleSignInTapped,
    )
}

@PreviewLightDarkSystemUI
@Composable
fun SignInScreenPreview() {
    AppTheme {
        SignInScreen(
            state = SignInViewModel.UiState(email = "traveler@example.com"),
            onEmailChanged = {},
            onPasswordChanged = {},
            onToggleMode = {},
            onSubmit = {},
            onGoogleSignInTapped = {},
        )
    }
}

object SignInScreenDestination {
    const val ROUTE = "signin"
}
