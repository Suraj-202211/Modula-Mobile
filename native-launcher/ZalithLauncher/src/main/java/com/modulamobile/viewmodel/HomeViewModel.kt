package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.auth.AuthRepository
import com.modulamobile.database.entities.ActivityEntity
import com.modulamobile.network.GithubApiService
import com.modulamobile.network.ModrinthApiService
import com.modulamobile.network.ModrinthMod
import com.modulamobile.network.NewsItem
import com.modulamobile.repositories.ActivityRepository
import com.modulamobile.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val activityRepository: ActivityRepository,
    private val modrinthApi: ModrinthApiService,
    private val githubApi: GithubApiService
) : ViewModel() {

    val currentAccount = authRepository.currentAccountFlow

    val ramAllocationMb = settingsRepository.ramAllocationMb

    val recentActivities = activityRepository.getRecentActivities()

    // Assuming we don't have a direct count query in InstalledModDao yet, we default to 0
    // A proper implementation would query InstalledModDao.count(), but for now we provide a StateFlow
    private val _installedModsCount = MutableStateFlow(0)
    val installedModsCount: StateFlow<Int> = _installedModsCount.asStateFlow()

    private val _modernModpacks = MutableStateFlow<List<ModrinthMod>>(emptyList())
    val modernModpacks: StateFlow<List<ModrinthMod>> = _modernModpacks.asStateFlow()

    private val _communityNews = MutableStateFlow<List<NewsItem>>(emptyList())
    val communityNews: StateFlow<List<NewsItem>> = _communityNews.asStateFlow()

    init {
        fetchModpacks()
        fetchNews()
    }

    private fun fetchModpacks() {
        viewModelScope.launch {
            try {
                val result = modrinthApi.searchMods(
                    query = "",
                    limit = 5,
                    projectType = "modpack"
                )
                _modernModpacks.value = result.hits
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchNews() {
        viewModelScope.launch {
            try {
                val result = githubApi.getCachedNews()
                _communityNews.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
