package com.modulamobile.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import javax.inject.Inject

class ModrinthApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        const val BASE = "https://api.modrinth.com/v2"
    }

    suspend fun searchMods(
        query: String = "",
        loaders: List<String> = emptyList(),
        gameVersions: List<String> = emptyList(),
        limit: Int = 20,
        offset: Int = 0,
        projectType: String = "mod"
    ): ModrinthSearchResult {
        val facets = buildList {
            add("[\"project_type:$projectType\"]")
            if (loaders.isNotEmpty()) {
                val loaderFacet = loaders.joinToString(",") { "\"categories:$it\"" }
                add("[$loaderFacet]")
            }
            if (gameVersions.isNotEmpty()) {
                val versionFacet = gameVersions.joinToString(",") { "\"versions:$it\"" }
                add("[$versionFacet]")
            }
        }

        return client.get("$BASE/search") {
            header("User-Agent", "ModulaMobile/1.0 (github.com/modula-mobile)")
            parameter("query", query)
            parameter("facets", "[${facets.joinToString(",")}]")
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("index", "downloads")
        }.body()
    }

    suspend fun getModVersions(
        projectId: String,
        loaders: List<String> = emptyList(),
        gameVersions: List<String> = emptyList()
    ): List<ModrinthVersion> =
        client.get("$BASE/project/$projectId/version") {
            header("User-Agent", "ModulaMobile/1.0 (github.com/modula-mobile)")
            if (loaders.isNotEmpty()) parameter("loaders", loaders.toString())
            if (gameVersions.isNotEmpty()) parameter("game_versions", gameVersions.toString())
        }.body()

    suspend fun getProject(idOrSlug: String): ModrinthProject =
        client.get("$BASE/project/$idOrSlug") {
            header("User-Agent", "ModulaMobile/1.0 (github.com/modula-mobile)")
        }.body()
}
