package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color



@Composable
fun aboutView() {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(Color.Gray)
            .verticalScroll(rememberScrollState())
    ) {
        Text("""
Trunk Friends is a simple friends tracking software.

It is free, use "as is" software.

This is still incomplete, though still usable software. 

This is public domain, no copyrights reserved, no warranty given.

https://github.com/steinareliassen/trunkfriends
        """.trimIndent(), color = Color.Black)
    }
}