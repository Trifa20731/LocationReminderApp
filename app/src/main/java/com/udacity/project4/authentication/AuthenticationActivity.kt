package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.Constants

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG: String = "AuthenticationActivity"
    }

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(LOG_TAG, "onCreate run.")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        initClickListener()
        //TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }


//------------------------------------- Click Function ---------------------------------------------


    private fun initClickListener() {
        binding.signinBtn.setOnClickListener {
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        // Provide the login methods.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            Constants.SIGN_IN_REQUEST_CODE
        )
    }


//--------------------------------------------------------------------------------------------------


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(LOG_TAG, "onActivityResult: run.")
        if (requestCode == Constants.SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                Log.i(LOG_TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
            } else {
                Log.i(LOG_TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
                binding.welcomeInfoTv.text = getString(R.string.authentication_title_reminder)
            }
        }
    }



}
