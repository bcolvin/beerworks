package com.bc.beerworks.ui.offflavordetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.ui.components.OffFlavorCard
import com.bc.beerworks.ui.components.OffFlavorCategoryBadge
import com.bc.beerworks.ui.ingredientdetail.AssociatedStylesSection
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailLoadingOrContent
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailScaffold
import com.bc.beerworks.ui.ingredientdetail.IngredientDetailViewModel

@Composable
fun OffFlavorDetailScreen(
    viewModel: IngredientDetailViewModel<OffFlavor>,
    onBackClick: () -> Unit,
    onOffFlavorClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit
) {
    val offFlavor by viewModel.item.collectAsState()
    val confusedWith by viewModel.substitutes.collectAsState()
    val permissibleStyles by viewModel.commonStyles.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    IngredientDetailLoadingOrContent(item = offFlavor) { current ->
        OffFlavorDetailContent(
            offFlavor = current,
            confusedWith = confusedWith,
            permissibleStyles = permissibleStyles,
            isFavorite = isFavorite,
            onBackClick = onBackClick,
            onOffFlavorClick = onOffFlavorClick,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = viewModel::toggleStyleFavorite,
            onToggleFavorite = viewModel::toggleFavorite
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OffFlavorDetailContent(
    offFlavor: OffFlavor,
    confusedWith: List<OffFlavor>,
    permissibleStyles: List<BeerStyle>,
    isFavorite: Boolean = false,
    onBackClick: () -> Unit,
    onOffFlavorClick: (String) -> Unit,
    onStyleClick: (BeerStyle) -> Unit,
    onToggleStyleFavorite: (BeerStyle) -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    IngredientDetailScaffold(
        title = offFlavor.name,
        isFavorite = isFavorite,
        onBackClick = onBackClick,
        onToggleFavorite = onToggleFavorite
    ) {
        // 1. Hero Header Card
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = offFlavor.emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = offFlavor.cardSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OffFlavorCategoryBadge(category = offFlavor.category)
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                offFlavor.disciplines.forEach { discipline ->
                    DisciplineTagChip(text = discipline)
                }
                PermissibilityBadge(permissibility = offFlavor.stylePermissibility)
            }

            if (offFlavor.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = offFlavor.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }

        // 2. Sensory Detection & Thresholds Card
        SectionCard {
            Text(
                text = "Sensory Detection & Thresholds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (offFlavor.detectedIn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detected In:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    offFlavor.detectedIn.forEach { modality ->
                        ModalityPill(text = modality)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (offFlavor.aromaPerception.isNotBlank()) {
                    ModalityLine(label = "Aroma", text = offFlavor.aromaPerception)
                }
                if (offFlavor.flavorPerception.isNotBlank()) {
                    ModalityLine(label = "Flavor", text = offFlavor.flavorPerception)
                }
                if (offFlavor.mouthfeelPerception.isNotBlank()) {
                    ModalityLine(label = "Mouthfeel", text = offFlavor.mouthfeelPerception)
                }
            }

            if (offFlavor.descriptors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    offFlavor.descriptors.forEach { descriptor ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = descriptor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecMetricBox(
                        label = "Perception Threshold",
                        value = offFlavor.perceptionThreshold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SpecMetricBox(
                        label = "Chemical Compound",
                        value = offFlavor.chemicalCompound,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpecMetricBox(
                        label = "Typical Beer Range",
                        value = offFlavor.typicalConcentration,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SpecMetricBox(
                        label = "Meilgaard Wheel #",
                        value = offFlavor.meilgaardCode.ifBlank { "N/A" },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3. Commonly Confused With & How to Tell Apart Card
        if (offFlavor.diagnosticContrasts.isNotEmpty() || confusedWith.isNotEmpty()) {
            SectionCard {
                Text(
                    text = "Commonly Confused With & How to Tell Apart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (offFlavor.diagnosticContrasts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BulletList(items = offFlavor.diagnosticContrasts)
                }

                if (confusedWith.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        confusedWith.forEach { lookalike ->
                            OffFlavorCard(
                                offFlavor = lookalike,
                                onClick = { onOffFlavorClick(lookalike.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Why It Happens (Typical Origins) Card
        if (offFlavor.typicalOrigins.isNotEmpty()) {
            SectionCard {
                Text(
                    text = "Why It Happens (Typical Origins)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                BulletList(items = offFlavor.typicalOrigins)
            }
        }

        // 5. How to Fix / Prevent It Card
        if (offFlavor.preventionTips.isNotEmpty()) {
            SectionCard {
                Text(
                    text = "How to Fix / Prevent It",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))
                BulletList(items = offFlavor.preventionTips)
            }
        }

        // 6. When Style-Appropriate & Permissible Styles
        if (offFlavor.whenAppropriate.isNotBlank()) {
            SectionCard {
                Text(
                    text = "When Style-Appropriate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = offFlavor.whenAppropriate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
        }

        AssociatedStylesSection(
            title = "Styles Where Permissible",
            styles = permissibleStyles,
            onStyleClick = onStyleClick,
            onToggleStyleFavorite = onToggleStyleFavorite
        )
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun ModalityLine(label: String, text: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            ) {
                append("$label: ")
            }
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            ) {
                append(text)
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ModalityPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SpecMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DisciplineTagChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun PermissibilityBadge(permissibility: String) {
    val isFault = permissibility == "Always a Fault"
    val bgColor = if (isFault) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
    val textColor = if (isFault) Color(0xFF991B1B) else Color(0xFF166534)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = permissibility,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = 11.sp
        )
    }
}
