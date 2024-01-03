package com.salman.signertool.models

/**
 * File: SignerToolRegistrationResponse.kt
 * Author: Salman Aziz
 * Description: This file will hold information from CreatePublicKeyCredentialResponse to create a json object for further use in submitting registration challenge.
 */
internal data class SignerToolRegistrationResponse(
    val authenticatorAttachment: String,
    val clientExtensionResults: ClientExtensionResults,
    val id: String,
    val rawId: String,
    val response: Response,
    val type: String
)

internal data class ClientExtensionResults(
    val credProps: CredProps
)

internal data class Response(
    val attestationObject: String,
    val authenticatorData: String,
    val clientDataJSON: String,
    val publicKey: String,
    val publicKeyAlgorithm: Int,
    val transports: List<String>
)

internal data class CredProps(
    val rk: Boolean
)
