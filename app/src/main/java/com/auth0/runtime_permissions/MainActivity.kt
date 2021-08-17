package com.auth0.runtime_permissions

import android.Manifest
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.runtime_permissions.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> updatePermissionsUI(granted) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.buttonLogin.setOnClickListener { launchLogin() }
        binding.buttonPermissionRetry.setOnClickListener { requestPermissions() }

        setContentView(binding.root)
        updatePermissionsUI()
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    private fun updatePermissionsUI(granted: Boolean = checkPermissionsAreGranted()) {
        if (granted) {
            binding.imageMap.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            binding.buttonPermissionRetry.isEnabled = false
        } else {
            binding.imageMap.imageTintList =
                ColorStateList.valueOf(Color.parseColor("#F44336"))
            binding.buttonPermissionRetry.isEnabled = true
        }
    }

    private fun checkPermissionsAreGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val alreadyGranted = checkPermissionsAreGranted()
        if (alreadyGranted) {
            // permission was previously granted
            updatePermissionsUI(true)
            return
        }

        val shouldExplain =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!shouldExplain) {
            // show Android permission request dialog
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        // show a friendly UI explaining why the permission is required by the app
        AlertDialog.Builder(this)
            .setTitle("Location required")
            .setMessage("This activity requires access to your location. \n\nGrant it to display the image in GREEN or reject it to keep it RED.")
            .setPositiveButton("Continue") { dialog, _ ->
                // accepting will prompt the user with the Android permission request dialog
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                dialog.dismiss()
            }
            .setNegativeButton("No way") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun launchLogin() {
        // Auth0 client of type 'Native' with Allowed Callback and Logout URLs of:
        // 'dsgpoc://lbalmaceda.auth0.com/android/com.auth0.runtime_permissions/callback'
        // and at least one Connection enabled.
        WebAuthProvider.login(Auth0(this))
            .withParameters(mapOf("prompt" to "login"))
            .withScheme(getString(R.string.com_auth0_scheme))
            .start(this, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Log in failed")
                        .setMessage("[${error.getCode()}]: ${error.getDescription()}")
                        .show()
                }

                override fun onSuccess(result: Credentials) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Log in successful")
                        .setMessage("Received access token [${result.accessToken}]")
                        .show()
                }
            })
    }
}