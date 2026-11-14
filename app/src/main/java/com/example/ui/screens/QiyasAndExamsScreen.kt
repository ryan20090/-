package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aistudio.saudiacademic.masar.R
import com.example.model.EventCategory
import com.example.model.ShortSchoolQuizItem
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HijriAmber
import com.example.ui.theme.SaudiGreenPrimary
import com.example.ui.theme.TahsiliPurple
import com.example.viewmodel.AcademicUiState
import com.example.viewmodel.QiyasSpeedTimerState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun QiyasAndExamsScreen(
    uiState: AcademicUiState,
    minuteVibrationEvents: SharedFlow<Unit>,
    onSaveComputerizedBooking: (String, String, Long) -> Unit,
    onClearComputerizedBooking: () -> Unit,
    onAddShortSchoolQuiz: (String, String, Long) -> Unit,
    onDeleteShortSchoolQuiz: (String) -> Unit,
    onUpdateTahsiliPages: (String) -> Unit,
    onUpdateSpeedTimerQuestions: (String) -> Unit,
    onToggleSpeedTimer: () -> Unit,
    onResetSpeedTimer: () -> Unit,
    onAddCustomExam: (String, String, EventCategory, Long) -> Unit,
    onDeleteCustomExam: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf<EventCategory?>(null) }
    var showAddExamDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }
    var showAddQuizDialog by remember { mutableStateOf(false) }

    // Trigger gentle device vibration at the end of each 60-second minute in Qiyas Speed Timer
    LaunchedEffect(minuteVibrationEvents) {
        minuteVibrationEvents.collect {
            triggerGentleMinuteVibration(context)
        }
    }

    val examsList = remember(uiState.qiyasAndCustomCountdowns, selectedTab) {
        if (selectedTab == null) {
            uiState.qiyasAndCustomCountdowns
        } else {
            uiState.qiyasAndCustomCountdowns.filter { it.categoryEnum == selectedTab }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExamDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_custom_exam_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة موعد اختبار")
                    Text("إضافة اختبار مخصص", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("qiyas_exams_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Banner for Qiyas & Tahsili
            item {
                QiyasBannerHeader(uiState = uiState)
            }

            // 2. Smart Training Tools: Tahsili Study Planner + Qiyas 60s Speed Timer
            item {
                SmartTrainingToolsSection(
                    tahsiliPagesInput = uiState.tahsiliRemainingPagesInput,
                    tahsiliDailyTarget = uiState.tahsiliDailyPagesTarget,
                    speedTimer = uiState.speedTimer,
                    onUpdateTahsiliPages = onUpdateTahsiliPages,
                    onUpdateSpeedTimerQuestions = onUpdateSpeedTimerQuestions,
                    onToggleSpeedTimer = onToggleSpeedTimer,
                    onResetSpeedTimer = onResetSpeedTimer
                )
            }

            // 3. STEP Info Card + Personal Computerized Exam Booking (Qudurat or STEP)
            item {
                StepAndComputerizedBookingCard(
                    uiState = uiState,
                    onOpenBookingDialog = { showBookingDialog = true },
                    onClearBooking = onClearComputerizedBooking
                )
            }

            // 4. Short School Quizzes Table (جدول الاختبارات المدرسية القصيرة / الفترات)
            item {
                ShortSchoolQuizzesSection(
                    quizzes = uiState.shortSchoolQuizzes,
                    onOpenAddQuizDialog = { showAddQuizDialog = true },
                    onDeleteQuiz = onDeleteShortSchoolQuiz
                )
            }

            // 5. Filter Tabs for Qiyas Categories
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("qiyas_filter_row")
                ) {
                    item {
                        FilterChip(
                            selected = selectedTab == null,
                            onClick = { selectedTab = null },
                            label = { Text("جميع اختبارات قياس") }
                        )
                    }
                    val qiyasTabs = listOf(
                        EventCategory.TAHSILI,
                        EventCategory.QUDURAT,
                        EventCategory.STEP,
                        EventCategory.CUSTOM
                    )
                    items(qiyasTabs) { cat ->
                        FilterChip(
                            selected = selectedTab == cat,
                            onClick = { selectedTab = if (selectedTab == cat) null else cat },
                            label = { Text(cat.arabicLabel) },
                            modifier = Modifier.testTag("qiyas_tab_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            // 6. Exam Countdown Cards with Hijri + Gregorian + Seconds
            items(
                items = examsList,
                key = { it.event.id }
            ) { item ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AcademicEventCountdownCard(item = item)
                    if (item.event.isCustom) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onDeleteCustomExam(item.event.id) },
                                modifier = Modifier.testTag("delete_custom_exam_${item.event.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الاختبار",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "حذف الموعد المخصص",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }

    if (showAddExamDialog) {
        AddCustomExamDialog(
            onDismiss = { showAddExamDialog = false },
            onConfirm = { title, subtitle, category, days ->
                onAddCustomExam(title, subtitle, category, days)
                showAddExamDialog = false
            }
        )
    }

    if (showBookingDialog) {
        ComputerizedBookingDialog(
            onDismiss = { showBookingDialog = false },
            onSave = { examType, title, days ->
                onSaveComputerizedBooking(examType, title, days)
                showBookingDialog = false
            }
        )
    }

    if (showAddQuizDialog) {
        AddShortQuizDialog(
            onDismiss = { showAddQuizDialog = false },
            onSave = { subject, note, days ->
                onAddShortSchoolQuiz(subject, note, days)
                showAddQuizDialog = false
            }
        )
    }
}

private fun triggerGentleMinuteVibration(context: Context) {
    runCatching {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(120L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120L)
            }
        }
    }
}

@Composable
private fun QiyasBannerHeader(uiState: AcademicUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("qiyas_banner_header"),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(TahsiliPurple, Color(0xFF283593))
                    )
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_academic_1794604236283),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.18f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldAccent
                    )
                    Text(
                        text = "مركز اختبارات قياس والأدوات الذكية 1448 هـ",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "التحصيلي • القدرات العامة • STEP • اختبارات المدرسة",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "المصدر: هيئة تقويم التعليم والتدريب - قياس (عدادات تنازلية بالثواني مع التواريخ الهجرية والميلادية)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SmartTrainingToolsSection(
    tahsiliPagesInput: String,
    tahsiliDailyTarget: String,
    speedTimer: QiyasSpeedTimerState,
    onUpdateTahsiliPages: (String) -> Unit,
    onUpdateSpeedTimerQuestions: (String) -> Unit,
    onToggleSpeedTimer: () -> Unit,
    onResetSpeedTimer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Tool 1: مُخطط ختم التحصيلي
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tahsili_study_planner_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = TahsiliPurple.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.dp, TahsiliPurple.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = TahsiliPurple
                    )
                    Column {
                        Text(
                            text = "أداة مُخطط ختم التحصيلي",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TahsiliPurple
                        )
                        Text(
                            text = "أدخل عدد الصفحات أو الدروس المتبقية ليحسب التطبيق معدل الإنجاز اليومي حتى الفترة الأولى",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tahsiliPagesInput,
                        onValueChange = onUpdateTahsiliPages,
                        label = { Text("الصفحات / الدروس المتبقية") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tahsili_pages_input")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TahsiliPurple.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "المعدل المطلوب: $tahsiliDailyTarget",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TahsiliPurple,
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("tahsili_daily_target_text")
                    )
                }
            }
        }

        // Tool 2: مؤقت سرعة قياس (60 ثانية لكل سؤال + اهتزاز عند نهاية كل دقيقة)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qiyas_speed_timer_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, HijriAmber.copy(alpha = 0.45f))
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = HijriAmber
                        )
                        Column {
                            Text(
                                text = "مؤقت سرعة قياس (60 ثانية لكل سؤال)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "اهتزاز خفيف تلقائي عند انتهاء كل دقيقة لتدريبك على سرعة الحل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = speedTimer.questionCountInput,
                        onValueChange = onUpdateSpeedTimerQuestions,
                        label = { Text("عدد الأسئلة التدريبية") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speed_timer_questions_input")
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = HijriAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, HijriAmber)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = speedTimer.formattedRemaining,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = HijriAmber,
                                modifier = Modifier.testTag("speed_timer_display")
                            )
                            Text(
                                text = "السؤال ${speedTimer.currentQuestionIndex}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val progress = if (speedTimer.totalSeconds > 0) {
                    speedTimer.remainingSeconds.toFloat() / speedTimer.totalSeconds.toFloat()
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = HijriAmber
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onToggleSpeedTimer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (speedTimer.isRunning) Color(0xFFC62828) else SaudiGreenPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speed_timer_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (speedTimer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Text(
                            text = if (speedTimer.isRunning) "إيقاف مؤقت" else "بدء التدريب",
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onResetSpeedTimer,
                        modifier = Modifier.testTag("speed_timer_reset_btn")
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null)
                        Text("إعادة ضبط", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepAndComputerizedBookingCard(
    uiState: AcademicUiState,
    onOpenBookingDialog: () -> Unit,
    onClearBooking: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_and_booking_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color(0xFF00796B)
                )
                Column {
                    Text(
                        text = "بطاقة كفايات اللغة الإنجليزية (STEP) وحجز المحوسب",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00796B)
                    )
                    Text(
                        text = "المصدر: هيئة تقويم التعليم والتدريب - قياس • صلاحية الاختبار: 3 سنوات من تاريخ الاختبار",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // STEP Weight Distribution Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StepSectionChip("فهم المقروء 40%", Modifier.weight(1f))
                StepSectionChip("التراكيب النحوية 40%", Modifier.weight(1f))
                StepSectionChip("فهم المسموع 20%", Modifier.weight(1f))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            val booking = uiState.computerizedBooking
            val countdown = uiState.personalComputerizedCountdown
            if (booking != null && countdown != null) {
                Text(
                    text = "موعد اختبارك المحوسب المسجل: ${booking.examTitle}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                LiveFourUnitCountdownRow(
                    days = countdown.daysRemaining,
                    hours = countdown.hoursRemaining,
                    minutes = countdown.minutesRemaining,
                    seconds = countdown.secondsRemaining,
                    accentColor = Color(0xFF00796B)
                )
                Text(
                    text = "${booking.hijriDateDisplay} | ${booking.gregorianDateDisplay}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenBookingDialog,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تغيير تاريخ الحجز")
                    }
                    TextButton(onClick = onClearBooking) {
                        Text("إلغاء الحجز", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                Button(
                    onClick = onOpenBookingDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("set_computerized_booking_btn")
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Text(
                        text = "تسجيل موعد اختبارك المحوسب (قدرات / STEP) للعد التنازلي بالثواني",
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepSectionChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF00796B).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF00796B).copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00796B),
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp)
        )
    }
}

@Composable
private fun ShortSchoolQuizzesSection(
    quizzes: List<ShortSchoolQuizItem>,
    onOpenAddQuizDialog: () -> Unit,
    onDeleteQuiz: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("short_school_quizzes_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "جدول الاختبارات المدرسية القصيرة / الفترات",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "سجل اختباراتك الشهرية والفترات المدرسية لمتابعة الأيام المتبقية لكل مادة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onOpenAddQuizDialog,
                    modifier = Modifier.testTag("add_short_quiz_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("إضافة مادة", modifier = Modifier.padding(start = 4.dp))
                }
            }

            quizzes.forEach { quiz ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${quiz.subjectName} • ${quiz.note}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${quiz.hijriDateDisplay} | ${quiz.gregorianDateDisplay}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SaudiGreenPrimary.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "متبقي ${quiz.daysRemaining} يوم",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SaudiGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteQuiz(quiz.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف اختبار المادة",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComputerizedBookingDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var selectedType by remember { mutableStateOf("قدرات محوسب") }
    var title by remember { mutableStateOf("اختبار القدرات العامة (محوسب)") }
    var daysStr by remember { mutableStateOf("21") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تسجيل موعد اختبار محوسب (قدرات / STEP)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("قدرات محوسب", "اختبار STEP").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                title = if (type.contains("STEP")) {
                                    "اختبار كفايات اللغة الإنجليزية STEP"
                                } else {
                                    "اختبار القدرات العامة (محوسب)"
                                }
                            },
                            label = { Text(type) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الموعد") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysStr,
                    onValueChange = { daysStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("الأيام المتبقية حتى موعد اختبارك") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysStr.toLongOrNull() ?: 14L
                    onSave(selectedType, title, days)
                }
            ) {
                Text("حفظ وتفعيل العداد بالثواني")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun AddShortQuizDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("اختبار الفترة") }
    var daysStr by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة اختبار مدرسي قصير / فترة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("اسم المادة (مثال: الكيمياء / الرياضيات)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("الفترة أو الوصف (مثال: اختبار الفترة الأولى)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = daysStr,
                    onValueChange = { daysStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("الأيام المتبقية حتى الاختبار") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysStr.toLongOrNull() ?: 7L
                    onSave(subject, note, days)
                },
                enabled = subject.isNotBlank()
            ) {
                Text("حفظ الاختبار")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun AddCustomExamDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, EventCategory, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf(EventCategory.CUSTOM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة اختبار مخصص للعداد التنازلي",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الاختبار (مثال: قدرات محوسب - محاولتي الثانية)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exam_title_input")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("ملاحظة أو تفاصيل القاعة/المقر") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exam_subtitle_input")
                )

                OutlinedTextField(
                    value = daysInput,
                    onValueChange = { daysInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("بعد كم يوم من اليوم؟") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exam_days_input")
                )

                Text(
                    text = "تصنيف الاختبار:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        EventCategory.TAHSILI,
                        EventCategory.QUDURAT,
                        EventCategory.STEP,
                        EventCategory.CUSTOM
                    ).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.arabicLabel) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = daysInput.toLongOrNull() ?: 14L
                    onConfirm(title, subtitle, category, days)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_custom_exam_btn")
            ) {
                Text("حفظ وإضافة للعداد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
