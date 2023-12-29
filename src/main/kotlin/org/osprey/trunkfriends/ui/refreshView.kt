package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    $action ${list.size} accounts. The 10 first are:
    
    ${list.take(10).reduce { a, b -> "$a\n    $b" }}
    
    There is a delay between each address in order to not spam the server. Please note, that while you can
    cancel the action at any time, the addresses that has had the action applied to them will not be rolled 
    back. If you have 20 followers you want to unfollow, and cancel after 5, you will still have 
    unfollowed 5 followers.
                            """

@Composable
fun refreshView(state: UIState) {
    val selectedList = remember { mutableStateOf<String?>(null) }
    val selectedDropdown = remember { mutableStateOf(false) }

    val action = state.context
    Text("\n")

    // Need to solve this in a better way.
    if (state.activeButtons) {
        BannerRow(
            if (action == null)
                bannerRefresh
            else
                executeManagement(state.actionList, action),
            14f
        )
    } else {
        BannerRow(state.feedback, 16f)
    }
    Text("\n")

    if (!state.activeButtons) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .background(colorTwo)
                .verticalScroll(rememberScrollState())
        ) {
            Button(
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = { state.refreshActive = false }
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
            Row {

                if (action == ManagementAction.ADD_TO_LIST) {
                    val lists = state.selectedConfig?.second?.hostInterface?.getLists()
                        ?: throw IllegalStateException("Should not happen")
                    DropdownMenu(
                        expanded = selectedDropdown.value,
                        onDismissRequest = { selectedDropdown.value = false }
                    ) {
                        lists.forEach { list ->
                            CommonDropDownItem(text = list.title) {
                                selectedDropdown.value = false
                                selectedList.value = list.title
                            }
                        }
                    }
                }

                Button(
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                    onClick = {
                        selectedDropdown.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Servers"
                    )
                    Text("Select list")
                }

                Button(
                    enabled = action != ManagementAction.ADD_TO_LIST|| selectedList.value != null,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                    onClick = {
                        try {
                            if (action == null)
                                state.startListRefresh()
                            else
                                state.startExecuteManagementAction(action, state.actionList, selectedList.value)
                        } finally {
                            selectedList.value = null
                        }
                    }
                ) {
                    if (action == null)
                        Text("Start importing following / followers list")
                    else
                        Text("$action ${state.actionList.size} accounts")
                }
            }

            if (action == ManagementAction.ADD_TO_LIST) {
                BannerRow("Selected list: ${selectedList.value ?: "Select list from dropdown"}")
            }
        }
    }

}

