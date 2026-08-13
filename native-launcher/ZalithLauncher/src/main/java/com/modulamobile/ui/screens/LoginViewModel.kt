package com.modulamobile.ui.screens

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.auth.microsoft.MicrosoftAuthManager
import com.modulamobile.auth.microsoft.MicrosoftSignInResult
import com.modulamobile.auth.microsoft.MinecraftTokenResult
import com.movtery.zalithlauncher.game.account.AccountsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: MicrosoftAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    init {
        viewModelScope.launch {
            authManager.initialize()
        }
    }

    fun signInMicrosoft(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit
    ) {
        viewModelScope.launch {
            val result = authManager.signIn(activity)
            when (result) {
                is MicrosoftSignInResult.Success -> {
                    val mcResult = authManager.getMinecraftToken(result.accessToken)
                    when (mcResult) {
                        is MinecraftTokenResult.Success -> {
                            // Inject into ZalithLauncher accounts system
                            val account = com.movtery.zalithlauncher.game.account.Account(
                                username = mcResult.username,
                                profileId = mcResult.uuid,
                                accessToken = mcResult.accessToken,
                                accountType = com.movtery.zalithlauncher.game.account.AccountType.MICROSOFT.tag
                            )
                            account.downloadYggdrasil()
                            AccountsManager.saveAccount(account)
                            onSuccess()
                        }
                        is MinecraftTokenResult.Error -> {
                            onError(mcResult.message)
                        }
                    }
                }
                is MicrosoftSignInResult.Error -> {
                    onError(result.message)
                }
                MicrosoftSignInResult.Cancelled -> {
                    onCancel()
                }
            }
        }
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null
)
