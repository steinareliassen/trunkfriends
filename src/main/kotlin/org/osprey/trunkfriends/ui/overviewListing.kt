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
import org.osprey.trunkfriends.api.dto.CurrentUser
import org.osprey.trunkfriends.dal.HistoryData
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
    state: AppState,
    height: Int
) {
    fun rows() =
        (height - 200) / 27

    val sortDropDownExpanded = remember { mutableStateOf(false) }
    val sortState = remember { mutableStateOf(SortStyle.ACCOUNT) }
    val searchText = remember { mutableStateOf("") }
    val page = remember { mutableStateOf(0) }

    // Create a list of accounts from history-map, keeping the latest account follow / following status
    val history = HistoryData(state.getSelectedConfig())

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
            val searchTextFieldText = remember { mutableStateOf<String?>(null) }

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    singleLine = true,
                    modifier = Modifier.width(500.dp),
                    enabled = searchTextFieldText.value == null,
                    value = searchText.value,
                    onValueChange = {
                        searchText.value = it
                    },
                    label = { Text("Text to search for") }
                )
                CommonButton(
                    enabled = searchText.value.isNotBlank(),
                    text = if (searchTextFieldText.value == null) "Search" else "Clear"
                ) {
                    page.value = 0
                    if (searchTextFieldText.value != null) {
                        searchTextFieldText.value = null
                        searchText.value = ""
                    } else searchTextFieldText.value = searchText.value
                }
                Box {
                    Button(
                        enabled = true,
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                        onClick = { sortDropDownExpanded.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort"
                        )
                        Text("Sort")
                    }

                    DropdownMenu(
                        expanded = sortDropDownExpanded.value,
                        onDismissRequest = { sortDropDownExpanded.value = false }
                    ) {
                        SortStyle.entries.forEach {
                            CommonDropDownItem(text = it.text) {
                                sortState.value = SortStyle.valueOf(it.name)
                                sortDropDownExpanded.value = false
                            }
                        }
                    }
                }
            }

            history.createListCards(
                CompareUser(state.getSelectedConfig().split("/")[0], sortState.value)
            ).filter {
                searchTextFieldText.value == null || it.acct.lowercase(Locale.getDefault())
                    .contains(searchTextFieldText.value?.lowercase(Locale.getDefault()) ?: "")
            }.takeIf { it.isNotEmpty() }.let {
                it?.chunked(rows())?.apply {
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
                                    followCard(historyCard, state.pasteBag, View.LIST) { name, view ->
                                        state.changeZoom(name, view)
                                        historyState.storeHistoryPage(page.value)
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
                    ?: BannerRow("Search returned no results")
            }
        }
    }


}
