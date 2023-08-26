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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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
                val date = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    .withLocale(Locale.GERMAN )
                    .withZone(ZoneId.of("CET"))
                    .format(Instant.ofEpochSecond(it.timeStamp / 1000))
                Column {
                    Row(modifier = Modifier.align(Alignment.Start)) {
                        FollowCard(it.prevFollower, it.follower, it.prevFollowing, it.following)
                        FancyCard(date, it.acct, it.username)
                    }
                }
            }
        }
    }
}

@Composable
fun FancyCard(date: String, account: String, username: String) {
    Card(
        elevation = 3.dp,
        border = BorderStroke(
            width = 1.dp,
            color = Color.LightGray
        ),
        modifier = Modifier.padding(Dp(4F)).width(670.dp)
    ) {
        Column {
            Text(text = "⏰ $date - $username")
            Text(text = "\uD83D\uDCE9 $account")
        }
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
        modifier = Modifier.padding(Dp(4F)).width(100.dp)
    ) {
        Column {
            Row {
                Text("\uD83E\uDEF5")
                if (prevFollower != follower) {
                    if (follower) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text("\uD83D\uDFE2 ➡", color = Color.Blue)
                }
                if (follower) Text("\uD83D\uDFE2", color = Color.Blue) else Text("\uD83D\uDD34", color = Color.Red)
            }
            Row {
                Text("\uD83D\uDC49")
                if (prevFollowing != following) {
                    if (following) Text("\uD83D\uDD34 ➡", color = Color.Black) else Text("\uD83D\uDFE2 ➡", color = Color.Blue)
                }
                if (following) Text("\uD83D\uDFE2", color = Color.Blue) else Text("\uD83D\uDD34", color = Color.Red)
            }
        }
    }

}