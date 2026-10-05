package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebatePhase
import com.example.data.model.MotionType
import com.example.data.model.MunSpeech
import com.example.data.model.SpeakerRole
import com.example.data.model.StandardCountries
import com.example.ui.components.DelegatePlacard
import com.example.ui.components.SpeechInputBar
import com.example.ui.components.TimerDisplay
import com.example.ui.components.VotingBoard
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.DiplomaticNavy
import com.example.ui.theme.DiplomaticNavySurface
import com.example.ui.theme.UNBlue
import com.example.viewmodel.DiplomatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebateChamberScreen(
    viewModel: DiplomatViewModel,
    onAdjournDebate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val committee by viewModel.currentCommittee.collectAsState()
    val topic by viewModel.currentTopic.collectAsState()
    val userCountry by viewModel.userCountry.collectAsState()
    val phase by viewModel.debatePhase.collectAsState()
    val rollCallChoice by viewModel.rollCallChoice.collectAsState()
    val speeches by viewModel.speeches.collectAsState()
    val currentSpeaker by viewModel.currentSpeaker.collectAsState()
    val isUserTurn by viewModel.isUserTurn.collectAsState()

    val totalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val remainingSeconds by viewModel.timerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    val speechDraft by viewModel.speechInputDraft.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()

    val activeMotion by viewModel.activeMotion.collectAsState()
    val userVoteChoice by viewModel.userVoteChoice.collectAsState()

    var showMotionsDialog by remember { mutableStateOf(false) }
    var showAdjournConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto-scroll when new speeches appear
    LaunchedEffect(speeches.size) {
        if (speeches.isNotEmpty()) {
            listState.animateScrollToItem(speeches.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${committee.acronym} CHAMBER",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PhaseBadge(phase = phase)
                        }
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Audio TTS Mute/Unmute
                    IconButton(
                        onClick = { viewModel.toggleTts() },
                        modifier = Modifier.testTag("tts_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Toggle Audio",
                            tint = if (isTtsEnabled) UNBlue else Color.Gray
                        )
                    }

                    // Adjourn and Evaluate Button
                    Button(
                        onClick = { showAdjournConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DiplomaticGold),
                        modifier = Modifier.padding(end = 8.dp).testTag("adjourn_session_btn"),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rate Session", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (phase != DebatePhase.ROLL_CALL) {
                SpeechInputBar(
                    speechText = speechDraft,
                    onSpeechTextChange = { viewModel.setSpeechInputDraft(it) },
                    userCountry = userCountry,
                    isListening = isListening,
                    onToggleVoiceInput = { viewModel.toggleVoiceInput() },
                    onSendSpeech = {
                        if (speechDraft.isNotBlank()) {
                            viewModel.submitUserSpeech(speechDraft)
                        }
                    },
                    onOpenMotionsDialog = { showMotionsDialog = true },
                    isUserFloorTime = isUserTurn
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Timer Strip (Only in active debate phases)
            if (phase == DebatePhase.GENERAL_SPEAKERS_LIST || phase == DebatePhase.MODERATED_CAUCUS) {
                TimerDisplay(
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalSeconds,
                    isRunning = isTimerRunning,
                    isUserSpeaking = isUserTurn,
                    onTogglePlayPause = { viewModel.toggleTimer() },
                    onReset = { viewModel.resetTimer() },
                    onAddSeconds = { viewModel.addTimerSeconds(it) },
                    onYieldToChair = { viewModel.yieldToChair() },
                    onYieldToQuestions = { viewModel.yieldToQuestions() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Current Speaker Placard
            currentSpeaker?.let { speaker ->
                DelegatePlacard(
                    country = speaker,
                    isCurrentSpeaker = true,
                    statusText = if (speaker.code == userCountry.code) "YOU HAVE THE FLOOR" else "DELIVERING SPEECH",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Roll Call Prompt Banner
            if (phase == DebatePhase.ROLL_CALL && rollCallChoice == null) {
                RollCallBanner(
                    countryName = userCountry.name,
                    onAnswer = { choice -> viewModel.answerRollCall(choice) }
                )
            }

            // Active Voting Procedure
            activeMotion?.let { motion ->
                VotingBoard(
                    motionTitle = "Motion: ${motion.type.title}",
                    motionDetails = "Proposed by ${motion.proposedByCountryCode} on \"${motion.topic}\"",
                    votesFavor = motion.votesInFavor,
                    votesAgainst = motion.votesAgainst,
                    votesAbstain = 0,
                    userHasVoted = userVoteChoice != null,
                    userVoteChoice = userVoteChoice,
                    canAbstain = rollCallChoice != "PRESENT_AND_VOTING",
                    onVote = { choice -> viewModel.castUserVote(choice) },
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Next Speaker / Advance Floor Button (When not user's turn)
            if (phase != DebatePhase.ROLL_CALL && !isUserTurn && activeMotion == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulated debate active...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { viewModel.recognizeNextSpeaker() },
                        modifier = Modifier.testTag("advance_speaker_btn"),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Next Speaker", fontSize = 11.sp)
                    }
                }
            }

            // Speeches Transcript Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(speeches, key = { it.id }) { speech ->
                    SpeechBubble(speech = speech, userCountryCode = userCountry.code)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Points & Motions Dialog
    if (showMotionsDialog) {
        MotionsDialog(
            userCountry = userCountry,
            onDismiss = { showMotionsDialog = false },
            onProposeMotion = { type, topicStr, totalMin, speakSec ->
                viewModel.proposeMotion(type, topicStr, totalMin, speakSec)
                showMotionsDialog = false
            }
        )
    }

    // Adjourn & Rate Confirmation Dialog
    if (showAdjournConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showAdjournConfirmDialog = false },
            icon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = DiplomaticGold) },
            title = { Text("Adjourn & Grade Session") },
            text = {
                Text("Adjourn the committee debate to receive your comprehensive MUN Delegate Performance Evaluation & Rating calibrated to your age bracket?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdjournConfirmDialog = false
                        viewModel.concludeDebateAndEvaluate()
                        onAdjournDebate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UNBlue),
                    modifier = Modifier.testTag("confirm_adjourn_btn")
                ) {
                    Text("Adjourn & Grade")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjournConfirmDialog = false }) {
                    Text("Continue Debate")
                }
            }
        )
    }
}

@Composable
private fun PhaseBadge(phase: DebatePhase) {
    val text = when (phase) {
        DebatePhase.ROLL_CALL -> "ROLL CALL"
        DebatePhase.GENERAL_SPEAKERS_LIST -> "GSL"
        DebatePhase.MODERATED_CAUCUS -> "MOD CAUCUS"
        DebatePhase.UNMODERATED_CAUCUS -> "UNMOD"
        DebatePhase.VOTING_PROCEDURE -> "VOTING"
        DebatePhase.ADJOURNED -> "ADJOURNED"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(UNBlue.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RollCallBanner(
    countryName: String,
    onAnswer: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("roll_call_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏛️ FORMAL ROLL CALL IN PROGRESS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "The Chair calls: \"$countryName\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAnswer("PRESENT") },
                    modifier = Modifier.weight(1f).testTag("roll_call_present_btn")
                ) {
                    Text("Present", fontSize = 12.sp)
                }
                Button(
                    onClick = { onAnswer("PRESENT_AND_VOTING") },
                    colors = ButtonDefaults.buttonColors(containerColor = DiplomaticGold),
                    modifier = Modifier.weight(1.3f).testTag("roll_call_present_voting_btn")
                ) {
                    Text("Present & Voting", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SpeechBubble(
    speech: MunSpeech,
    userCountryCode: String
) {
    val isChair = speech.speakerRole == SpeakerRole.CHAIR
    val isUser = speech.speakerRole == SpeakerRole.USER
    val country = speech.countryCode?.let { StandardCountries.find(it) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Speaker Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (isChair) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = DiplomaticGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CHAIRPERSON (DAIS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = DiplomaticGold
                )
            } else {
                Text(text = country?.flagEmoji ?: "🌐", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isUser) "DELEGATION OF ${country?.name ?: "YOUR COUNTRY"} (YOU)" else country?.name ?: speech.speakerName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Bubble Content
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = when {
                isChair -> DiplomaticNavy
                isUser -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = speech.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = when {
                        isChair -> Color.White
                        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun MotionsDialog(
    userCountry: com.example.data.model.Country,
    onDismiss: () -> Unit,
    onProposeMotion: (type: MotionType, topic: String, totalMinutes: Int, speakingSeconds: Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(MotionType.MODERATED_CAUCUS) }
    var topicText by remember { mutableStateOf("") }
    var totalMin by remember { mutableIntStateOf(10) }
    var speakingSec by remember { mutableIntStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Raise Point or Motion", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Select Parliamentary Action:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                MotionType.values().forEach { mType ->
                    val isSelected = selectedType == mType
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) UNBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { selectedType = mType }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "●" else "○",
                            color = if (isSelected) UNBlue else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(mType.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(mType.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (selectedType == MotionType.MODERATED_CAUCUS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = topicText,
                        onValueChange = { topicText = it },
                        label = { Text("Caucus Sub-topic") },
                        placeholder = { Text("e.g. Legal safeguards and inspection mechanisms") },
                        modifier = Modifier.fillMaxWidth().testTag("caucus_topic_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onProposeMotion(selectedType, topicText, totalMin, speakingSec) },
                modifier = Modifier.testTag("submit_motion_btn")
            ) {
                Text("Raise on Floor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
