package com.modulamobile.updater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class UpdateInfo(
    @SerialName("versionCode")
    val versionCode: Int,
    @SerialName("versionName")
    val versionName: String,
    @SerialName("releaseNotes")
    val releaseNotes: List<String>,
    @SerialName("mandatory")
    val mandatory: Boolean = false,
    @SerialName("apkUrl")
    val apkUrl: String,
    @SerialName("apkSizeBytes")
    val apkSizeBytes: Long,
    @SerialName("apkSha256")
    val apkSha256: String,
    @SerialName("releaseDate")
    val releaseDate: String = "",
    @SerialName("patchUrl")
    val patchUrl: String? = null,
    @SerialName("patchSizeBytes")
    val patchSizeBytes: Long? = null,
    @SerialName("patchSha256")
    val patchSha256: String? = null,
    @SerialName("patchFromVersionCode")
    val patchFromVersionCode: Int? = null,
    @SerialName("patchFromSha256")
    val patchFromSha256: String? = null
)

sealed class DownloadPayload {
    abstract val url: String
    abstract val sizeBytes: Long
    abstract val sha256: String

    data class Patch(
        override val url: String,
        override val sizeBytes: Long,
        override val sha256: String,
        val sourceVersionCode: Int,
        val sourceSha256: String
    ) : DownloadPayload()

    data class FullApk(
        override val url: String,
        override val sizeBytes: Long,
        override val sha256: String
    ) : DownloadPayload()
}

// GitHub API response for file content
@Serializable
data class GitHubFileContent(
    val name: String,
    val content: String,  // base64 encoded
    val encoding: String,
    @SerialName("download_url")
    val downloadUrl: String? = null
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class Available(
        val info: UpdateInfo
    ) : UpdateState()
    data class Downloading(
        val info: UpdateInfo,
        val progress: Float,
        val downloadedMb: Float,
        val totalMb: Float,
        val speedMbps: Float
    ) : UpdateState()
    data class Installing(
        val info: UpdateInfo
    ) : UpdateState()
    data class ReadyToInstall(
        val info: UpdateInfo,
        val apkFile: File
    ) : UpdateState()
    data class Failed(
        val info: UpdateInfo,
        val message: String
    ) : UpdateState()
}
