package com.example.trackme.ui.common

import androidx.compose.foundation.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import kotlin.math.roundToInt

@Composable
fun Altitude(
    altitude: Double,
    style: TextStyle = MaterialTheme.typography.body1,
    modifier: Modifier = Modifier,
) {
    Text("${altitude.roundToInt()} mas", style = style, modifier = modifier)
}