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

    val history = HistoryHandler().readHistory(serverUser)

    if (history.isNotEmpty() && state.time == 0L) {
        state.time = history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.max()
        state.timeslotPage = history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.distinct().size-1
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        if (history.isEmpty()) {
            Text("\n")
            BannerRow("""
You do not seem to have imported the followers / following list from the mastodon 
instance. Click on "Refresh followers" and start importing. If you have a large
amount of followers, this can take some time, as we do not want to swamp the
server with requests. Once followers are imported, you will be see them here.
            """.trimIndent())
            return
        }
        val timeslots = history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.distinct()

        Row(modifier = Modifier.fillMaxWidth()) {
            if (history.isNotEmpty() && state.name.isEmpty()) {
                CommonIconButton(text = "select timeslot", icon = Icons.Default.MoreVert) {
                    state.historyDropdownState = true
                }
                if (state.timeslotPage > 0)
                    CommonIconButton(text = "Previous timeslot", icon = Icons.Default.ArrowBack) {
                        state.timeslotPage--
                        state.time = timeslots[state.timeslotPage]
                        state.page = 0
                    }
                CommonButton(enabled = false, text = "${state.timeslotPage+1}/${timeslots.size}") {}
                if (state.timeslotPage < timeslots.size-1)
                    CommonIconButton(text = "Next timeslot", icon = Icons.Default.ArrowForward, iconBefore = false) {
                        state.timeslotPage++
                        state.time = timeslots[state.timeslotPage]
                        state.page = 0
                    }
            }
        }

        DropdownMenu(
            expanded = state.historyDropdownState,
            onDismissRequest = { state.historyDropdownState = false }
        ) {

            timeslots.forEachIndexed { i, time ->
                DropdownMenuItem(
                    onClick = {
                        onTimeChange(time)
                        state.timeslotPage = i
                        state.page = 0
                        state.historyDropdownState = false
                    }
                ) {
                    Text(timestampToDateString(time))
                }
            }
        }

        HistoryHandler().createHistoryCards(history).filter {
            ((name == "" && time == it.timeStamp) || name == it.acct)
        }.chunked(7).apply {
            drop(state.page).first().forEach {
                Card(
                    elevation = Dp(2F),
                    modifier = Modifier
                        .width(740.dp)
                        .wrapContentHeight()
                        .padding(2.dp)
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
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (state.page > 0) CommonButton(text = "<< prev page") {
                    state.page--
                }
                CommonButton(enabled = false, text = "${state.page+1}/${size}") {}
                if (state.page < size-1) CommonButton(text = "next page >>") {
                    state.page++
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