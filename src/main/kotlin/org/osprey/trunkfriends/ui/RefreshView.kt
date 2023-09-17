package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.osprey.trunkfriends.historyhandler.refresh


@Composable
fun RefreshView(stopWatch: StopWatch) {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(Color.Gray)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Refreshing...")
        stopWatch.start()
        Text("Done refreshing...")
    }
}