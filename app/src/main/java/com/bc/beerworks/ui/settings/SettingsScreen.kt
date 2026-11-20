package com.bc.beerworks.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.TemperatureUnit
import com.bc.beerworks.ui.components.getDisciplineEmoji
import com.bc.beerworks.ui.theme.BeerworksTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onMenuClick: () -> Unit,
    onBecomeJudgeClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedGuideline by viewModel.selectedGuideline.collectAsState()
    val temperatureUnit by viewModel.temperatureUnit.collectAsState()
    val confirmFavoriteRemoval by viewModel.confirmFavoriteRemoval.collectAsState()

    SettingsContent(
        selectedGuideline = selectedGuideline,
        onGuidelineChange = viewModel::onGuidelineChange,
        temperatureUnit = temperatureUnit,
        onTemperatureUnitChange = viewModel::onTemperatureUnitChange,
        confirmFavoriteRemoval = confirmFavoriteRemoval,
        onConfirmFavoriteRemovalChange = viewModel::onConfirmFavoriteRemovalChange,
        onMenuClick = onMenuClick,
        onBecomeJudgeClick = onBecomeJudgeClick,
        onSourceCodeClick = { context.openExternalUrl(BjcpJudgeLinks.BEERWORKS_GITHUB_URL) },
        onOpenUrl = context::openExternalUrl
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    selectedGuideline: GuidelineType = GuidelineType.BJCP_2021,
    onGuidelineChange: (GuidelineType) -> Unit = {},
    temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit = {},
    confirmFavoriteRemoval: Boolean = true,
    onConfirmFavoriteRemovalChange: (Boolean) -> Unit = {},
    onMenuClick: () -> Unit,
    onBecomeJudgeClick: () -> Unit = {},
    onSourceCodeClick: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {}
) {
    var showAttributionsSheet by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Active Style Guideline Card
            var guidelineExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Active Style Guideline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Choose the reference standard for styles and competition flights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = guidelineExpanded,
                        onExpandedChange = { guidelineExpanded = !guidelineExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${getDisciplineEmoji(selectedGuideline)} ${selectedGuideline.displayName}",
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = guidelineExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = guidelineExpanded,
                            onDismissRequest = { guidelineExpanded = false }
                        ) {
                            GuidelineType.entries.forEach { guideline ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${getDisciplineEmoji(guideline)} ${guideline.displayName}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    trailingIcon = if (guideline == selectedGuideline) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    onClick = {
                                        guidelineExpanded = false
                                        onGuidelineChange(guideline)
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature Unit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Temperature Unit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Display fermentation temperatures in Fahrenheit or Celsius.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = temperatureUnit == TemperatureUnit.FAHRENHEIT,
                            onClick = { onTemperatureUnitChange(TemperatureUnit.FAHRENHEIT) },
                            label = { Text("°F") }
                        )
                        FilterChip(
                            selected = temperatureUnit == TemperatureUnit.CELSIUS,
                            onClick = { onTemperatureUnitChange(TemperatureUnit.CELSIUS) },
                            label = { Text("°C") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Favorite Removal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Confirm Favorite Removal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ask for confirmation before removing a swiped item on the Favorites screen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = confirmFavoriteRemoval,
                        onCheckedChange = onConfirmFavoriteRemovalChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interested in Becoming a Judge Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBecomeJudgeClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Interested in becoming a judge?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Explore the BJCP certification pathway, Beer, Mead, and Cider exams, study tools, and upcoming competitions to judge or steward.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Open Source / GitHub Repository Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSourceCodeClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Have an idea or want to see how the App is made?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Explore the Zymurgist Desk source code, share ideas, or report issues on GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Opens in browser",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Licenses & Attributions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAttributionsSheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Licenses & Attributions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "View BJCP, Brewers Association 2026, sensory fault guide credits, GPLv3 license, and offline privacy details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showAttributionsSheet) {
        LicensesAndAttributionsSheet(
            onDismiss = { showAttributionsSheet = false },
            onOpenUrl = onOpenUrl
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesAndAttributionsSheet(
    onDismiss: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Licenses & Attributions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            AttributionSection(
                title = "BJCP Style Guidelines",
                body = "BJCP Beer Style Guidelines (Copyright 2021, Beer Judge Certification Program, Inc.), BJCP Mead Guidelines, and BJCP Cider Guidelines are used with permission of the BJCP. The most current guidelines can be found on www.bjcp.org."
            )

            AttributionSection(
                title = "Brewers Association Style Guidelines",
                body = "Based on Brewers Association 2026 Beer Style Guidelines with changes. Used with permission of Brewers Association. Provided free of charge."
            )

            AttributionSection(
                title = "Off-Flavors & Sensory Faults",
                body = "Sensory fault profiles and diagnostic contrasts incorporate and adapt concepts from Thomas Barnes' Complete Beer Fault Guide v1.4 (© Thomas Barnes, 2011–2013, personal/non-profit use)."
            )

            AttributionSection(
                title = "Open Source License (GPLv3)",
                body = "Zymurgist Desk application source code is licensed under the GNU General Public License, Version 3.0 (GPLv3). Android Jetpack, Room, DataStore, Compose, Coroutines, and Gson libraries are licensed under Apache 2.0."
            )

            AttributionSection(
                title = "Offline Privacy",
                body = "Zymurgist Desk requests zero Android permissions and collects, stores remotely, or shares no personal data. All favorites, preferences, and scoresheets stay on your device."
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            AttributionLinkRow(
                label = "Official BJCP Guidelines (www.bjcp.org)",
                onClick = { onOpenUrl(BjcpJudgeLinks.BJCP_MAIN_WEBSITE_URL) }
            )
            AttributionLinkRow(
                label = "Source Code & GPLv3 License on GitHub",
                onClick = { onOpenUrl(BjcpJudgeLinks.BEERWORKS_GITHUB_URL) }
            )
            AttributionLinkRow(
                label = "Privacy Policy (PRIVACY.md)",
                onClick = { onOpenUrl(BjcpJudgeLinks.PRIVACY_POLICY_URL) }
            )
        }
    }
}

@Composable
private fun AttributionSection(
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttributionLinkRow(
    label: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Opens in browser",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsContentPreview() {
    BeerworksTheme {
        SettingsContent(
            onMenuClick = {}
        )
    }
}
