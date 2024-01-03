package com.salman.signertool.operations

import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.salman.signertool.SignerTool
import com.salman.signertool.models.SignerToolRegistrationResponse
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext
/**
 * File: [Fido2Creation.kt]
 * @author: Salman Aziz
 * Description: This File implements the [SignerToolOperation.kt] to invoke the Register pass key on a new device
 *
 */
class Fido2Creation(private val gson: Gson): SignerToolOperation {

    private val signerTool  by lazy {
        SignerTool.getInstance()
    }

    /**
     * @param challengeIdentifier [String] challengeIdentifier is temporaryAuthenticationToken .
     * @param registrationJson [String] Challenge json for register fido2 key.
     * @return The Json Object  .
     */
    private suspend fun createPasskey(
        registrationJson: String,
        challengeIdentifier: String
    ): Result<JsonObject> {
         val request = CreatePublicKeyCredentialRequest(registrationJson)
        var response: CreatePublicKeyCredentialResponse? = null
        return try {
            response = signerTool.getCredentialManager().createCredential(
                signerTool.getContext(),
                request
            ) as CreatePublicKeyCredentialResponse
            Result.success(
                mapResponse(
                    response.registrationResponseJson,
                    challengeIdentifier
                )
            )
        } catch (e: CreateCredentialException) {
            Result.failure(handlePasskeyFailure(e))
        }


    }


    private fun mapResponse(
        registrationResponseJson: String,
        challengeIdentifier: String
    ): JsonObject {
        val registrationJson =
            gson.fromJson(registrationResponseJson, SignerToolRegistrationResponse::class.java)

        val credentialInfo = JsonObject().apply {
            addProperty("credId", registrationJson.rawId)
            addProperty("attestationData", registrationJson.response.attestationObject)
            addProperty("clientData", registrationJson.response.clientDataJSON)
        }

        return JsonObject().apply {
            addProperty("challengeIdentifier", challengeIdentifier)
            addProperty("credentialName", "Second key")
             add("credentialInfo", credentialInfo)

        }
    }

    /**
     * Take [CreateCredentialException] as input and returned the exception message in case of pass key creation failure
     * @param e [CreateCredentialException]
     * @return [Exception].
     */
    private fun handlePasskeyFailure(e: CreateCredentialException): Exception {
        val msg = when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // Handle the passkey DOM errors thrown according to the
                // WebAuthn spec using e.domError
                "An error occurred while creating a passkey, please check logs for additional details."
            }

            is CreateCredentialCancellationException -> {
                // The user intentionally canceled the operation and chose not
                // to register the credential.
                "The user intentionally canceled the operation and chose not to register the credential. Check logs for additional details."
            }

            is CreateCredentialInterruptedException -> {
                // Retry-able error. Consider retrying the call.
                "The operation was interrupted, please retry the call. Check logs for additional details."
            }

            is CreateCredentialProviderConfigurationException -> {
                // Your app is missing the provider configuration dependency.
                // Most likely, you're missing "credentials-play-services-auth".
                "Your app is missing the provider configuration dependency. Check logs for additional details."
            }

            is CreateCredentialUnknownException -> {
                "An unknown error occurred while creating passkey. Check logs for additional details."
            }

            is CreateCredentialCustomException -> {
                // You have encountered an error from a 3rd-party SDK. If you
                // make the API call with a request object that's a subclass of
                // CreateCustomCredentialRequest using a 3rd-party SDK, then you
                // should check for any custom exception type constants within
                // that SDK to match with e.type. Otherwise, drop or log the
                // exception.
                "An unknown error occurred from a 3rd party SDK. Check logs for additional details."
            }

            else -> {
                Log.w("Auth", "Unexpected exception type ${e::class.java.name}")
                "An unknown error occurred."
            }
        }
        Log.e("Auth", "createPasskey failed with exception: " + e.message.toString())
        return Exception(msg)
    }

    /**
     * Invokes the createPasskey (Fido2) operation.
     *
     * @param scope CoroutineContext.
     * @param json Challenge json for register fido2 key.
     * @return The Json Object
     */
    override suspend fun invoke(scope: CoroutineContext, json: String): Result<JsonObject> {
        return withContext(scope) {
            //extracting the temporaryAuthenticationToken to put in the desired output
            if (JSONObject(json).has("temporaryAuthenticationToken")) {
                val challengeIdentifier =
                    JSONObject(json).getString("temporaryAuthenticationToken")
                createPasskey(json, challengeIdentifier)
            } else {
                throw Exception("temporaryAuthenticationToken not found")
            }
        }
    }


}
