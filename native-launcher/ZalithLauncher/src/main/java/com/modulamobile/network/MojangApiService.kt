package com.modulamobile.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class MojangApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        const val BASE = "https://launchermeta.mojang.com"
        const val RESOURCES = "https://resources.download.minecraft.net"
        const val SESSION = "https://sessionserver.mojang.com"
    }

    // Version manifest with 5-minute cache
    private var manifestCache: VersionManifest? = null
    private var manifestCacheTime = 0L

    suspend fun getVersionManifest(): VersionManifest {
        val now = System.currentTimeMillis()
        if (manifestCache != null && now - manifestCacheTime < 5 * 60 * 1000) {
            return manifestCache!!
        }
        val result = client.get("$BASE/mc/game/version_manifest_v2.json").body<VersionManifest>()
        manifestCache = result
        manifestCacheTime = now
        return result
    }

    suspend fun getVersionJson(url: String): VersionJson = client.get(url).body()

    suspend fun getPlayerProfile(uuid: String): PlayerProfile =
        client.get("$SESSION/session/minecraft/profile/$uuid").body()
}
