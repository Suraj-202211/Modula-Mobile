package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val accounts = authRepository.accountsFlow
    val currentAccount = authRepository.currentAccountFlow

    fun logout() {
        viewModelScope.launch {
            val current = currentAccount.firstOrNull()
            if (current != null) {
                authRepository.deleteAccount(current.id)
            }
        }
    }
}
