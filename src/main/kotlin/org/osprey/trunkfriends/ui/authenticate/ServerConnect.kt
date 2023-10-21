package org.osprey.trunkfriends.ui.authenticate

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.api.mastodon.MastodonApi
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.util.mapper
import java.io.File

@Composable
@Preview
fun ServerConnect(state: AuthState) {
    val file = File("config.json")

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val api = MastodonApi(
        if (file.exists()) {
            file.readLines().first().let {
                mapper.readValue(
                    it, Config::class.java
                )
            }
        } else
            throw Exception("Config file not found")
    )
    Column {
        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            TextField(
                enabled = true,
                value = state.domain,
                onValueChange = {state.domain = it},
                label = { Text("The domain you want to register") }
            )
            Button(
                enabled = true,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    val client = api.registerClient(state.domain)
                    state.clientId = client.clientId
                    state.clientSecret = client.clientSecret
                    state.url = "https://${state.domain}/oauth/authorize?client_id=${client.clientId}" +
                            "&scope=read" +
                            "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                            "&response_type=code"

                }
            ) {
                Text("Activate")
            }

        }

        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            TextField(
                value = state.url.substring(0 ..  if (state.url.length < 20) state.url.length-1 else 20),
                onValueChange = {},
                label = { Text("Copy this URL and paste it in a browser logged in with your account") }
            )

            Button(
                enabled = true,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    clipboardManager.setText(AnnotatedString(state.url))
                }
            ) {
                Text("Copy URL")
            }

        }

        Column {

            Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
                TextField(
                    value = state.code,
                    onValueChange = {},
                    label = { Text("Paste the code you got from your server in here and press activate") }
                )

                Button(
                    enabled = true,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                    onClick = {
                        state.code = clipboardManager.getText()?.text ?: "empty clipbord"
                    }
                ) {
                    Text("Paste")
                }

            }
        }

        Column {
            Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
                Text(
                    text = "Once the code you got from the server is pasted into the field over, press register"
                )

                Button(
                    enabled = true,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                    onClick = {
                        state.token = api.obtainToken(
                            state.domain,
                            state.clientId,
                            state.clientSecret,
                            state.code
                        )

                    }
                ) {
                    Text("Register")
                }

            }
        }

    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Server Connection"
    ) {
        ServerConnect(remember { AuthState() })
    }
}
