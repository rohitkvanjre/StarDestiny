package com.example.stardestiny.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stardestiny.data.local.UserProfile
import com.example.stardestiny.model.MatchmakingResult
import com.example.stardestiny.viewmodel.HoroscopeViewModel.MatchmakingTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakingScreen(
    profiles: List<UserProfile>,
    selectedBride: UserProfile?,
    selectedGroom: UserProfile?,
    result: MatchmakingResult?,
    onBrideSelected: (UserProfile) -> Unit,
    onGroomSelected: (UserProfile) -> Unit,
    onCreateNew: (MatchmakingTarget) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ashta-Kuta Matchmaking", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Select Profiles to Match",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Selection Section
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileSelector(
                        label = "Bride",
                        selectedProfile = selectedBride,
                        profiles = profiles,
                        onSelected = onBrideSelected,
                        onCreateNew = { onCreateNew(MatchmakingTarget.BRIDE) },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileSelector(
                        label = "Groom",
                        selectedProfile = selectedGroom,
                        profiles = profiles,
                        onSelected = onGroomSelected,
                        onCreateNew = { onCreateNew(MatchmakingTarget.GROOM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Results Section
            if (result != null) {
                item {
                    ScoreCard(result)
                }

                item {
                    Text(
                        "Detailed Compatibility Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    KutaBreakdown(result)
                }

                item {
                    RecommendationCard(result.recommendations)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Select both Bride and Groom to see results.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSelector(
    label: String,
    selectedProfile: UserProfile?,
    profiles: List<UserProfile>,
    onSelected: (UserProfile) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (selectedProfile != null) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedProfile?.name ?: "Select $label",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedProfile != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    maxLines = 1
                )
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    onClick = {
                        onCreateNew()
                        expanded = false
                    }
                )
                HorizontalDivider()
                profiles.forEach { profile ->
                    DropdownMenuItem(
                        text = { Text(profile.name) },
                        onClick = {
                            onSelected(profile)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreCard(result: MatchmakingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Compatibility Score",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = result.totalScore.toInt().toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary // Saffron Orange / Gold
                    )
                )
                Text(
                    text = " / 36",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun KutaBreakdown(result: MatchmakingResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            KutaRow("Varna (Class)", result.varnaScore, 1.0)
            KutaRow("Vashya (Control)", result.vashyaScore, 2.0)
            KutaRow("Tara (Destiny)", result.taraScore, 3.0)
            KutaRow("Yoni (Sexual)", result.yoniScore, 4.0)
            KutaRow("Maitri (Friendship)", result.maitriScore, 5.0)
            KutaRow("Gana (Temperament)", result.ganaScore, 6.0)
            KutaRow("Bhakoot (Emotional)", result.bhakootScore, 7.0)
            KutaRow("Nadi (Biological)", result.nadiScore, 8.0)
        }
    }
}

@Composable
fun KutaRow(label: String, score: Double, max: Double) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            "${score.toInt()} / ${max.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (score == max) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RecommendationCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        ),
        border = border(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun border(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
