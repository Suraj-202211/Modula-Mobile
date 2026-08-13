package com.modulamobile.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

val Context.modulaDataStore: DataStore<Preferences> by preferencesDataStore(name = "modula_prefs_v1")

object PreferenceKeys {
    val RAM_MB = intPreferencesKey("ram_mb")
    val FPS_UNLOCK = booleanPreferencesKey("fps_unlock")
    val JVM_ARGS = stringPreferencesKey("jvm_args")
    val PARTICLE_INTENSITY = floatPreferencesKey("particle_intensity")
    val MOTION_BLUR = floatPreferencesKey("motion_blur")
    val DISCORD_RPC = booleanPreferencesKey("discord_rpc")
    val RENDERER = stringPreferencesKey("renderer")
    val LAST_VERSION_ID = stringPreferencesKey("last_version_id")
    val FIRST_LOGIN_DATE = longPreferencesKey("first_login_date")
    val ACCOUNT_JSON = stringPreferencesKey("account_json")
    val SKIPPED_UPDATE_VERSION = intPreferencesKey("skipped_update")
}

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val ramAllocationMb: Flow<Int> = dataStore.data.map { it[PreferenceKeys.RAM_MB] ?: 2048 }
    val fpsUnlock: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.FPS_UNLOCK] ?: true }
    val jvmArgs: Flow<String> = dataStore.data.map { it[PreferenceKeys.JVM_ARGS] ?: "" }
    val particleIntensity: Flow<Float> = dataStore.data.map { it[PreferenceKeys.PARTICLE_INTENSITY] ?: 0.6f }
    val motionBlur: Flow<Float> = dataStore.data.map { it[PreferenceKeys.MOTION_BLUR] ?: 0.8f }
    val discordRpc: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.DISCORD_RPC] ?: false }
    val renderer: Flow<String> = dataStore.data.map { it[PreferenceKeys.RENDERER] ?: "auto" }
    val lastVersionId: Flow<String> = dataStore.data.map { it[PreferenceKeys.LAST_VERSION_ID] ?: "" }
    val firstLoginDate: Flow<Long> = dataStore.data.map { it[PreferenceKeys.FIRST_LOGIN_DATE] ?: 0L }
    val accountJson: Flow<String> = dataStore.data.map { it[PreferenceKeys.ACCOUNT_JSON] ?: "" }
    val skippedUpdateVersion: Flow<Int> = dataStore.data.map { it[PreferenceKeys.SKIPPED_UPDATE_VERSION] ?: -1 }

    suspend fun setRamAllocation(mb: Int) {
        dataStore.edit { it[PreferenceKeys.RAM_MB] = mb }
        com.movtery.zalithlauncher.setting.AllSettings.ramAllocation.save(mb)
    }

    fun getRamMbSync(): Int {
        return runBlocking {
            dataStore.data
                .map { it[PreferenceKeys.RAM_MB] ?: 2048 }
                .first()
        }
    }

    suspend fun setFpsUnlock(unlock: Boolean) {
        dataStore.edit { it[PreferenceKeys.FPS_UNLOCK] = unlock }
    }

    suspend fun setJvmArgs(args: String) {
        dataStore.edit { it[PreferenceKeys.JVM_ARGS] = args }
    }

    suspend fun setParticleIntensity(intensity: Float) {
        dataStore.edit { it[PreferenceKeys.PARTICLE_INTENSITY] = intensity }
    }

    suspend fun setMotionBlur(blur: Float) {
        dataStore.edit { it[PreferenceKeys.MOTION_BLUR] = blur }
    }

    suspend fun setDiscordRpc(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DISCORD_RPC] = enabled }
    }

    suspend fun setRenderer(renderer: String) {
        dataStore.edit { it[PreferenceKeys.RENDERER] = renderer }
    }

    suspend fun setLastVersionId(versionId: String) {
        dataStore.edit { it[PreferenceKeys.LAST_VERSION_ID] = versionId }
    }

    suspend fun setFirstLoginDate(timestamp: Long) {
        dataStore.edit { it[PreferenceKeys.FIRST_LOGIN_DATE] = timestamp }
    }

    suspend fun setAccountJson(json: String) {
        dataStore.edit { it[PreferenceKeys.ACCOUNT_JSON] = json }
    }

    suspend fun setSkippedUpdateVersion(version: Int) {
        dataStore.edit { it[PreferenceKeys.SKIPPED_UPDATE_VERSION] = version }
    }
}
