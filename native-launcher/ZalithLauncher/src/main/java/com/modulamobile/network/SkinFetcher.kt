package com.modulamobile.network

import android.util.Base64
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkinFetcher @Inject constructor(
    private val httpClient: HttpClient
) {

    companion object {
        private const val TAG = "SkinFetcher"

        // Step 1: username → UUID
        private const val MOJANG_API = "https://api.mojang.com/users/profiles/minecraft"

        // Step 2: UUID → profile with skin URL
        private const val SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile"
    }

    // Cache skin URLs to avoid repeated API calls
    // Key: username, Value: skin texture URL
    private val skinCache = mutableMapOf<String, String>()

    suspend fun getSkinUrl(username: String, uuid: String? = null): SkinResult = withContext(Dispatchers.IO) {
        // Check cache first
        skinCache[username]?.let { cachedUrl ->
            Log.d(TAG, "Cache hit for $username")
            return@withContext SkinResult.Success(cachedUrl)
        }

        try {
            // Use provided UUID or fetch it
            val resolvedUuid = uuid?.replace("-", "") ?: fetchUUID(username)

            if (resolvedUuid == null) {
                Log.w(TAG, "UUID not found for $username")
                // Username not found on Mojang
                // Return default skin
                return@withContext SkinResult.DefaultSkin(isAlex = isAlexModel(username))
            }

            // Fetch full profile with skin data
            val skinUrl = fetchSkinFromProfile(resolvedUuid)

            if (skinUrl != null) {
                // Cache the successful result
                skinCache[username] = skinUrl
                Log.d(TAG, "Skin found for $username: $skinUrl")
                SkinResult.Success(skinUrl)
            } else {
                Log.w(TAG, "No skin in profile for $username")
                SkinResult.DefaultSkin(isAlex = isAlexModel(username))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch skin for $username", e)
            SkinResult.Error(e.message ?: "Unknown error")
        }
    }

    // STEP 1: Get UUID from username
    private suspend fun fetchUUID(username: String): String? {
        return try {
            val response = httpClient.get("$MOJANG_API/$username")
            if (response.status.value == 200) {
                val json = response.bodyAsText()
                JSONObject(json).getString("id")
            } else if (response.status.value == 204 || response.status.value == 404) {
                // Username not found
                null
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "UUID fetch failed for $username", e)
            null
        }
    }

    // STEP 2 + 3: Get skin URL from UUID
    private suspend fun fetchSkinFromProfile(uuid: String): String? {
        return try {
            val response = httpClient.get("$SESSION_API/$uuid")

            if (response.status.value != 200) {
                return null
            }

            val profileJson = JSONObject(response.bodyAsText())
            
            // Get properties array
            val properties = profileJson.getJSONArray("properties")
            
            // Find "textures" property
            for (i in 0 until properties.length()) {
                val prop = properties.getJSONObject(i)
                if (prop.getString("name") == "textures") {
                    // Decode base64 value
                    val encoded = prop.getString("value")
                    val decoded = String(Base64.decode(encoded, Base64.DEFAULT))
                    
                    // Parse decoded JSON
                    val texturesJson = JSONObject(decoded)
                    val textures = texturesJson.getJSONObject("textures")
                    
                    // Get SKIN url
                    return if (textures.has("SKIN")) {
                        textures.getJSONObject("SKIN").getString("url")
                    } else {
                        null
                    }
                }
            }
            null

        } catch (e: Exception) {
            Log.e(TAG, "Profile fetch failed for uuid: $uuid", e)
            null
        }
    }

    // Determine Steve vs Alex model
    private fun isAlexModel(username: String): Boolean {
        var hash = 0L
        for (char in username) {
            hash = hash * 31 + char.code
        }
        return (hash and 1L) != 0L
    }

    fun clearCache() = skinCache.clear()
}

sealed class SkinResult {
    data class Success(val skinUrl: String) : SkinResult()
    data class DefaultSkin(val isAlex: Boolean) : SkinResult()
    data class Error(val message: String) : SkinResult()
}
