package com.modulamobile.repositories

import com.modulamobile.database.dao.ActivityDao
import com.modulamobile.database.entities.ActivityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {
    fun getRecentActivities(): Flow<List<ActivityEntity>> = activityDao.getRecentActivities()

    suspend fun insertActivity(activity: ActivityEntity) {
        activityDao.insert(activity)
    }

    suspend fun clearOldActivities(cutoffTimestamp: Long) {
        activityDao.deleteOlderThan(cutoffTimestamp)
    }
}
