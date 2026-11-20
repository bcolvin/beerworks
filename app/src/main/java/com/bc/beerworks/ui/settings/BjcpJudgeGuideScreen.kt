package com.bc.beerworks.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.ui.theme.BeerworksTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BjcpJudgeLinks {
    private const val COMPETITION_CALENDAR_BASE = "https://app.bjcp.org/competition-calendar"
    const val UPCOMING_TASTING_EXAMS_URL = "https://app.bjcp.org/upcoming-tasting-exams"
    const val ONLINE_ENTRANCE_EXAM_PORTAL_URL = "https://app.bjcp.org/"
    const val BJCP_MAIN_WEBSITE_URL = "https://www.bjcp.org/"
    const val BEERWORKS_GITHUB_URL = "https://github.com/bcolvin/beerworks"
    const val PRIVACY_POLICY_URL = "https://github.com/bcolvin/beerworks/blob/master/PRIVACY.md"

    fun buildCompetitionCalendarUrl(date: LocalDate = LocalDate.now()): String {
        val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "$COMPETITION_CALENDAR_BASE?start_date=$formattedDate"
    }
}

internal fun Context.openExternalUrl(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@Immutable
data class BjcpExternalResource(
    val title: String,
    val subtitle: String,
    val url: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BjcpJudgeGuideScreen(
    onBackClick: () -> Unit,
    onStudyStyleGuideline: (GuidelineType) -> Unit,
    onStudyOffFlavors: () -> Unit,
    onPracticeScoresheet: () -> Unit,
    currentDate: LocalDate = LocalDate.now()
) {
    val context = LocalContext.current
    val openExternalUrl: (String) -> Unit = { url -> context.openExternalUrl(url) }

    var pathwayExpanded by rememberSaveable { mutableStateOf(true) }
    var examTracksExpanded by rememberSaveable { mutableStateOf(false) }
    var rubricExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Become a BJCP Judge",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Always-Visible Hero & Competition Steward Callout
            OverviewAndStewardCard()

            // 2. Collapsible Deep-Dive Sections
            ExpandableGuideCard(
                title = "The Certification Pathway",
                subtitle = "Step-by-step from your first online test to Grand Master rank",
                icon = Icons.Default.School,
                expanded = pathwayExpanded,
                onToggle = { pathwayExpanded = !pathwayExpanded }
            ) {
                CertificationPathwayContent()
            }

            ExpandableGuideCard(
                title = "Exam Tracks: Beer, Mead & Cider",
                subtitle = "Compare entrance tests, tasting flights, and discipline endorsements",
                icon = Icons.Default.EmojiEvents,
                expanded = examTracksExpanded,
                onToggle = { examTracksExpanded = !examTracksExpanded }
            ) {
                ExamTracksContent()
            }

            ExpandableGuideCard(
                title = "Scoresheet Rubric & Exam Strategy",
                subtitle = "The 5 grading criteria BJCP exam proctors evaluate on every sheet",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                expanded = rubricExpanded,
                onToggle = { rubricExpanded = !rubricExpanded }
            ) {
                ScoresheetRubricContent()
            }

            // 3. Always-Visible In-App Study Toolkit
            InAppStudyToolkitCard(
                onStudyStyleGuideline = onStudyStyleGuideline,
                onStudyOffFlavors = onStudyOffFlavors,
                onPracticeScoresheet = onPracticeScoresheet
            )

            // 4. Always-Visible Official BJCP Calendars, Portal & Resources
            OfficialBjcpResourcesCard(
                currentDate = currentDate,
                onOpenUrl = openExternalUrl
            )

            // 5. Always-Visible Community & BJCP Attribution Card
            BjcpCommunityAttributionCard()
        }
    }
}

@Composable
private fun OverviewAndStewardCard() {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "What is the BJCP?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Beer Judge Certification Program • Est. 1985",
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Founded in 1985, the Beer Judge Certification Program (BJCP) is a global non-profit organization dedicated to promoting beer, mead, and cider literacy, recognizing sensory evaluation excellence, and sanctioning homebrew and craft competitions worldwide.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start Today as a Competition Steward",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Not ready for an exam yet? Every sanctioned competition needs Competition Stewards—and no certification or prior exam is required. Stewards sit right at the flight table alongside certified judges to organize, pour, and verify anonymous entries, collect scoresheets, and taste the flight entries alongside the panel while earning official BJCP Experience Points.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableGuideCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    content()
                }
            }
        }
    }
}

@Composable
private fun CertificationPathwayContent() {
    PathwayStepItem(
        stepNumber = "Step 1",
        title = "Pass the Online BJCP Entrance Exam",
        body = "Create an account on app.bjcp.org and take the open-book, timed online prerequisite exam for Beer, Mead, or Cider. It verifies foundational knowledge of style guidelines, fermentation, off-flavors, and judging etiquette. Passing earns a certificate that qualifies you to register for an in-person Tasting Exam."
    )
    PathwayStepItem(
        stepNumber = "Step 2",
        title = "Judge or Steward to Build Palate Experience",
        body = "While waiting for a Tasting Exam seat (or immediately after passing the entrance exam as a Provisional Judge), volunteer at local homebrew club competitions as a Competition Steward or paired novice judge. Tasting flights with National or Master judges is the fastest way to calibrate your scoring and vocabulary."
    )
    PathwayStepItem(
        stepNumber = "Step 3",
        title = "Sit for the In-Person BJCP Tasting Exam",
        body = "Evaluate 6 blind beverages in 90 minutes (15 minutes per scoresheet). You are given the declared style for each entry and must write a complete sensory evaluation and assign an accurate 50-point score. Two or more high-ranking proctors also score the same flight, and your sheets are graded against their consensus."
    )
    PathwayStepItem(
        stepNumber = "Step 4",
        title = "Earn Experience Points & Advance in BJCP Judge Rank",
        body = "Your exam score establishes your maximum eligible rank tier, while judging and stewarding at sanctioned competitions earns Experience Points to unlock rank promotions: Recognized (60%+ exam score) → Certified (70%+ exam score + 5 judging points) → National (80%+ Tasting & Written Proficiency exams + 20 points) → Master (90%+ exams + 40 points) → Grand Master."
    )
}

@Composable
private fun PathwayStepItem(
    stepNumber: String,
    title: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExamTracksContent() {
    TrackDetailBox(
        emoji = "🍺",
        trackTitle = "Beer Judge Track",
        entranceSpec = "Entrance Exam: 180 questions in 60 minutes (online, open-book)",
        tastingSpec = "Tasting Exam: 6 beers in 90 minutes (15 min/sheet • 5 scored sections)",
        details = "Covers the BJCP 2021 Beer Style Guidelines, brewing recipe formulation (hops, malts, water chemistry, yeast), and 20+ canonical fermentation and oxidation faults. Advancing to National or Master Beer Judge also requires passing the BJCP Written Proficiency Exam."
    )
    TrackDetailBox(
        emoji = "🐝",
        trackTitle = "Mead Judge Track",
        entranceSpec = "Entrance Exam: 100 questions in 30 minutes (online, open-book)",
        tastingSpec = "Tasting Exam: 6 meads in 90 minutes (15 min/sheet • 4 scored sections)",
        details = "Covers the BJCP Mead Guidelines (Categories M1–M4: Traditional, Fruit, Spiced, and Specialty Meads), varietal honeys, must nutrition, staggered nutrient additions, and mead faults. Earns standalone Mead Judge rank or adds a Mead Endorsement to an existing Beer Judge credential."
    )
    TrackDetailBox(
        emoji = "🍏",
        trackTitle = "Cider Judge Track",
        entranceSpec = "Entrance Exam: 100 questions in 30 minutes (online, open-book)",
        tastingSpec = "Tasting Exam: 6 ciders/perries in 90 minutes (15 min/sheet • 4 scored sections)",
        details = "Covers the BJCP Cider Guidelines (Categories C1–C2: Standard & Specialty Cider and Perry), pomology (Bittersweet, Bittersharp, Sharp, Sweet classifications), malic acid, tannins, perry pear sorbitol, and cider faults (volatile acidity, mousiness, H2S, phenolic/Brett character)."
    )
}

@Composable
private fun TrackDetailBox(
    emoji: String,
    trackTitle: String,
    entranceSpec: String,
    tastingSpec: String,
    details: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$emoji $trackTitle",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "• $entranceSpec",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "• $tastingSpec",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoresheetRubricContent() {
    Text(
        text = "On the BJCP Tasting Exam, graders evaluate your 6 scoresheets across five equal dimensions (20% each):",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )

    RubricCriterionItem(
        number = "1. Perception (20%)",
        description = "Did you detect the actual aromas, flavors, body, carbonation, and off-flavors present in the glass that the proctor consensus panel identified?"
    )
    RubricCriterionItem(
        number = "2. Descriptive Ability (20%)",
        description = "Do you paint a clear sensory picture using specific descriptors (e.g., \"light toasted bread crust, spicy Saaz hops, low diacetyl butterscotch\") rather than generic placeholders (\"malty, hoppy, nice\")?"
    )
    RubricCriterionItem(
        number = "3. Feedback (20%)",
        description = "Do you explain how the entry compares to the declared BJCP style guidelines and offer constructive, actionable brewing or fermentation advice to fix detected faults?"
    )
    RubricCriterionItem(
        number = "4. Completeness (20%)",
        description = "Did you comment on every required attribute in every section (e.g., Malt, Hops, Esters, and Other in Aroma) with an explicit intensity level (low, medium-low, medium, high), leaving no blank space?"
    )
    RubricCriterionItem(
        number = "5. Scoring Accuracy (20%)",
        description = "Is your total score out of 50 within a few points of the proctors' consensus score, and does your numerical score match the tone of your written comments?"
    )
}

@Composable
private fun RubricCriterionItem(
    number: String,
    description: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InAppStudyToolkitCard(
    onStudyStyleGuideline: (GuidelineType) -> Unit,
    onStudyOffFlavors: () -> Unit,
    onPracticeScoresheet: () -> Unit
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "In-App Study Toolkit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Jump directly into Zymurgist Table's offline reference guides and practice tools.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StudyToolkitActionRow(
                emoji = "🍺",
                title = "Study BJCP 2021 Beer Style Guidelines",
                subtitle = "Switch active guide to BJCP Beer and review vital stats & comparisons",
                onClick = { onStudyStyleGuideline(GuidelineType.BJCP_2021) }
            )
            StudyToolkitActionRow(
                emoji = "🐝",
                title = "Study BJCP Mead Style Guidelines",
                subtitle = "Switch active guide to BJCP Mead (Categories M1–M4)",
                onClick = { onStudyStyleGuideline(GuidelineType.BJCP_MEAD) }
            )
            StudyToolkitActionRow(
                emoji = "🍏",
                title = "Study BJCP Cider Style Guidelines",
                subtitle = "Switch active guide to BJCP Cider & Perry (Categories C1–C2)",
                onClick = { onStudyStyleGuideline(GuidelineType.BJCP_CIDER) }
            )
            StudyToolkitActionRow(
                icon = Icons.Default.Science,
                title = "Study Off-Flavors & Sensory Faults",
                subtitle = "Master perception thresholds, root causes, lookalikes, and remedies",
                onClick = onStudyOffFlavors
            )
            StudyToolkitActionRow(
                icon = Icons.AutoMirrored.Filled.Assignment,
                title = "Practice a 15-Minute Blind Scoresheet",
                subtitle = "Open the digital BJCP Beer, Mead, or Cider Scoresheet editor",
                onClick = onPracticeScoresheet
            )
        }
    }
}

@Composable
private fun StudyToolkitActionRow(
    emoji: String? = null,
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OfficialBjcpResourcesCard(
    currentDate: LocalDate,
    onOpenUrl: (String) -> Unit
) {
    val competitionCalendarUrl = BjcpJudgeLinks.buildCompetitionCalendarUrl(currentDate)
    val resources = listOf(
        BjcpExternalResource(
            title = "Upcoming Competitions Calendar",
            subtitle = "Find BJCP-sanctioned competitions in your area to enter, judge, or steward • Opens app.bjcp.org",
            url = competitionCalendarUrl,
            icon = Icons.Default.CalendarMonth
        ),
        BjcpExternalResource(
            title = "Upcoming Tasting Exams",
            subtitle = "View scheduled in-person Beer, Mead, and Cider tasting exams worldwide • Opens app.bjcp.org",
            url = BjcpJudgeLinks.UPCOMING_TASTING_EXAMS_URL,
            icon = Icons.Default.EventAvailable
        ),
        BjcpExternalResource(
            title = "BJCP Online Entrance Exam & Portal",
            subtitle = "Create a BJCP profile, take the online prerequisite exam, or view your judge record • Opens app.bjcp.org",
            url = BjcpJudgeLinks.ONLINE_ENTRANCE_EXAM_PORTAL_URL,
            icon = Icons.Default.Public
        ),
        BjcpExternalResource(
            title = "Official BJCP Website & Study Guides",
            subtitle = "Download official study guides, exam manuals, sample scoresheets, and international resources • Opens www.bjcp.org",
            url = BjcpJudgeLinks.BJCP_MAIN_WEBSITE_URL,
            icon = Icons.Default.Language
        )
    )

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Upcoming Events, Exams & BJCP Portal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live schedules and exam portals open externally in your web browser.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            resources.forEach { resource ->
                ExternalResourceRow(
                    resource = resource,
                    onClick = { onOpenUrl(resource.url) }
                )
            }
        }
    }
}

@Composable
private fun ExternalResourceRow(
    resource: BjcpExternalResource,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = resource.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = resource.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

@Composable
private fun BjcpCommunityAttributionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Built on the Foundation of the BJCP Community",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Much of the style reference data, sensory evaluation frameworks, off-flavor references, and scoresheet structures in Zymurgist Table are made possible by the decades of volunteer work, research, and dedication of the Beer Judge Certification Program (BJCP) and its global community of judges, stewards, and exam directors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Our goal is to honor those resources by putting them to their fullest use in the hands of brewers, meadmakers, cidermakers, and judges—and to inspire the next generation of volunteers to give back by stewarding, judging, and supporting local competitions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BjcpJudgeGuideScreenPreview() {
    BeerworksTheme {
        BjcpJudgeGuideScreen(
            onBackClick = {},
            onStudyStyleGuideline = {},
            onStudyOffFlavors = {},
            onPracticeScoresheet = {},
            currentDate = LocalDate.of(2026, 11, 19)
        )
    }
}
