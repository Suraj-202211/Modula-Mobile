package com.modulamobile.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.modulamobile.database.ModulaDatabase
import com.modulamobile.database.dao.ActivityDao
import com.modulamobile.database.dao.InstalledModDao
import com.modulamobile.database.dao.InstalledVersionDao
import com.modulamobile.network.GithubApiService
import com.modulamobile.network.ModrinthApiService
import com.modulamobile.network.MojangApiService
import com.modulamobile.settings.modulaDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 30_000
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(3)
            retryOnException(3, true)
            exponentialDelay()
        }
        defaultRequest {
            header("User-Agent", "ModulaMobile/1.0.0")
            header("Accept", "application/json")
        }
    }

    @Provides
    @Singleton
    fun provideMojangApi(client: HttpClient): MojangApiService = MojangApiService(client)

    @Provides
    @Singleton
    fun provideModrinthApi(client: HttpClient): ModrinthApiService = ModrinthApiService(client)

    @Provides
    @Singleton
    fun provideGithubApi(client: HttpClient): GithubApiService = GithubApiService(client)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): ModulaDatabase =
        Room.databaseBuilder(
            ctx,
            ModulaDatabase::class.java,
            "modula_db_v1"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideActivityDao(db: ModulaDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideVersionDao(db: ModulaDatabase): InstalledVersionDao = db.versionDao()

    @Provides
    fun provideModDao(db: ModulaDatabase): InstalledModDao = db.modDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.modulaDataStore
}
