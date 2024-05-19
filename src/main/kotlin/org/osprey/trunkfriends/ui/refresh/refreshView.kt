package org.osprey.trunkfriends.ui.refresh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osprey.trunkfriends.handlers.dto.BackupOptions
import org.osprey.trunkfriends.handlers.managementAction
import org.osprey.trunkfriends.handlers.refreshHistory
import org.osprey.trunkfriends.ui.*

// This is not pretty, need to be refactored.
val bannerRefresh =
    """
    You can now start importing your followers / following list by pressing the button below. It fetches about 1200
    followers per minute, so you can do a rough calculation on how long time it will take, and get yourself a cup of
    coffee while you wait, if the wait is too long. The delay is to prevent the mastodon instance from being flooded
    with requests. Most instances have a max request pr 5 minute interval.
                            """.trimIndent()

fun executeManagement(list: List<String>, action: ManagementAction) =
    """
    You are about to do the following:
    ${action.text} ${list.size} accounts. ${if (list.size > 10) "The 10 first are:" else ""}
    
    ${list.take(10).reduce { a, b -> "$a\n    $b" }}
    
    There is a delay between each address in order to not spam the server. Please note, that while you can
    cancel the action at any time, the addresses that has had the action applied to them will not be rolled 
    back. If you have 20 followers you want to unfollow, and cancel after 5, you will still have 
    unfollowed 5 followers. Followed/Unfollowed accounts are not immediately shown in history / list view,
    you must refresh followers first.
                            """

// Todo: Needs to be refactored to two views
@Composable
fun refreshView(state: AppState, action: ManagementAction? = null) {

    val coroutineScope = CoroutineScope(Dispatchers.Main)
    var feedback by remember { mutableStateOf("Refreshing") }
    var backupPlan by remember { mutableStateOf(setOf<BackupOptions>()) }
    val feedbackFunction = { param: String ->
        feedback = param
    }

    fun runBackgroundTask(task: () -> Unit) {
        if (state.networkTaskActive) return
        state.networkTaskActive = true
        task()
    }

    fun startListRefresh() {
        runBackgroundTask {
            coroutineScope.launch {
                refreshHistory(
                    state.selectedConfig
                        ?: throw IllegalStateException("Should not be null"),
                    backupPlan,
                    { !state.networkTaskActive },
                    feedbackFunction
                )
                // We are done with network task, enable UI again
                state.networkTaskActive = false

                // We need to refresh the history object, do so by issuing a server select.
                state.onServerSelect(View.HISTORY, state.selectedConfig)
            }
        }
    }

    fun startExecuteManagementAction(action: ManagementAction, accounts: List<String>, list: String? = null) {
        runBackgroundTask {
            coroutineScope.launch {
                managementAction(
                    accounts,
                    action,
                    list,
                    state.selectedConfig ?: throw IllegalStateException("Should not be null"),
                    { !state.networkTaskActive },
                    feedbackFunction
                )
            }
        }
    }

    var selectedList by remember { mutableStateOf<String?>(null) }
    var selectedDropdown by remember { mutableStateOf(false) }

    Text("\n")

    if (!state.networkTaskActive) {
        BannerRow(
            if (action == null)
                bannerRefresh
            else
                executeManagement(state.actionList, action),
            14f
        )
    } else {
        BannerRow(feedback, 16f)
    }
    Text("\n")

    if (action == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                fontSize = TextUnit(20f, TextUnitType.Sp),
                text = "Important:\nBacking up notes has restrictions due to Mastodon API\n" +
                "limitations. Read Trunkfriends about page for info."
            )
        }
        BackupOptions.entries.forEach { backupOption ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .background(colorOne)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    Checkbox(
                        checked = backupPlan.contains(backupOption),
                        onCheckedChange = {
                            backupPlan = backupPlan.toMutableSet().apply {
                                if (contains(backupOption)) {
                                    remove(backupOption)
                                    if (backupOption == BackupOptions.EVERYTHING) {
                                        backupOption.getOptions().forEach {
                                            key -> remove(key)
                                        }
                                    }
                                } else {
                                    add(backupOption)
                                    if (backupOption == BackupOptions.EVERYTHING) {
                                        backupOption.getOptions().forEach {
                                                key -> add(key)
                                        }
                                    }
                                }
                            }.toSet()
                        }
                    )
                }
                Column (
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text(
                        if (backupOption == BackupOptions.EVERYTHING)
                            backupOption.title
                        else
                            "Backup ${backupOption.title} (${backupOption.name}.csv)"
                    )
                }

            }
        }
    }
    if (state.networkTaskActive) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.networkTaskActive = false }
            ) {
                Text("Click to cancel.")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {

                if (action == ManagementAction.ADD_TO_LIST) {
                    runCatching {
                        val lists = state.selectedConfig?.second?.hostInterface?.getLists()
                            ?: throw IllegalStateException("Should not happen")
                        DropdownMenu(
                            expanded = selectedDropdown,
                            onDismissRequest = { selectedDropdown = false }
                        ) {
                            lists.forEach { list ->
                                CommonDropDownItem(text = list.title) {
                                    selectedDropdown = false
                                    selectedList = list.title
                                }
                            }
                        }

                        Button(
                            modifier = Modifier.padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            ),
                            onClick = {
                                selectedDropdown = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Servers"
                            )
                            Text("Select list")
                        }
                    }.onFailure {
                        Text("Error fetching lists")
                    }
                }

                Button(
                    enabled = action != ManagementAction.ADD_TO_LIST || selectedList != null,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                    onClick = {
                        feedbackFunction("Initializing...")
                        try {
                            if (action == null)
                                startListRefresh()
                            else
                                startExecuteManagementAction(action, state.actionList, selectedList)
                        } finally {
                            selectedList = null
                        }
                    }
                ) {
                    if (action == null)
                        Text("Start importing following / followers list")
                    else
                        Text("${action.text} ${state.actionList.size} accounts")
                }
            }

            if (action == ManagementAction.ADD_TO_LIST) {
                BannerRow("Selected list: ${selectedList ?: "Select list from dropdown"}")
            }
        }
    }

}

