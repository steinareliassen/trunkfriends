package org.osprey.trunkfriends.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.*

@Composable
@Preview
fun App() {
    var name by remember { mutableStateOf("") }

    MaterialTheme {
        HistoryListing(name, onNameChange = { name = it })
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Trunk Friends"
    ) {
        App()
    }
}
