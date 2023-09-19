package org.osprey.trunkfriends.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.osprey.trunkfriends.historyhandler.refresh

@Composable
@Preview
fun App(stopWatch : StopWatch) {

    Column {
        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { stopWatch.view = "Friends" }
            ) {
                Text("About")
            }
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { stopWatch.view = "History" }
            ) {
                Text("History Overview")
            }
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    stopWatch.start("History")
                    stopWatch.view = "Refresh"
                }
            ) {
                Text("Refresh against server")
            }

        }
        if (stopWatch.view == "History") HistoryListing(stopWatch.name, onNameChange = { stopWatch.name = it })
        if (stopWatch.view == "Refresh") {
            RefreshView(stopWatch)
        }
    }
}

fun main() = application {
    val stopWatch = remember { StopWatch() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Trunk Friends"
    ) {
        App(stopWatch)
    }
}

class StopWatch {
    var name by mutableStateOf("")
    var view by mutableStateOf("History")
    var formattedTime by mutableStateOf("\n\nRefreshing followers / following list, please wait\n\nStarting fetch\n")
    var activeButtons by mutableStateOf(true)

    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isActive = false

    fun start(returnView : String) {
        if (isActive) return
        coroutineScope.launch {
            activeButtons = false
            refresh {
                formattedTime += it+"\n"
            }
            activeButtons = true
            view = returnView
            formattedTime = "Starting fetch\n"
        }
    }

}