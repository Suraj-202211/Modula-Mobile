package com.modulamobile.auth

import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isNoLoginRequired
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain model representing an Account in the Modula Mobile UI.
 */
data class AccountModel(
    val id: String,
    val username: String,
    val type: String,
    val isMicrosoft: Boolean,
    val isOffline: Boolean
)

/**
 * Repository for managing Authentication using the underlying AccountsManager.
 */
@Singleton
class AuthRepository @Inject constructor() {

    /**
     * Flow of all accounts mapped to UI model.
     */
    val accountsFlow: Flow<List<AccountModel>> = AccountsManager.accountsFlow.map { list ->
        list.map { account ->
            AccountModel(
                id = account.uniqueUUID,
                username = account.username,
                type = account.accountType ?: "Local",
                isMicrosoft = account.isMicrosoftAccount(),
                isOffline = account.isNoLoginRequired()
            )
        }
    }

    /**
     * Flow of the currently selected account.
     */
    val currentAccountFlow: Flow<AccountModel?> = AccountsManager.currentAccountFlow.map { account ->
        account?.let {
            AccountModel(
                id = it.uniqueUUID,
                username = it.username,
                type = it.accountType ?: "Local",
                isMicrosoft = it.isMicrosoftAccount(),
                isOffline = it.isNoLoginRequired()
            )
        }
    }

    /**
     * Adds an offline account.
     */
    suspend fun addOfflineAccount(username: String) {
        val account = Account(
            username = username,
            accountType = AccountType.LOCAL.tag
        )
        AccountsManager.suspendSaveAccount(account)
    }

    /**
     * Sets the active account.
     */
    fun selectAccount(accountId: String) {
        val account = AccountsManager.accountsFlow.value.find { it.uniqueUUID == accountId }
        if (account != null) {
            AccountsManager.setCurrentAccount(account)
        }
    }

    /**
     * Deletes an account.
     */
    fun deleteAccount(accountId: String) {
        val account = AccountsManager.accountsFlow.value.find { it.uniqueUUID == accountId }
        if (account != null) {
            AccountsManager.deleteAccount(account)
        }
    }
}
