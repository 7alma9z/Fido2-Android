package com.salman.signertool.operations

import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.salman.signertool.SignerTool
import com.salman.signertool.models.SignerToolSignInResponse
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

/**
 * File: [Signing.kt]
 * @author: Salman Aziz
 * Description: This File implements the [SignerToolOperation.kt] to invoke the Signing challenge with existing pass key
 *
 */
class Signing(private val gson: Gson): SignerToolOperation {

    private val signerTool  by lazy {
        SignerTool.getInstance()
    }

    /**
     * @param challengeIdentifier [String]
     * @param signingJson [String] Challenge json for register pass key.
     * @return The Json Object according to desired output which is map able on [CompleteRecoveryKeyCreationRequestData.kt] of network sdk module.
     */
    private suspend fun sign(
        signingJson: String,
        challengeIdentifier: String

    ): Result<JsonObject> {
         val getPublicKeyCredentialOption =
            GetPublicKeyCredentialOption(signingJson)
        val result = try {
            signerTool.getCredentialManager().getCredential(
                signerTool.getContext(),
                GetCredentialRequest(
                    listOf(
                        getPublicKeyCredentialOption,
                    )
                )
            ).credential
        } catch (e: Exception) {
            e
        }

        return when (result) {
            is PublicKeyCredential -> {

                Result.success(mapResponse(result.authenticationResponseJson, challengeIdentifier))
            }

            is java.lang.Exception -> {
                Result.failure(result)
            }

            else -> {
                Result.failure(Exception("unknown result"))
            }
        }
    }

    /**
     * @param challengeIdentifier [String]  which will be added in response json.
     * @param authenticationResponseJson [String] Signed challenge response .
     * @return The Json Object according to desired output which is map able on [CompleteRecoveryKeyCreationRequestData.kt] of network sdk module.
     */
    private fun mapResponse(
        authenticationResponseJson: String,
        challengeIdentifier: String,

        ): JsonObject {
        val signinJson =
            gson.fromJson(authenticationResponseJson, SignerToolSignInResponse::class.java)

        val credentialAssertion = JsonObject().apply {
            addProperty("credId", signinJson.rawId)
            addProperty("clientData", signinJson.response.clientDataJSON)
            addProperty("authenticatorData", signinJson.response.authenticatorData)
            addProperty("signature", signinJson.response.signature)
            addProperty("userHandle", signinJson.response.userHandle)
        }
        val firstFactor = JsonObject().apply {
            addProperty("kind", "Fido2")
            add("credentialAssertion", credentialAssertion)
        }
        val signedChallenge = JsonObject().apply {
            addProperty("challengeIdentifier", challengeIdentifier)
            add("firstFactor", firstFactor)
        }
        return JsonObject().apply { add("signedChallenge", signedChallenge) }
    }

    /**
     * Invokes the createPasskey (Fido2) operation.
     *
     * @param scope CoroutineContext.
     * @param json Challenge json for Signing with existing pass key.
     * @return The Json Object
     */
    override suspend fun invoke(scope: CoroutineContext, json: String): Result<JsonObject> {
        return withContext(scope) {
            //extracting the challengeIdentifier to put in the desired output
            if (JSONObject(json).has("challengeIdentifier")) {
                val challengeIdentifier =
                    JSONObject(json).getString("challengeIdentifier")
                sign(json, challengeIdentifier)
            } else {
                throw Exception("challengeIdentifier not found")
            }
        }
    }
}

