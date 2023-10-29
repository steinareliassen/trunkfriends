package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun refreshView(state: UIState) {
    Text("\n")

    if (!state.activeButtons) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.historyDropdownState = true }
            ) {
                Text(state.feedback)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                enabled = state.activeButtons,
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.start() }
            ) {
                Text("Start importing following / followers list")
            }
        }
    }


}