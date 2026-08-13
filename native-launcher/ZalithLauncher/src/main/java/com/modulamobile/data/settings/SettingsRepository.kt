package com.modulamobile.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "modula_ui_prefs_v1")

object SettingsKeys {
    val THEME_SELECTION = stringPreferencesKey("themeSelection")
    val PARTICLE_DENSITY = intPreferencesKey("particleDensity")
    val MOTION_INTERPOLATION = intPreferencesKey("motionInterpolation")
    val UI_TRANSPARENCY = intPreferencesKey("uiTransparency")
    val UI_SCALING = intPreferencesKey("uiScaling")
    val BLOOM_EFFECTS = booleanPreferencesKey("bloomEffects")
    val DYNAMIC_SHADOWS = booleanPreferencesKey("dynamicShadows")
    val RICH_PRESENCE = booleanPreferencesKey("richPresence")
    val SHOW_FPS_COUNTER = booleanPreferencesKey("showFpsCounter")
    val ENABLE_SNAPSHOTS = booleanPreferencesKey("enableSnapshots")
    val ADVANCED_DEBUG = booleanPreferencesKey("advancedDebug")
    val LOW_RAM_MODE = booleanPreferencesKey("lowRamMode")
    val BATTERY_SAVER = booleanPreferencesKey("batterySaver")
    val LANGUAGE_SELECTION = stringPreferencesKey("languageSelection")
    val AUTO_UPDATE = booleanPreferencesKey("autoUpdate")
    val GPU_ACCELERATION = booleanPreferencesKey("gpuAcceleration")
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val dataStore = context.dataStore

    private val safeData = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    val themeSelection: Flow<String> = safeData.map { it[SettingsKeys.THEME_SELECTION] ?: "DEFAULT" }
    val particleDensity: Flow<Int> = safeData.map { it[SettingsKeys.PARTICLE_DENSITY] ?: 60 }
    val motionInterpolation: Flow<Int> = safeData.map { it[SettingsKeys.MOTION_INTERPOLATION] ?: 80 }
    val uiTransparency: Flow<Int> = safeData.map { it[SettingsKeys.UI_TRANSPARENCY] ?: 10 }
    val uiScaling: Flow<Int> = safeData.map { it[SettingsKeys.UI_SCALING] ?: 100 }
    val bloomEffects: Flow<Boolean> = safeData.map { it[SettingsKeys.BLOOM_EFFECTS] ?: true }
    val dynamicShadows: Flow<Boolean> = safeData.map { it[SettingsKeys.DYNAMIC_SHADOWS] ?: true }
    val richPresence: Flow<Boolean> = safeData.map { it[SettingsKeys.RICH_PRESENCE] ?: true }
    val showFpsCounter: Flow<Boolean> = safeData.map { it[SettingsKeys.SHOW_FPS_COUNTER] ?: false }
    val enableSnapshots: Flow<Boolean> = safeData.map { it[SettingsKeys.ENABLE_SNAPSHOTS] ?: false }
    val advancedDebug: Flow<Boolean> = safeData.map { it[SettingsKeys.ADVANCED_DEBUG] ?: false }
    val lowRamMode: Flow<Boolean> = safeData.map { it[SettingsKeys.LOW_RAM_MODE] ?: false }
    val batterySaver: Flow<Boolean> = safeData.map { it[SettingsKeys.BATTERY_SAVER] ?: true }
    val languageSelection: Flow<String> = safeData.map { it[SettingsKeys.LANGUAGE_SELECTION] ?: "ENGLISH (US)" }
    val autoUpdate: Flow<Boolean> = safeData.map { it[SettingsKeys.AUTO_UPDATE] ?: true }
    val gpuAcceleration: Flow<Boolean> = safeData.map { it[SettingsKeys.GPU_ACCELERATION] ?: true }

    suspend fun setThemeSelection(value: String) { dataStore.edit { it[SettingsKeys.THEME_SELECTION] = value } }
    suspend fun setParticleDensity(value: Int) { dataStore.edit { it[SettingsKeys.PARTICLE_DENSITY] = value } }
    suspend fun setMotionInterpolation(value: Int) { dataStore.edit { it[SettingsKeys.MOTION_INTERPOLATION] = value } }
    suspend fun setUiTransparency(value: Int) { dataStore.edit { it[SettingsKeys.UI_TRANSPARENCY] = value } }
    suspend fun setUiScaling(value: Int) { dataStore.edit { it[SettingsKeys.UI_SCALING] = value } }
    
    suspend fun setBloomEffects(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting BloomEffects = $value")
        dataStore.edit { it[SettingsKeys.BLOOM_EFFECTS] = value } 
    }
    suspend fun setDynamicShadows(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting DynamicShadows = $value")
        dataStore.edit { it[SettingsKeys.DYNAMIC_SHADOWS] = value } 
    }
    suspend fun setRichPresence(value: Boolean) { dataStore.edit { it[SettingsKeys.RICH_PRESENCE] = value } }
    
    suspend fun setShowFpsCounter(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting ShowFpsCounter = $value")
        dataStore.edit { it[SettingsKeys.SHOW_FPS_COUNTER] = value } 
    }
    suspend fun setEnableSnapshots(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting EnableSnapshots = $value")
        dataStore.edit { it[SettingsKeys.ENABLE_SNAPSHOTS] = value } 
    }
    suspend fun setAdvancedDebug(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting AdvancedDebug = $value")
        dataStore.edit { it[SettingsKeys.ADVANCED_DEBUG] = value } 
    }
    suspend fun setAutoUpdate(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting AutoUpdate = $value")
        dataStore.edit { it[SettingsKeys.AUTO_UPDATE] = value } 
    }
    suspend fun setGpuAcceleration(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting GpuAcceleration = $value")
        dataStore.edit { it[SettingsKeys.GPU_ACCELERATION] = value } 
    }
    
    suspend fun setLanguageSelection(value: String) { dataStore.edit { it[SettingsKeys.LANGUAGE_SELECTION] = value } }

    // Handled in ViewModel to also update AllSettings
    suspend fun setLowRamMode(value: Boolean) { dataStore.edit { it[SettingsKeys.LOW_RAM_MODE] = value } }
    suspend fun setBatterySaver(value: Boolean) { 
        android.util.Log.d("SettingsRepo", "Persisting BatterySaver = $value")
        dataStore.edit { it[SettingsKeys.BATTERY_SAVER] = value } 
    }
}
