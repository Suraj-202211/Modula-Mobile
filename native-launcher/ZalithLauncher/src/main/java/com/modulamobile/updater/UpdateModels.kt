package com.modulamobile.updater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class PatchEntry(
    @SerialName("fromVersionCode")
    val fromVersionCode: Int,
    @SerialName("toVersionCode")
    val toVersionCode: Int,
    @SerialName("patchUrl")
    val patchUrl: String,
    @SerialName("patchSizeBytes")
    val patchSizeBytes: Long,
    @SerialName("patchSha256")
    val patchSha256: String,
    @SerialName("sourceSha256")
    val sourceSha256: String,
    @SerialName("targetSha256")
    val targetSha256: String
)

@Serializable
data class PatchIndex(
    @SerialName("patches")
    val patches: List<PatchEntry> = emptyList()
)

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
    @SerialName("patchToVersionCode")
    val patchToVersionCode: Int? = null,
    @SerialName("patchFromSha256")
    val patchFromSha256: String? = null,
    @SerialName("patchToSha256")
    val patchToSha256: String? = null,
    @SerialName("patches")
    val patches: List<PatchEntry> = emptyList()
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
        val targetVersionCode: Int = 0,
        val sourceSha256: String,
        val targetSha256: String = ""
    ) : DownloadPayload()

    data class PatchChain(
        val patches: List<PatchEntry>,
        override val sizeBytes: Long = patches.sumOf { it.patchSizeBytes },
        override val url: String = patches.firstOrNull()?.patchUrl ?: "",
        override val sha256: String = patches.lastOrNull()?.targetSha256 ?: "",
        val targetVersionCode: Int = patches.lastOrNull()?.toVersionCode ?: 0
    ) : DownloadPayload()

    data class FullApk(
        override val url: String,
        override val sizeBytes: Long,
        override val sha256: String,
        val targetVersionCode: Int = 0
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

data class PostInstallNotification(
    val isSuccess: Boolean,
    val title: String,
    val message: String
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
        val speedMbps: Float,
        val currentStep: Int = 1,
        val totalSteps: Int = 1
    ) : UpdateState()
    data class Applying(
        val info: UpdateInfo,
        val statusMessage: String = "Applying update...",
        val currentStep: Int = 1,
        val totalSteps: Int = 1
    ) : UpdateState()
    data class Verifying(
        val info: UpdateInfo,
        val statusMessage: String = "Verifying update...",
        val currentStep: Int = 1,
        val totalSteps: Int = 1
    ) : UpdateState()
    data class Installing(
        val info: UpdateInfo
    ) : UpdateState()
    data class ReadyToInstall(
        val info: UpdateInfo,
        val apkFile: File
    ) : UpdateState()
    data class Failed(
        val info: UpdateInfo?,
        val message: String
    ) : UpdateState()
    data class Success(
        val installedVersionCode: Int
    ) : UpdateState()
}

