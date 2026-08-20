package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuickSampleQuery
import com.example.data.model.ResearchMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResearchInputPanel(
    queryText: String,
    onQueryChange: (String) -> Unit,
    contextText: String,
    onContextChange: (String) -> Unit,
    selectedMode: ResearchMode,
    onModeSelect: (ResearchMode) -> Unit,
    isLoading: Boolean,
    onConductResearch: () -> Unit,
    onSelectSample: (QuickSampleQuery) -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextInput by remember { mutableStateOf(false) }

    val sampleQueries = remember {
        listOf(
            QuickSampleQuery(
                title = "Solid-State vs Li-Ion",
                subtitle = "Comparative energy density & thermal safety",
                query = "Solid-State Batteries vs Lithium-Ion: Energy density, thermal runaway risks, cost per kWh, and commercial EV timelines",
                context = "Focus on chemical electrolyte mechanisms, dendrite formation, roll-to-roll manufacturing yield, and automotive adoption forecasts.",
                mode = ResearchMode.COMPARATIVE
            ),
            QuickSampleQuery(
                title = "Rust vs Go Concurrency",
                subtitle = "Ownership vs Goroutines benchmark",
                query = "Rust vs Go (Golang) memory safety models, concurrency paradigms, runtime latency, and systems vs cloud suitability",
                context = "Compare borrow checker zero-cost abstraction with Go garbage collection pause times and CSP channels.",
                mode = ResearchMode.COMPARATIVE
            ),
            QuickSampleQuery(
                title = "CRISPR-Cas9 Therapeutics",
                subtitle = "In-vivo vs ex-vivo clinical verification",
                query = "Current clinical status of in-vivo vs ex-vivo CRISPR-Cas9 gene therapies, off-target edit risks, and delivery vector constraints",
                context = "Include FDA-approved sickle cell treatments (Casgevy) and lipid nanoparticle delivery limits.",
                mode = ResearchMode.FACT_CHECK
            ),
            QuickSampleQuery(
                title = "Post-Quantum Cryptography",
                subtitle = "NIST algorithms & Shor's algorithm threat",
                query = "NIST Post-Quantum Cryptography (PQC) standards (ML-KEM, ML-DSA) migration timeline against Shor's algorithm on RSA-2048",
                context = "Analyze Kyber lattice-based key encapsulation versus classical ECC and RSA key length trade-offs.",
                mode = ResearchMode.DEEP_ANALYSIS
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("research_input_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Mode Selector Chips
            Text(
                text = "RESEARCH MODE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResearchMode.values().forEach { mode ->
                    val isSelected = mode == selectedMode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onModeSelect(mode) },
                        label = {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            val icon = when (mode) {
                                ResearchMode.DEEP_ANALYSIS -> Icons.Default.Search
                                ResearchMode.COMPARATIVE -> Icons.Default.Compare
                                ResearchMode.EXECUTIVE_BRIEF -> Icons.Default.Bolt
                                ResearchMode.FACT_CHECK -> Icons.Default.Verified
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("mode_chip_${mode.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Query Input Field
            OutlinedTextField(
                value = queryText,
                onValueChange = onQueryChange,
                label = { Text("Research Query / Topic / Hypothesis") },
                placeholder = { Text("e.g. Quantum vs Classical computing complexity classes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("research_query_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (queryText.isNotBlank() && !isLoading) {
                        onConductResearch()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Background Context Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showContextInput = !showContextInput }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = if (contextText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (contextText.isNotBlank()) "Background Reference Attached (${contextText.length} chars)" else "Add Background Data / Source Reference (Optional)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (contextText.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (contextText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (showContextInput) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = showContextInput) {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                    OutlinedTextField(
                        value = contextText,
                        onValueChange = onContextChange,
                        label = { Text("Reference Text / Context / Constraints") },
                        placeholder = { Text("Paste background articles, meeting transcripts, raw data, or specific constraints to synthesize...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("context_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Action Button
            Button(
                onClick = onConductResearch,
                enabled = queryText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_research_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Analyzing & Synthesizing...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conduct Research",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Topic Inspiration
            Text(
                text = "QUICK TOPIC TEMPLATES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sampleQueries.forEach { sample ->
                    SuggestionChip(
                        onClick = { onSelectSample(sample) },
                        label = {
                            Text(
                                text = sample.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("sample_chip_${sample.title.take(10).replace(" ", "_").lowercase()}")
                    )
                }
            }
        }
    }
}
