
# Fido2-Android

Fido2-Android is an open-source Android library that provides support for FIDO2 (Fast Identity Online 2) authentication on Android devices. FIDO2 is a set of standards developed by the FIDO Alliance for secure and convenient authentication without passwords.

# Features

FIDO2 authentication: Enable secure and passwordless authentication on Android devices.
Easy integration: Simple and straightforward integration into Android applications.
Compatibility: Works with FIDO2-compliant servers and platforms.

# Getting Started



## Prerequisites

Android Studio with Kotlin support

Android API level 23 (Android 6.0) or higher
## Usage
## FIDO2 Authentication

Iniitallize Signing operation of SignerTool

```
 private val sign by lazy {
        Signing(gson)
    }
```
    
Get the Challenge json to Sign from server or local

Note: Challenge should be base64url encoded


# Sample Challenge json

```
{
 "rpId": "fido2passkeys.web.app",
    "challenge": "qTi9l7uDE5M2lXyMVa4P4Zkm6R4 ",
    "challengeIdentifier": "qTi9l7uDE5M2lXyMVa4P4Zkm6R4",
    "externalAuthenticationUrl": "qTi9l7uDE5M2lXyMVa4P4Zkm6R4"
}
```

Pass signerToolOperation which is Singing in case of WebAuthn, Challenge json into performOperation methode.


```   
SignerTool.getInstance().performOperation(Dispatchers.Main, signerToolOperation, json)
```
![App Screenshot](https://raw.githubusercontent.com/7alma9z/Fido2-Android/main/screenshots/webauthn.png)

In the response you will get the signed challenge which you can pass to server for additional vaildations.

```
{
  "signedChallenge": {
    "challengeIdentifier": "",
    "firstFactor": {
      "kind": "",
      "credentialAssertion": {
        "credId": "",
        "clientData": "",
        "authenticatorData": "",
        "signature": "",
        "userHandle": ""
      }
    }
  }
}
```
## FIDO2 Registration

Iniitallize Registration operation of SignerTool

```
 private val registration by lazy {
        Registration(gson)
    }
```

Get the Challenge json to Sign from server or local

Note: Challenge and User id should be base64url encoded

Sample Registration Challenge json

```
 {
        "rp": {
            "id": "fido2passkeys.web.app",
            "name": "CredMan App Test"
        },
        "user": {
            "id": "7alma9",
            "displayName": "testemail",
            "name": "testemail@test.com"
        },
        "temporaryAuthenticationToken": "iNllTH8H9O9n1CQuIzzIRL8CkrVUjMphN4xUzEhBRou9XFUwUxvFrMTK93ggL_STGg8LwNMvxOqCvFbH21wbAQ",
        "challenge": "ch-1dtec-pdmbi-9doq08qmgnkidss",
        "pubKeyCredParams": [
            {
                "type": "public-key",
                "alg": -257
            },
            {
                "type": "public-key",
                "alg": -7
            }
        ],
        "excludeCredentials": [],
        "authenticatorSelection": {
            "residentKey": "required",
            "requireResidentKey": true,
            "userVerification": "required"
        },
        "attestation": "direct",
        "supportedCredentialKinds": {
            "firstFactor": [
                "Fido2",
                "Key",
                "Password"
            ],
            "secondFactor": [
                "Fido2",
                "Key",
                "Totp"
            ]
        },   "allowCredentials": {
                  "webauthn": [
                      {
                          "type": "public-key",
                          "id": "jtOYTJhXL4sd6Liiejdtbg"
                      }
                  ],
                  "key": []
              }
     }
```

Pass signerToolOperation which is Registration in case of Registring pass key and Challenge json into performOperation methode.

```
SignerTool.getInstance().performOperation(Dispatchers.Main, signerToolOperation, json)

```

## Registring passkey

![App Screenshot](https://raw.githubusercontent.com/7alma9z/Fido2-Android/main/screenshots/pass%20key%20.png)


In response you will get the registration response from system

```
{
  "temporaryAuthenticationToken": "",
  "firstFactorCredential": {
    "credentialKind": "Fido2",
    "credentialInfo": {
      "credId": " ",
      "attestationData": "",
      "clientData": ""
    }
  }
}
```

## Also contains

This repo also contains the logic for creation of private keys for recovery.

If you are struggling with RSA private keys generation and signing using RSA private key you can get help from the recovery package of Signertool

    1. RSA Public private key pair generation
    2. Signing using private key
    3. Private key PEM to public key generation.
## Additional Sources

 - [identity-samples](https://github.com/android/identity-samples/tree/main/CredentialManager)

 - [FIDO Alliance](https://fidoalliance.org/android-now-fido2-certified-accelerating-global-migration-beyond-passwords/)

