package org.osprey.trunkfriends.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.ui.*


@Composable
fun pasteView(state: UIState) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
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
                text = "Below is the accounts selected (max 15 displayed)\n",
                fontSize = TextUnit(20f, TextUnitType.Sp)
            )
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = state.getSelected(15)+"\n",
                fontSize = TextUnit(18f, TextUnitType.Sp)
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
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            CommonButton(text = "Copy accounts to clipboard") {
                clipboardManager.setText(
                    AnnotatedString(state.getSelected())
                )
            }
            CommonButton(text = "Clear selections") {
                state.clearSelect()
            }

        }
    }

}