package com.modulamobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulamobile.database.dao.InstalledModDao
import com.modulamobile.network.ModrinthApiService
import com.modulamobile.network.ModrinthMod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class ModsViewModel @Inject constructor(
    private val modrinthApi: ModrinthApiService,
    private val modDao: InstalledModDao
) : ViewModel() {

    private val _mods = MutableStateFlow<List<ModrinthMod>>(emptyList())
    val mods: StateFlow<List<ModrinthMod>> = _mods.asStateFlow()

    private val _modpacks = MutableStateFlow<List<ModrinthMod>>(emptyList())
    val modpacks: StateFlow<List<ModrinthMod>> = _modpacks.asStateFlow()
    
    private val _isLoadingMods = MutableStateFlow(false)
    val isLoadingMods: StateFlow<Boolean> = _isLoadingMods.asStateFlow()
    
    private val _isLoadingModpacks = MutableStateFlow(false)
    val isLoadingModpacks: StateFlow<Boolean> = _isLoadingModpacks.asStateFlow()
    
    private val _errorMods = MutableStateFlow<String?>(null)
    val errorMods: StateFlow<String?> = _errorMods.asStateFlow()

    private val _errorModpacks = MutableStateFlow<String?>(null)
    val errorModpacks: StateFlow<String?> = _errorModpacks.asStateFlow()

    val installedMods = modDao.getAllInstalledMods()

    private var currentOffsetMods = 0
    private var currentOffsetModpacks = 0
    private var searchJobMods: Job? = null
    private var searchJobModpacks: Job? = null

    private var currentQuery = ""
    private var currentLoader = ""
    
    init {
        viewModelScope.launch {
            loadMods()
            loadModpacks()
        }
    }

    fun search(query: String) {
        currentQuery = query
        searchJobMods?.cancel()
        searchJobMods = viewModelScope.launch {
            delay(400)
            currentOffsetMods = 0
            loadMods(query = query, append = false)
        }
        
        searchJobModpacks?.cancel()
        searchJobModpacks = viewModelScope.launch {
            delay(400)
            currentOffsetModpacks = 0
            loadModpacks(query = query, append = false)
        }
    }

    fun setLoader(loader: String) {
        currentLoader = loader
        currentOffsetMods = 0
        currentOffsetModpacks = 0
        viewModelScope.launch {
            loadMods(query = currentQuery, append = false)
            loadModpacks(query = currentQuery, append = false)
        }
    }

    private suspend fun loadMods(query: String = currentQuery, offset: Int = 0, append: Boolean = true) {
        try {
            _isLoadingMods.value = true
            _errorMods.value = null
            val loaders = if (currentLoader.isNotEmpty()) listOf(currentLoader) else emptyList()
            val result = modrinthApi.searchMods(
                query = query,
                loaders = loaders,
                offset = offset,
                projectType = "mod"
            )
            if (append) {
                _mods.value = _mods.value + result.hits
            } else {
                _mods.value = result.hits
            }
        } catch (e: Exception) {
            Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
            e.printStackTrace()
            _errorMods.value = "Failed to load mods. Check connection."
        } finally {
            _isLoadingMods.value = false
        }
    }

    private suspend fun loadModpacks(query: String = currentQuery, offset: Int = 0, append: Boolean = true) {
        try {
            _isLoadingModpacks.value = true
            _errorModpacks.value = null
            val loaders = if (currentLoader.isNotEmpty()) listOf(currentLoader) else emptyList()
            val result = modrinthApi.searchMods(
                query = query,
                loaders = loaders,
                offset = offset,
                projectType = "modpack"
            )
            if (append) {
                _modpacks.value = _modpacks.value + result.hits
            } else {
                _modpacks.value = result.hits
            }
        } catch (e: Exception) {
            Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
            e.printStackTrace()
            _errorModpacks.value = "Failed to load modpacks. Check connection."
        } finally {
            _isLoadingModpacks.value = false
        }
    }

    fun loadMoreMods() {
        currentOffsetMods += 20
        viewModelScope.launch {
            loadMods(offset = currentOffsetMods, append = true)
        }
    }

    fun loadMoreModpacks() {
        currentOffsetModpacks += 20
        viewModelScope.launch {
            loadModpacks(offset = currentOffsetModpacks, append = true)
        }
    }

    suspend fun getAvailableGameVersionsForMod(projectId: String): List<String> {
        return try {
            val versions = modrinthApi.getModVersions(projectId, emptyList(), emptyList())
            versions.flatMap { it.gameVersions }.distinct().sortedDescending()
        } catch (e: Exception) {
            Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getAvailableGameVersionsForModpack(projectId: String): List<String> {
        return try {
            val searcher = com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher()
            val versions = searcher.getVersionsChunk(projectId)
            versions.flatMap { it.gameVersions.toList() }.distinct().sortedDescending()
        } catch (e: Exception) {
            Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
            emptyList()
        }
    }

    fun downloadMod(mod: ModrinthMod, targetGameVersion: String, targetGameDir: java.io.File? = null): com.movtery.zalithlauncher.coroutine.Task {
        return com.movtery.zalithlauncher.coroutine.Task.runTask(
            id = "DownloadMod_${mod.projectId}",
            dispatcher = kotlinx.coroutines.Dispatchers.IO,
            task = { task ->
                task.updateProgress(-1f, com.movtery.zalithlauncher.R.string.empty_holder, "Fetching ${mod.title} info...")
                val versions = modrinthApi.getModVersions(mod.projectId, emptyList(), listOf(targetGameVersion))
                if (versions.isEmpty()) {
                    throw Exception("No compatible version found for ${mod.title} on Minecraft $targetGameVersion")
                }
                val bestVersion = versions.first()
                val file = bestVersion.files.firstOrNull { it.primary } ?: bestVersion.files.first()

                val gameDir = targetGameDir ?: com.movtery.zalithlauncher.game.version.installed.VersionsManager.currentVersion.value?.getGameDir()
                    ?: throw Exception("No game version selected")
                val modsDir = com.movtery.zalithlauncher.game.version.installed.VersionFolders.MOD.getDir(gameDir)
                if (!modsDir.exists()) modsDir.mkdirs()

                val outputFile = java.io.File(modsDir, file.filename)
                task.updateProgress(0f, com.movtery.zalithlauncher.R.string.empty_holder, "Downloading ${file.filename}...")

                var downloaded = 0L
                val total = file.size
                
                com.movtery.zalithlauncher.utils.network.downloadFileSuspend(
                    url = file.url,
                    outputFile = outputFile,
                    sha1 = file.hashes.sha1.takeIf { it.isNotEmpty() },
                    sizeCallback = { chunk ->
                        if (chunk > 0) {
                            downloaded += chunk
                            if (total > 0) {
                                task.updateProgress((downloaded.toDouble() / total).toFloat())
                            }
                        } else if (chunk < 0) {
                            downloaded += chunk // Rollback on error attempt
                        }
                    }
                )
                
                // Add to installed DB
                // modDao.insertMod(
                //     com.modulamobile.database.entity.InstalledModEntity(
                //         projectId = mod.projectId,
                //         slug = mod.slug,
                //         fileName = file.filename,
                //         installedGameVersion = currentVersionName
                //     )
                // )
                
                task.updateProgress(1f, null)
            }
        )
    }

    fun downloadModpack(
        pack: ModrinthMod,
        targetGameVersion: String,
        context: android.content.Context,
        scope: kotlinx.coroutines.CoroutineScope,
        onTasksUpdate: (List<com.movtery.zalithlauncher.coroutine.TitledTask>) -> Unit
    ) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val searcher = com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher()
                val versions = searcher.getVersionsChunk(pack.projectId)
                val bestVersion = versions.firstOrNull { it.gameVersions.contains(targetGameVersion) } ?: throw Exception("No versions found for modpack on Minecraft $targetGameVersion")
                
                bestVersion.initFile(pack.projectId)

                val installer = com.movtery.zalithlauncher.game.download.modpack.install.ModPackInstaller(
                    context = context,
                    version = bestVersion,
                    iconUrl = pack.iconUrl,
                    scope = scope,
                    waitForVersionName = { pack.title },
                    waitForConfirmMobileData = { true }
                )
                
                launch {
                    installer.tasksFlow.collect { tasks ->
                        onTasksUpdate(tasks)
                    }
                }

                installer.installModPack(
                    onInstalled = { version -> 
                        com.movtery.zalithlauncher.game.version.installed.VersionsManager.refresh("Modpack Installed", version)
                        // Auto-select the newly installed modpack so it is immediately ready to launch
                        com.movtery.zalithlauncher.game.version.installed.VersionsManager.saveCurrentVersion(version, refresh = true)
                    },
                    onCancelled = {},
                    onError = { e -> e.printStackTrace() }
                )
            } catch (e: Exception) {
                Log.e("MODULA_API_ERROR", "Failed: ${e.javaClass.simpleName} - ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
}
