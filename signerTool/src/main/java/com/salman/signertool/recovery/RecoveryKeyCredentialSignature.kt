package com.salman.signertool.recovery

import android.util.Base64
import com.google.gson.JsonObject
import com.salman.signertool.ClientDataKeyType
import com.salman.signertool.KEY_FOOTER
import com.salman.signertool.KEY_HEADER
import com.salman.signertool.KeyType
import com.salman.signertool.SignerToolKeyType
import com.salman.signertool.PRIVATE_KEY_ALGORITHM
import com.salman.signertool.PRIVATE_KEY_HASH_ALGORITHM
import com.salman.signertool.getClientData
import com.salman.signertool.getKeyPairs
import com.salman.signertool.signData
import com.salman.signertool.toBase64EncodeString
import com.salman.signertool.toBase64UrlEncodeString
import com.salman.signertool.toHexString
import java.security.PrivateKey
import java.util.UUID

class RecoveryKeyCredentialSignature {

    private val appKeyPairs by lazy {
        getKeyPairs(PRIVATE_KEY_ALGORITHM)
    }

    fun createRecovery(challenge: String, challengeIdentifier: String, origin: String): Pair<JsonObject, String> {
        val publicKey = getPublicKey()
        val privateKey = getPrivateKey()
        val clientData = getClientData(challenge, origin, ClientDataKeyType.create)
        val clientDataHash = getClientDataHash(clientData)
        val publicKeyPem = getKeyPem(publicKey)
        val signaturePayload = getSignaturePayload(clientDataHash, publicKeyPem)
        val signature = signaturePayload.signData((privateKey.key as PrivateKey))
        return Pair(getFinalOutput(publicKeyPem, signature, clientData, challengeIdentifier), getKeyPem(privateKey))
    }


    private fun getPublicKey() = SignerToolKeyType(appKeyPairs.public, KeyType.PUBLIC)
    private fun getPrivateKey() = SignerToolKeyType(appKeyPairs.private, KeyType.PRIVATE)


    private fun getKeyPem(key: SignerToolKeyType): String {
        return key.key.encoded.toBase64EncodeString().replace("\n", "\\n").chunked(64)
            .joinToString(
                separator = "\n",
                prefix = String.format(KEY_HEADER, key.keyType.name),
                postfix = String.format(KEY_FOOTER, key.keyType.name)
            )
    }


    private fun getClientDataHash(clientData: ByteArray): String {
        return hashSHA256(clientData)
    }

    private fun getSignaturePayload(clientDataHash: String, publicKeyPem: String): ByteArray {
        return JsonObject().apply {
            addProperty("clientDataHash", clientDataHash)
            addProperty("publicKey", publicKeyPem)
        }.toString().toByteArray()
    }

    private fun hashSHA256(data: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance(PRIVATE_KEY_HASH_ALGORITHM)
        val hash = digest.digest(data)
        return hash.fold("") { str, it -> str + "%02x".format(it) }
    }


    private fun getFinalOutput(
        publicKeyPem: String,
        signature: ByteArray,
        clientData: ByteArray,
        challengeIdentifier: String
    ): JsonObject {
        val attestationData = JsonObject().apply {
            addProperty("publicKey", publicKeyPem)
            addProperty("signature", signature.toHexString())//signature.toHexString())
        }.toString().toByteArray()

        val credentialInfo = JsonObject().apply {
            addProperty(
                "credId",
                UUID.randomUUID().toString().toByteArray().toBase64UrlEncodeString()
            )
            addProperty(
                "attestationData",

                Base64.encodeToString(
                    attestationData,
                    Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE
                )
            )
            addProperty(
                "clientData",
                clientData.toBase64UrlEncodeString()
            )
        }
        return JsonObject().apply {
            addProperty("challengeIdentifier", challengeIdentifier)
            addProperty("credentialName", "My Recovery Key")
            addProperty("encryptedPrivateKey", "124324")
            add("credentialInfo", credentialInfo)
        }
    }

}
