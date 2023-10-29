package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp


// #E0D3DE
// #D8D0C1
// #CBB8A9
// #B3B492
// #6F686D

@Composable
fun CommonButton(enabled: Boolean = true, text: String, onClick: () -> Unit) =
    Button(
        enabled = enabled,
        modifier = Modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
        onClick = onClick
    ) {
        Text(text)
    }

@Composable
fun CommonIconButton(
    enabled: Boolean = true,
    text: String,
    icon: ImageVector,
    iconBefore: Boolean = true,
    onClick: () -> Unit
) =
    Button(
        enabled = enabled,
        modifier = Modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
        onClick = onClick
    ) {
        if (iconBefore) Icon(imageVector = icon, contentDescription = text)
        Text(text)
        if (!iconBefore) Icon(imageVector = icon, contentDescription = text)
    }

@Composable
fun BannerRow(
    text: String,
    size: Float = 20f
) = Row(
    modifier = Modifier
        .border(width = 2.dp, color = colorTwo)
        .background(colorOne).fillMaxWidth()
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = text,
            fontSize = TextUnit(size, TextUnitType.Sp)
        )
    }
}


val palette1 = listOf(
    Color(0xE0,0xD3,0xDE),
    Color(0xD8,0xD0,0xC1),
    Color(0xCB,0xB8,0xA9),
    Color(0xB3,0xB4,0x92),
    Color(0x6F,0x68,0x6D)
)

val palette2 = "EFBC9B,EE92C2,9D6A89,725D68,A8B4A5".split(",")

val colorTwo = Color(0xE0, 0xD3, 0xDE, 0xFF)
val colorOne = Color(0xD8, 0xD0, 0xC1, 0xFF)
val colorBackground = Color(0xCB,0xB8,0xA9,0xFF)