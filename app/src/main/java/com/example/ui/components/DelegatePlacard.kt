package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Country
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.UNBlue

@Composable
fun DelegatePlacard(
    country: Country,
    isCurrentSpeaker: Boolean = false,
    statusText: String? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCurrentSpeaker) DiplomaticGold else UNBlue.copy(alpha = 0.4f)
    val backgroundBrush = if (isCurrentSpeaker) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                DiplomaticGold.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                BorderStroke(if (isCurrentSpeaker) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(8.dp)
            ),
        shadowElevation = if (isCurrentSpeaker) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier
                .background(backgroundBrush)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = country.flagEmoji,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = country.name.uppercase(),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (country.isP5) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "PERMANENT MEMBER (P5)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiplomaticGold
                    )
                }

                if (!statusText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCurrentSpeaker) DiplomaticGold else UNBlue.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentSpeaker) Color.Black else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
