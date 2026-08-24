package com.movtery.zalithlauncher

import org.junit.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import com.movtery.zalithlauncher.upgrade.RemoteData

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
            val remoteData = GLOBAL_JSON.decodeFromString<RemoteData>(jsonString)
            println("Parsed RemoteData successfully! Code: " + remoteData.code + ", Version: " + remoteData.version)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
