package com.example.innogeeks.feature_onboarding.domain.auth

import com.example.innogeeks.core.domain.error.DataError
import com.example.innogeeks.core.domain.util.EmptyResult
import com.example.innogeeks.core.domain.util.Result
import com.example.innogeeks.feature_onboarding.domain.model.AuthUser

interface AuthRepository  {

    suspend fun signUp(name : String ,email : String, password : String) : EmptyResult<DataError>

    suspend fun login(email : String, password: String) : Result<AuthUser , DataError>

    suspend fun logout()

    suspend fun hasSeenIntro() : Boolean

    suspend fun markIntroSeen()
}