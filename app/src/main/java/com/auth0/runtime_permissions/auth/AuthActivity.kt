package com.auth0.runtime_permissions.auth

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
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

    //TODO: Keep this one safe, it's important!
    private var transaction: AuthTransaction? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        /*
        * FIXME:
        *  Parcelable is not enough, there's no 'savedInstanceState' after the Android OS terminates the app.
         */
        transaction = savedInstanceState?.getParcelable("tx")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("tx", transaction)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        if (transaction == null) {
            beginTransaction()
            return
        }

        // Callback URI plus values from server: error, error_description, code
        if (intent.data == null) {
            // The browser was closed
            finish()
            return
        }

        val values = expandQuery(intent.data!!.query)
        val error = values["error"]
        error?.let {
            val errorDescription = values["error_description"]
            Log.e(AuthActivity::class.java.simpleName, "Error: $it - $errorDescription")
            finish()
            return
        }

        // If there were no errors, continue
        completeTransaction(values["state"]!!, values["code"]!!)
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

    private fun completeTransaction(state: String, authorizationCode: String) {
        val domain = getString(R.string.com_auth0_domain)
        val clientId = getString(R.string.com_auth0_client_id)
        if (transaction!!.state != state) {
            Log.e(AuthActivity::class.java.simpleName, "Error: 'state' mismatch")
            finish()
            return
        }

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
                    Log.e(
                        AuthActivity::class.java.simpleName,
                        "Error: ${error.getCode()} - ${error.getDescription()}"
                    )
                    finish()
                }

                override fun onSuccess(result: Credentials) {
                    //TODO: ID token verification
                    Log.e(AuthActivity::class.java.simpleName, "Success: ${result.accessToken}")
                    finish()
                }
            })
    }
}