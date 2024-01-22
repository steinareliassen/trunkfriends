package org.osprey.trunkfriends.ui.history

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
import org.osprey.trunkfriends.ui.BannerRow
import org.osprey.trunkfriends.ui.CommonButton
import org.osprey.trunkfriends.ui.CommonIconButton
import org.osprey.trunkfriends.ui.View
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun historyListing(
    historyState: HistoryViewState,
    serverUser: String,
    zoomedName: String?,
    onNameChange: (String?, View) -> Unit,
) {
    fun rows() =
        (historyState.height - 200) / 27

    fun timestampToDateString(timestamp: Long) =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("CET"))
            .format(Instant.ofEpochSecond(timestamp / 1000)).run {
                this.substring(0..this.length - 4).replace("T", " ")
            }

    if (historyState.resetHistoryPage(zoomedName)) {
        onNameChange(null, historyState.returnView)
        return
    }

    val history = HistoryHandler().readHistory(serverUser)

    // Did we just get to history view from another view? If so, calculate the time of the
    // last page, and return, so UI can refresh to that page.
    if (history.isNotEmpty() && historyState.time == 0L) {
        historyState.time = history.map { (_, control) ->
            control.substring(0, control.length - 3).toLong()
        }.max()
        historyState.timeslotPage = history.map { (_, control) ->
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
server with requests. Once followers are imported, you will see them here.
            """.trimIndent(), 16f
            )
        } else {

            val timeslots = history.map { (_, control) ->
                control.substring(0, control.length - 3).toLong()
            }.distinct()

            Row(modifier = Modifier.fillMaxWidth()) {
                if (history.isNotEmpty() && zoomedName == null) {
                    CommonIconButton(text = timestampToDateString(historyState.time), icon = Icons.Default.MoreVert) {
                        historyState.historyDropdownState = true
                    }
                    if (historyState.timeslotPage > 0)
                        CommonIconButton(text = "Previous timeslot", icon = Icons.Default.ArrowBack) {
                            historyState.time = timeslots[historyState.previousTimeslot()]
                        }
                    CommonButton(enabled = false, text = "${historyState.timeslotPage + 1}/${timeslots.size}") {}
                    if (historyState.timeslotPage < timeslots.size - 1)
                        CommonIconButton(
                            text = "Next timeslot",
                            icon = Icons.Default.ArrowForward,
                            iconBefore = false
                        ) {
                            historyState.time = timeslots[historyState.nextTimeslot()]
                        }
                }
            }

            DropdownMenu(
                expanded = historyState.historyDropdownState,
                onDismissRequest = { historyState.historyDropdownState = false }
            ) {

                timeslots.forEachIndexed { i, time ->
                    DropdownMenuItem(
                        onClick = {
                            historyState.time = time
                            historyState.timeslotPage = i
                            historyState.page = 0
                            historyState.historyDropdownState = false
                        }
                    ) {
                        Text(timestampToDateString(time))
                    }
                }
            }

            HistoryHandler().createHistoryCards(history).filter {
                ((zoomedName == null && historyState.time == it.timeStamp) || zoomedName == it.acct)
            }.chunked(if (zoomedName == null) rows() else rows()/2).apply {
                Card(
                    elevation = Dp(2F),
                    modifier = Modifier
                        .width(740.dp)
                        .wrapContentHeight()
                        .padding(2.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Column(modifier = Modifier.background(Color(0xB3, 0xB4, 0x92, 0xFF))) {
                        drop(historyState.page).first().forEach { historyCard ->
                            Row(modifier = Modifier.align(Alignment.Start)) {
                                if (zoomedName != null) {
                                    Column {
                                        Text("⏰ ${timestampToDateString(historyCard.timeStamp)}")
                                        followCard(historyCard, historyState, View.HISTORY)
                                    }
                                } else {
                                    followCard(historyCard, historyState, View.HISTORY) { name, view ->
                                        onNameChange(name, view)
                                        historyState.storeHistoryPage()
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    if (historyState.page > 0) CommonButton(text = "<< prev page") {
                        historyState.page--
                    }
                    CommonButton(enabled = false, text = "${historyState.page + 1}/${size}") {}
                    if (historyState.page < size - 1) CommonButton(text = "next page >>") {
                        historyState.page++
                    }
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
        modifier = Modifier.padding(0.dp).height(25.dp),
        onClick = { onClick() },
    ) {
        Text(text, fontSize = 7.sp)
    }
}

@Composable
fun followCard(
    historyCard: HistoryCard,
    historyState: HistoryViewState,
    view: View,
    onNameChange: ((String?, View) -> Unit)? = null
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
                    Row(modifier = Modifier.width(450.dp)) {
                        Text(text = acct)
                    }
                }
                Column {
                    Checkbox(
                        checked = historyState.pasteBag.contains(acct),
                        onCheckedChange = {
                            if (historyState.pasteBag.contains(acct)) {
                                historyState.pasteBag.remove(acct)
                            } else {
                                historyState.pasteBag.add(acct)
                            }
                        }
                    )
                }
                if (onNameChange != null) {
                    Column {
                        zoomButton(text = "\uD83D\uDD0D") {
                            onNameChange(acct, view)
                        }
                    }
                }
            }
        }
    }

}
