package com.auth0.runtime_permissions.auth

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class AuthTransaction(
    val issuer: String,
    val audience: String,
    val scheme: String,
    val state: String = secureRandomString(),
    val nonce: String = secureRandomString(),
    val codeVerifier: String = secureRandomString()
) : Parcelable {

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(issuer)
        parcel.writeString(audience)
        parcel.writeString(scheme)
        parcel.writeString(state)
        parcel.writeString(nonce)
        parcel.writeString(codeVerifier)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    companion object CREATOR : Parcelable.Creator<AuthTransaction> {
        override fun createFromParcel(parcel: Parcel): AuthTransaction {
            return AuthTransaction(parcel)
        }

        override fun newArray(size: Int): Array<AuthTransaction?> {
            return arrayOfNulls(size)
        }
    }

}
