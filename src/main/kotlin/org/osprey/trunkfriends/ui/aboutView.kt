package org.osprey.trunkfriends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun aboutView() {
    Text("\n")
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .background(colorTwo)
            .verticalScroll(rememberScrollState())
    ) {
        val text = """
  Trunk Friends is a simple application to help you track friends on the fediverse.

  It is free, use "as is" software, still in alpha-state. This means it is not seen as stable and 
  tested enough to be considered finished. Though it should still be usable, and it has been 
  put out as we continue to read about people loosing connection to friends and followers 
  because of defederation, but have a hard time tracking who they lost. 

  This software is public domain, no copyrights reserved, no warranty given.

  https://github.com/steinareliassen/trunkfriends
  
  New backup functionality includes ability to back up notes. This comes with a few limitations it
  is good to know about what are, why they are there, and possible ways to avoid it. Read this page
  for more info:
  
  https://steinareliassen.github.io/trunkfriends/backupfeatures.html
  """
        val annotatedString = buildAnnotatedString {
            append(text)
            val regex = Regex("(https://.*?)[\n|$]")
            val matches = regex.findAll(text)
            matches.forEach {
                val startIndex = it.range.start
                val endIndex = it.range.last
                println(it.value.trim())
                addStyle(
                    style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                    start = startIndex,
                    end = endIndex
                )
                addStringAnnotation(
                    tag = "url",
                    annotation = it.value,
                    start = startIndex,
                    end = endIndex
                )
            }

        }

        AnnotatedBannerRow(annotatedString, 16f)

    }
}