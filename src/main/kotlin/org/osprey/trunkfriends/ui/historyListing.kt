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
import androidx.compose.ui.unit.sp
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import org.osprey.trunkfriends.ui.dto.HistoryCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun historyListing(
    serverUser: String,
    zoomedName: String?,
    time: Long,
    state: UIState,
    onNameChange: (String?) -> Unit,
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
        }.distinct().size - 1
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        if (history.isEmpty()) {
            Text("\n")
            BannerRow(
                """
You do not seem to have imported the followers / following list from the mastodon 
instance. Click on "Refresh followers" and start importing. If you have a large
amount of followers, this can take some time, as we do not want to swamp the
server with requests. Once followers are imported, you will be see them here.
            """.trimIndent(), 16f
            )
            return
        }
        val timeslots = history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.distinct()

        Row(modifier = Modifier.fillMaxWidth()) {
            if (history.isNotEmpty() && state.zoomedName == null) {
                CommonIconButton(text = timestampToDateString(state.time), icon = Icons.Default.MoreVert) {
                    state.historyDropdownState = true
                }
                if (state.timeslotPage > 0)
                    CommonIconButton(text = "Previous timeslot", icon = Icons.Default.ArrowBack) {
                        state.timeslotPage--
                        state.time = timeslots[state.timeslotPage]
                        state.page = 0
                    }
                CommonButton(enabled = false, text = "${state.timeslotPage + 1}/${timeslots.size}") {}
                if (state.timeslotPage < timeslots.size - 1)
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
            ((zoomedName == null && time == it.timeStamp) || zoomedName == it.acct)
        }.chunked(if (zoomedName == null) 14 else 8).apply {
            Card(
                elevation = Dp(2F),
                modifier = Modifier
                    .width(740.dp)
                    .wrapContentHeight()
                    .padding(2.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(modifier = Modifier.background(Color(0xB3, 0xB4, 0x92, 0xFF))) {
                    drop(state.page).first().forEach { historyCard ->
                        Row(modifier = Modifier.align(Alignment.Start)) {
                            if (zoomedName != null) {
                                Column {
                                    Text("⏰ ${timestampToDateString(historyCard.timeStamp)}")
                                    followCard(historyCard, onNameChange)
                                }
                            } else {
                                followCard(historyCard, onNameChange)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (state.page > 0) CommonButton(text = "<< prev page") {
                    state.page--
                }
                CommonButton(enabled = false, text = "${state.page + 1}/${size}") {}
                if (state.page < size - 1) CommonButton(text = "next page >>") {
                    state.page++
                }
            }
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
        modifier = Modifier.padding(0.dp).height(25.dp).width(100.dp),
        onClick = { onClick() },
    ) {
        Text(text, fontSize = 7.sp)
    }
}

@Composable
fun followCard(
    historyCard: HistoryCard,
    onNameChange: (String?) -> Unit
) {
    Card(
        elevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xB3, 0xB4, 0x92, 0xFF)
        ),
        modifier = Modifier.padding(Dp(1F)).width(740.dp).height(23.dp)
    ) {
        with(historyCard) {
            Row {
                Column(
                    modifier = Modifier.background(Color.White)
                ) {
                    Row(modifier = Modifier.width(200.dp)) {
                        Column {
                            Row(modifier = Modifier.width(100.dp)) {
                                Text("\uD83E\uDEF5")
                                if (prevFollower != follower) {
                                    if (follower) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text(
                                        "\uD83D\uDFE2 ➡",
                                        color = Color.Blue
                                    )
                                }
                                if (follower) Text("\uD83D\uDFE2", color = Color.Blue) else Text(
                                    "\uD83D\uDD34",
                                    color = Color.Red
                                )
                            }
                        }
                        Column {
                            Row(modifier = Modifier.width(100.dp)) {
                                Text("\uD83D\uDC49")
                                if (prevFollowing != following) {
                                    if (following) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text(
                                        "\uD83D\uDFE2 ➡",
                                        color = Color.Blue
                                    )
                                }
                                if (following) Text("\uD83D\uDFE2", color = Color.Blue) else Text(
                                    "\uD83D\uDD34",
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
                Column {
                    Row(modifier = Modifier.width(500.dp)) {
                        Text(text = acct)
                    }
                }
                zoomButton(text = "\uD83D\uDD0D") {
                    onNameChange(acct)
                }
            }
        }
    }

}