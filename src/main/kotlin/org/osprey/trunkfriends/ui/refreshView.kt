package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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

fun executeManagement(count : Int, action : String) =
    """
    You have $count addresses you wish to $action. There is a delay between each address in order to not spam the
    server. Please note, that while you can cancel the action at any time, the addresses that has had the action
    applied to them will not be rolled back. If you have 20 followers you want to unfollow, and cancel after 5,
    you will still have unfollowed 5 followers.
                            """.trimIndent()
@Composable
fun refreshView(state: UIState, action: String? = null, count : Int = 0) {
    Text("\n")

    if (state.activeButtons)
        BannerRow(
            // Need to solve this in a better way.
            if (action == null)
                bannerRefresh
            else
                executeManagement(count, action)
            ,
            16f
        )
    else
        BannerRow(state.feedback, 16f)
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
            Button(
                enabled = state.activeButtons,
                modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White, contentColor = Color.Black),
                onClick = {
                    if (action == null)
                        state.startListRefresh()
                    else
                        state.startExecuteManagementAction(action, listOf())
                }
            ) {
                Text("Start importing following / followers list")
            }
        }
    }


}