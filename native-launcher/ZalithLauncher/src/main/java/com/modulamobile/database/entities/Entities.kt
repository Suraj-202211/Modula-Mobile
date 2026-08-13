package com.modulamobile.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: ActivityType,
    val versionId: String,
    val modLoader: String? = null,
    val modName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ActivityType {
    PLAYED, DOWNLOADED, MOD_INSTALLED, MOD_UNINSTALLED
}

@Entity(tableName = "installed_versions")
data class InstalledVersionEntity(
    @PrimaryKey
    val versionId: String,
    val loader: String,
    val loaderVersion: String = "",
    val mainClass: String,
    val classpath: String,
    val assetIndex: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val lastPlayedAt: Long = 0L
)

@Entity(tableName = "installed_mods")
data class InstalledModEntity(
    @PrimaryKey
    val modId: String,
    val slug: String,
    val name: String,
    val version: String,
    val loader: String,
    val gameVersion: String,
    val jarPath: String,
    val enabled: Boolean = true,
    val iconUrl: String = "",
    val installedAt: Long = System.currentTimeMillis()
)
