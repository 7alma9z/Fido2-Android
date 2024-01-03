package com.salman.signertool.operations

import com.google.gson.JsonObject
import kotlin.coroutines.CoroutineContext

/**
 * File: SignerToolOperation.kt
 * @author: Salman Aziz
 * Description: This interface is exposing the Operations of Signer tool.
 */
interface SignerToolOperation {
    /**
     * Invokes the provided operation.
     *
     * @param scope
     * @param json Json string required for signing/registration operation.
     * @return [JsonObject] The Json Object according to desired output.
     */
    suspend fun invoke(
        scope: CoroutineContext,
        json: String
    ): Result<JsonObject>
}
