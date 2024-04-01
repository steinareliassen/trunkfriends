package org.osprey.trunkfriends.ui

import org.osprey.trunkfriends.ui.history.HistoryViewState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.api.dto.CurrentUser
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

    var sortDropDownExpanded by remember { mutableStateOf(false) }
    var sortState by remember { mutableStateOf(SortStyle.ACCOUNT) }
    var searchText by remember { mutableStateOf("") }

    var page by remember { mutableStateOf(0) }
    var rowsByPage by remember { mutableStateOf(0) }

    // On return back from zooming in on a user, we need to reset back to where we were.
    state.onZoomOut = {
        historyState.overviewReset = true
    }

    if (historyState.overviewReset) {
        page = historyState.returnPage
        rowsByPage = rows()
        historyState.overviewReset = false
    }

    // If we have changed window height enough to change number of rows, reset page counter
    // to avoid hitting a page that no longer exist.
    if (rowsByPage != rows()) {
        rowsByPage = rows()
        page = 0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .verticalScroll(rememberScrollState())
    ) {

        if (!state.history.isNotEmpty()) {
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
            var searchTextFieldText by remember { mutableStateOf<String?>(null) }

            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    singleLine = true,
                    modifier = Modifier.width(500.dp),
                    enabled = searchTextFieldText == null,
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    label = { Text("Text to search for") }
                )
                CommonButton(
                    enabled = searchText.isNotBlank(),
                    text = if (searchTextFieldText == null) "Search" else "Clear"
                ) {
                    page = 0
                    if (searchTextFieldText != null) {
                        searchTextFieldText = null
                        searchText = ""
                    } else searchTextFieldText = searchText
                }
                Box {
                    Button(
                        enabled = true,
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                        onClick = { sortDropDownExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort"
                        )
                        Text("Sort")
                    }

                    DropdownMenu(
                        expanded = sortDropDownExpanded,
                        onDismissRequest = { sortDropDownExpanded = false }
                    ) {
                        SortStyle.entries.forEach {
                            CommonDropDownItem(text = it.text) {
                                sortState = SortStyle.valueOf(it.name)
                                sortDropDownExpanded = false
                            }
                        }
                    }
                }
            }

            state.history.createListCards(
                CompareUser(state.getSelectedConfig().split("/")[0], sortState)
            ).filter {
                searchTextFieldText == null || it.acct.lowercase(Locale.getDefault())
                    .contains(searchTextFieldText?.lowercase(Locale.getDefault()) ?: "")
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
                            drop(page).first().forEach { historyCard ->
                                Row(modifier = Modifier.align(Alignment.Start)) {
                                    followCard(historyCard, state.pasteBag, View.LIST) { name, view ->
                                        state.changeZoom(name, view)
                                        historyState.storeHistoryPage(page)
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        if (page > 0) CommonButton(text = "<< prev page") {
                            page--
                        }
                        CommonButton(enabled = false, text = "${page + 1}/${size}") {}
                        if (page < size - 1) CommonButton(text = "next page >>") {
                            page++
                        }
                    }
                }
                    ?: BannerRow("Search returned no results")
            }
        }
    }


}
