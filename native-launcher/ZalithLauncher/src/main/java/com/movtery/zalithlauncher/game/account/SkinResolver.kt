package com.movtery.zalithlauncher.game.account

import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.setting.AllSettings
import io.ktor.client.request.head
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SkinResolver {

    /**
     * Resolves the skin URL by checking custom URLs and public APIs asynchronously.
     * Returns null if all endpoints fail or the device is offline, prompting the UI to use a local fallback.
     */
    suspend fun getSkinUrl(account: Account?): String? {
        return withContext(Dispatchers.IO) {
            if (account == null) return@withContext null

            val isPremium = account.accountType.equals("Microsoft", ignoreCase = true)
            val username = account.username
            val uuid = account.profileId

            // 2. If Premium Microsoft account, try Crafatar (highest fidelity for official UUIDs)
            if (isPremium && uuid != null && uuid.isNotBlank()) {
                val crafatarUrl = "https://crafatar.com/renders/body/$uuid?overlay=true"
                if (isValidImageUrl(crafatarUrl)) {
                    return@withContext crafatarUrl
                }
            }

            // 3. Try Minotar (Username based, reliable fallback)
            val minotarUrl = "https://minotar.net/armor/body/$username/280"
            if (isValidImageUrl(minotarUrl)) {
                return@withContext minotarUrl
            }

            // 4. Try MC-Heads (Last resort public API)
            val mcHeadsUrl = "https://mc-heads.net/body/$username/280"
            if (isValidImageUrl(mcHeadsUrl)) {
                return@withContext mcHeadsUrl
            }

            // 5. Offline or all failed
            null
        }
    }

    suspend fun getAvatarUrl(account: Account?): String? {
        return withContext(Dispatchers.IO) {
            if (account == null) return@withContext null

            val isPremium = account.accountType.equals("Microsoft", ignoreCase = true)
            val username = account.username
            val uuid = account.profileId

            if (isPremium && uuid != null && uuid.isNotBlank()) {
                return@withContext "https://crafatar.com/avatars/$uuid?overlay=true"
            }

            if (username.isNotBlank()) {
                return@withContext "https://minotar.net/helm/$username/280"
            }

            null
        }
    }

    /**
     * Resolves the raw skin texture URL for 3D rendering (64x64 or 64x32 PNG).
     */
    suspend fun getRawSkinUrl(account: Account?): String? {
        return withContext(Dispatchers.IO) {
            val customUrl = AllSettings.customSkinUrl.state
            if (customUrl.isNotBlank() && isValidImageUrl(customUrl)) {
                return@withContext customUrl
            }

            if (account == null) return@withContext null

            val isPremium = account.accountType.equals("Microsoft", ignoreCase = true)
            val username = account.username
            val uuid = account.profileId

            if (isPremium && uuid != null && uuid.isNotBlank()) {
                val crafatarUrl = "https://crafatar.com/skins/$uuid"
                if (isValidImageUrl(crafatarUrl)) return@withContext crafatarUrl
            }

            val minotarUrl = "https://minotar.net/skin/$username"
            if (isValidImageUrl(minotarUrl)) return@withContext minotarUrl

            val mcHeadsUrl = "https://mc-heads.net/skin/$username"
            if (isValidImageUrl(mcHeadsUrl)) return@withContext mcHeadsUrl

            null
        }
    }

    private suspend fun isValidImageUrl(url: String): Boolean {
        return try {
            val response: HttpResponse = GLOBAL_CLIENT.head(url)
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
