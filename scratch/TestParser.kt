import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.Base64

@Serializable
data class GithubContentApi(
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

@Serializable
data class RemoteData(
    @SerialName("code") val code: Int,
    @SerialName("version") val version: String,
    @SerialName("created_at") val createdAt: String
)

fun main() {
    val jsonString = File("release.json").readText()
    val GLOBAL_JSON = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
        coerceInputValues = true
    }
    
    try {
        val api = GLOBAL_JSON.decodeFromString<GithubContentApi>(jsonString)
        println("Parsed GithubContentApi successfully!")
        
        val contentString = String(Base64.getMimeDecoder().decode(api.content))
        println("Decoded base64 successfully! Content begins with: ${contentString.take(50)}")
        
        val remoteData = GLOBAL_JSON.decodeFromString<RemoteData>(contentString)
        println("Parsed RemoteData successfully! Code: ${remoteData.code}, Version: ${remoteData.version}")
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
