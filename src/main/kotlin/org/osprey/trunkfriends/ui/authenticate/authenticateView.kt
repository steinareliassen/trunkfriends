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
import org.osprey.trunkfriends.ui.UIState
import org.osprey.trunkfriends.util.mapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Composable
fun authenticateView(state: AuthState, uiState: UIState) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val api = MastodonAuthApi()

    Column {

        Text("\n")

        if (state.activeStep == "") {
            BannerRow(
                """
First thing, you need to enter the domain name of the server you want to connect to.
If your username is @user@mastodon.social, the domain name is mastodon.social
Press "ACTIVATE" after you enter the domain name.
                """.trimIndent(),
                16f
            )
            Text("\n")
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    modifier = Modifier.width(500.dp),
                    enabled = true,
                    value = state.domain,
                    onValueChange = { state.domain = it },
                    label = { Text("The domain you want to register") }
                )
                CommonButton(text = "Activate") {
                    val client = api.registerClient(state.domain)
                    state.clientId = client.clientId
                    state.clientSecret = client.clientSecret
                    state.url = "https://${state.domain}/oauth/authorize?client_id=${client.clientId}" +
                            "&scope=read" +
                            "&redirect_uri=urn:ietf:wg:oauth:2.0:oob" +
                            "&response_type=code"
                    state.activeStep = "step2"

                }

            }
        }

        if (state.activeStep == "step2" || state.activeStep == "step3") {
            BannerRow(
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
                    value = state.url.substring(0..if (state.url.length < 20) state.url.length - 1 else 20),
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

                    val configPath = FileUtils.getUserDirectoryPath() +
                            "/.trunkfriends/${state.domain}/${userClass.acct}";
                    val userPath = Paths.get(configPath)

                    if (!Files.exists(userPath)) {
                        Files.createDirectory(userPath)
                    }

                    File("$configPath/config.json").printWriter().use { pw ->
                        pw.println(mapper.writeValueAsString(config))
                    }

                }

            }

        }

        Row(modifier = Modifier.fillMaxWidth()) {
            CommonButton(text = "Cancel server registration") {
                uiState.view = "History"
            }
        }
    }

}