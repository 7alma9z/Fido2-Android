package com.salman.signertool.recovery

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.salman.signertool.ClientDataKeyType
import com.salman.signertool.KeyType
import com.salman.signertool.PRIVATE_KEY_ALGORITHM
import com.salman.signertool.PRIVATE_KEY_SIGNATURE_ALGORITHM
import com.salman.signertool.getClientData
import com.salman.signertool.getEncodedKeyFromPem
import com.salman.signertool.signData
import com.salman.signertool.toBase64UrlEncodeString
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPrivateCrtKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec


/**
 * File: [AccountRecoverySignature.kt]
 * @author: Salman Aziz
 * Description: This file hold the logic to recover pass key using recovery key / private key pem
 *
 */
class AccountRecoverySignature {

    /**
     * Complete recovery
     *
     * @param privateKeyPem
     * @param origin
     * @param challenge
     * @param credId
     * @param temporaryAuthenticationToken
     * @return [JsonObject]
     */
    fun completeRecovery(
        privateKeyPem: String,
        origin: String,
        challenge: JsonObject,
        credId: String,
        temporaryAuthenticationToken: String
    ): JsonObject {
        val encodedKey = getEncodedKeyFromPem(privateKeyPem, KeyType.PRIVATE)
        val privateKey = getPrivateKeyFromPem(PRIVATE_KEY_ALGORITHM,encodedKey)
        val clientData = getClientData(challenge , origin, ClientDataKeyType.get)
        val signature = clientData.signData(privateKey)

        val publicKeySpec =   RSAPublicKeySpec((privateKey as RSAPrivateCrtKey).modulus,privateKey.publicExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)
        println( "signature-verification : == ${verify(clientData, signature, publicKey)}")
        return getFinalOutput(clientData, signature, challenge, credId, temporaryAuthenticationToken)
    }

    /**
     * Get final output
     *
     * @param clientData
     * @param signature
     * @param challenge
     * @param credId
     * @param temporaryAuthenticationToken
     * @return [JsonObject]
     */
    private fun getFinalOutput(
        clientData: ByteArray,
        signature: ByteArray,
        challenge: JsonObject,
        credId: String,
        temporaryAuthenticationToken: String
    ): JsonObject {
        val credentialAssertion = JsonObject().apply {
            addProperty("credId", credId)
            addProperty("clientData", clientData.toBase64UrlEncodeString())
            addProperty("signature", signature.toBase64UrlEncodeString())
        }
        return JsonObject().apply {
            add("newCredentials", challenge)
            addProperty(
                "temporaryAuthenticationToken",
                temporaryAuthenticationToken
            )
            add("credentialAssertion", credentialAssertion)

        }

    }

    /**
     * Get private key from pem
     *
     * @param algorithm
     * @param encodedKey
     * @return [PrivateKey]
     */
    private fun getPrivateKeyFromPem(algorithm: String, encodedKey: ByteArray): PrivateKey {
        val keySpec = PKCS8EncodedKeySpec(encodedKey)
        val kf = KeyFactory.getInstance(algorithm)
        return kf.generatePrivate(keySpec)
    }
    private fun verify(message: ByteArray,signature: ByteArray, publicKey: PublicKey){
        val message: ByteArray = message
        val signature: ByteArray =signature
        val key: PublicKey =publicKey
        val s = Signature.getInstance(PRIVATE_KEY_SIGNATURE_ALGORITHM)
            .apply {
                initVerify(key)
                update(message)
            }
        val valid: Boolean = s.verify(signature)
        println("s.verify(signature) $valid")
    }


}
