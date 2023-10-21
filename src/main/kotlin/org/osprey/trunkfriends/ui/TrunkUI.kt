package org.osprey.trunkfriends.ui

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
import androidx.compose.ui.unit.dp
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths

@Composable
@Preview
fun App(state : UIState) {
    Column {
        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            Button(
                enabled = state.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.view = "About" }
            ) {
                Text("About")
            }
            Button(
                enabled = state.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.view = "History" }
            ) {
                Text("History Overview")
            }
            Button(
                enabled = state.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    state.start()
                    state.view = "Refresh"
                }
            ) {
                Text("Refresh against server")
            }

        }
        if (state.view == "History")
            historyListing(
                state.name,
                state.time,
                onNameChange = { state.name = it },
                onTimeChange = { state.time = it}
            )
        if (state.view == "About") aboutView()
        if (state.view == "Refresh") {
            refreshView(state)
        }
    }
}

fun main() = application {

    val path = Paths.get(FileUtils.getUserDirectoryPath()+"/.trunkfriends")
    if (!Files.exists(path)) {
        Files.createDirectory(path)
    }

    if (Files.isDirectory(path)) throw IllegalStateException("Config folder is not a folder")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Trunk Friends"
    ) {
        App(remember { UIState() })
    }
}
