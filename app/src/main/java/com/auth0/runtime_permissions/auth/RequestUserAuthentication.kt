package com.auth0.runtime_permissions.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.result.Credentials
import java.util.*


class RequestUserAuthentication :
    ActivityResultContract<Unit, Pair<AuthenticationException?, Credentials?>>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, AuthActivity::class.java)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Pair<AuthenticationException?, Credentials?> {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            val error = AuthenticationException(
                "a0.result_invalid",
                "The authentication result is missing or invalid."
            )
            return Pair(error, null)
        }

        val errorCode = intent.getStringExtra("error_code")
        val errorDescription = intent.getStringExtra("error_description")
        if (!errorCode.isNullOrEmpty()) {
            val error = AuthenticationException(errorCode, errorDescription.orEmpty())
            return Pair(error, null)
        }

        // TODO: Use shared constants
        val idToken = intent.getStringExtra("id_token")!!
        val accessToken = intent.getStringExtra("access_token")!!
        val tokenType = intent.getStringExtra("token_type")!!
        val refreshToken = intent.getStringExtra("refresh_token")
        val expiresAt = intent.getSerializableExtra("expires_at") as Date
        val scope = intent.getStringExtra("scope")
        val credentials =
            Credentials(idToken, accessToken, tokenType, refreshToken, expiresAt, scope)
        return Pair(null, credentials)
    }

}