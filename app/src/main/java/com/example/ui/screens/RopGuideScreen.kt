package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.UNBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RopGuideScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rules of Procedure (RoP)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("rop_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "OFFICIAL MUN PARLIAMENTARY HANDBOOK",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            RopSectionCard(
                title = "1. Roll Call & Accreditation",
                icon = "📋",
                content = "During Roll Call, the Dais calls each country in alphabetical order.\n\n• 'Present': The delegation may vote In Favor, Against, or Abstain on substantive draft resolutions.\n• 'Present and Voting': The delegation MUST vote In Favor or Against on draft resolutions—no abstentions permitted."
            )

            RopSectionCard(
                title = "2. General Speakers' List (GSL)",
                icon = "🎙️",
                content = "The default mode of debate. Each speaker is given a set speaking time (e.g. 60-90s).\n\nYielding Remaining Time:\n• Yield to the Chair: Concludes remarks cleanly.\n• Yield to Questions / POIs: Floor opens to points of information from fellow delegates.\n• Yield to Another Delegate: Floor transfers to an ally (must be accepted beforehand)."
            )

            RopSectionCard(
                title = "3. Caucuses",
                icon = "🤝",
                content = "• Moderated Caucus: Formal structured discussion on a specific sub-issue. The proposing delegate specifies topic, total duration (e.g. 10 min), and speaking time per country (e.g. 60 sec).\n\n• Unmoderated Caucus: Informal suspension of formal rules for face-to-face negotiations, bloc formation, and resolution drafting."
            )

            RopSectionCard(
                title = "4. Points & Parliamentary Inquiries",
                icon = "⚖️",
                content = "• Point of Order: Raised when the Chair or a delegate violates the Rules of Procedure.\n• Point of Parliamentary Inquiry: Ask the Chair for clarification regarding procedure.\n• Point of Personal Privilege: Physical discomfort or audibility issues.\n\nNote: Points do not require a vote and take precedence over motions."
            )

            RopSectionCard(
                title = "5. Voting Procedure",
                icon = "🗳️",
                content = "• Procedural Votes: Voting on motions or caucuses. Simple majority required; all delegates vote (no abstentions).\n\n• Substantive Votes: Voting on Draft Resolutions and amendments. In the UN Security Council, resolutions require 9 affirmative votes and ZERO vetoes from the Permanent Five (USA, GBR, FRA, CHN, RUS)."
            )

            RopSectionCard(
                title = "6. How Age Calibrates Best Delegate Scoring",
                icon = "🏆",
                content = "• Junior (Under 15): Rewarded for courage, clear voice, basic decorum, and understanding national positions.\n• High School (15-18): Evaluated on third-person discipline (never say 'I/me'), problem-solution-action speeches, and time control.\n• University/Adult (19+): Strict demands on treaty citations, multilateral consensus diplomacy, strategic caucus maneuvering, and subtle counter-arguments."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RopSectionCard(
    title: String,
    icon: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
