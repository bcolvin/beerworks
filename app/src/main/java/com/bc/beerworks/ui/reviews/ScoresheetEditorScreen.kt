package com.bc.beerworks.ui.reviews

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bc.beerworks.data.model.BeerScoresheet
import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.BjcpScoringTier
import com.bc.beerworks.data.model.CiderScoresheet
import com.bc.beerworks.data.model.FourSectionScoresheet
import com.bc.beerworks.data.model.IntensityLevel
import com.bc.beerworks.data.model.MeadScoresheet
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.ScoresheetDescriptor
import com.bc.beerworks.data.model.ScoresheetDiscipline
import com.bc.beerworks.data.model.ScoresheetSection
import com.bc.beerworks.data.model.isCider
import com.bc.beerworks.data.model.isMead
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScoresheetEditorScreen(
    viewModel: ScoresheetEditorViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bjcpBeerStyles by viewModel.bjcpBeerStyles.collectAsState()
    val bjcpMeadStyles by viewModel.bjcpMeadStyles.collectAsState()
    val bjcpCiderStyles by viewModel.bjcpCiderStyles.collectAsState()
    val offFlavors by viewModel.offFlavors.collectAsState()
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

    // Only dismiss the IME on genuine user scroll gestures, not Compose's programmatic BringIntoView
    // scroll triggered when .imePadding() shrinks the viewport as the keyboard opens.
    val nestedScrollConnection = remember(focusManager, keyboardController) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    dismissKeyboard()
                }
                return Offset.Zero
            }
        }
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
    val activeStyles = remember(sheet.discipline, bjcpBeerStyles, bjcpMeadStyles, bjcpCiderStyles) {
        when (sheet) {
            is BeerScoresheet -> bjcpBeerStyles
            is MeadScoresheet -> bjcpMeadStyles
            is CiderScoresheet -> bjcpCiderStyles
        }
    }
    val linkedStyle = remember(sheet.styleId, activeStyles) {
        sheet.styleId?.let { id -> activeStyles.firstOrNull { it.id == id } }
    }
    val filteredOffFlavors = remember(offFlavors, sheet.discipline) {
        offFlavors.filter { it.disciplines.contains(sheet.discipline.shortLabel) }
    }

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
            if (uiState.selectedDiscipline.isSupported) {
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
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection)
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
                val isFourSection = sheet is FourSectionScoresheet
                val entryLabel = when (sheet) {
                    is BeerScoresheet -> "Entry # or Beer Name *"
                    is MeadScoresheet -> "Entry # or Mead Name *"
                    is CiderScoresheet -> "Entry # or Cider Name *"
                }
                val makerLabel = when (sheet) {
                    is BeerScoresheet -> "Brewer / Brewery (Optional)"
                    is MeadScoresheet -> "Meadmaker / Meadery (Optional)"
                    is CiderScoresheet -> "Cidermaker / Cidery (Optional)"
                }
                val styleLabel = "${sheet.discipline.displayName} Style"

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
                        label = { Text(entryLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sheet.brewer,
                        onValueChange = viewModel::updateBrewer,
                        label = { Text(makerLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Typable BJCP Style Dropdown + Book Quick-Peek Icon
                    var styleDropdownExpanded by remember(sheet.discipline) { mutableStateOf(false) }
                    val filteredStyles = remember(uiState.styleQuery, activeStyles) {
                        val q = uiState.styleQuery.trim()
                        if (q.isEmpty()) {
                            activeStyles.take(25)
                        } else {
                            activeStyles.filter { style ->
                                style.name.contains(q, ignoreCase = true) ||
                                    style.number.contains(q, ignoreCase = true) ||
                                    style.category.contains(q, ignoreCase = true)
                            }.take(25)
                        }
                    }

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
                            specialIngredientsLabel = if (sheet is CiderScoresheet) {
                                "Special Ingredients / Varietals (Optional)"
                            } else {
                                "Special Ingredients (Optional)"
                            },
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

            val aromaMax = if (isFourSection) 10 else 12
            val appearanceMax = if (isFourSection) 6 else 3
            val flavorMax = if (isFourSection) 24 else 20

            // Individual BJCP Scoring Bubble Sections with Info Buttons
            ScoringSectionCard(
                title = "Aroma",
                score = sheet.aromaScore,
                maxScore = aromaMax,
                hasLinkedStyle = linkedStyle != null,
                onScoreChange = viewModel::updateAromaScore,
                onInfoClick = {
                    sectionInfoModal = SectionGuidelineInfo(
                        sectionTitle = "Aroma",
                        maxScore = aromaMax,
                        linkedStyle = linkedStyle,
                        guidelineText = linkedStyle?.aroma.orEmpty(),
                        fallbackTip = when (sheet) {
                            is BeerScoresheet -> "Evaluate malt, hops, fermentation esters/phenols, and any other aromatic components against the target style."
                            is MeadScoresheet -> "Evaluate honey aromatics, esters/floral notes, special ingredients, fermentation character, and alcohol presence against the target style."
                            is CiderScoresheet -> "Evaluate apple/pear fruit aromatics, esters, special ingredients, oak/wood, fermentation character, and alcohol presence against the target style."
                        },
                        styleLabel = styleLabel
                    )
                },
                descriptorSection = ScoresheetSection.AROMA,
                discipline = sheet.discipline,
                descriptorValues = sheet.descriptorValues,
                onCycleDescriptor = viewModel::cycleDescriptor,
                notesLabel = "Aroma Notes (Optional)",
                notesValue = sheet.aromaNotes,
                onNotesChange = viewModel::updateAromaNotes
            )

            ScoringSectionCard(
                title = "Appearance",
                score = sheet.appearanceScore,
                maxScore = appearanceMax,
                hasLinkedStyle = linkedStyle != null,
                onScoreChange = viewModel::updateAppearanceScore,
                onInfoClick = {
                    sectionInfoModal = SectionGuidelineInfo(
                        sectionTitle = "Appearance",
                        maxScore = appearanceMax,
                        linkedStyle = linkedStyle,
                        guidelineText = linkedStyle?.appearance.orEmpty(),
                        fallbackTip = if (isFourSection) {
                            "Evaluate color, clarity, legs/viscosity, and carbonation against the target style."
                        } else {
                            "Evaluate color, clarity, head size, head retention, and head color against the target style."
                        },
                        styleLabel = styleLabel
                    )
                },
                descriptorSection = ScoresheetSection.APPEARANCE,
                discipline = sheet.discipline,
                descriptorValues = sheet.descriptorValues,
                onCycleDescriptor = viewModel::cycleDescriptor,
                notesLabel = "Appearance Notes (Optional)",
                notesValue = sheet.appearanceNotes,
                onNotesChange = viewModel::updateAppearanceNotes
            )

            ScoringSectionCard(
                title = "Flavor",
                score = sheet.flavorScore,
                maxScore = flavorMax,
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
                        maxScore = flavorMax,
                        linkedStyle = linkedStyle,
                        guidelineText = flavorText,
                        fallbackTip = when (sheet) {
                            is BeerScoresheet -> "Evaluate malt, hops, bitterness, fermentation character, balance, and finish/aftertaste against the target style."
                            is MeadScoresheet -> "Evaluate honey expression, sweetness, acidity, tannin, body, carbonation, alcohol warmth, balance, and finish against the target style."
                            is CiderScoresheet -> "Evaluate apple/pear character, sweetness, acidity, tannin/astringency, body, carbonation, alcohol warmth, balance, and finish against the target style."
                        },
                        styleLabel = styleLabel
                    )
                },
                descriptorSection = ScoresheetSection.FLAVOR,
                discipline = sheet.discipline,
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
                    maxScore = 5,
                    hasLinkedStyle = linkedStyle != null,
                    onScoreChange = viewModel::updateMouthfeelScore,
                    onInfoClick = {
                        sectionInfoModal = SectionGuidelineInfo(
                            sectionTitle = "Mouthfeel",
                            maxScore = 5,
                            linkedStyle = linkedStyle,
                            guidelineText = linkedStyle?.mouthfeel.orEmpty(),
                            fallbackTip = "Evaluate body, carbonation, alcoholic warmth, creaminess, and astringency against the target style.",
                            styleLabel = styleLabel
                        )
                    },
                    descriptorSection = ScoresheetSection.MOUTHFEEL,
                    discipline = sheet.discipline,
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
                maxScore = 10,
                hasLinkedStyle = linkedStyle != null,
                onScoreChange = viewModel::updateOverallScore,
                onInfoClick = {
                    sectionInfoModal = SectionGuidelineInfo(
                        sectionTitle = "Overall Impression",
                        maxScore = 10,
                        linkedStyle = linkedStyle,
                        guidelineText = linkedStyle?.overallImpression.orEmpty(),
                        fallbackTip = when (sheet) {
                            is BeerScoresheet -> "Summarize drinking pleasure, stylistic accuracy, technical merit, and actionable feedback for the brewer."
                            is MeadScoresheet -> "Summarize drinking pleasure, stylistic accuracy, technical merit, and actionable feedback for the meadmaker."
                            is CiderScoresheet -> "Summarize drinking pleasure, stylistic accuracy, technical merit, and actionable feedback for the cidermaker."
                        },
                        styleLabel = styleLabel
                    )
                },
                descriptorSection = null,
                discipline = sheet.discipline,
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
                    minScore = 0,
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
                    minScore = 0,
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
                    minScore = 0,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdditionalInfoSection(
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

@Immutable
private data class SectionGuidelineInfo(
    val sectionTitle: String,
    val maxScore: Int,
    val linkedStyle: BeerStyle?,
    val guidelineText: String,
    val fallbackTip: String,
    val styleLabel: String = "BJCP Beer Style"
)

@Composable
private fun ScoringSectionCard(
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
private fun SectionCard(
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
private fun ScoreSliderRow(
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
private fun IntensityCycleChip(
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
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
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

@Composable
private fun StyleQuickPeekSheet(style: BeerStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${style.number} • ${style.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = style.category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        if (style.overallImpression.isNotBlank()) {
            Text(
                text = style.overallImpression,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (style.guidelineType.isMead || style.guidelineType.isCider) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalStat("OG", formatRange(style.ogMin, style.ogMax, "%.3f"))
                VitalStat("FG", formatRange(style.fgMin, style.fgMax, "%.3f"))
                VitalStat("ABV", formatRange(style.abvMin, style.abvMax, "%.1f%%"))
            }
            listOfNotNull(
                style.sweetness?.takeIf { it.isNotBlank() }?.let { "Sweetness" to it },
                style.carbonation?.takeIf { it.isNotBlank() }?.let { "Carbonation" to it },
                style.strength?.takeIf { it.isNotBlank() }?.let { "Strength" to it }
            ).forEach { (label, value) ->
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VitalStat("OG", formatRange(style.ogMin, style.ogMax, "%.3f"))
                VitalStat("FG", formatRange(style.fgMin, style.fgMax, "%.3f"))
                VitalStat("IBU", formatIntRange(style.ibuMin, style.ibuMax))
                VitalStat("SRM", formatIntRange(style.srmMin, style.srmMax))
                VitalStat("ABV", formatRange(style.abvMin, style.abvMax, "%.1f%%"))
            }
        }
    }
}

@Composable
private fun VitalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatRange(min: Double?, max: Double?, pattern: String): String =
    if (min != null && max != null) "${pattern.format(min)}–${pattern.format(max)}" else "—"

private fun formatIntRange(min: Double?, max: Double?): String =
    if (min != null && max != null) "${min.roundToInt()}–${max.roundToInt()}" else "—"

@Composable
private fun OffFlavorQuickPeekSheet(offFlavor: OffFlavor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${offFlavor.emoji} ${offFlavor.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${offFlavor.chemicalCompound} • Threshold: ${offFlavor.perceptionThreshold}",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        if (offFlavor.description.isNotBlank()) {
            Text(text = offFlavor.description, style = MaterialTheme.typography.bodyMedium)
        }
        if (offFlavor.aromaPerception.isNotBlank()) {
            Text(text = "Aroma: ${offFlavor.aromaPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.flavorPerception.isNotBlank()) {
            Text(text = "Flavor: ${offFlavor.flavorPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.mouthfeelPerception.isNotBlank()) {
            Text(text = "Mouthfeel: ${offFlavor.mouthfeelPerception}", style = MaterialTheme.typography.bodySmall)
        }
        if (offFlavor.typicalOrigins.isNotEmpty()) {
            Text(
                text = "Typical Origins: ${offFlavor.typicalOrigins.joinToString("; ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionInfoSheet(info: SectionGuidelineInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (info.linkedStyle != null) {
            Text(
                text = "${info.linkedStyle.number} • ${info.linkedStyle.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider()
        }
        if (info.guidelineText.isNotBlank()) {
            Text(
                text = info.guidelineText,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = info.fallbackTip,
                style = MaterialTheme.typography.bodyMedium
            )
            if (info.linkedStyle == null) {
                Text(
                    text = "Tip: Select a ${info.styleLabel} under Entry Details to view style-specific ${info.sectionTitle.lowercase()} guidelines here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BjcpScoringGuideSheet(activeTier: BjcpScoringTier) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "BJCP Scoring Guide",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider()
        BjcpScoringTier.entries.forEach { tier ->
            val isActive = tier == activeTier
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tier.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tier.rangeText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tier.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
