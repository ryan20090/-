package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.saudiacademic.masar.R
import com.example.model.CalendarRegion
import com.example.model.CountdownItem
import com.example.model.EventCategory
import com.example.model.GradeLevel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HijriAmber
import com.example.ui.theme.SaudiGreenDark
import com.example.ui.theme.SaudiGreenPrimary
import com.example.ui.theme.TahsiliPurple
import com.example.ui.theme.VacationTeal
import com.example.viewmodel.AcademicUiState
import com.example.widget.MasarAppWidgetProvider
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: AcademicUiState,
    onSelectGrade: (GradeLevel) -> Unit,
    onSelectRegion: (CalendarRegion) -> Unit,
    onSelectFilter: (EventCategory?) -> Unit,
    onRefreshSync: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToWeighted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGradeSheet by remember { mutableStateOf(false) }

    val filteredCountdowns = remember(uiState.allCountdowns, uiState.selectedHomeFilter) {
        if (uiState.selectedHomeFilter == null) {
            uiState.allCountdowns.filter { !it.isPast }
        } else {
            uiState.allCountdowns.filter { it.categoryEnum == uiState.selectedHomeFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 0. Reference DB Version & Decoupled Monthly Update Badge
        item {
            DecoupledDatabaseVersionBanner(
                referenceDbBadge = uiState.referenceDbBadge
            )
        }

        // 1. Hero Academic Week Card with Grade Selector & Hijri Sync
        item {
            AcademicHeroCard(
                uiState = uiState,
                onOpenGradePicker = { showGradeSheet = true },
                onRefreshSync = onRefreshSync
            )
        }

        // 2. Geographic Region Selector Card (General vs Western Region)
        item {
            RegionSelectorCard(
                selectedRegion = uiState.selectedRegion,
                onSelectRegion = onSelectRegion
            )
        }

        // 3. Third High School Special Focus: Tahsili Live Seconds Countdown (Period 1 & Period 2)
        item {
            AnimatedVisibility(visible = uiState.selectedGrade.isThirdHighSchool) {
                uiState.primaryTahsiliExam?.let { tahsili ->
                    ThirdHighSchoolTahsiliSpotlightCard(
                        tahsiliCountdown = tahsili,
                        secondPeriodCountdown = uiState.secondTahsiliExam,
                        registrationCountdown = uiState.tahsiliRegistration,
                        onOpenExams = onNavigateToExams,
                        onOpenWeighted = onNavigateToWeighted
                    )
                }
            }
        }

        // 4. Personal Computerized Exam Booking Live Countdown (Qudurat or STEP)
        item {
            uiState.personalComputerizedCountdown?.let { personalExam ->
                PersonalComputerizedCountdownCard(
                    countdown = personalExam,
                    onManageClick = onNavigateToExams
                )
            }
        }

        // 5. Dual Key Countdowns by Seconds: Next Vacation & Next School Exam / Year End
        item {
            KeyMilestonesSecondsSection(
                nextVacation = uiState.nextVacation,
                nextSchoolExam = uiState.nextSchoolExam,
                yearEndCountdown = uiState.yearEndCountdown
            )
        }

        // 6. Remaining Official Vacations Live Countdowns
        if (uiState.upcomingVacations.size > 1) {
            item {
                RemainingVacationsStrip(
                    vacations = uiState.upcomingVacations.drop(1)
                )
            }
        }

        // 6.5. Jetpack Glance Home Screen App Widget Live Preview & Pinning Card
        item {
            GlanceWidgetShowcaseCard(uiState = uiState)
        }

        // 7. Category Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "العدادات التنازلية للتقويم الدراسي 1448 هـ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredCountdowns.size} موعد",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("home_category_filters")
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedHomeFilter == null,
                            onClick = { onSelectFilter(null) },
                            label = { Text("الكل (القادمة)") },
                            modifier = Modifier.testTag("filter_chip_all")
                        )
                    }
                    items(EventCategory.entries) { category ->
                        if (!uiState.selectedGrade.isHighSchool &&
                            category in listOf(EventCategory.TAHSILI, EventCategory.QUDURAT, EventCategory.STEP)
                        ) {
                            return@items
                        }
                        FilterChip(
                            selected = uiState.selectedHomeFilter == category,
                            onClick = {
                                onSelectFilter(
                                    if (uiState.selectedHomeFilter == category) null else category
                                )
                            },
                            label = { Text(category.arabicLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 8. Full Countdown Cards List (Hijri + Gregorian + 4-Unit Live Seconds)
        items(
            items = filteredCountdowns,
            key = { it.event.id }
        ) { item ->
            AcademicEventCountdownCard(item = item)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showGradeSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showGradeSheet = false },
            sheetState = sheetState
        ) {
            GradeSelectorSheetContent(
                selectedGrade = uiState.selectedGrade,
                onGradeChosen = { grade ->
                    onSelectGrade(grade)
                    showGradeSheet = false
                }
            )
        }
    }
}

@Composable
private fun DecoupledDatabaseVersionBanner(
    referenceDbBadge: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reference_db_version_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = referenceDbBadge,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "بياناتك الشخصية محفوظة محلياً بمعزل عن تحديثات قاعدة البيانات الشهرية (بدون إنترنت)",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AcademicHeroCard(
    uiState: AcademicUiState,
    onOpenGradePicker: () -> Unit,
    onRefreshSync: () -> Unit
) {
    val week = uiState.weekInfo

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_academic_week_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(SaudiGreenDark, SaudiGreenPrimary)
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_academic_1794604236283),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.16f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(255.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onOpenGradePicker,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("grade_selector_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "اختيار الصف الدراسي",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${uiState.selectedGrade.stageArabic}: ${uiState.selectedGrade.arabicTitle}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshSync,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .testTag("hero_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "مزامنة التاريخ الهجري والتحقق من التوقيت",
                            tint = Color.White
                        )
                    }
                }

                // Current Academic Week Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = week.semesterName,
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                        Text(
                            text = if (week.isActiveStudyWeek) {
                                "الأسبوع الدراسي ${week.weekNumber} من ${week.totalSemesterWeeks}"
                            } else {
                                week.statusSummary
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.testTag("current_week_title")
                        )
                        Text(
                            text = week.statusSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoldAccent.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, GoldAccent)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "إنجاز الفصل",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                            Text(
                                text = "${(week.progressFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldAccent
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { week.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = GoldAccent,
                    trackColor = Color.White.copy(alpha = 0.24f)
                )

                // Dual Hijri + Gregorian Today Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.24f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = week.hijriToday,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = week.gregorianToday,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionSelectorCard(
    selectedRegion: CalendarRegion,
    onSelectRegion: (CalendarRegion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("region_selector_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "نطاق التقويم الدراسي المعتمد (يُحفظ تلقائياً):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarRegion.entries.forEach { region ->
                    val isSelected = selectedRegion == region
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRegion(region) },
                        label = {
                            Text(
                                text = region.shortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("region_chip_${region.name.lowercase()}")
                    )
                }
            }

            Text(
                text = if (selectedRegion == CalendarRegion.WESTERN_HOLY_CITIES) {
                    "مفعل: تقويم مكة والمدينة وجدة والطائف (الأسبوع 18، بداية إجازة الأضحى 23 ذو القعدة، نهاية العام الخميس 26 محرم 1449 هـ)"
                } else {
                    "مفعل: التقويم العام لبقية مناطق المملكة (المصدر: تقويم وزارة التعليم 1448 هـ)"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThirdHighSchoolTahsiliSpotlightCard(
    tahsiliCountdown: CountdownItem,
    secondPeriodCountdown: CountdownItem?,
    registrationCountdown: CountdownItem?,
    onOpenExams: () -> Unit,
    onOpenWeighted: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("third_high_tahsili_spotlight"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = TahsiliPurple.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.5.dp, TahsiliPurple.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = TahsiliPurple
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "تركيز الثالث الثانوي • العداد التنازلي بالثواني للتحصيلي",
                            style = MaterialTheme.typography.labelMedium,
                            color = TahsiliPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tahsiliCountdown.event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Live 4-Unit Seconds Countdown Grid for Tahsili Period 1
            LiveFourUnitCountdownRow(
                days = tahsiliCountdown.daysRemaining,
                hours = tahsiliCountdown.hoursRemaining,
                minutes = tahsiliCountdown.minutesRemaining,
                seconds = tahsiliCountdown.secondsRemaining,
                accentColor = TahsiliPurple
            )

            // Dual Hijri + Gregorian Dates
            DualDateBadgeRow(
                hijriDate = tahsiliCountdown.event.hijriDateDisplay,
                gregorianDate = tahsiliCountdown.event.gregorianDateDisplay,
                sourceName = tahsiliCountdown.event.sourcePlatform
            )

            // Period 2 Summary if present
            secondPeriodCountdown?.let { p2 ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, TahsiliPurple.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p2.event.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TahsiliPurple
                            )
                            Text(
                                text = "${p2.daysRemaining} يوم • ${p2.hoursRemaining} س • ${p2.minutesRemaining} د • ${p2.secondsRemaining} ث",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TahsiliPurple
                            )
                        }
                        Text(
                            text = "${p2.event.hijriDateDisplay} | ${p2.event.gregorianDateDisplay}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            registrationCountdown?.let { reg ->
                if (!reg.isPast) {
                    Text(
                        text = "بداية التسجيل المبكر: متبقي ${reg.daysRemaining} يوم (${reg.event.hijriDateDisplay} - ${reg.event.gregorianDateDisplay})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TahsiliPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenExams,
                    shape = RoundedCornerShape(12.dp),
                    color = TahsiliPurple,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("spotlight_exams_btn")
                ) {
                    Text(
                        text = "مركز قياس ومؤقت السرعة",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }
                Surface(
                    onClick = onOpenWeighted,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, TahsiliPurple),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("spotlight_weighted_btn")
                ) {
                    Text(
                        text = "حاسبة الموزونة والجامعات",
                        style = MaterialTheme.typography.labelLarge,
                        color = TahsiliPurple,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalComputerizedCountdownCard(
    countdown: CountdownItem,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("personal_computerized_countdown_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "عدّاد اختبارك المحوسب المسجل بالثواني",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = countdown.event.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Surface(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "تعديل الموعد",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            LiveFourUnitCountdownRow(
                days = countdown.daysRemaining,
                hours = countdown.hoursRemaining,
                minutes = countdown.minutesRemaining,
                seconds = countdown.secondsRemaining,
                accentColor = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "التاريخ: ${countdown.event.hijriDateDisplay} | ${countdown.event.gregorianDateDisplay}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeyMilestonesSecondsSection(
    nextVacation: CountdownItem?,
    nextSchoolExam: CountdownItem?,
    yearEndCountdown: CountdownItem?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        nextVacation?.let { vac ->
            MilestoneSecondsWideCard(
                headerLabel = "أقرب إجازة رسمية قادمة (عدّاد حي بالثواني)",
                countdown = vac,
                accentColor = VacationTeal,
                icon = Icons.Default.BeachAccess,
                testTag = "next_vacation_card"
            )
        }

        val secondTarget = nextSchoolExam ?: yearEndCountdown
        secondTarget?.let { target ->
            MilestoneSecondsWideCard(
                headerLabel = if (nextSchoolExam != null) {
                    "أقرب اختبارات مدرسية ونهاية العام"
                } else {
                    "نهاية العام الدراسي 1448 هـ (عدّاد حي بالثواني)"
                },
                countdown = target,
                accentColor = SaudiGreenPrimary,
                icon = Icons.Default.Flag,
                testTag = "next_school_exam_card"
            )
        }
    }
}

@Composable
private fun MilestoneSecondsWideCard(
    headerLabel: String,
    countdown: CountdownItem,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
                Column {
                    Text(
                        text = headerLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = countdown.event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            LiveFourUnitCountdownRow(
                days = countdown.daysRemaining,
                hours = countdown.hoursRemaining,
                minutes = countdown.minutesRemaining,
                seconds = countdown.secondsRemaining,
                accentColor = accentColor
            )

            Text(
                text = "هجري: ${countdown.event.hijriDateDisplay}  •  ميلادي: ${countdown.event.gregorianDateDisplay}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RemainingVacationsStrip(
    vacations: List<CountdownItem>
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "بقية الإجازات الرسمية المتبقية في العام 1448 هـ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vacations, key = { it.event.id }) { vac ->
                Card(
                    modifier = Modifier.width(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, VacationTeal.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = vac.event.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${vac.daysRemaining} يوم • ${vac.hoursRemaining} س • ${vac.minutesRemaining} د • ${vac.secondsRemaining} ث",
                            style = MaterialTheme.typography.labelMedium,
                            color = VacationTeal,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = vac.event.hijriDateDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = HijriAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = vac.event.gregorianDateDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveFourUnitCountdownRow(
    days: Long,
    hours: Long,
    minutes: Long,
    seconds: Long,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CountdownUnitBox(
            value = days.toString(),
            label = "يوم",
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
        CountdownUnitBox(
            value = String.format(Locale.ENGLISH, "%02d", hours),
            label = "ساعة",
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
        CountdownUnitBox(
            value = String.format(Locale.ENGLISH, "%02d", minutes),
            label = "دقيقة",
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
        CountdownUnitBox(
            value = String.format(Locale.ENGLISH, "%02d", seconds),
            label = "ثانية",
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CountdownUnitBox(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.11f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AcademicEventCountdownCard(
    item: CountdownItem,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (item.categoryEnum) {
        EventCategory.VACATION -> VacationTeal
        EventCategory.TAHSILI -> TahsiliPurple
        EventCategory.QUDURAT -> HijriAmber
        EventCategory.STEP -> Color(0xFF00796B)
        EventCategory.SCHOOL_EXAM -> Color(0xFFC62828)
        EventCategory.MILESTONE -> SaudiGreenPrimary
        EventCategory.CUSTOM -> Color(0xFF37474F)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("countdown_card_${item.event.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = item.categoryEnum.arabicLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (item.event.highSchoolOnly) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TahsiliPurple.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "ثانوي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TahsiliPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = item.event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.event.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!item.isPast && !item.isOngoing) {
                LiveFourUnitCountdownRow(
                    days = item.daysRemaining,
                    hours = item.hoursRemaining,
                    minutes = item.minutesRemaining,
                    seconds = item.secondsRemaining,
                    accentColor = badgeColor
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (item.isOngoing) "جارٍ الآن" else "مكتمل",
                        style = MaterialTheme.typography.labelLarge,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            DualDateBadgeRow(
                hijriDate = item.event.hijriDateDisplay,
                gregorianDate = item.event.gregorianDateDisplay,
                sourceName = item.event.sourcePlatform
            )
        }
    }
}

@Composable
fun DualDateBadgeRow(
    hijriDate: String,
    gregorianDate: String,
    sourceName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = HijriAmber,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "هجري: $hijriDate",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ميلادي: $gregorianDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Static trusted source text only (Zero Clickable External Links)
            Text(
                text = sourceName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GradeSelectorSheetContent(
    selectedGrade: GradeLevel,
    onGradeChosen: (GradeLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "اختر الصف الدراسي لتخصيص التقويم والاختبارات",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "عند اختيار الصف الثالث الثانوي يتم تفعيل عداد الاختبار التحصيلي بالثواني ومخطط ختم التحصيلي في أعلى الشاشة.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GradeLevel.entries.forEach { grade ->
            val isSelected = grade == selectedGrade
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGradeChosen(grade) }
                    .testTag("grade_option_${grade.name.lowercase()}"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                border = if (isSelected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${grade.stageArabic} - ${grade.arabicTitle}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (grade.isThirdHighSchool) {
                            Text(
                                text = "يشمل عداد التحصيلي بالثواني + حاسبة الموزونة والجامعات",
                                style = MaterialTheme.typography.labelSmall,
                                color = TahsiliPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (isSelected) {
                        Text(
                            text = "مُفعّل",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun GlanceWidgetShowcaseCard(
    uiState: AcademicUiState
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val week = uiState.weekInfo
    val nextVac = uiState.nextVacation
    val tahsili = uiState.primaryTahsiliExam

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("glance_widget_showcase_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, SaudiGreenPrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SaudiGreenPrimary.copy(alpha = 0.14f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = SaudiGreenPrimary,
                            modifier = Modifier
                                .padding(7.dp)
                                .size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ودجت الشاشة الرئيسية (Jetpack Glance)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "يعرض الأسبوع الدراسي وأقرب إجازة وعدّاد التحصيلي بالأيام على شاشة جوالك",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Live Visual Replica of the Jetpack Glance Widget
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SaudiGreenDark,
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("glance_widget_live_preview")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "مسار 1448 • ${uiState.selectedRegion.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = week.hijriToday,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            onClick = {
                                MasarAppWidgetProvider.refreshAllWidgets(context)
                                statusMessage = "تم تحديث بيانات ودجت الشاشة الرئيسية (Glance) بنجاح."
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = SaudiGreenPrimary,
                            modifier = Modifier.testTag("glance_widget_refresh_btn")
                        ) {
                            Text(
                                text = "تحديث ↻",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Current Academic Week Banner inside Widget
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SaudiGreenPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (week.isActiveStudyWeek) {
                                        "الأسبوع الدراسي ${week.weekNumber} من ${week.totalSemesterWeeks}"
                                    } else {
                                        week.statusSummary
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = week.semesterName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SaudiGreenDark
                            ) {
                                Text(
                                    text = "إنجاز ${(week.progressFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Dual Cards Row: Next Vacation & Tahsili Days Countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = VacationTeal,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "أقرب إجازة رسمية",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nextVac?.event?.title ?: "إجازة الخريف الدراسية",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (nextVac?.isOngoing == true) {
                                        "إجازة جارية الآن"
                                    } else {
                                        "متبقي ${nextVac?.daysRemaining ?: 0} يوم"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = nextVac?.event?.hijriDateDisplay ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TahsiliPurple,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "الاختبار التحصيلي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الاختبار التحصيلي (الفترة 1)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "متبقي ${tahsili?.daysRemaining ?: 0} يوم",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = tahsili?.event?.hijriDateDisplay ?: "16 - 20 ذو الحجة 1448 هـ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = {
                        val pinned = MasarAppWidgetProvider.requestPinMasarWidget(context)
                        MasarAppWidgetProvider.refreshAllWidgets(context)
                        statusMessage = if (pinned) {
                            "تم إرسال طلب تثبيت ودجت مسار 1448 إلى الشاشة الرئيسية."
                        } else {
                            "تم تحديث الودجت؛ يمكنك أيضاً إضافته بالضغط المطول على الشاشة الرئيسية واختيار (ودجت مسار 1448)."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = SaudiGreenPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_glance_widget_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة ودجت Glance للشاشة الرئيسية",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            statusMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = SaudiGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

