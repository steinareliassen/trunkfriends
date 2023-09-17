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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
@Preview
fun App(stopWatch : StopWatch) {
    var name by remember { mutableStateOf("") }
    var view by remember { mutableStateOf("History") }

    Column {
        Text(stopWatch.formattedTime)
        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { view = "Friends" }
            ) {
                Text("Friend Overview")
            }
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { view = "History" }
            ) {
                Text("History Overview")
            }
            Button(
                enabled = stopWatch.activeButtons,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    view = "Refresh"
                }
            ) {
                Text("Refresh against server")
            }

        }
        if (view == "History") HistoryListing(name, onNameChange = { name = it })
        if (view == "Refresh") {
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

    var formattedTime by mutableStateOf("00:00:00")
    var activeButtons by mutableStateOf(true)

    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var isActive = false

    private var timeMillis = 0L
    private var lastTimestamp = 0L

    fun start() {
        if (isActive) return
        coroutineScope.launch {
            activeButtons = false
            lastTimestamp = System.currentTimeMillis()
            this@StopWatch.isActive = true
            while (this@StopWatch.isActive) {
                delay(10L)
                timeMillis += System.currentTimeMillis() - lastTimestamp
                lastTimestamp = System.currentTimeMillis()
                formattedTime = formatTime(timeMillis)
            }
        }
    }

    fun pause() {
        isActive = false
    }

    fun reset() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        timeMillis = 0L
        lastTimestamp = 0L
        formattedTime = "00:00:00"
        isActive = false
    }

    private fun formatTime(timeMillis: Long): String {
        val localDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timeMillis),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern(
            "mm:ss:SS",
            Locale.getDefault()
        )
        return localDateTime.format(formatter)
    }
}