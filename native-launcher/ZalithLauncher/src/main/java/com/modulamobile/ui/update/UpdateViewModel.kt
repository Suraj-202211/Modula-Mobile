package com.modulamobile.ui.update

import android.app.Activity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.updater.ApkDownloader
import com.modulamobile.updater.ApkInstaller
import com.modulamobile.updater.UpdateChecker
import com.modulamobile.updater.UpdateInfo
import com.modulamobile.updater.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

import com.modulamobile.network.RemoteConfigManager
import com.modulamobile.network.RemoteConfig

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val checker: UpdateChecker,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
    private val dataStore: DataStore<Preferences>,
    private val remoteConfigManager: RemoteConfigManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state = _state.asStateFlow()

    private val _isBannerDismissed = MutableStateFlow(false)
    val isBannerDismissed = _isBannerDismissed.asStateFlow()

    private var downloadJob: Job? = null
    private var lastCheckTime = 0L

    val remoteConfig: StateFlow<RemoteConfig> = remoteConfigManager.config

    val targetVersionKey = intPreferencesKey("target_update_version")

    init {
        viewModelScope.launch {
            val targetVersion = dataStore.data.first()[targetVersionKey] ?: 0
            if (targetVersion > 0) {
                val pm = context.packageManager
                val installedVersionCode = try {
                    val pkgInfo = pm.getPackageInfo(context.packageName, 0)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        pkgInfo.longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        pkgInfo.versionCode
                    }
                } catch (e: Exception) {
                    -1
                }

                if (installedVersionCode >= targetVersion) {
                    // Update success!
                    _state.value = UpdateState.Success(installedVersionCode)
                } else if (installedVersionCode > 0) {
                    // Update failed or was cancelled
                    _state.value = UpdateState.Failed(null, "App update cancelled or failed to install.")
                }
                
                // Clear the target
                dataStore.edit { it.remove(targetVersionKey) }
            }
        }
    }

    fun checkSilently(force: Boolean = false) {

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (!force && now - lastCheckTime < 10 * 60 * 1000L) {
                return@launch
            }
            lastCheckTime = now

            try {
                remoteConfigManager.fetchConfig()
            } catch(e: Exception) {}
            
            try {
                // Clear any legacy permanent skip in DataStore
                dataStore.edit { prefs ->
                    prefs.remove(intPreferencesKey("skipped_version"))
                }

                val info = checker.checkForUpdate()

                if (info != null) {
                    _isBannerDismissed.value = false
                    _state.value = UpdateState.Available(info)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun checkManually() {
        viewModelScope.launch {
            _state.value = UpdateState.Checking
            _isBannerDismissed.value = false
            lastCheckTime = System.currentTimeMillis()
            val info = checker.checkForUpdate()
            _state.value = if (info != null) {
                UpdateState.Available(info)
            } else {
                UpdateState.UpToDate
            }
        }
    }

    private suspend fun downloadWithPayload(
        info: UpdateInfo,
        payload: com.modulamobile.updater.DownloadPayload
    ): File {
        _state.value = UpdateState.Downloading(
            info = info,
            progress = 0f,
            downloadedMb = 0f,
            totalMb = payload.sizeBytes / 1024f / 1024f,
            speedMbps = 0f
        )

        return downloader.download(
            info = info,
            payload = payload,
            onStatus = { status ->
                when (status) {
                    "APPLYING" -> _state.value = UpdateState.Applying(info, "Processing update...")
                    "VERIFYING" -> _state.value = UpdateState.Verifying(info, "Verifying update...")
                    else -> {}
                }
            },
            onProgress = { progress, dlMb, totalMb, speed ->
                if (_state.value is UpdateState.Downloading) {
                    _state.value = UpdateState.Downloading(
                        info = info,
                        progress = progress,
                        downloadedMb = dlMb,
                        totalMb = totalMb,
                        speedMbps = speed
                    )
                }
            }
        )
    }

    fun startDownload(info: UpdateInfo) {
        downloadJob = viewModelScope.launch {
            try {
                if (!checker.hasEnoughStorage(info.apkSizeBytes)) {
                    _state.value = UpdateState.Failed(
                        info,
                        "Not enough storage.\nNeed ${info.apkSizeBytes / 1024 / 1024 * 2}MB free space."
                    )
                    return@launch
                }

                val initialPayload = com.modulamobile.updater.PayloadSelector.selectPayload(
                    context, info
                )

                val apkFile = try {
                    downloadWithPayload(info, initialPayload)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (initialPayload !is com.modulamobile.updater.DownloadPayload.FullApk) {
                        android.util.Log.w("UPDATE", "Delta patch failed (${e.message}), falling back to Full APK", e)
                        val fullApkPayload = com.modulamobile.updater.DownloadPayload.FullApk(
                            url = info.apkUrl,
                            sizeBytes = info.apkSizeBytes,
                            sha256 = info.apkSha256,
                            targetVersionCode = info.versionCode
                        )
                        downloadWithPayload(info, fullApkPayload)
                    } else {
                        throw e
                    }
                }

                _state.value = UpdateState.ReadyToInstall(info, apkFile)

            } catch (e: CancellationException) {
                _state.value = UpdateState.Available(info)
            } catch (e: Exception) {
                _state.value = UpdateState.Failed(info, e.message ?: "Update failed. Please try again.")
            }
        }
    }

    fun install(apkFile: File, activity: Activity, targetVersionCode: Int) {
        if (!checker.canInstallPackages()) {
            installer.requestInstallPermission(activity)
            return
        }
        viewModelScope.launch {
            dataStore.edit { it[targetVersionKey] = targetVersionCode }
            installer.install(apkFile)
        }
    }

    fun cancelDownload(info: UpdateInfo) {
        downloadJob?.cancel()
        _state.value = UpdateState.Available(info)
    }

    fun dismissBanner() {
        // Temporarily dismiss the banner for this UI session without suppressing the update permanently
        _isBannerDismissed.value = true
    }

    fun skipVersion(versionCode: Int) {
        dismissBanner()
    }

    fun dismiss() {
        dismissBanner()
    }

    fun resetState() {
        _state.value = UpdateState.Idle
    }
}
