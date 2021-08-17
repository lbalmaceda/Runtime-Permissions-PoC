package com.auth0.runtime_permissions

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.auth0.runtime_permissions.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityWelcomeBinding.inflate(layoutInflater)
        binding.buttonLogin.setOnClickListener {
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show()
        }
        binding.buttonGuest.setOnClickListener {
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }

        setContentView(binding.root)
    }
}