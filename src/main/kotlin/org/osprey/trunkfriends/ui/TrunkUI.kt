package org.osprey.trunkfriends.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.apache.commons.io.FileUtils
import org.osprey.trunkfriends.config.Config
import org.osprey.trunkfriends.ui.authenticate.AuthState
import org.osprey.trunkfriends.ui.authenticate.authenticateView
import org.osprey.trunkfriends.ui.history.historyListing
import org.osprey.trunkfriends.ui.history.pasteView
import org.osprey.trunkfriends.util.mapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Composable
@Preview
fun App(state: UIState) {

    // Todo: startpoint for scalable UI
    /*val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp*/
    Column(Modifier.background(colorBackground).fillMaxHeight()) {

        if (state.view == View.ADD_SERVER) {
            // Authenticate view, without header
            authenticateView(remember { AuthState() }, state)
        }
        else if (state.zoomedName != null) {
            // Zoomed history view, with clear search button
            Row(modifier = Modifier.fillMaxWidth()) {
                CommonButton(
                    text = "Clear search"
                ) {
                    state.zoomedName = null
                }
            }
        } else {
            // Regular header, with button row
            ButtonRowHeader(state)
        }

        if (state.view == View.HISTORY) {
            state.historyViewState.page = 0
            state.historyViewState.time = 0
            historyListing(
                state.historyViewState,
                state.selectedConfig?.first ?: "No Server",
                state.zoomedName,
                state.changeZoom
            )
        }
        if (state.view == View.LIST) {
            state.historyViewState.page = 0
            state.historyViewState.time = 0
            overviewListing(
                state.historyViewState,
                state.selectedConfig?.first ?: "No Server",
                state.changeZoom
            )
        }
        if (state.view == View.MANAGE) {
            addRemoveView(state)
        }
        if (state.view == View.ABOUT) aboutView()
        if (state.view == View.REFRESH ||
            state.view == View.EXECUTE_MANAGEMENT) {
            refreshView(state)
        }
        if (state.view == View.PASTE_BAG) {
            pasteView(state)
        }
    }
}

@Composable
fun ButtonRowHeader(state : UIState) {
    Row(modifier = Modifier.fillMaxWidth()) {

        Button(
            enabled = state.activeButtons,
            modifier = Modifier.padding(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
            onClick = { state.menuDrownDownState = true }
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
            Text("Menu")
        }

        val dropDownItems = listOf(
            Triple("About", View.ABOUT, false),
            Triple("List Overview", View.LIST, true),
            Triple("History Overview", View.HISTORY, true),
            Triple("Refresh followers", View.REFRESH, true),
            Triple("Manage followers", View.MANAGE, true),

            )
        DropdownMenu(
            expanded = state.menuDrownDownState,
            onDismissRequest = { state.menuDrownDownState = false }
        ) {
            dropDownItems.forEach {
                if (((it.third && state.selectedConfig != null) || !it.third)) {
                    CommonDropDownItem(text = it.first) {
                        state.menuDrownDownState = false
                        state.view = it.second
                    }
                }
            }
        }

        Button(
            enabled = state.activeButtons,
            modifier = Modifier.padding(4.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
            onClick = { state.selectServerDropDownState = true }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Servers"
            )
            Text("select server")
        }

        DropdownMenu(
            expanded = state.selectServerDropDownState,
            onDismissRequest = { state.selectServerDropDownState = false }
        ) {
            state.configMap.forEach { configPair ->
                DropdownMenuItem(
                    onClick = {
                        state.onServerSelect(View.HISTORY, configPair)
                    }
                ) {
                    Text(configPair.first)
                }
            }
            DropdownMenuItem(
                onClick = {
                    state.onServerSelect(View.ADD_SERVER, null)
                }
            ) {
                Text("Add new server")
            }
        }

        if (state.selectedConfig != null) {
            CommonButton(enabled = state.activeButtons, text = "(${state.historyViewState.pasteBag.size}) Bag") {
                state.view = View.PASTE_BAG
            }
        }

    }

    BannerRow(
        "Selected server: " +
                (state.selectedConfig?.first ?: "select server from dropdown")
    )

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

    val icon = painterResource("icon.png")
    Window(
        icon = icon,
        onCloseRequest = ::exitApplication,
        title = "Trunk Friends"
    ) {
        App(remember { UIState(configMap) })
    }
}
