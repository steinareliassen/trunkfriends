package org.osprey.trunkfriends

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(Color.Gray)
                .size(height = 600.dp, width = 800.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val previousUserMap = mutableMapOf<String, CurrentUser>()
            HistoryHandler().readHistory().map {
                    user ->
                with(user) {
                    previousUserMap[first.acct]?.let {
                        HistoryCard(
                            follower = first.follower,
                            prevFollower = it.follower,
                            following = first.following,
                            prevFollowing = it.following,
                            acct = first.acct,
                            username = first.username,
                            timeStamp = second.substring(0,second.length-3).toLong()
                        )
                    } ?: HistoryCard(
                        follower = first.follower,
                        prevFollower = first.follower,
                        following = first.following,
                        prevFollowing = first.following,
                        acct = first.acct,
                        username = first.username,
                        timeStamp = second.substring(0,second.length-3).toLong()
                    ).also {
                        previousUserMap[first.acct] = first
                    }
                }
            }.forEach {
                Card(
                    elevation = Dp(4F),
                    modifier = Modifier.padding(4.dp).size(height = 60.dp, width = 740.dp).align(Alignment.CenterHorizontally)
                ) {
                    val date = DateTimeFormatter.ISO_INSTANT
                        .format(Instant.ofEpochSecond(it.timeStamp / 1000))
                    Column {
                        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            FancyCard(text = "($date) Account: "+it.acct + " "+it.username)
                        }
                        FollowCard(it.prevFollower, it.follower, it.prevFollowing, it.following)
                    }
                }
            }
        }
    }
}

@Composable
fun FancyCard(text: String) {
    Card(
        elevation = Dp(3F),
        border = BorderStroke(
            width = Dp(1F),
            color = Color.LightGray
        ),
        modifier = Modifier.padding(Dp(4F))
    ) {
        Text(text = text)
    }

}

@Composable
fun FollowCard(prevFollower: Boolean, follower: Boolean, prevFollowing: Boolean, following: Boolean) {
    Card(
        elevation = Dp(3F),
        border = BorderStroke(
            width = Dp(1F),
            color = Color.LightGray
        ),
        modifier = Modifier.padding(Dp(4F))
    ) {
        Row {
            Text("They follow you :")
            if (prevFollower != follower) {
                if (follower) Text("(x ->)", color = Color.Black) else Text("(✓ ->)", color = Color.Blue)
            }
            if (follower) Text("✓", color = Color.Blue) else Text("x", color = Color.Red)
            Text("   You follow them :")
            if (prevFollowing != following) {
                if (following) Text("(x ->)", color = Color.Black) else Text("(✓ ->)", color = Color.Blue)
            }
            if (following) Text("✓", color = Color.Blue) else Text("x", color = Color.Red)
            if (prevFollower != follower || prevFollowing != following) {
                Card(
                    backgroundColor = Color.Cyan
                ) {
                    Text("-CHANGE-", color = Color.Magenta)
                }
            }
        }
    }

}
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
