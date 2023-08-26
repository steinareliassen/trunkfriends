package org.osprey.trunkfriends.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.osprey.trunkfriends.historyhandler.CurrentUser
import org.osprey.trunkfriends.historyhandler.HistoryCard
import org.osprey.trunkfriends.historyhandler.HistoryHandler
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun HistoryListing() {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(Color.Gray)
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
                    ).also {
                        previousUserMap[first.acct] = first
                    }
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
                modifier = Modifier
                    .width(740.dp)
                    .wrapContentHeight()
                    .padding(4.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                val date = DateTimeFormatter.ISO_INSTANT
                    .format(Instant.ofEpochSecond(it.timeStamp / 1000))
                Column {
                    Row(modifier = Modifier.align(Alignment.Start)) {
                        FancyCard(text = "($date) Account: "+it.acct + " "+it.username)
                    }
                    FollowCard(it.prevFollower, it.follower, it.prevFollowing, it.following)
                }
            }
        }
    }
}

@Composable
fun FancyCard(text: String) {
    Card(
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
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
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
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
                    Text("<->", color = Color.Magenta)
                }
            }
        }
    }

}