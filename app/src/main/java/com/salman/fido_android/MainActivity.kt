package com.salman.fido_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.salman.fido_android.ui.theme.FidoAndroidTheme
import com.salman.signertool.SignerTool
import com.salman.signertool.operations.Registration
import com.salman.signertool.operations.SignerToolOperation
import com.salman.signertool.operations.Signing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


class MainActivity : ComponentActivity() {
    private val gson by lazy {
        Gson()
    }
    private val registration by lazy {
        Registration(gson)
    }
    private val sign by lazy {
        Signing(gson)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SignerTool.initialize(WeakReference(this))

        setContent {
            FidoAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Authentication")
                        Button(onClick = {
                            val json =
                                fetchRegistrationJsonFromServer(this@MainActivity, "RegFromServer")

                            lifecycleScope.launch {
                                val result = signertoolOperation(json = json, registration)
                                println(result.getOrNull())
                            }
                        }) {
                            Text(text = "Register")
                        }

                        Button(onClick = {
                            val json =
                                fetchRegistrationJsonFromServer(this@MainActivity, "AuthFromServer")
                            lifecycleScope.launch {
                                val result = signertoolOperation(json = json, sign)
                                println(result.getOrNull())
                            }
                        }) {
                            Text(text = "Sign")
                        }
                        Text(text = "Recovery")
                        Button(onClick = {
                            val signedResult = SignerTool.getInstance()
                                .createRecoveryKey("challenge", "temporaryAuthenticationToken")

                            println("${signedResult.first}")
                            println("Private key pem : ${signedResult.second}")

                        }) {
                            Text(text = "Create recovery key")
                        }

                        Button(onClick = {
                            /*
                            * 1- Get your challenge from server
                            * 2- Register the Credentials using SignerTool registration
                            * 3- get FirstFactorCredential from registered credential and replace the JsonObject()
                            * */
                            val firstFactorCredential = JsonObject().apply {
                                add("firstFactorCredential", JsonObject())
                            }
                            try {
                                val signedResult =
                                    SignerTool.getInstance().recoverKeyFromPrivateKey(
                                        firstFactorCredential,
                                        "temporaryAuthenticationToken",
                                        "pemPrivateKey",
                                        "credId"
                                    )
                                println(signedResult)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }) {
                            Text(text = "Recover credentials")
                        }
                    }


                }
            }
        }
    }

    private suspend fun signertoolOperation(
        json: String,
        signerToolOperation: SignerToolOperation
    ): Result<JsonObject> {
        return withContext(Dispatchers.Main) {
            SignerTool.getInstance().performOperation(Dispatchers.Main, signerToolOperation, json)
        }
    }
}

private fun fetchRegistrationJsonFromServer(context: MainActivity, fileName: String): String {

    return context.assets.open(fileName).bufferedReader().use { it.readText() }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FidoAndroidTheme {
        Greeting("Android")
    }
}