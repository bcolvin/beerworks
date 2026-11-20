package com.bc.beerworks.ui.reviews

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.FourSectionScoresheet
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.model.ScoresheetSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScoresheetEditorScreen(
    viewModel: ScoresheetEditorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboard: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) {
        dismissKeyboard()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var quickPeekStyle by remember { mutableStateOf<BeerStyle?>(null) }
    var quickPeekOffFlavor by remember { mutableStateOf<OffFlavor?>(null) }
    var sectionInfoModal by remember { mutableStateOf<SectionGuidelineInfo?>(null) }
    var showScoringGuide by remember { mutableStateOf(false) }

    val handleBackAttempt: () -> Unit = {
        dismissKeyboard()
        if (uiState.hasUnsavedChanges) {
            showDiscardConfirm = true
        } else {
            onBack()
        }
    }

    BackHandler(
        enabled = uiState.hasUnsavedChanges &&
            !showDiscardConfirm &&
            !showDeleteConfirm &&
            !showDatePicker &&
            quickPeekStyle == null &&
            quickPeekOffFlavor == null &&
            sectionInfoModal == null &&
            !showScoringGuide
    ) {
        handleBackAttempt()
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard Unsaved Changes?") },
            text = { Text("You have unsaved changes to this scoresheet. Leave without saving?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardConfirm = false
                        onBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    val sheet = uiState.scoresheet
    val linkedStyle = uiState.linkedStyle
    val filteredStyles = uiState.filteredStyles
    val filteredOffFlavors = uiState.availableOffFlavors

    uiState.pendingDisciplineSwitch?.let { targetDiscipline ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDisciplineSwitch,
            title = { Text("Switch to ${targetDiscipline.displayName}?") },
            text = {
                Text(
                    "Switching scoresheets will reset your current style, scores, descriptors, and section notes. Entry name, maker, date, and bottle inspection will be kept."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDisciplineSwitch) {
                    Text("Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDisciplineSwitch) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Scoresheet?") },
            text = { Text("Delete \"${sheet.entryTitle.ifBlank { "this scoresheet" }}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.delete(onBack)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = sheet.evaluatedAtMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(viewModel::updateEvaluatedAt)
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    quickPeekStyle?.let { style ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { quickPeekStyle = null },
            sheetState = sheetState
        ) {
            StyleQuickPeekSheet(style = style)
        }
    }

    quickPeekOffFlavor?.let { flavor ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { quickPeekOffFlavor = null },
            sheetState = sheetState
        ) {
            OffFlavorQuickPeekSheet(offFlavor = flavor)
        }
    }

    sectionInfoModal?.let { info ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { sectionInfoModal = null },
            sheetState = sheetState
        ) {
            SectionInfoSheet(info = info)
        }
    }

    if (showScoringGuide) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showScoringGuide = false },
            sheetState = sheetState
        ) {
            BjcpScoringGuideSheet(activeTier = sheet.scoringTier)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isNew) {
                            "New ${uiState.selectedDiscipline.shortLabel} Scoresheet"
                        } else {
                            "Edit ${sheet.discipline.shortLabel} Scoresheet"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackAttempt) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Scoresheet"
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.save(onBack) },
                        enabled = uiState.canSave
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
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
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures { dismissKeyboard() }
                }
        ) {
            // Scroll-locked Total Score Bubble anchored at the top
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 2.dp
            ) {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sheet.totalScore} / 50",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${sheet.scoringTier.label} (${sheet.scoringTier.rangeText})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = {
                                        dismissKeyboard()
                                        showScoringGuide = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Scoring Guide Info",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    // Dismiss the IME only on real vertical pointer drags, not ContentInViewNode's
                    // BringIntoView scroll (which reports NestedScrollSource.UserInput in Compose 1.7).
                    .pointerInput(focusManager, keyboardController) {
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            val startPos = down.position
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                val dx = abs(change.position.x - startPos.x)
                                val dy = abs(change.position.y - startPos.y)
                                if (dy > viewConfiguration.touchSlop && dy > dx) {
                                    dismissKeyboard()
                                    break
                                }
                            }
                        }
                    }
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isNew) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScoresheetDiscipline.entries.forEachIndexed { index, discipline ->
                            SegmentedButton(
                                selected = uiState.selectedDiscipline == discipline,
                                onClick = {
                                    dismissKeyboard()
                                    viewModel.updateDiscipline(discipline)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ScoresheetDiscipline.entries.size
                                )
                            ) {
                                Text(
                                    text = "${discipline.emoji} ${discipline.shortLabel}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                val formattedDate = remember(sheet.evaluatedAtMillis) {
                    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(sheet.evaluatedAtMillis))
                }
                val discipline = sheet.discipline
                val isFourSection = sheet is FourSectionScoresheet
                val styleLabel = "${discipline.displayName} Style"

                // Entry Metadata Card
                SectionCard(
                    title = "Entry Details",
                    headerTrailing = {
                        OutlinedButton(
                            onClick = {
                                dismissKeyboard()
                                showDatePicker = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date Evaluated",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = sheet.entryTitle,
                        onValueChange = viewModel::updateEntryTitle,
                        label = { Text(discipline.entryFieldLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sheet.brewer,
                        onValueChange = viewModel::updateBrewer,
                        label = { Text(discipline.makerFieldLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Typable BJCP Style Dropdown + Book Quick-Peek Icon
                    var styleDropdownExpanded by remember(sheet.discipline) { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = styleDropdownExpanded && filteredStyles.isNotEmpty(),
                            onExpandedChange = { styleDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = uiState.styleQuery,
                                onValueChange = {
                                    viewModel.onStyleQueryChange(it)
                                    styleDropdownExpanded = true
                                },
                                label = { Text(styleLabel) },
                                singleLine = true,
                                trailingIcon = if (uiState.styleQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { viewModel.onStyleQueryChange("") }) {
                                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear style")
                                        }
                                    }
                                } else {
                                    null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = styleDropdownExpanded && filteredStyles.isNotEmpty(),
                                onDismissRequest = { styleDropdownExpanded = false }
                            ) {
                                filteredStyles.forEach { style ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${style.number} • ${style.name}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectBjcpStyle(style)
                                            styleDropdownExpanded = false
                                            dismissKeyboard()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedIconButton(
                            onClick = {
                                linkedStyle?.let { style ->
                                    dismissKeyboard()
                                    quickPeekStyle = style
                                }
                            },
                            enabled = linkedStyle != null,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = linkedStyle?.let {
                                    "View ${it.number} ${it.name} Guidelines"
                                } ?: "Select a BJCP style to view guidelines",
                                tint = if (linkedStyle != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }

                    if (sheet is FourSectionScoresheet) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        AdditionalInfoSection(
                            scoresheet = sheet,
                            isExistingWithData = !uiState.isNew && sheet.hasAdditionalInfo,
                            specialIngredientsLabel = discipline.specialIngredientsLabel,
                            onCycleCarbonation = {
                                dismissKeyboard()
                                viewModel.cycleDeclaredCarbonation()
                            },
                            onCycleSweetness = {
                                dismissKeyboard()
                                viewModel.cycleDeclaredSweetness()
                            },
                            onCycleStrength = {
                                dismissKeyboard()
                                viewModel.cycleDeclaredStrength()
                            },
                            onSpecialIngredientsChange = viewModel::updateSpecialIngredients
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                dismissKeyboard()
                                viewModel.updateBottleInspectionOk(!sheet.bottleInspectionOk)
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bottle Inspection OK",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Appropriate fill, cap, and absence of rings/sediment issues.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(
                            checked = sheet.bottleInspectionOk,
                            onCheckedChange = {
                                dismissKeyboard()
                                viewModel.updateBottleInspectionOk(it)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sheet.bottleInspectionNotes,
                        onValueChange = viewModel::updateBottleInspectionNotes,
                        label = { Text("Bottle Inspection Notes (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Individual BJCP Scoring Bubble Sections with Info Buttons
                ScoringSectionCard(
                    title = "Aroma",
                    score = sheet.aromaScore,
                    maxScore = discipline.aromaMax,
                    hasLinkedStyle = linkedStyle != null,
                    onScoreChange = viewModel::updateAromaScore,
                    onInfoClick = {
                        sectionInfoModal = SectionGuidelineInfo(
                            sectionTitle = "Aroma",
                            linkedStyle = linkedStyle,
                            guidelineText = linkedStyle?.aroma.orEmpty(),
                            fallbackTip = discipline.sectionFallbackTip(ScoresheetSection.AROMA),
                            styleLabel = styleLabel
                        )
                    },
                    descriptorSection = ScoresheetSection.AROMA,
                    discipline = discipline,
                    descriptorValues = sheet.descriptorValues,
                    onCycleDescriptor = viewModel::cycleDescriptor,
                    notesLabel = "Aroma Notes (Optional)",
                    notesValue = sheet.aromaNotes,
                    onNotesChange = viewModel::updateAromaNotes
                )

                ScoringSectionCard(
                    title = "Appearance",
                    score = sheet.appearanceScore,
                    maxScore = discipline.appearanceMax,
                    hasLinkedStyle = linkedStyle != null,
                    onScoreChange = viewModel::updateAppearanceScore,
                    onInfoClick = {
                        sectionInfoModal = SectionGuidelineInfo(
                            sectionTitle = "Appearance",
                            linkedStyle = linkedStyle,
                            guidelineText = linkedStyle?.appearance.orEmpty(),
                            fallbackTip = discipline.sectionFallbackTip(ScoresheetSection.APPEARANCE),
                            styleLabel = styleLabel
                        )
                    },
                    descriptorSection = ScoresheetSection.APPEARANCE,
                    discipline = discipline,
                    descriptorValues = sheet.descriptorValues,
                    onCycleDescriptor = viewModel::cycleDescriptor,
                    notesLabel = "Appearance Notes (Optional)",
                    notesValue = sheet.appearanceNotes,
                    onNotesChange = viewModel::updateAppearanceNotes
                )

                ScoringSectionCard(
                    title = "Flavor",
                    score = sheet.flavorScore,
                    maxScore = discipline.flavorMax,
                    hasLinkedStyle = linkedStyle != null,
                    onScoreChange = viewModel::updateFlavorScore,
                    onInfoClick = {
                        val flavorText = if (isFourSection) {
                            listOf(linkedStyle?.flavor, linkedStyle?.mouthfeel)
                                .filter { !it.isNullOrBlank() }
                                .joinToString("\n\n")
                        } else {
                            linkedStyle?.flavor.orEmpty()
                        }
                        sectionInfoModal = SectionGuidelineInfo(
                            sectionTitle = "Flavor",
                            linkedStyle = linkedStyle,
                            guidelineText = flavorText,
                            fallbackTip = discipline.sectionFallbackTip(ScoresheetSection.FLAVOR),
                            styleLabel = styleLabel
                        )
                    },
                    descriptorSection = ScoresheetSection.FLAVOR,
                    discipline = discipline,
                    descriptorValues = sheet.descriptorValues,
                    onCycleDescriptor = viewModel::cycleDescriptor,
                    notesLabel = "Flavor Notes (Optional)",
                    notesValue = sheet.flavorNotes,
                    onNotesChange = viewModel::updateFlavorNotes
                )

                if (sheet is BeerScoresheet) {
                    ScoringSectionCard(
                        title = "Mouthfeel",
                        score = sheet.mouthfeelScore,
                        maxScore = discipline.mouthfeelMax,
                        hasLinkedStyle = linkedStyle != null,
                        onScoreChange = viewModel::updateMouthfeelScore,
                        onInfoClick = {
                            sectionInfoModal = SectionGuidelineInfo(
                                sectionTitle = "Mouthfeel",
                                linkedStyle = linkedStyle,
                                guidelineText = linkedStyle?.mouthfeel.orEmpty(),
                                fallbackTip = discipline.sectionFallbackTip(ScoresheetSection.MOUTHFEEL),
                                styleLabel = styleLabel
                            )
                        },
                        descriptorSection = ScoresheetSection.MOUTHFEEL,
                        discipline = discipline,
                        descriptorValues = sheet.descriptorValues,
                        onCycleDescriptor = viewModel::cycleDescriptor,
                        notesLabel = "Mouthfeel Notes (Optional)",
                        notesValue = sheet.mouthfeelNotes,
                        onNotesChange = viewModel::updateMouthfeelNotes
                    )
                }

                ScoringSectionCard(
                    title = "Overall Impression",
                    score = sheet.overallScore,
                    maxScore = discipline.overallMax,
                    hasLinkedStyle = linkedStyle != null,
                    onScoreChange = viewModel::updateOverallScore,
                    onInfoClick = {
                        sectionInfoModal = SectionGuidelineInfo(
                            sectionTitle = "Overall Impression",
                            linkedStyle = linkedStyle,
                            guidelineText = linkedStyle?.overallImpression.orEmpty(),
                            fallbackTip = discipline.sectionFallbackTip(null),
                            styleLabel = styleLabel
                        )
                    },
                    descriptorSection = null,
                    discipline = discipline,
                    descriptorValues = sheet.descriptorValues,
                    onCycleDescriptor = viewModel::cycleDescriptor,
                    notesLabel = "Overall Comments (Optional)",
                    notesValue = sheet.overallNotes,
                    onNotesChange = viewModel::updateOverallNotes,
                    notesMinLines = 3,
                    notesMaxLines = 6
                )

                // Overall Impressions (0–10) Card (no section header) — above Off-Flavors
                SectionCard {
                    ScoreSliderRow(
                        label = "Stylistic Accuracy",
                        score = sheet.stylisticAccuracy,
                        maxScore = 10,
                        onScoreChange = {
                            dismissKeyboard()
                            viewModel.updateStylisticAccuracy(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreSliderRow(
                        label = "Technical Merit",
                        score = sheet.technicalMerit,
                        maxScore = 10,
                        onScoreChange = {
                            dismissKeyboard()
                            viewModel.updateTechnicalMerit(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ScoreSliderRow(
                        label = "Intangibles",
                        score = sheet.intangibles,
                        maxScore = 10,
                        onScoreChange = {
                            dismissKeyboard()
                            viewModel.updateIntangibles(it)
                        }
                    )
                }

                // Off-Flavors / Faults Card (4-state chips + long-press quick-peek)
                if (filteredOffFlavors.isNotEmpty()) {
                    SectionCard(
                        title = "Off-Flavors & Faults",
                        subtitle = "Tap to cycle Low / Med / High • Hold for details"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredOffFlavors.forEach { flavor ->
                                IntensityCycleChip(
                                    label = flavor.name,
                                    level = sheet.offFlavorLevels[flavor.id],
                                    isFault = true,
                                    onClick = {
                                        dismissKeyboard()
                                        viewModel.cycleOffFlavor(flavor.id)
                                    },
                                    onLongClick = {
                                        dismissKeyboard()
                                        quickPeekOffFlavor = flavor
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
