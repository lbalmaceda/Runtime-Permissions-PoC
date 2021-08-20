package com.auth0.runtime_permissions.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class RedirectActivity : Activity() {
    public override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (getIntent() != null) {
            intent.data = getIntent().data
        }
        startActivity(intent)
        finish()
    }
}