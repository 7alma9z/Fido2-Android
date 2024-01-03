package com.salman.signertool

import android.security.keystore.KeyProperties.KEY_ALGORITHM_EC
import android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature

const val PRIVATE_KEY_ALGORITHM = KEY_ALGORITHM_RSA
const val PRIVATE_KEY_SIGNATURE_ALGORITHM = "SHA256withRSA"
const val PRIVATE_KEY_HASH_ALGORITHM = "SHA-256"
const val PRIVATE_KEY_FACET_ID_ALGORITHM = "SHA256"

internal fun String.base64Decode(): ByteArray {
   return Base64.decode(
       this,
        Base64.DEFAULT
    )
}
internal fun ByteArray.toBase64EncodeString(): String{
   return Base64.encodeToString(
       this,
       Base64.NO_WRAP
   )
}
internal fun ByteArray.toBase64UrlEncodeString(): String{
   return Base64.encodeToString(
       this,
       Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE
   )
}
internal fun String.toBase64UrlEncodeString(): String{
    return Base64.encodeToString(
        this.toByteArray(),
        Base64.NO_PADDING or Base64.NO_WRAP or Base64.URL_SAFE
    )
}
internal fun ByteArray.signData(  privateKey: PrivateKey): ByteArray {
    val signature = Signature.getInstance(PRIVATE_KEY_SIGNATURE_ALGORITHM)
    signature.initSign(privateKey)
    signature.update(this)
    return signature.sign()
}

internal fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}

internal fun getClientData(challenge: String, origin: String,type: ClientDataKeyType): ByteArray {

    val keyClientData = JsonObject().apply {
        addProperty("type", "key.${type.name}")
        addProperty("challenge", challenge )
        addProperty("origin", origin)
        addProperty("crossOrigin", false)

    }.toString()


    return keyClientData.toByteArray()

}
internal fun getClientData(challenge: JsonElement, origin: String,type: ClientDataKeyType): ByteArray {

    val keyClientData = JsonObject().apply {
        addProperty("type", "key.${type.name}")
        addProperty("challenge", challenge.toString().toBase64UrlEncodeString() )
        addProperty("origin", origin)
        addProperty("crossOrigin", false)

    }.toString()


    return keyClientData.toByteArray()

}
internal fun getKeyPairs(algorithm:String= PRIVATE_KEY_ALGORITHM): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)
    keyPairGenerator.initialize(2048) // Adjust the key size as needed
    return keyPairGenerator.generateKeyPair()
}
internal fun getEncodedKeyFromPem(keyPem: String, keyType: KeyType): ByteArray {
   return keyPem
        .replace(String.format(KEY_HEADER, keyType.name).removeNewLines(), "")
        .replace(String.format(KEY_FOOTER, keyType.name).removeNewLines(), "")
       .removeNewLines()
        .base64Decode()

}

internal fun String.removeNewLines(): String {
   return this.replace("\\n","").replace("\n","")
}
