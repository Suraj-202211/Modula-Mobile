package com.modulamobile.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.modulamobile.database.entities.ActivityEntity
import com.modulamobile.database.entities.InstalledModEntity
import com.modulamobile.database.entities.InstalledVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC LIMIT 10")
    fun getRecentActivities(): Flow<List<ActivityEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity)
    
    @Query("DELETE FROM activities WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

@Dao
interface InstalledVersionDao {
    @Query("SELECT * FROM installed_versions ORDER BY downloadedAt DESC")
    fun getAllInstalledVersions(): Flow<List<InstalledVersionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(version: InstalledVersionEntity)
    
    @Query("DELETE FROM installed_versions WHERE versionId = :versionId")
    suspend fun deleteByVersionId(versionId: String)
}

@Dao
interface InstalledModDao {
    @Query("SELECT * FROM installed_mods ORDER BY installedAt DESC")
    fun getAllInstalledMods(): Flow<List<InstalledModEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: InstalledModEntity)
    
    @Query("DELETE FROM installed_mods WHERE modId = :modId")
    suspend fun deleteByModId(modId: String)
}
