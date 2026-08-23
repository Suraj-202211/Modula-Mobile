package com.movtery.zalithlauncher

import org.junit.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.Base64
import com.movtery.zalithlauncher.upgrade.RemoteData
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GithubContentApiMock(
    @SerialName("name") val name: String,
    @SerialName("path") val path: String,
    @SerialName("sha") val sha: String,
    @SerialName("size") val size: Int,
    @SerialName("url") val url: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("git_url") val gitUrl: String,
    @SerialName("download_url") val downloadUrl: String?,
    @SerialName("type") val type: String,
    @SerialName("content") val content: String,
    @SerialName("encoding") val encoding: String,
    @SerialName("_links") val links: Links
) {
    @Serializable
    data class Links(
        @SerialName("self") val self: String,
        @SerialName("git") val git: String,
        @SerialName("html") val html: String
    )
}

class ParserTest {
    @Test
    fun testParse() {
        val jsonString = File("../../release.json").readText()
        val GLOBAL_JSON = Json {
            ignoreUnknownKeys = true
            explicitNulls = true
            coerceInputValues = true
        }
        
        try {
            val api = GLOBAL_JSON.decodeFromString<GithubContentApiMock>(jsonString)
            println("Parsed GithubContentApi successfully!")
            
            val contentString = String(Base64.getMimeDecoder().decode(api.content))
            println("Decoded base64 successfully! Content begins with: " + contentString.take(50))
            
            val remoteData = GLOBAL_JSON.decodeFromString<RemoteData>(contentString)
            println("Parsed RemoteData successfully! Code: " + remoteData.code + ", Version: " + remoteData.version)
            
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
