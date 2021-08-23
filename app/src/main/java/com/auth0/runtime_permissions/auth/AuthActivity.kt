package com.auth0.runtime_permissions.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.request.DefaultClient
import com.auth0.android.result.Credentials
import com.auth0.runtime_permissions.R

class AuthActivity : AppCompatActivity() {

    private var transaction: AuthTransaction? = null

    private fun loadTransaction() {
        val sp = getPreferences(MODE_PRIVATE)
        val txJSON = sp.getString("authTransaction", "")
        transaction = AuthTransaction.fromJSON(txJSON)
    }

    private fun saveTransaction() {
        val txJSON = transaction?.toJSON()
        getPreferences(MODE_PRIVATE).edit()
            .putString("authTransaction", txJSON)
            .apply()
    }

    override fun onPause() {
        saveTransaction()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        loadTransaction()
        if (transaction == null) {
            beginTransaction()
            return
        }

        // Callback URI plus values from server: error, error_description, code
        if (intent.data == null) {
            // The browser was closed
            finishWithError("a0.authentication_canceled")
            return
        }

        val values = expandQuery(intent.data!!.query)
        val error = values["error"]
        error?.let {
            // Server failed with an error
            val errorDescription = values["error_description"]
            finishWithError(error, errorDescription)
            return
        }
        val state = values["state"]
        if (transaction!!.state != state) {
            // The state received doesn't match
            finishWithError("a0.state_mismatch")
            return
        }

        // If there were no errors, continue
        completeTransaction(values["code"]!!)
    }

    private fun finishWithError(errorCode: String, errorDescription: String? = null) {
        val result = Intent()
        result.putExtra("error_code", errorCode)
        result.putExtra("error_description", errorDescription)
        setResult(RESULT_OK, result)
        transaction = null
        finish()
    }

    private fun finishWithCredentials(credentials: Credentials) {
        val result = Intent()
        result.putExtra("id_token", credentials.idToken)
        result.putExtra("access_token", credentials.accessToken)
        result.putExtra("token_type", credentials.type)
        result.putExtra("refresh_token", credentials.refreshToken)
        result.putExtra("expires_at", credentials.expiresAt)
        result.putExtra("scope", credentials.scope)
        setResult(RESULT_OK, result)
        transaction = null
        finish()
    }

    private fun beginTransaction() {
        val domain = getString(R.string.com_auth0_domain)
        val clientId = getString(R.string.com_auth0_client_id)
        val scheme = getString(R.string.com_auth0_scheme)

        // A new instance MUST be created for every authentication attempt
        transaction = AuthTransaction(domain, clientId, scheme)

        val optionalParameters = mapOf("prompt" to "login")
        // Note: Assumes a Browser app is installed on the device
        CustomTabsIntent.Builder().build()
            .launchUrl(this, transaction!!.getAuthorizeUri(optionalParameters))
    }

    private fun completeTransaction(authorizationCode: String) {
        val domain = getString(R.string.com_auth0_domain)
        val clientId = getString(R.string.com_auth0_client_id)

        val account = Auth0(clientId, domain).apply {
            networkingClient = DefaultClient(enableLogging = true)
        }
        AuthenticationAPIClient(account)
            .token(
                authorizationCode,
                transaction!!.codeVerifier,
                transaction!!.redirectUri.toString()
            )
            .start(object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    finishWithError(error.getCode(), error.getDescription())
                }

                override fun onSuccess(result: Credentials) {
                    //TODO: ID token verification
                    finishWithCredentials(result)
                }
            })
    }
}