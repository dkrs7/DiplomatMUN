package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Committee
import com.example.data.model.Country
import com.example.data.model.StandardCommittees
import com.example.data.model.StandardCountries
import com.example.ui.theme.DiplomaticGold
import com.example.ui.theme.UNBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSetupScreen(
    initialCountryCode: String,
    onBack: () -> Unit,
    onStartDebate: (committee: Committee, topic: String, country: Country, speechTime: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCommittee by remember { mutableStateOf(StandardCommittees.ALL.first()) }
    var selectedTopic by remember { mutableStateOf(selectedCommittee.agendaOptions.first()) }
    var customTopic by remember { mutableStateOf("") }
    var isCustomTopic by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(StandardCountries.find(initialCountryCode)) }
    var speechTimeSeconds by remember { mutableIntStateOf(selectedCommittee.defaultSpeakingTimeSeconds) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convene Committee Chamber", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("setup_back_btn")) {
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
                .padding(16.dp)
        ) {
            // Step 1: Select Committee
            Text(
                text = "1. SELECT COMMITTEE ORGAN",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StandardCommittees.ALL.forEach { committee ->
                    val isSelected = selectedCommittee.id == committee.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedCommittee = committee
                                selectedTopic = committee.agendaOptions.first()
                                speechTimeSeconds = committee.defaultSpeakingTimeSeconds
                                isCustomTopic = false
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) UNBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) UNBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) UNBlue else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = committee.acronym,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = committee.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = committee.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 2: Select Agenda Topic
            Text(
                text = "2. COMMITTEE AGENDA TOPIC",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            selectedCommittee.agendaOptions.forEach { topic ->
                val isSelected = !isCustomTopic && selectedTopic == topic
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            selectedTopic = topic
                            isCustomTopic = false
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSelected) "◉" else "○",
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = topic,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Custom Topic Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCustomTopic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .clickable { isCustomTopic = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCustomTopic) "◉" else "○",
                    color = if (isCustomTopic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Custom Agenda Topic...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            if (isCustomTopic) {
                OutlinedTextField(
                    value = customTopic,
                    onValueChange = { customTopic = it },
                    label = { Text("Enter Custom Resolution or Crisis Topic") },
                    modifier = Modifier.fillMaxWidth().testTag("custom_topic_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 3: Choose Your Delegation
            Text(
                text = "3. YOUR ASSIGNED COUNTRY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(StandardCountries.ALL) { country ->
                    val isSelected = selectedCountry.code == country.code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) UNBlue else MaterialTheme.colorScheme.surface)
                            .border(
                                1.5.dp,
                                if (isSelected) DiplomaticGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCountry = country }
                            .padding(12.dp)
                            .width(130.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(country.flagEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = country.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (country.isP5) {
                                Text(
                                    text = "P5 VETO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) DiplomaticGold else DiplomaticGold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Country Stance preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${selectedCountry.name} Diplomatic Stance:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedCountry.standardStance,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 4: Speaking Time per Turn
            Text(
                text = "4. GSL SPEAKING TIME LIMIT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(45, 60, 90, 120).forEach { seconds ->
                    val isSelected = speechTimeSeconds == seconds
                    FilterChip(
                        selected = isSelected,
                        onClick = { speechTimeSeconds = seconds },
                        label = { Text("${seconds}s") },
                        leadingIcon = {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Gavel In Button
            Button(
                onClick = {
                    val finalTopic = if (isCustomTopic && customTopic.isNotBlank()) customTopic.trim() else selectedTopic
                    onStartDebate(selectedCommittee, finalTopic, selectedCountry, speechTimeSeconds)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("gavel_in_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = DiplomaticGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GAVEL IN & COMMENCE DEBATE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
