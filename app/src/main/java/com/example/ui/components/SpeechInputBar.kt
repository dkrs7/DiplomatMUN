package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Country
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.SpeechTimerDanger
import com.example.ui.theme.UNBlue

@Composable
fun SpeechInputBar(
    speechText: String,
    onSpeechTextChange: (String) -> Unit,
    userCountry: Country,
    isListening: Boolean,
    onToggleVoiceInput: () -> Unit,
    onSendSpeech: () -> Unit,
    onOpenMotionsDialog: () -> Unit,
    isUserFloorTime: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speech_input_bar"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Quick Diplomatic Starter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {
                        val prefix = "Honorable Chair and fellow delegates, "
                        if (!speechText.startsWith(prefix)) {
                            onSpeechTextChange(prefix + speechText)
                        }
                    },
                    label = { Text("Honorable Chair...", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = UNBlue.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.testTag("chip_honorable_chair")
                )

                AssistChip(
                    onClick = {
                        val statement = "The delegation of ${userCountry.name} asserts that "
                        if (!speechText.contains(statement)) {
                            onSpeechTextChange(speechText + statement)
                        }
                    },
                    label = { Text("This delegation asserts...", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = UNBlue.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.testTag("chip_delegation_asserts")
                )

                AssistChip(
                    onClick = {
                        val yieldPhrase = " The delegate yields the remaining time to the Chair."
                        if (!speechText.contains("yield")) {
                            onSpeechTextChange(speechText.trimEnd() + yieldPhrase)
                        }
                    },
                    label = { Text("Yield to Chair...", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DiplomaticGold.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("chip_yield_chair")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Voice Listening Banner
            AnimatedVisibility(visible = isListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpeechTimerDanger.copy(alpha = 0.12f))
                        .border(1.dp, SpeechTimerDanger.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(SpeechTimerDanger)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Listening to delegate speech... Speak clearly",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpeechTimerDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Raise Placard / Motion Button
                IconButton(
                    onClick = onOpenMotionsDialog,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DiplomaticGold.copy(alpha = 0.18f))
                        .testTag("raise_placard_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = "Raise Placard / Motions",
                        tint = DiplomaticGold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input
                OutlinedTextField(
                    value = speechText,
                    onValueChange = onSpeechTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("speech_text_input"),
                    placeholder = {
                        Text(
                            text = if (isUserFloorTime) "Deliver your speech on the floor..." else "Prepare speech or type point/motion...",
                            fontSize = 13.sp
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Voice Dictation Toggle Button
                IconButton(
                    onClick = onToggleVoiceInput,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) SpeechTimerDanger else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("voice_dictation_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop voice" else "Start voice speech",
                        tint = if (isListening) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                FloatingActionButton(
                    onClick = onSendSpeech,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("send_speech_fab"),
                    containerColor = if (speechText.isNotBlank()) UNBlue else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (speechText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Deliver Speech",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
