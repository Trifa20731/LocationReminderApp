package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.authentication.firebase.FirebaseUserLiveData

class AuthenticationViewModel: ViewModel() {
    companion object {
        const val LOG_TAG: String = "AuthenticationViewModel"
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null ) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }

    }
}