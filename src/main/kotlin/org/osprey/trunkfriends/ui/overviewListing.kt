package org.osprey.trunkfriends.ui

import org.osprey.trunkfriends.ui.history.HistoryViewState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.api.CurrentUser
import org.osprey.trunkfriends.dal.HistoryHandler
import org.osprey.trunkfriends.ui.history.followCard
import java.util.*
import kotlin.Comparator

enum class SortStyle(val text: String) {
    FOLLOW_STATUS("Following/follower"),
    SERVER("Server name"),
    ACCOUNT("Account")
}

class CompareUser(
    private val server: String,
    private val sortStyle: SortStyle
) : Comparator<CurrentUser> {
    override fun compare(o1: CurrentUser, o2: CurrentUser): Int {
        val first = serverify(o1.acct)
        val second = serverify(o2.acct)

        if (sortStyle == SortStyle.FOLLOW_STATUS) {
            // Follower and following different?
            if ((o1.follower && !o2.follower) && (o1.following && !o2.following)) return -1
            if ((!o1.follower && o2.follower) && (!o1.following && o2.following)) return 1

            // Following different?
            if ((o1.following && !o2.following)) return -1
            if ((!o1.following && o2.following)) return 1

            // Followers different?
            if ((o1.follower && !o2.follower)) return -1
            if ((!o1.follower && o2.follower)) return 1

        }
        // If we sort by server, and servers are different, use this.
        if (sortStyle == SortStyle.SERVER) {
            val compareServers = first.split("@")[1].compareTo(second.split("@")[1])
            if (compareServers != 0) {
                return compareServers
            }
        }

        // Default, compare accounts, checking sortStyle ACCOUNT is pointless
        return first.compareTo(second)
    }

    private fun serverify(username: String) = if (username.contains("@")) username else "$username@$server"
}

@Composable
fun overviewListing(
    historyState: HistoryViewState,
    state: UIState,
    height: Int
) {
    fun rows() =
        (height - 200) / 27

    println("xxx $historyState ${historyState.time}")
    val sortDropDown = remember { mutableStateOf(false) }
    val sortState = remember { mutableStateOf(SortStyle.ACCOUNT) }
    val searchText = remember { mutableStateOf<String?>(null) }

    // Create a list of accounts from history-map, keeping the latest account follow / following status
    val list = HistoryHandler().readHistory(state.getSelectedConfig()).associate { it.first.acct to it.first }.map { it.value }
        .sortedWith(CompareUser(state.getSelectedConfig().split("/")[0], sortState.value))

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
server with requests. Once followers are imported, you will see them here.
            """.trimIndent(), 16f
            )
        } else {

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    singleLine = true,
                    modifier = Modifier.width(500.dp),
                    enabled = searchText.value == null,
                    value = historyState.searchText,
                    onValueChange = {
                        historyState.searchText = it
                    },
                    label = { Text("Text to search for") }
                )
                CommonButton(
                    enabled = historyState.searchText.isNotBlank(),
                    text = if (searchText.value == null) "Search" else "Clear"
                ) {
                    historyState.page = 0
                    if (searchText.value != null) {
                        searchText.value = null
                        historyState.searchText = ""
                    } else searchText.value = historyState.searchText
                }
                Box {
                    Button(
                        enabled = true,
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                        onClick = { sortDropDown.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort"
                        )
                        Text("Sort")
                    }

                    DropdownMenu(
                        expanded = sortDropDown.value,
                        onDismissRequest = { sortDropDown.value = false }
                    ) {
                        SortStyle.entries.forEach {
                            CommonDropDownItem(text = it.text) {
                                sortState.value = SortStyle.valueOf(it.name)
                                sortDropDown.value = false
                            }
                        }
                    }
                }
            }

            HistoryHandler().createListCards(list).filter {
                searchText.value == null || it.acct.lowercase(Locale.getDefault())
                    .contains(searchText.value?.lowercase(Locale.getDefault()) ?: "")
            }.takeIf { it.isNotEmpty() }.let {
                if (it != null) {
                    it.chunked(rows()).apply {
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
                                        followCard(historyCard, historyState, View.LIST) { name, view ->
                                            state.changeZoom(name, view)
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
                } else {
                    BannerRow("Search returned no results")
                }
            }
        }
    }


}
