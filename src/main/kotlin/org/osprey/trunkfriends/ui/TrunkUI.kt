package org.osprey.trunkfriends.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.ui.authenticate.AuthState
import org.osprey.trunkfriends.ui.authenticate.authenticateView
import org.osprey.trunkfriends.util.mapper
import java.io.File
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

            Button(onClick = {
                state.dropDownState = true
            }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Servers"
                )
                Text("select server")
            }

            DropdownMenu(
                expanded = state.dropDownState,
                onDismissRequest = { state.dropDownState = false }
            ) {
                state.configMap.forEach { configPair ->
                    DropdownMenuItem(
                        onClick = {
                            state.dropDownState = false
                            state.selectedConfig = configPair
                        }
                    ) {
                        Text(configPair.first)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        state.view = "Add server"
                        state.dropDownState = false
                    }
                ) {
                    Text("Add new server")
                }
            }

        }
        Row(
            modifier = Modifier
                .border(width = 2.dp, color = Color.Magenta)
                .background(Color.White).fillMaxWidth()) {
            Text("Selected server: " +
                    (state.selectedConfig?.first ?: "select server from dropdown"),
                fontSize = TextUnit(20f, TextUnitType.Sp)
            )
        }
        if (state.view == "History")
            historyListing(
                state.selectedConfig?.first ?: "No Server",
                state.name,
                state.time,
                onNameChange = { state.name = it },
                onTimeChange = { state.time = it}
            )
        if (state.view == "About") aboutView()
        if (state.view == "Refresh") {
            refreshView(state)
        }
        if (state.view == "Add server") authenticateView(remember { AuthState() })
    }
}

fun main() = application {

    val rootPath = FileUtils.getUserDirectoryPath()+"/.trunkfriends"
    val path = Paths.get(rootPath)
    if (!Files.exists(path)) {
        Files.createDirectory(path)
    }

    if (!Files.isDirectory(path)) throw IllegalStateException("Config folder is not a folder")

    val configMap = File(rootPath).listFiles()!!.map { server ->
        File(rootPath+"/"+server.name).listFiles()!!.map { user ->
            (server.name to user.name)
        }
    }.flatten().mapNotNull {
        File("$rootPath/${it.first}/${it.second}/config.json").let { file ->
            if (file.exists()) {
                (
                        "${it.first}/${it.second}"
                        to
                        file.readLines().first().let {
                            mapper.readValue(
                                it, Config::class.java
                            )
                        }
                )
            } else null
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Trunk Friends"
    ) {
        App(remember { UIState(configMap) })
    }
}
