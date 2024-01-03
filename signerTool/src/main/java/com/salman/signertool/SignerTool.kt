package com.salman.signertool

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

 import androidx.credentials.CredentialManager
import com.google.gson.JsonObject
import com.salman.signertool.operations.SignerToolOperation
import com.salman.signertool.recovery.AccountRecoverySignature
import com.salman.signertool.recovery.RecoveryKeyCredentialSignature
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateFactory
import javax.security.cert.CertificateEncodingException
import javax.security.cert.CertificateException
import javax.security.cert.X509Certificate
import kotlin.coroutines.CoroutineContext


/**
 * Signer tool
 *
 * @property context of activity context type
 * @constructor Create Signer tool private instance
 */
class SignerTool private constructor(
    private val context: WeakReference<Activity>,

    ) {
    private val credentialManager by lazy { CredentialManager.create(getContext()) }
    private val accountRecoverySignature by lazy { AccountRecoverySignature() }
    private val recoveryKeyCredentialSignature by lazy { RecoveryKeyCredentialSignature() }


    companion object {

        private lateinit var instance: SignerTool

        /**
         * Initialize the SDK with the required context.
         *
         * @param context The application context.
         */
        fun initialize(context: WeakReference<Activity>) {
            instance = SignerTool(context = context)
         }

        /**
         * Get the initialized instance of the SDK.
         * This method assumes that the SDK has been initialized before calling.
         * Make sure to call [initialize] before using this method.
         *
         * @return The initialized instance of the SDK.
         * @throws UninitializedPropertyAccessException if the SDK has not been initialized.
         */
        fun getInstance(): SignerTool {
            if (!Companion::instance.isInitialized) {
                throw UninitializedPropertyAccessException("SDK has not been initialized. Call initialize() first.")
            }
            return instance
        }

    }

    /**
     * Get credential manager
     *
     * @return [CredentialManager]
     */
    internal fun getCredentialManager(): CredentialManager {
        return credentialManager
    }

    /**
     * Get context
     *
     * @return [Context] which should be activity context
     */
    internal fun getContext(): Context {
        return context.get()!!
    }

    /**
     * Perform operation
     * This method will perform operations under [SignerToolOperation.kt]
     * @param scope
     * @param signerToolOperation
     * @param registrationJson
     * @return [JsonObject]  encapsulated in [Result]
     */
    suspend fun performOperation(
        scope: CoroutineContext,
        signerToolOperation: SignerToolOperation,
        registrationJson: String,

        ): Result<JsonObject> {
        return signerToolOperation.invoke(scope, registrationJson)
    }

    /**
     * Create recovery key
     *
     * @param challenge
     * @param challengeIdentifier
     * @return [Pair] of [JsonObject] and  [String] where [Pair.first] is map able on [RecoveryKeyCreationRequestData.kt] located in network module and [Pair.second] is private key of signed signature.
     */
    fun createRecoveryKey(challenge: String, challengeIdentifier: String): Pair<JsonObject, String> {
        return recoveryKeyCredentialSignature.createRecovery(challenge, challengeIdentifier, getFacetID(getContext()))
    }

    /**
     * Recover key from private key
     *
     * @param challenge
     * @param challengeIdentifier
     * @param pemkey
     * @param credId
     * @return [JsonObject]
     */
    fun recoverKeyFromPrivateKey(challenge: JsonObject, challengeIdentifier: String, pemKey: String, credId: String): JsonObject {

        return accountRecoverySignature.completeRecovery(pemKey, getFacetID(getContext()), challenge, credId, challengeIdentifier)

    }

    /**
     * Get facet id
     *
     * @param aContext
     * @return [String] which is origin for client data json
     */

    @Suppress("DEPRECATION")
    @SuppressLint("PackageManagerGetSignatures")
    private fun getFacetID(aContext: Context): String {
        try {
            val info =
                aContext.packageManager.getPackageInfo(
                    aContext.packageName,
                    PackageManager.GET_SIGNATURES
                )
            val cert = info.signatures[0].toByteArray()
            val input: InputStream = ByteArrayInputStream(cert)
            val cf = CertificateFactory.getInstance("X509")
            val c = cf.generateCertificate(input)
            val md = MessageDigest.getInstance(PRIVATE_KEY_FACET_ID_ALGORITHM)
            return "android:apk-key-hash:" +md.digest(c.encoded).toBase64UrlEncodeString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateEncodingException) {
            e.printStackTrace()
        }
        return ""
    }


}
