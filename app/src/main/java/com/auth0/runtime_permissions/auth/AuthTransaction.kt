package com.auth0.runtime_permissions.auth

import android.net.Uri
import org.json.JSONObject

class AuthTransaction(
    val issuer: String,
    val audience: String,
    val scheme: String,
    val state: String = secureRandomString(),
    val nonce: String = secureRandomString(),
    val codeVerifier: String = secureRandomString()
) {

    /**
     * Code Challenge: required for PKCE flow completion
     */
    private val codeChallenge = deriveCodeChallenge(codeVerifier)

    /**
     * Redirect URI: Used to call back into the app from Auth0's Universal Login
     * Doesn't need to have a specific format. But it does need to match against:
     * - What is set in the "Allowed Callback URLs" section of the application (Auth0 dashboard)
     * - What is defined in the intent-filter that captures it (AndroidManifest.xml file)
     */
    val redirectUri: Uri = Uri.parse("${scheme}://${issuer}/android/callback")

    /**
     * Authorize URI: Used to show Auth0's Universal Login
     */
    fun getAuthorizeUri(additionalParameters: Map<String, String> = emptyMap()): Uri {
        val builder = Uri.parse("https://${issuer}/authorize").buildUpon()
            // Required parameters to follow
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", audience)
            .appendQueryParameter("redirect_uri", redirectUri.toString())
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("scope", "openid profile email")
            // Recommended parameters to follow
            .appendQueryParameter("nonce", nonce)
            .appendQueryParameter("state", state)

        // Optional parameters (or overrides) to follow
        additionalParameters.map { builder.appendQueryParameter(it.key, it.value) }
        return builder.build()
    }

    fun toJSON(): String {
        val tx = JSONObject()
        tx.put("issuer", issuer)
        tx.put("audience", audience)
        tx.put("scheme", scheme)
        tx.put("state", state)
        tx.put("nonce", nonce)
        tx.put("codeVerifier", codeVerifier)
        return tx.toString()
    }

    companion object {
        fun fromJSON(json: String?): AuthTransaction? {
            if (json.isNullOrEmpty()) {
                return null
            }
            val tx = JSONObject(json)
            // TODO: Use shared constants
            return AuthTransaction(
                tx.getString("issuer"),
                tx.getString("audience"),
                tx.getString("scheme"),
                tx.getString("state"),
                tx.getString("nonce"),
                tx.getString("codeVerifier"),
            )
        }
    }

}
