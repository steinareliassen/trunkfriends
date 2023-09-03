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

@Composable
@Preview
fun App() {
    var name by remember { mutableStateOf("") }
    var view by remember { mutableStateOf("History") }

    Column {
        Row(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
            Button(
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { view = "Friends" }
            ) {
                Text("Friend Overview")
            }
            Button(
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { view = "History" }
            ) {
                Text("History Overview")
            }
            Button(
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
        if (view == "Refresh") RefreshView()
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
