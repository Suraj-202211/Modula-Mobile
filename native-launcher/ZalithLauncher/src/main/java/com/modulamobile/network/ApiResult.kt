package com.modulamobile.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(call())
} catch (e: ClientRequestException) {
    android.util.Log.e("ModulaNetwork", "Error type: ${e.javaClass.simpleName}")
    android.util.Log.e("ModulaNetwork", "Error message: ${e.message}")
    android.util.Log.e("ModulaNetwork", "Cause: ${e.cause?.message}")
    e.printStackTrace()
    ApiResult.Error("Request failed: ${e.response.status}", e.response.status.value)
} catch (e: ServerResponseException) {
    android.util.Log.e("ModulaNetwork", "Error type: ${e.javaClass.simpleName}")
    android.util.Log.e("ModulaNetwork", "Error message: ${e.message}")
    android.util.Log.e("ModulaNetwork", "Cause: ${e.cause?.message}")
    e.printStackTrace()
    ApiResult.Error("Server error: ${e.response.status}", e.response.status.value)
} catch (e: IOException) {
    android.util.Log.e("ModulaNetwork", "Error type: ${e.javaClass.simpleName}")
    android.util.Log.e("ModulaNetwork", "Error message: ${e.message}")
    android.util.Log.e("ModulaNetwork", "Cause: ${e.cause?.message}")
    e.printStackTrace()
    ApiResult.NetworkError
} catch (e: Exception) {
    android.util.Log.e("ModulaNetwork", "Error type: ${e.javaClass.simpleName}")
    android.util.Log.e("ModulaNetwork", "Error message: ${e.message}")
    android.util.Log.e("ModulaNetwork", "Cause: ${e.cause?.message}")
    e.printStackTrace()
    ApiResult.Error(e.message ?: "Unknown error")
}
