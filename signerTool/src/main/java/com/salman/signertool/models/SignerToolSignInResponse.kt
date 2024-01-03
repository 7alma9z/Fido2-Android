package com.salman.signertool.models

/**
 * File: SignerToolSignInResponse.kt
 * Author: Salman Aziz
 * Description: This file will hold information from PublicKeyCredential.kt to create a json object for further use in submitting webauthn.
 */

internal data class SignerToolSignInResponse(
    val authenticatorAttachment: String,
    val id: String,
    val rawId: String,
    val response: SignInResponse,
    val type: String
)

internal data class SignInResponse(
    val authenticatorData: String,
    val clientDataJSON: String,
    val signature: String,
    val userHandle: String
)
