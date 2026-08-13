package com.modulamobile.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.modulamobile.database.dao.ActivityDao
import com.modulamobile.database.dao.InstalledModDao
import com.modulamobile.database.dao.InstalledVersionDao
import com.modulamobile.database.entities.ActivityEntity
import com.modulamobile.database.entities.ActivityType
import com.modulamobile.database.entities.InstalledModEntity
import com.modulamobile.database.entities.InstalledVersionEntity

class ModulaTypeConverters {
    @TypeConverter
    fun fromStringList(value: String): List<String> =
        value.split(",").filter { it.isNotEmpty() }
    
    @TypeConverter
    fun toStringList(list: List<String>): String =
        list.joinToString(",")
    
    @TypeConverter
    fun fromActivityType(type: ActivityType): String =
        type.name
    
    @TypeConverter
    fun toActivityType(value: String): ActivityType =
        ActivityType.valueOf(value)
}

@Database(
    entities = [
        ActivityEntity::class,
        InstalledVersionEntity::class,
        InstalledModEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ModulaTypeConverters::class)
abstract class ModulaDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun versionDao(): InstalledVersionDao
    abstract fun modDao(): InstalledModDao
}
