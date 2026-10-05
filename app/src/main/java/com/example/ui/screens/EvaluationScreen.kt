package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EvaluationResult
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.DiplomaticGoldContainer
import com.example.ui.theme.UNBlue
import com.example.viewmodel.DiplomatViewModel

@Composable
fun EvaluationScreen(
    viewModel: DiplomatViewModel,
    onReturnHome: () -> Unit,
    onNewDebate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEvaluating by viewModel.isEvaluating.collectAsState()
    val evaluation by viewModel.currentEvaluation.collectAsState()

    if (isEvaluating || evaluation == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                CircularProgressIndicator(
                    color = DiplomaticGold,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Dais is Deliberating...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Analyzing speech transcripts, geopolitical stance, parliamentary procedure adherence, and age-calibrated standard...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val eval = evaluation!!

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Award Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DiplomaticGoldContainer)
                .border(2.dp, DiplomaticGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(eval.awardIcon, fontSize = 34.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "OFFICIAL MUN SCORECARD",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = eval.awardTier,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = DiplomaticGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Overall Score Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("evaluation_score_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${eval.overallScore}",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "OVERALL DELEGATE RATING (/100)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Age Calibration Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(UNBlue.copy(alpha = 0.08f))
                        .border(1.dp, UNBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚖️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Age-Calibrated Rubric: ${eval.ageBracket}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = eval.ageRubricContext,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Rubrics Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Performance Metric Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                RubricProgressBar(label = "Substantive Policy & Geopolitics", score = eval.substantivePolicyScore)
                Spacer(modifier = Modifier.height(10.dp))
                RubricProgressBar(label = "Rules of Procedure & Decorum", score = eval.parliamentaryScore)
                Spacer(modifier = Modifier.height(10.dp))
                RubricProgressBar(label = "Rhetoric, Oratory & Persuasion", score = eval.rhetoricScore)
                Spacer(modifier = Modifier.height(10.dp))
                RubricProgressBar(label = "Time Management & Yields", score = eval.timeManagementScore)
                Spacer(modifier = Modifier.height(10.dp))
                RubricProgressBar(label = "Caucus & Consensus Strategy", score = eval.strategicConsensusScore)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chair Concluding Remarks
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = DiplomaticGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chairperson's Dais Assessment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${eval.chairSummaryRemarks}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Strengths
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Key Diplomatic Strengths",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                eval.strengths.forEach { str ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("✓ ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text(str, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actionable Improvements
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DiplomaticGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Targeted Areas for Improvement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DiplomaticGold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                eval.areasForImprovement.forEach { imp ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("→ ", color = DiplomaticGold, fontWeight = FontWeight.Bold)
                        Text(imp, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onReturnHome,
                modifier = Modifier.weight(1f).height(50.dp).testTag("eval_return_home_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dashboard")
            }

            Button(
                onClick = onNewDebate,
                modifier = Modifier.weight(1f).height(50.dp).testTag("eval_new_debate_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Debate")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RubricProgressBar(
    label: String,
    score: Int
) {
    val progress = (score / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("$score / 100", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (score >= 85) Color(0xFF2E7D32) else if (score >= 75) UNBlue else DiplomaticGold,
            strokeCap = StrokeCap.Round
        )
    }
}
