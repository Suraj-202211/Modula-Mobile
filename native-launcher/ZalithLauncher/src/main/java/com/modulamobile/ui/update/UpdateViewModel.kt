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

    private var downloadJob: Job? = null

    val remoteConfig: StateFlow<RemoteConfig> = remoteConfigManager.config

    fun checkSilently() {
        viewModelScope.launch {
            try {
                remoteConfigManager.fetchConfig()
            } catch(e: Exception) {}
            
            try {
                val skipped = dataStore.data
                    .map { it[intPreferencesKey("skipped_version")] ?: 0 }
                    .first()

                val info = checker.checkForUpdate()

                if (info != null && info.versionCode != skipped) {
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

                val apkFile = downloader.download(info) { progress, dlMb, totalMb, speed ->
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

    fun skipVersion(versionCode: Int) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[intPreferencesKey("skipped_version")] = versionCode
            }
            _state.value = UpdateState.Idle
        }
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }
}
