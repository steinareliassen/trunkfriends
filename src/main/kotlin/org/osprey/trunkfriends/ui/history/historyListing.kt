package org.osprey.trunkfriends.ui.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.osprey.trunkfriends.dal.HistoryData
import org.osprey.trunkfriends.ui.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun historyListing(
    historyState: HistoryViewState,
    state: AppState,
    zoomedName: String?,
    height: Int
) {
    val history = HistoryData(state.getSelectedConfig())
    var timeslotPage by remember { mutableStateOf(history.getTimeslots().distinct().size - 1) }
    val page = remember { mutableStateOf(0) }

    fun rows() =
        (height - 200) / 27

    fun nextTimeslot()  {
        timeslotPage++
        page.value = 0
    }

    fun previousTimeslot()  {
        timeslotPage--
        page.value = 0
    }

    fun timestampToDateString(timestamp: Long) =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("CET"))
            .format(Instant.ofEpochSecond(timestamp / 1000)).run {
                this.substring(0..this.length - 4).replace("T", " ")
            }

    if (historyState.resetHistoryPage(zoomedName)) {
        timeslotPage = historyState.returnTimeslot
        state.changeZoom(null, state.returnView)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        if (!history.isNotEmpty()) {
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

            val timeslots = history.getTimeslots()
            val historyDropdownState = remember { mutableStateOf(false) }

            Row(modifier = Modifier.fillMaxWidth()) {
                if (history.isNotEmpty() && zoomedName == null) {
                    CommonIconButton(text = timestampToDateString(timeslots[timeslotPage]), icon = Icons.Default.MoreVert) {
                        historyDropdownState.value = true
                    }
                    if (timeslotPage > 0)
                        CommonIconButton(text = "Previous timeslot", icon = Icons.Default.ArrowBack) {
                            previousTimeslot()
                        }
                    CommonButton(enabled = false, text = "${timeslotPage + 1}/${timeslots.size}") {}
                    if (timeslotPage < timeslots.size - 1)
                        CommonIconButton(
                            text = "Next timeslot",
                            icon = Icons.Default.ArrowForward,
                            iconBefore = false
                        ) {
                            nextTimeslot()
                        }
                }
            }

            DropdownMenu(
                expanded = historyDropdownState.value,
                onDismissRequest = { historyDropdownState.value = false }
            ) {

                timeslots.forEachIndexed { i, time ->
                    DropdownMenuItem(
                        onClick = {
                            timeslotPage = i
                            page.value = 0
                            historyDropdownState.value = false
                        }
                    ) {
                        Text(timestampToDateString(time))
                    }
                }
            }

            history.createHistoryCards().filter {
                ((zoomedName == null && timeslots[timeslotPage] == it.timeStamp) || zoomedName == it.acct)
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
                        drop(page.value).first().forEach { historyCard ->
                            Row(modifier = Modifier.align(Alignment.Start)) {
                                if (zoomedName != null) {
                                    Column {
                                        Text("⏰ ${timestampToDateString(historyCard.timeStamp)}")
                                        followCard(historyCard, state.pasteBag, View.HISTORY)
                                    }
                                } else {
                                    followCard(historyCard, state.pasteBag, View.HISTORY) { name, view ->
                                        state.changeZoom(name, view)
                                        historyState.storeHistoryPage(timeslotPage)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    if (page.value > 0) CommonButton(text = "<< prev page") {
                        page.value--
                    }
                    CommonButton(enabled = false, text = "${page.value + 1}/${size}") {}
                    if (page.value < size - 1) CommonButton(text = "next page >>") {
                        page.value++
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
    pasteBag: PasteBag,
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
                        checked = pasteBag.contains(acct),
                        onCheckedChange = {
                            if (pasteBag.contains(acct)) {
                                pasteBag.remove(acct)
                            } else {
                                pasteBag.add(acct)
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