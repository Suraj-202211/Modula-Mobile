package com.modulamobile.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class VersionManifest(
    val latest: LatestVersions,
    val versions: List<VersionEntry>
)

@Serializable
data class LatestVersions(
    val release: String,
    val snapshot: String
)

@Serializable
data class VersionEntry(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String,
    val sha1: String = "",
    val complianceLevel: Int = 0
)

@Serializable
data class ModrinthSearchResult(
    val hits: List<ModrinthMod>,
    @SerialName("total_hits")
    val totalHits: Int,
    val offset: Int,
    val limit: Int
)

@Serializable
data class ModrinthMod(
    @SerialName("project_id")
    val projectId: String,
    val slug: String,
    val title: String,
    val description: String,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val author: String,
    val downloads: Long = 0,
    @SerialName("follows")
    val follows: Int = 0,
    val categories: List<String> = emptyList(),
    val versions: List<String> = emptyList(),
    @SerialName("display_categories")
    val displayCategories: List<String> = emptyList()
)

@Serializable
data class ModrinthVersion(
    val id: String,
    val name: String,
    @SerialName("version_number")
    val versionNumber: String,
    val loaders: List<String>,
    @SerialName("game_versions")
    val gameVersions: List<String>,
    val files: List<ModrinthFile>,
    val dependencies: List<ModrinthDependency> = emptyList()
)

@Serializable
data class ModrinthFile(
    val url: String,
    val filename: String,
    val primary: Boolean,
    val size: Long,
    val hashes: ModrinthHashes
)

@Serializable
data class ModrinthHashes(
    val sha1: String = "",
    val sha512: String = ""
)

@Serializable
data class ModrinthDependency(
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("dependency_type")
    val dependencyType: String
)

@Serializable
data class ModrinthProject(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val downloads: Long,
    val followers: Int,
    @SerialName("icon_url")
    val iconUrl: String? = null
)

@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String,
    val name: String,
    @SerialName("published_at")
    val publishedAt: String,
    val assets: List<GithubAsset>,
    val body: String = ""
)

@Serializable
data class GithubAsset(
    val name: String,
    val size: Long,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)

@Serializable
data class NewsItem(
    val id: String,
    val category: String,
    val title: String,
    val excerpt: String,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("publishedAt")
    val publishedAt: String,
    val url: String
)

// Dummy models for PlayerProfile and VersionJson if needed
@Serializable
data class PlayerProfile(
    val id: String,
    val name: String,
    val properties: List<ProfileProperty> = emptyList()
)

@Serializable
data class ProfileProperty(
    val name: String,
    val value: String
)

typealias VersionJson = JsonObject
