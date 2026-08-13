package com.modulamobile.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Serializable
data class RemoteConfig(
    val maintenanceMode: Boolean = false,
    val latestAppVersion: String = "1.0.0",
    val forceUpdate: Boolean = false,
    val updateUrl: String = "",
    val disableSnapshots: Boolean = false,
    val motdMessage: String? = null
)

@Singleton
class RemoteConfigManager @Inject constructor(
    private val client: HttpClient
) {
    private val _config = MutableStateFlow(RemoteConfig())
    val config: StateFlow<RemoteConfig> = _config.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            fetchConfig()
        }
    }

    suspend fun fetchConfig() {
        try {
            // Using a dummy URL for now, fallback to default config if fails
            val configUrl = "https://raw.githubusercontent.com/modula-mobile/modula-mobile/main/remote_config.json"
            val response: String = client.get(configUrl).body()
            _config.value = Json { ignoreUnknownKeys = true }.decodeFromString(response)
        } catch (e: Exception) {
            e.printStackTrace()
            _config.value = RemoteConfig()
        }
    }
}
