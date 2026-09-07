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

                _state.value = UpdateState.Downloading(
                    info = info,
                    progress = 0f,
                    downloadedMb = 0f,
                    totalMb = initialPayload.sizeBytes / 1024f / 1024f,
                    speedMbps = 0f
                )

                val apkFile = downloader.download(info, initialPayload) { progress, dlMb, totalMb, speed ->
                    if (progress >= 1f) {
                        _state.value = UpdateState.Installing(info)
                    } else {
                        _state.value = UpdateState.Downloading(
                            info = info,
                            progress = progress,
                            downloadedMb = dlMb,
                            totalMb = totalMb,
                            speedMbps = speed
                        )
                    }
                }

                _state.value = UpdateState.Installing(info)

                val valid = downloader.verifySha256(apkFile, info.apkSha256)
                if (!valid) {
                    apkFile.delete()
                    _state.value = UpdateState.Failed(info, "Download corrupted. Please try again.")
                    return@launch
                }

                _state.value = UpdateState.ReadyToInstall(info, apkFile)

            } catch (e: CancellationException) {
                _state.value = UpdateState.Available(info)
            } catch (e: Exception) {
                _state.value = UpdateState.Failed(info, e.message ?: "Download failed")
            }
        }
    }

    fun install(apkFile: File, activity: Activity) {
        if (!checker.canInstallPackages()) {
            installer.requestInstallPermission(activity)
            return
        }
        installer.install(apkFile)
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
}
