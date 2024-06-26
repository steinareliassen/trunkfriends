package org.osprey.trunkfriends.ui.authenticate

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.api.mastodon.MastodonAuthApi
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.ui.BannerRow
import org.osprey.trunkfriends.ui.CommonButton
import org.osprey.trunkfriends.ui.AppState
import org.osprey.trunkfriends.ui.View
import org.osprey.trunkfriends.util.mapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun authenticateView(state: AuthState, uiState: AppState) {

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val api = MastodonAuthApi()

    fun registerClient() {
        runCatching {
            val client = api.registerClient(state.domain)
            state.clientId = client.clientId
            state.clientSecret = client.clientSecret
            state.url = "https://${state.domain}/oauth/authorize?client_id=${client.clientId}" +
                    "&scope=read%20write%20follow" +
                    "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                    "&response_type=code"
            state.activeStep = "step2"
        }.onFailure {
            state.activeStep = "failed"
        }
    }

    Column {

        Text("\n")

        // Did we come here to get a new token for an already registered instance?
        if (state.skipDomain) {
            state.skipDomain = false
            registerClient()
        }

        if (state.activeStep == "failed") {
            BannerRow(
                """
Something went wrong, maybe you entered a non-existing domain name?
                """.trimIndent(),
                16f
            )

        }
        if (state.activeStep == "") {
            Text("\n")
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    singleLine = true,
                    modifier = Modifier.width(500.dp),
                    enabled = true,
                    value = state.domain,
                    onValueChange = { state.domain = it },
                    label = { Text("The domain you want to register") }
                )
                CommonButton(
                    text = "Activate",
                    enabled = state.domain.length > 3) {
                    registerClient()
                }

            }
        }

        if (state.activeStep == "step2" || state.activeStep == "step3") {
            BannerRow(
                if (uiState.view == View.NEW_TOKEN) {
                    """
Note: When you obtain a new token, the old one might still be register on the instance you use.
You can go to Preferences -> Account -> Authorized apps and revoke the oldest Trunkfriends
authorization you find there. 

Copy the URL below by pressing "Copy URL" and paste it into a browser that is logged in 
to your mastodon instance you wish to get a new token for and press enter to go to that page. 
If you are not logged in, you will be asked to do so. Do not attempt to copy the URL by 
selecting the text, only press the "Copy URL" button".
                        
                    """.trimIndent()
                } else
                """
Now that this is done, you need to verify the activation with your mastodon instance.
To do that, copy the URL below by pressing "Copy URL" and paste it into a browser that is 
logged in to your mastodon instance and press enter to go to that page. If you are not 
logged in, you will be asked to do so. Do not attempt to copy the URL by selecting the 
text, only press the "Copy URL" button".
                        """.trimIndent(),
                16f
            )
            Text("\n")
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    modifier = Modifier.width(500.dp),
                    value = state.url.substring(0..if (state.url.length < 50) state.url.length - 1 else 50),
                    onValueChange = {},
                    label = { Text("Copy this URL and paste it in a browser logged in with your account") }
                )

                CommonButton(text = "Copy URL") {
                    clipboardManager.setText(AnnotatedString(state.url))
                    state.activeStep = "step3"
                }
            }

        }

        if (state.activeStep == "step3") {
            BannerRow(
                """
Once you authorized access, you will get a code back, with the option to copy this code
into the clipboard. Do this, and press the "PASTE" button to paste the code in.
                        """.trimIndent(),
                16f
            )
            Text("\n")
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    modifier = Modifier.width(500.dp),
                    value = state.code,
                    onValueChange = {},
                    label = { Text("Paste the code you got from your server in here and press activate") }
                )
                CommonButton(text = "Paste") {
                    state.code = clipboardManager.getText()?.text ?: "empty clipboard"
                    state.activeStep = "step4"
                }

            }

        }

        if (state.activeStep == "step4") {
            BannerRow(
                """
The final step now is simply to press "Register". The connection will be verified,
we will fetch your username and you can begin using the application to track your followers"
on this account.
                    """.trimIndent(),
                16f
            )
            Text("\n")
            Row(modifier = Modifier.fillMaxWidth()) {

                CommonButton(text = "Register") {
                    state.token = api.obtainToken(
                        state.domain,
                        state.clientId,
                        state.clientSecret,
                        state.code
                    )

                    val userClass = api.getUserInformation(state.token, state.domain)

                    val config = Config(
                        bearer = "Bearer ${state.token}",
                        server = state.domain
                    )

                    val serverPath = Paths.get(FileUtils.getUserDirectoryPath() + "/.trunkfriends/${state.domain}")
                    if (!Files.exists(serverPath)) {
                        Files.createDirectory(serverPath)
                    }

                    val configKey = "${state.domain}/${userClass.acct}"
                    val configPath = FileUtils.getUserDirectoryPath() +
                            "/.trunkfriends/$configKey"
                    val userPath = Paths.get(configPath)

                    if (!Files.exists(userPath)) {
                        Files.createDirectory(userPath)
                    }

                    File("$configPath/config.json").printWriter().use { pw ->
                        pw.println(mapper.writeValueAsString(config))
                    }

                    // If a config with the same key exists, remove it (happens when obtaining
                    // new token). This config would have an outdated token.
                    uiState.configMap.removeIf { it.first == configKey }

                    // Add the new config
                    uiState.configMap.add(
                        (configKey to config)
                    )
                    uiState.selectedConfig = (configKey to config)
                    uiState.view = View.HISTORY
                }

            }

        }

        Row(modifier = Modifier.fillMaxWidth()) {
            CommonButton(text = "Cancel server registration") {
                uiState.view = if (uiState.selectedConfig!= null)
                    View.HISTORY
                else
                    View.ABOUT
            }
        }
    }

}