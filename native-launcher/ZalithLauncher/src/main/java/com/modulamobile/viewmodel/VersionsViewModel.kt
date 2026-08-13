package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.database.dao.InstalledVersionDao
import com.modulamobile.database.entities.InstalledVersionEntity
import com.modulamobile.network.ApiResult
import com.modulamobile.network.VersionEntry
import com.modulamobile.repositories.VersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

import com.modulamobile.data.settings.SettingsRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@HiltViewModel
class VersionsViewModel @Inject constructor(
    private val versionRepository: VersionRepository,
    private val installedVersionDao: InstalledVersionDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _rawVersions = MutableStateFlow<List<VersionEntry>>(emptyList())

    val versions: StateFlow<List<VersionEntry>> = combine(_rawVersions, settingsRepository.enableSnapshots) { list, enableSnapshots ->
        if (enableSnapshots) {
            list
        } else {
            list.filter { it.type != "snapshot" }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _latestRelease = MutableStateFlow("")
    val latestRelease: StateFlow<String> = _latestRelease.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val installedVersions = installedVersionDao.getAllInstalledVersions()

    init {
        fetchVersions()
    }

    private val CACHE_KEY = stringPreferencesKey("VERSION_MANIFEST_CACHE")
    private val CACHE_TIME_KEY = longPreferencesKey("VERSION_MANIFEST_CACHE_TIME")

    fun fetchVersions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Check cache first
            val cacheTime = settingsRepository.dataStore.data.map { it[CACHE_TIME_KEY] ?: 0L }.first()
            val isCacheFresh = System.currentTimeMillis() - cacheTime < 5 * 60 * 1000 // 5 minutes
            
            if (isCacheFresh) {
                val cachedJson = settingsRepository.dataStore.data.map { it[CACHE_KEY] }.first()
                if (!cachedJson.isNullOrEmpty()) {
                    try {
                        val manifest: com.modulamobile.network.VersionManifest = Json { ignoreUnknownKeys = true }.decodeFromString(cachedJson)
                        _rawVersions.value = manifest.versions
                        _latestRelease.value = manifest.latest.release
                        _isLoading.value = false
                        return@launch
                    } catch (e: Exception) {
                        Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
                        e.printStackTrace()
                    }
                }
            }

            when (val result = versionRepository.getVersions()) {
                is ApiResult.Success -> {
                    _rawVersions.value = result.data.versions
                    _latestRelease.value = result.data.latest.release
                    
                    try {
                        settingsRepository.dataStore.edit { prefs ->
                            prefs[CACHE_KEY] = Json { ignoreUnknownKeys = true }.encodeToString(result.data)
                            prefs[CACHE_TIME_KEY] = System.currentTimeMillis()
                        }
                    } catch (e: Exception) {
                        Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
                        e.printStackTrace()
                    }
                }
                is ApiResult.Error, is ApiResult.NetworkError -> {
                    // Try to load cached version even if stale
                    val cachedJson = settingsRepository.dataStore.data.map { it[CACHE_KEY] }.first()
                    if (!cachedJson.isNullOrEmpty()) {
                        try {
                            val manifest: com.modulamobile.network.VersionManifest = Json { ignoreUnknownKeys = true }.decodeFromString(cachedJson)
                            _rawVersions.value = manifest.versions
                            _latestRelease.value = manifest.latest.release
                        } catch (e: Exception) {
                            Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
                            _error.value = "Cannot load versions. Check your internet connection and try again."
                        }
                    } else {
                        _error.value = "Cannot load versions. Check your internet connection and try again."
                    }
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }
}
