package org.osprey.trunkfriends.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import org.osprey.trunkfriends.ui.dto.HistoryCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun historyListing(
    piper: String,
    name: String,
    time: Long,
    onNameChange: (String) -> Unit,
    onTimeChange: (Long) -> Unit
) {
    fun timestampToDateString(timestamp: Long) =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("CET"))
            .format(Instant.ofEpochSecond(timestamp / 1000)).run {
                this.substring(0..this.length-4).replace("T"," ")
            }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(Color.Gray)
            .verticalScroll(rememberScrollState())
    ) {
        Row {
            Text("pip $piper")
        }
        val previousUserMap = mutableMapOf<String, CurrentUser>()
        val history = HistoryHandler().readHistory(piper)
        history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.distinct().chunked(5).forEach {

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                it.forEach {
                    Column(modifier = Modifier.padding(2.dp)) {
                        dateButton(text = timestampToDateString(it)) {
                            onTimeChange(it)
                        }
                    }
                }
            }

        }

        history.map { user ->
            with(user) {
                previousUserMap[first.acct]?.let {
                    HistoryCard(
                        follower = first.follower,
                        prevFollower = it.follower,
                        following = first.following,
                        prevFollowing = it.following,
                        acct = first.acct,
                        username = first.username,
                        timeStamp = second.substring(0, second.length - 3).toLong()
                    ).also {
                        previousUserMap[first.acct] = first
                    }
                } ?: HistoryCard(
                    follower = first.follower,
                    prevFollower = first.follower,
                    following = first.following,
                    prevFollowing = first.following,
                    acct = first.acct,
                    username = first.username,
                    timeStamp = second.substring(0, second.length - 3).toLong()
                ).also {
                    previousUserMap[first.acct] = first
                }
            }
        }.forEach {
            if ((name == "" || name == it.acct) && time == it.timeStamp) Card(
                elevation = Dp(4F),
                modifier = Modifier
                    .width(740.dp)
                    .wrapContentHeight()
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column {
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
fun dateButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        colors = ButtonDefaults
            .buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black
            ),
        modifier = Modifier.padding(0.dp).height(25.dp).width(130.dp),
        onClick = { onClick() }
    ) {
        Text(
            text= text,
            fontSize = TextUnit(9f, TextUnitType.Sp)
        )
    }
}

@Composable
fun followCard(prevFollower: Boolean, follower: Boolean, prevFollowing: Boolean, following: Boolean) {
    Card(
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.LightGray
        ),
        modifier = Modifier.padding(Dp(4F)).width(100.dp)
    ) {
        Column {
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