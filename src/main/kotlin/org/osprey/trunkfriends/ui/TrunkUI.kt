package org.osprey.trunkfriends.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.ui.authenticate.AuthState
import org.osprey.trunkfriends.ui.authenticate.authenticateView
import org.osprey.trunkfriends.ui.history.historyListing
import org.osprey.trunkfriends.ui.history.pasteView
import org.osprey.trunkfriends.ui.refresh.refreshView
import org.osprey.trunkfriends.util.mapper
import java.awt.Dimension
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Composable
@Preview
fun App(state: AppState) {

    Column(Modifier.background(colorBackground).fillMaxHeight()) {

        ButtonRowHeader(state)

        when (state.view) {
            View.HISTORY -> historyListing(
                state.historyViewState,
                state,
                state.zoomedName,
                state.windowState.size.height.value.toInt()
            )
            View.LIST -> overviewListing(
                state.historyViewState,
                state,
                state.windowState.size.height.value.toInt()
            )
            View.MANAGE -> managementView(state)
            View.NOTES -> noteView()
            View.ABOUT -> aboutView()
            View.PASTE_BAG -> pasteView(state.pasteBag)
            View.REFRESH -> refreshView(state)
            View.EXECUTE_MANAGEMENT -> refreshView(state, state.managementAction)
            View.ADD_SERVER, View.NEW_TOKEN -> authenticateView(
                remember {
                    AuthState(
                        if (state.view == View.NEW_TOKEN)
                            state.selectedConfig?.second?.server
                        else
                            null
                    )
                },
                state
            )
        }

    }
}

@Composable
fun ButtonRowHeader(state: AppState) {

    var menuDrownDownState by remember { mutableStateOf(false) }
    var selectServerDropDownState by remember { mutableStateOf(false) }

    // Zoomed header
    if (state.zoomedName != null) {
        Row(modifier = Modifier.fillMaxWidth()) {
            CommonButton(
                text = "Clear search"
            ) {
                state.changeZoom(null, state.returnView)
            }
        }
        return
    }

    if(state.view.header) {
        Row(modifier = Modifier.fillMaxWidth()) {

            Button(
                enabled = !state.networkTaskActive,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { menuDrownDownState = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
                Text("Menu")
            }

            DropdownMenu(
                expanded = menuDrownDownState,
                onDismissRequest = { menuDrownDownState = false }
            ) {
                listOf(
                    View.ABOUT, View.LIST, View.HISTORY,
                    View.REFRESH, View.MANAGE, View.NOTES, View.NEW_TOKEN
                ).forEach { view ->
                    if (view == View.ABOUT || state.selectedConfig != null)  {
                        CommonDropDownItem(text = view.title) {
                            menuDrownDownState = false
                            state.changeView(view)
                        }
                    }
                }
            }

            Button(
                enabled = !state.networkTaskActive,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { selectServerDropDownState = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Servers"
                )
                Text("Select server")
            }

            DropdownMenu(
                expanded = selectServerDropDownState,
                onDismissRequest = { selectServerDropDownState = false }
            ) {
                state.configMap.forEach { configPair ->
                    DropdownMenuItem(
                        onClick = {
                            state.onServerSelect(View.HISTORY, configPair)
                            selectServerDropDownState = false
                        }
                    ) {
                        Text(configPair.first)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        state.onServerSelect(View.ADD_SERVER, null)
                        selectServerDropDownState = false
                    }
                ) {
                    Text("Add new server")
                }
            }

            if (state.selectedConfig != null) {
                CommonButton(enabled = !state.networkTaskActive, text = "(${state.pasteBag.getSize()}) Bag") {
                    state.view = View.PASTE_BAG
                }
            }

        }

        BannerRow(
            "Selected server: " +
                    (state.selectedConfig?.first ?: "select server from dropdown")
        )
    }

}

fun main() = application {

    val rootPath = FileUtils.getUserDirectoryPath() + "/.trunkfriends"
    val path = Paths.get(rootPath)
    if (!Files.exists(path)) {
        Files.createDirectory(path)
    }

    if (!Files.isDirectory(path)) throw IllegalStateException("Config folder is not a folder")

    val configMap = File(rootPath).listFiles()!!.map { server ->
        File(rootPath + "/" + server.name).listFiles()!!.map { user ->
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
    } as MutableList<Pair<String, Config>>

    val state = rememberWindowState(width = 800.dp, height = 750.dp)
    val appState = AppState(configMap,state)
    val icon = painterResource("icon.png")
    Window(
        icon = icon,
        onCloseRequest = ::exitApplication,
        onKeyEvent =  {
            keyevent ->
            println(keyevent.key)
            if(keyevent.key == Key.T) appState.view = View.ADD_SERVER
            true
        } ,
        title = "Trunk Friends",
        state = state,
    ) {
        window.maximumSize = Dimension(850, 2000)
        window.minimumSize = Dimension(800, 600)
        App(remember { appState })
    }
}

