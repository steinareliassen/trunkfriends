package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun aboutView() {
    Text("\n")
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(colorTwo)
            .verticalScroll(rememberScrollState())
    ) {
        BannerRow(
                """
  Trunk Friends is a simple application to help you track friends on the fediverse.

  It is free, use "as is" software, still in alpha-state. This means it is not seen as stable and 
  tested enough to be considered finished. Though it should still be usable, and it has been 
  put out as we continue to read about people loosing connection to friends and followers 
  because of defederation, but have a hard time tracking who they lost. 

  This software is public domain, no copyrights reserved, no warranty given.

  https://github.com/steinareliassen/trunkfriends
        """,18f)

    }
}