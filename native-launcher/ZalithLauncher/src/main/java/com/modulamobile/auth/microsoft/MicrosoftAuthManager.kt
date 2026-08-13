package com.modulamobile.auth.microsoft

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import com.movtery.zalithlauncher.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class MicrosoftAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var msalApp: ISingleAccountPublicClientApplication? = null

    // We use Ktor for network requests
    private val httpClient = HttpClient()

    suspend fun initialize() = suspendCancellableCoroutine<Boolean> { cont ->
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.msal_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    msalApp = application
                    cont.resume(true)
                }
                override fun onError(exception: MsalException) {
                    exception.printStackTrace()
                    cont.resume(false)
                }
            }
        )
    }

    suspend fun signIn(activity: Activity): MicrosoftSignInResult = suspendCancellableCoroutine { cont ->
        val app = msalApp ?: run {
            cont.resume(MicrosoftSignInResult.Error("MSAL not initialized"))
            return@suspendCancellableCoroutine
        }

        val scopes = arrayOf("XboxLive.signin", "offline_access")

        app.signIn(
            activity,
            null,
            scopes,
            object : AuthenticationCallback {
                override fun onSuccess(result: IAuthenticationResult) {
                    cont.resume(MicrosoftSignInResult.Success(result.accessToken))
                }
                override fun onError(exception: MsalException) {
                    exception.printStackTrace()
                    cont.resume(MicrosoftSignInResult.Error(exception.message ?: "Sign in failed"))
                }
                override fun onCancel() {
                    cont.resume(MicrosoftSignInResult.Cancelled)
                }
            }
        )
    }

    suspend fun getMinecraftToken(msAccessToken: String): MinecraftTokenResult {
        return try {
            // Step 1: Xbox Live auth
            val xblResponse = httpClient.post("https://user.auth.xboxlive.com/user/authenticate") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "Properties": {
                            "AuthMethod": "RPS",
                            "SiteName": "user.auth.xboxlive.com",
                            "RpsTicket": "d=$msAccessToken"
                        },
                        "RelyingParty": "http://auth.xboxlive.com",
                        "TokenType": "JWT"
                    }
                    """.trimIndent()
                )
            }
            val xblJson = JSONObject(xblResponse.bodyAsText())
            val xblToken = xblJson.getString("Token")
            val userHash = xblJson.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs")

            // Step 2: XSTS token
            val xstsResponse = httpClient.post("https://xsts.auth.xboxlive.com/xsts/authorize") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "Properties": {
                            "SandboxId": "RETAIL",
                            "UserTokens": ["$xblToken"]
                        },
                        "RelyingParty": "rp://api.minecraftservices.com/",
                        "TokenType": "JWT"
                    }
                    """.trimIndent()
                )
            }
            val xstsJson = JSONObject(xstsResponse.bodyAsText())

            if (xstsJson.has("XErr")) {
                val xerr = xstsJson.getLong("XErr")
                return MinecraftTokenResult.Error(getXstsError(xerr))
            }

            val xstsToken = xstsJson.getString("Token")

            // Step 3: Minecraft token
            val mcResponse = httpClient.post("https://api.minecraftservices.com/authentication/login_with_xbox") {
                contentType(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "identityToken": "XBL3.0 x=$userHash;$xstsToken"
                    }
                    """.trimIndent()
                )
            }
            val mcJson = JSONObject(mcResponse.bodyAsText())
            val mcToken = mcJson.getString("access_token")
            val expiresIn = mcJson.getLong("expires_in")

            // Step 4: Get Minecraft profile
            val profileResponse = httpClient.get("https://api.minecraftservices.com/minecraft/profile") {
                header("Authorization", "Bearer $mcToken")
            }

            if (profileResponse.status.value == 404) {
                return MinecraftTokenResult.Error(
                    "This Microsoft account does not own Minecraft.\n" +
                    "Purchase Minecraft at minecraft.net"
                )
            }

            val profileJson = JSONObject(profileResponse.bodyAsText())
            val uuid = profileJson.getString("id")
            val username = profileJson.getString("name")

            MinecraftTokenResult.Success(
                username = username,
                uuid = uuid,
                accessToken = mcToken,
                expiresIn = expiresIn
            )

        } catch (e: Exception) {
            e.printStackTrace()
            MinecraftTokenResult.Error(e.message ?: "Auth failed")
        }
    }

    private fun getXstsError(code: Long): String = when (code) {
        2148916233L -> "This Microsoft account has no Xbox profile.\nGo to xbox.com to create one."
        2148916235L -> "Xbox Live is not available in your country."
        2148916236L, 2148916237L -> "Adult verification required.\nCheck your Microsoft account."
        2148916238L -> "Child account detected.\nParental consent required."
        else -> "Xbox authentication failed.\nError code: $code"
    }
}

sealed class MicrosoftSignInResult {
    data class Success(val accessToken: String) : MicrosoftSignInResult()
    data class Error(val message: String) : MicrosoftSignInResult()
    object Cancelled : MicrosoftSignInResult()
}

sealed class MinecraftTokenResult {
    data class Success(
        val username: String,
        val uuid: String,
        val accessToken: String,
        val expiresIn: Long
    ) : MinecraftTokenResult()
    data class Error(val message: String) : MinecraftTokenResult()
}
