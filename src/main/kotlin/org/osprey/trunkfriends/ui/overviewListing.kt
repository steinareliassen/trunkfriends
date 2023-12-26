package org.osprey.trunkfriends.ui

import org.osprey.trunkfriends.ui.history.HistoryViewState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import org.osprey.trunkfriends.ui.history.followCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun overviewListing(
    historyState: HistoryViewState,
    serverUser: String,
    onNameChange: (String?) -> Unit
) {
    fun timestampToDateString(timestamp: Long) =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMAN)
            .withZone(ZoneId.of("CET"))
            .format(Instant.ofEpochSecond(timestamp / 1000)).run {
                this.substring(0..this.length - 4).replace("T", " ")
            }

    val list = HistoryHandler().readHistory(serverUser).associate { it.first.acct to it.first }.map { it.value }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        if (list.isEmpty()) {
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
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier.width(500.dp),
                enabled = true,
                value = historyState.searchText,
                onValueChange = { historyState.searchText = it },
                label = { Text("Text to search for") }
            )
            CommonButton(text = "Search") {
            }

            Button(
                enabled = true,
                modifier = Modifier.padding(4.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { true }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Sort"
                )
                Text("Sort")
            }
        }

        HistoryHandler().createListCards(list).chunked(14).apply {
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
                            followCard(historyCard, historyState) {
                                onNameChange(it)
                                historyState.storeHistoryPage()
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
