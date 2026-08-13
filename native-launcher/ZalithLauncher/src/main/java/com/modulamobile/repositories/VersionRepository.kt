package com.modulamobile.repositories

import com.modulamobile.network.ApiResult
import com.modulamobile.network.MojangApiService
import com.modulamobile.network.VersionManifest
import com.modulamobile.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionRepository @Inject constructor(
    private val mojangApi: MojangApiService
) {
    suspend fun getVersions(): ApiResult<VersionManifest> = safeApiCall {
        mojangApi.getVersionManifest()
    }
}
