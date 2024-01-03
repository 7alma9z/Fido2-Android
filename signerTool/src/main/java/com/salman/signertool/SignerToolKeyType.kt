package com.salman.signertool

import java.security.Key

/**
 * Open wallet keys
 *
 * @property key
 * @property keyType
 *
 */

data class SignerToolKeyType(val key: Key, val keyType: KeyType)

/**
 * File: [KeyType.kt]
 * @author: Salman Aziz
 * Description: This file holds the Key types enum
 *
 */
enum class KeyType{
    /**
     * Public enum for public key
     *
     */
    PUBLIC,

    /**
     * Private enum for private key
     *
     * */
    PRIVATE
}

const val KEY_HEADER = "-----BEGIN %s KEY-----\n"
const val KEY_FOOTER = "\n-----END %s KEY-----\n"
