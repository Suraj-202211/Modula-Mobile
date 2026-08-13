package com.modulamobile.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import javax.inject.Inject

class GithubApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        const val RAW = "https://raw.githubusercontent.com"
        const val API = "https://api.github.com"
        const val OWNER = "NOVE300IQ"
        const val REPO = "modula-mobile"
        const val NEWS_REPO = "modula-news"
    }

    suspend fun getLatestRelease(): GithubRelease =
        client.get("$API/repos/$OWNER/$REPO/releases/latest") {
            header("Accept", "application/vnd.github.v3+json")
        }.body()

    suspend fun getNewsItems(): List<NewsItem> =
        client.get("$RAW/$OWNER/$NEWS_REPO/main/news.json").body()

    // Cache news for 15 minutes
    private var newsCache: List<NewsItem>? = null
    private var newsCacheTime = 0L

    suspend fun getCachedNews(): List<NewsItem> {
        val now = System.currentTimeMillis()
        if (newsCache != null && now - newsCacheTime < 15 * 60 * 1000) {
            return newsCache!!
        }
        return try {
            val news = getNewsItems()
            newsCache = news
            newsCacheTime = now
            news
        } catch (e: Exception) {
            newsCache ?: emptyList()
        }
    }
}
