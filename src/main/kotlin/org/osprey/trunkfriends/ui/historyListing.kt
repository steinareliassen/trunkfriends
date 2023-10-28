package org.osprey.trunkfriends.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun historyListing(
    serverUser: String,
    name: String,
    time: Long,
    state: UIState,
    onNameChange: (String) -> Unit,
    onTimeChange: (Long) -> Unit
) {
    fun timestampToDateString(timestamp: Long) =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("CET"))
            .format(Instant.ofEpochSecond(timestamp / 1000)).run {
                this.substring(0..this.length - 4).replace("T", " ")
            }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        val history = HistoryHandler().readHistory(serverUser)
        Row(modifier = Modifier.fillMaxWidth()) {
            if (history.isNotEmpty() && state.name.isEmpty()) {
                CommonIconButton(text = "select timeslot", icon = Icons.Default.MoreVert) {
                    state.historyDropdownState = true
                }
                CommonIconButton(text = "Previous", icon = Icons.Default.ArrowBack) {
                }
                CommonIconButton(text = "Next", icon = Icons.Default.ArrowForward, iconBefore = false) {
                }
            }

        }
        DropdownMenu(
            expanded = state.historyDropdownState,
            onDismissRequest = { state.historyDropdownState = false }
        ) {

            history.map { (_, control) ->
                control.substring(0, control.length - 3).toLong()
            }.distinct().forEach {
                DropdownMenuItem(
                    onClick = {
                        onTimeChange(it)
                        state.historyDropdownState = false
                    }
                ) {
                    Text(timestampToDateString(it))
                }
            }
        }

        HistoryHandler().createHistoryCards(history).forEach {
            if ((name == "" && time == it.timeStamp) || name == it.acct) Card(
                elevation = Dp(4F),
                modifier = Modifier
                    .width(740.dp)
                    .wrapContentHeight()
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(modifier = Modifier.background(Color(0xB3, 0xB4, 0x92, 0xFF))) {
                    Row(modifier = Modifier.align(Alignment.Start)) {
                        followCard(it.prevFollower, it.follower, it.prevFollowing, it.following)
                        infoCard(timestampToDateString(it.timeStamp), it.acct, it.username)
                        zoomButton(text = "\uD83D\uDD0D") {
                            if (name == "") onNameChange(it.acct) else onNameChange("")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun infoCard(date: String, account: String, username: String) {
    Card(
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.LightGray
        ),
        modifier = Modifier.padding(Dp(4F)).width(570.dp)
    ) {
        Column {
            Text(text = "⏰ $date - $username")
            Text(text = account)
        }
    }

}

@Composable
fun zoomButton(text: String, onClick: () -> Unit) {
    TextButton(
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black
            ),
        modifier = Modifier.padding(0.dp),
        onClick = { onClick() }
    ) {
        Text(text)
    }
}

@Composable
fun followCard(prevFollower: Boolean, follower: Boolean, prevFollowing: Boolean, following: Boolean) {
    Card(
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xB3, 0xB4, 0x92, 0xFF)
        ),
        modifier = Modifier.padding(Dp(4F)).width(100.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            Row {
                Text("\uD83E\uDEF5")
                if (prevFollower != follower) {
                    if (follower) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text(
                        "\uD83D\uDFE2 ➡",
                        color = Color.Blue
                    )
                }
                if (follower) Text("\uD83D\uDFE2", color = Color.Blue) else Text("\uD83D\uDD34", color = Color.Red)
            }
            Row {
                Text("\uD83D\uDC49")
                if (prevFollowing != following) {
                    if (following) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text(
                        "\uD83D\uDFE2 ➡",
                        color = Color.Blue
                    )
                }
                if (following) Text("\uD83D\uDFE2", color = Color.Blue) else Text("\uD83D\uDD34", color = Color.Red)
            }
        }
    }

}