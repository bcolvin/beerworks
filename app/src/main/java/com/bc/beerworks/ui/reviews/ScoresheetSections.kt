package com.bc.beerworks.ui.reviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.FourSectionScoresheet
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.model.ScoresheetSection
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdditionalInfoSection(
    scoresheet: FourSectionScoresheet,
    isExistingWithData: Boolean,
    specialIngredientsLabel: String,
    onCycleCarbonation: () -> Unit,
    onCycleSweetness: () -> Unit,
    onCycleStrength: () -> Unit,
    onSpecialIngredientsChange: (String) -> Unit
) {
    var expanded by remember(scoresheet.id) { mutableStateOf(isExistingWithData) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    expanded = !expanded
                }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Optional declared carbonation, sweetness, strength, and special ingredients.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse Additional Information" else "Expand Additional Information",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DeclaredAttributeChip(
                        label = "Carbonation",
                        value = scoresheet.declaredCarbonation,
                        onClick = onCycleCarbonation
                    )
                    DeclaredAttributeChip(
                        label = "Sweetness",
                        value = scoresheet.declaredSweetness,
                        onClick = onCycleSweetness
                    )
                    DeclaredAttributeChip(
                        label = "Strength",
                        value = scoresheet.declaredStrength,
                        onClick = onCycleStrength
                    )
                }

                OutlinedTextField(
                    value = scoresheet.specialIngredients,
                    onValueChange = onSpecialIngredientsChange,
                    label = { Text(specialIngredientsLabel) },
                    minLines = 1,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DeclaredAttributeChip(
    label: String,
    value: String?,
    onClick: () -> Unit
) {
    CycleChipSurface(
        text = if (value == null) label else "$label • $value",
        isActive = value != null,
        containerColor = if (value == null) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (value == null) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        onClick = onClick
    )
}

@Composable
internal fun ScoringSectionCard(
    title: String,
    score: Int,
    maxScore: Int,
    hasLinkedStyle: Boolean,
    onScoreChange: (Int) -> Unit,
    onInfoClick: () -> Unit,
    descriptorSection: ScoresheetSection?,
    discipline: ScoresheetDiscipline,
    descriptorValues: Map<String, String>,
    onCycleDescriptor: (ScoresheetDescriptor) -> Unit,
    notesLabel: String,
    notesValue: String,
    onNotesChange: (String) -> Unit,
    notesMinLines: Int = 2,
    notesMaxLines: Int = 4
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboard: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            dismissKeyboard()
                            onInfoClick()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "$title Guidelines Info",
                            tint = if (hasLinkedStyle) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "$score / $maxScore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Slider(
                value = score.toFloat(),
                onValueChange = {
                    dismissKeyboard()
                    onScoreChange(it.roundToInt())
                },
                valueRange = 0f..maxScore.toFloat(),
                steps = (maxScore - 1).coerceAtLeast(0)
            )

            if (descriptorSection != null) {
                SectionDescriptorChips(
                    section = descriptorSection,
                    discipline = discipline,
                    descriptorValues = descriptorValues,
                    onCycleDescriptor = {
                        dismissKeyboard()
                        onCycleDescriptor(it)
                    }
                )
            }

            SectionNotesField(
                label = notesLabel,
                value = notesValue,
                onValueChange = onNotesChange,
                minLines = notesMinLines,
                maxLines = notesMaxLines
            )
        }
    }
}

@Composable
internal fun SectionCard(
    title: String? = null,
    subtitle: String? = null,
    headerTrailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
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
            if (title != null || headerTrailing != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    headerTrailing?.invoke()
                }
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            content()
        }
    }
}

@Composable
internal fun ScoreSliderRow(
    label: String,
    score: Int,
    maxScore: Int,
    minScore: Int = 0,
    onScoreChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$score / $maxScore",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        val stepCount = (maxScore - minScore - 1).coerceAtLeast(0)
        Slider(
            value = score.toFloat(),
            onValueChange = { onScoreChange(it.roundToInt()) },
            valueRange = minScore.toFloat()..maxScore.toFloat(),
            steps = stepCount
        )
    }
}

@Composable
private fun SectionNotesField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 2,
    maxLines: Int = 4
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
        maxLines = maxLines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SectionDescriptorChips(
    section: ScoresheetSection,
    discipline: ScoresheetDiscipline,
    descriptorValues: Map<String, String>,
    onCycleDescriptor: (ScoresheetDescriptor) -> Unit
) {
    val descriptors = remember(section, discipline) { ScoresheetDescriptor.forSection(section, discipline) }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
    ) {
        descriptors.forEach { descriptor ->
            val currentValue = descriptorValues[descriptor.key]
            CycleChipSurface(
                text = if (currentValue == null) descriptor.label else "${descriptor.label} • $currentValue",
                isActive = currentValue != null,
                containerColor = if (currentValue == null) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (currentValue == null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                },
                onClick = { onCycleDescriptor(descriptor) }
            )
        }
    }
}

@Composable
internal fun IntensityCycleChip(
    label: String,
    level: IntensityLevel?,
    isFault: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val containerColor = when (level) {
        null -> MaterialTheme.colorScheme.surface
        IntensityLevel.LOW -> if (isFault) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
        IntensityLevel.MEDIUM -> if (isFault) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primaryContainer
        IntensityLevel.HIGH -> if (isFault) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
    }
    val contentColor = when (level) {
        null -> MaterialTheme.colorScheme.onSurface
        IntensityLevel.LOW -> if (isFault) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
        IntensityLevel.MEDIUM -> if (isFault) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
        IntensityLevel.HIGH -> if (isFault) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
    }

    CycleChipSurface(
        text = if (level == null) label else "$label • ${level.shortLabel}",
        isActive = level != null,
        containerColor = containerColor,
        contentColor = contentColor,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CycleChipSurface(
    text: String,
    isActive: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val borderColor = if (isActive) {
        contentColor.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }
    val shape = RoundedCornerShape(10.dp)
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
