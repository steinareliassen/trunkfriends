package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp


@Composable
fun addRemoveView(state: UIState) {
    val text = rememberSaveable { mutableStateOf("") }
    Text("\n")

    Row(
        modifier = Modifier
            .border(width = 2.dp, color = colorTwo)
            .background(colorOne).fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "Enter accounts below, one per line, separated only by newline (no comma, etc)\n",
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

            TextField(
                value = text.value,
                onValueChange = { text.value = it }, modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(10.dp)
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            )

        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(colorTwo)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            CommonButton(
                enabled = state.historyViewState.pasteBag.isNotEmpty(),
                text = "Insert from bag"
            ) {
                text.value = state.historyViewState.pasteBag.reduce { acc, s -> "$acc\n$s" }
            }
            val managementCommand = { context : ManagementAction ->
                state.view = View.EXECUTE_MANAGEMENT
                state.context = context
                state.actionList = text.value.split("\n")
            }

            CommonButton(
                enabled = text.value.isNotBlank(),
                text = "Unfollow selected"
            ) {
                managementCommand(ManagementAction.UNFOLLOW)
            }
            CommonButton(
                enabled = text.value.isNotBlank(),
                text = "Follow selected"
            ) {
                managementCommand(ManagementAction.FOLLOW)
            }
            CommonButton(
                enabled = text.value.isNotBlank(),
                text = "Add to list"
            ) {
                managementCommand(ManagementAction.ADD_TO_LIST)
            }

        }
    }

}