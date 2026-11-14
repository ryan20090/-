package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aistudio.saudiacademic.masar.R
import com.example.model.MajorFieldCategory
import com.example.model.StudyTaskEntity
import com.example.model.UniversityMajorEvaluation
import com.example.model.WeightedScenarioEntity
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HijriAmber
import com.example.ui.theme.SaudiGreenDark
import com.example.ui.theme.SaudiGreenPrimary
import com.example.ui.theme.TahsiliPurple
import com.example.viewmodel.AcademicUiState
import java.util.Locale

@Composable
fun StudyAndWeightedScreen(
    uiState: AcademicUiState,
    onUpdateMasaratGrades: (String, String, String) -> Unit,
    onAdoptMasaratGpa: () -> Unit,
    onUpdateWeightedInputs: (String, String, String, String, String) -> Unit,
    onApplyPreset: (String, Int, Int, Int, Int) -> Unit,
    onSaveScenario: (String) -> Unit,
    onDeleteScenario: (Int) -> Unit,
    onToggleEligibleMajorsFilter: (Boolean) -> Unit,
    onSelectMajorField: (MajorFieldCategory) -> Unit,
    onSelectUniversityRegion: (String?) -> Unit,
    onAddStudyTask: (String, String, Long, Int) -> Unit,
    onToggleStudyTask: (Int, Boolean) -> Unit,
    onDeleteStudyTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableIntStateOf(0) } // 0 = Weighted & Universities, 1 = Study Tasks
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("study_weighted_segmented_row")
        ) {
            SegmentedButton(
                selected = activeSection == 0,
                onClick = { activeSection = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                label = { Text("الموزونة ومستشار القبول") },
                modifier = Modifier.testTag("tab_weighted_calculator")
            )
            SegmentedButton(
                selected = activeSection == 1,
                onClick = { activeSection = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                icon = { Icon(Icons.Default.TaskAlt, contentDescription = null) },
                label = { Text("خطة المذاكرة (${uiState.studyTasks.count { !it.isCompleted }})") },
                modifier = Modifier.testTag("tab_study_planner")
            )
        }

        if (activeSection == 0) {
            WeightedAndUniversityAdvisorContent(
                uiState = uiState,
                onUpdateMasaratGrades = onUpdateMasaratGrades,
                onAdoptMasaratGpa = onAdoptMasaratGpa,
                onUpdateWeightedInputs = onUpdateWeightedInputs,
                onApplyPreset = onApplyPreset,
                onSaveScenario = onSaveScenario,
                onDeleteScenario = onDeleteScenario,
                onToggleEligibleMajorsFilter = onToggleEligibleMajorsFilter,
                onSelectMajorField = onSelectMajorField,
                onSelectUniversityRegion = onSelectUniversityRegion
            )
        } else {
            StudyPlannerSection(
                tasks = uiState.studyTasks,
                onOpenAddDialog = { showAddTaskDialog = true },
                onToggleTask = onToggleStudyTask,
                onDeleteTask = onDeleteStudyTask
            )
        }
    }

    if (showAddTaskDialog) {
        AddStudyTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, subject, days, priority ->
                onAddStudyTask(title, subject, days, priority)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun WeightedAndUniversityAdvisorContent(
    uiState: AcademicUiState,
    onUpdateMasaratGrades: (String, String, String) -> Unit,
    onAdoptMasaratGpa: () -> Unit,
    onUpdateWeightedInputs: (String, String, String, String, String) -> Unit,
    onApplyPreset: (String, Int, Int, Int, Int) -> Unit,
    onSaveScenario: (String) -> Unit,
    onDeleteScenario: (Int) -> Unit,
    onToggleEligibleMajorsFilter: (Boolean) -> Unit,
    onSelectMajorField: (MajorFieldCategory) -> Unit,
    onSelectUniversityRegion: (String?) -> Unit
) {
    val calc = uiState.weightedCalc
    var customScenarioTitle by remember { mutableStateOf("") }
    var customW1 by remember(calc.hsWeight) { mutableStateOf(calc.hsWeight.toString()) }
    var customW2 by remember(calc.quduratWeight) { mutableStateOf(calc.quduratWeight.toString()) }
    var customW3 by remember(calc.tahsiliWeight) { mutableStateOf(calc.tahsiliWeight.toString()) }
    var customWStep by remember(calc.stepWeight) { mutableStateOf(calc.stepWeight.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weighted_calculator_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // 1. Masarat High School Cumulative GPA Calculator (20% + 40% + 40%)
        item {
            MasaratCumulativeGpaCard(
                g1 = calc.hsGrade1,
                g2 = calc.hsGrade2,
                g3 = calc.hsGrade3,
                formattedCumulative = calc.formattedMasaratGpa,
                onUpdateGrades = onUpdateMasaratGrades,
                onAdoptGpa = onAdoptMasaratGpa
            )
        }

        // 2. Main Weighted Score Banner + Inputs (HS, Qudurat, Tahsili, STEP)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weighted_result_hero_card"),
                shape = RoundedCornerShape(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SaudiGreenDark, TahsiliPurple)
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
                            .height(175.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "النسبة الموزونة الموحدة وحاسبة الهدف العكسي",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = calc.presetName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ثانوية ${calc.hsWeight}% • قدرات ${calc.quduratWeight}% • تحصيلي ${calc.tahsiliWeight}%" +
                                        if (calc.stepWeight > 0) " • STEP ${calc.stepWeight}%" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Text(
                                text = calc.formattedScore,
                                style = MaterialTheme.typography.displayMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("weighted_score_display")
                            )
                        }
                    }
                }
            }
        }

        // 3. Unified Student Scores Input Card + Reverse Target Calculator ("كم أحتاج في التحصيلي؟")
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "درجات الطالب (تُحفظ تلقائياً في مساحتك الشخصية المستقلة)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = calc.highSchoolGpa,
                            onValueChange = {
                                onUpdateWeightedInputs(
                                    it,
                                    calc.quduratScore,
                                    calc.tahsiliScore,
                                    calc.stepScore,
                                    calc.targetGoalScore
                                )
                            },
                            label = { Text("معدل الثانوية %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_hs_gpa")
                        )

                        OutlinedTextField(
                            value = calc.quduratScore,
                            onValueChange = {
                                onUpdateWeightedInputs(
                                    calc.highSchoolGpa,
                                    it,
                                    calc.tahsiliScore,
                                    calc.stepScore,
                                    calc.targetGoalScore
                                )
                            },
                            label = { Text("درجة القدرات") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_qudurat_score")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = calc.tahsiliScore,
                            onValueChange = {
                                onUpdateWeightedInputs(
                                    calc.highSchoolGpa,
                                    calc.quduratScore,
                                    it,
                                    calc.stepScore,
                                    calc.targetGoalScore
                                )
                            },
                            label = { Text("درجة التحصيلي") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_tahsili_score")
                        )

                        OutlinedTextField(
                            value = calc.stepScore,
                            onValueChange = {
                                onUpdateWeightedInputs(
                                    calc.highSchoolGpa,
                                    calc.quduratScore,
                                    calc.tahsiliScore,
                                    it,
                                    calc.targetGoalScore
                                )
                            },
                            label = { Text("درجة STEP (اختياري)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_step_score")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    // Reverse Target Calculator ("كم أحتاج في التحصيلي؟")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = TahsiliPurple
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "حاسبة الهدف العكسية: كم أحتاج في التحصيلي؟",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TahsiliPurple
                            )
                            Text(
                                text = "أدخل النسبة الموزونة المستهدفة لمعرفة الدرجة المطلوبة في التحصيلي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedTextField(
                            value = calc.targetGoalScore,
                            onValueChange = {
                                onUpdateWeightedInputs(
                                    calc.highSchoolGpa,
                                    calc.quduratScore,
                                    calc.tahsiliScore,
                                    calc.stepScore,
                                    it
                                )
                            },
                            label = { Text("الموزونة المستهدفة %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("input_target_goal_score")
                        )
                    }

                    val reqTahsili = calc.requiredTahsiliForGoal
                    val reqBannerColor = when {
                        reqTahsili <= 0.0 -> SaudiGreenPrimary
                        reqTahsili <= 100.0 -> TahsiliPurple
                        else -> Color(0xFFC62828)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = reqBannerColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, reqBannerColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val message = when {
                            reqTahsili <= 0.0 ->
                                "حققت الموزونة المستهدفة (${calc.targetGoalDouble}%) بالفعل بدرجاتك الحالية!"
                            reqTahsili <= 100.0 ->
                                String.format(
                                    Locale.ENGLISH,
                                    "للوصول إلى موزونة %.2f%% تحتاج على الأقل إلى %.1f في الاختبار التحصيلي.",
                                    calc.targetGoalDouble,
                                    reqTahsili
                                )
                            else ->
                                String.format(
                                    Locale.ENGLISH,
                                    "الوصول إلى %.2f%% يتطلب %.1f في التحصيلي (أعلى من 100)؛ يُنصح برفع درجة القدرات أيضاً.",
                                    calc.targetGoalDouble,
                                    reqTahsili
                                )
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = reqBannerColor,
                            modifier = Modifier
                                .padding(12.dp)
                                .testTag("reverse_tahsili_result_text")
                        )
                    }
                }
            }
        }

        // 4. All Saudi University Formulas Calculated Simultaneously
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("all_university_formulas_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "موزونتك المحسوبة فوراً لجميع معادلات الجامعات السعودية:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    calc.allUniversityFormulasScores.forEach { (formula, score) ->
                        val isCurrent = calc.presetName == formula.arabicLabel
                        Surface(
                            onClick = {
                                onApplyPreset(
                                    formula.arabicLabel,
                                    formula.hsWeight,
                                    formula.quduratWeight,
                                    formula.tahsiliWeight,
                                    formula.stepWeight
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                            border = if (isCurrent) {
                                BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                            } else null,
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
                                        text = formula.arabicLabel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formula.exampleUniversities,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = String.format(Locale.ENGLISH, "%.2f%%", score),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Custom Manual Weights Row
                    Text(
                        text = "تخصيص الأوزان يدوياً (ثانوية / قدرات / تحصيلي / STEP):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customW1,
                            onValueChange = { customW1 = it.filter { ch -> ch.isDigit() } },
                            label = { Text("ث%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customW2,
                            onValueChange = { customW2 = it.filter { ch -> ch.isDigit() } },
                            label = { Text("ق%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customW3,
                            onValueChange = { customW3 = it.filter { ch -> ch.isDigit() } },
                            label = { Text("ت%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customWStep,
                            onValueChange = { customWStep = it.filter { ch -> ch.isDigit() } },
                            label = { Text("STEP%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val w1 = customW1.toIntOrNull() ?: 30
                                val w2 = customW2.toIntOrNull() ?: 30
                                val w3 = customW3.toIntOrNull() ?: 40
                                val w4 = customWStep.toIntOrNull() ?: 0
                                onApplyPreset("تخصيص يدوي ($w1/$w2/$w3/$w4)", w1, w2, w3, w4)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تطبيق الأوزان المخصصة")
                        }
                        Button(
                            onClick = {
                                onSaveScenario(customScenarioTitle)
                                customScenarioTitle = ""
                            },
                            modifier = Modifier.testTag("save_scenario_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("حفظ السيناريو", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }

        // 5. Smart University Admissions Advisor & Filter ("المستشار والفلتر الذكي للقبول")
        item {
            UniversityAdvisorHeaderCard(
                filterOnlyEligible = uiState.filterOnlyEligibleMajors,
                selectedField = uiState.selectedMajorField,
                selectedRegion = uiState.selectedUniversityRegion,
                totalMatched = uiState.universityEvaluations.size,
                onToggleEligibleFilter = onToggleEligibleMajorsFilter,
                onSelectField = onSelectMajorField,
                onSelectRegion = onSelectUniversityRegion
            )
        }

        // 6. University Admission Evaluation Cards
        items(
            items = uiState.universityEvaluations,
            key = { it.admission.id }
        ) { evalItem ->
            UniversityAdmissionEvaluationCard(evaluation = evalItem)
        }

        // 7. Saved Scenarios
        if (uiState.weightedScenarios.isNotEmpty()) {
            item {
                Text(
                    text = "السيناريوهات المحفوظة للمقارنة (${uiState.weightedScenarios.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(
                items = uiState.weightedScenarios,
                key = { it.id }
            ) { scenario ->
                SavedScenarioCard(
                    scenario = scenario,
                    onDelete = { onDeleteScenario(scenario.id) }
                )
            }
        }
    }
}

@Composable
private fun MasaratCumulativeGpaCard(
    g1: String,
    g2: String,
    g3: String,
    formattedCumulative: String,
    onUpdateGrades: (String, String, String) -> Unit,
    onAdoptGpa: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("masarat_gpa_calculator_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SaudiGreenPrimary.copy(alpha = 0.07f)
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = SaudiGreenPrimary
                )
                Column {
                    Text(
                        text = "حاسبة المعدل التراكمي للثانوية (نظام المسارات)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SaudiGreenPrimary
                    )
                    Text(
                        text = "المعادلة الرسمية: 20% أول ثانوي + 40% ثاني ثانوي + 40% ثالث ثانوي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = g1,
                    onValueChange = { onUpdateGrades(it, g2, g3) },
                    label = { Text("أول ثانوي (20%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("masarat_g1_input")
                )
                OutlinedTextField(
                    value = g2,
                    onValueChange = { onUpdateGrades(g1, it, g3) },
                    label = { Text("ثاني ثانوي (40%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("masarat_g2_input")
                )
                OutlinedTextField(
                    value = g3,
                    onValueChange = { onUpdateGrades(g1, g2, it) },
                    label = { Text("ثالث ثانوي (40%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("masarat_g3_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المعدل التراكمي المحسوب:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedCumulative,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = SaudiGreenPrimary,
                        modifier = Modifier.testTag("masarat_cumulative_result")
                    )
                }

                Button(
                    onClick = onAdoptGpa,
                    colors = ButtonDefaults.buttonColors(containerColor = SaudiGreenPrimary),
                    modifier = Modifier.testTag("adopt_masarat_gpa_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("اعتماد في حاسبة الموزونة", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun UniversityAdvisorHeaderCard(
    filterOnlyEligible: Boolean,
    selectedField: MajorFieldCategory,
    selectedRegion: String?,
    totalMatched: Int,
    onToggleEligibleFilter: (Boolean) -> Unit,
    onSelectField: (MajorFieldCategory) -> Unit,
    onSelectRegion: (String?) -> Unit
) {
    val regions = listOf("الرياض", "مكة المكرمة", "الشرقية", "المدينة المنورة", "القصيم", "عسير", "تبوك")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("university_advisor_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
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
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "المستشار والفلتر الذكي للقبول الجامعي ($totalMatched تخصص)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "يقارن موزونتك المحسوبة بمعادلة كل جامعة مع آخر نسب قبول معلنة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Mandatory Disclaimer Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HijriAmber.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, HijriAmber.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("university_disclaimer_banner")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = HijriAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "تنبيه: النسب المعروضة تمثل آخر نسب قبول معلنة للعام الماضي وتُستخدم للاسترشاد فقط، ويتم تحديث قاعدة البيانات شهرياً.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Quick Filter Switch: "اعرض التخصصات المتاحة لموزونتي الحالية فقط"
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اعرض التخصصات المتاحة لموزونتي الحالية فقط",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = filterOnlyEligible,
                        onCheckedChange = onToggleEligibleFilter,
                        modifier = Modifier.testTag("filter_eligible_majors_switch")
                    )
                }
            }

            // Major Field Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(MajorFieldCategory.entries) { field ->
                    FilterChip(
                        selected = selectedField == field,
                        onClick = { onSelectField(field) },
                        label = { Text(field.arabicLabel) }
                    )
                }
            }

            // Geographic Region Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedRegion == null,
                        onClick = { onSelectRegion(null) },
                        label = { Text("كل المناطق") }
                    )
                }
                items(regions) { reg ->
                    FilterChip(
                        selected = selectedRegion == reg,
                        onClick = { onSelectRegion(if (selectedRegion == reg) null else reg) },
                        label = { Text(reg) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UniversityAdmissionEvaluationCard(
    evaluation: UniversityMajorEvaluation
) {
    val adm = evaluation.admission
    val isEligible = evaluation.isEligibleByLastYear
    val badgeColor = if (isEligible) SaudiGreenPrimary else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("uni_eval_card_${adm.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = adm.universityName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = adm.majorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "المنطقة: ${adm.regionName} • ${evaluation.formulaEnum.arabicLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "نسبة العام الماضي (${adm.lastYearCohortHijri})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.ENGLISH, "%.2f%%", adm.lastYearMinScore),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (adm.stepRequirementNote.isNotBlank()) {
                Text(
                    text = "شرط STEP: ${adm.stepRequirementNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF00796B),
                    fontWeight = FontWeight.Medium
                )
            }

            // Comparison Status Badge (Green if >= lastYearMinScore, Red/Amber if below)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = badgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = evaluation.comparisonBadgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format(Locale.ENGLISH, "موزونتك: %.2f%%", evaluation.studentCalculatedScore),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = adm.sourcePlatform,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SavedScenarioCard(
    scenario: WeightedScenarioEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = scenario.scenarioTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ثانوية ${scenario.highSchoolGpa}% (${scenario.hsWeight}%) • قدرات ${scenario.quduratScore} (${scenario.quduratWeight}%) • تحصيلي ${scenario.tahsiliScore} (${scenario.tahsiliWeight}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SaudiGreenPrimary.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = String.format(Locale.ENGLISH, "%.2f%%", scenario.calculatedWeighted),
                        style = MaterialTheme.typography.titleMedium,
                        color = SaudiGreenPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف السيناريو",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyPlannerSection(
    tasks: List<StudyTaskEntity>,
    onOpenAddDialog: () -> Unit,
    onToggleTask: (Int, Boolean) -> Unit,
    onDeleteTask: (Int) -> Unit
) {
    val completedCount = tasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_tasks_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "جدول متابعة المذاكرة (قدرات • تحصيلي • مدرسة)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "أنجزت $completedCount من أصل ${tasks.size} مهام دراسية مسجلة",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = onOpenAddDialog,
                        modifier = Modifier.testTag("add_study_task_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("مهمة جديدة", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }

        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            StudyTaskRowCard(
                task = task,
                onToggle = { checked -> onToggleTask(task.id, checked) },
                onDelete = { onDeleteTask(task.id) }
            )
        }
    }
}

@Composable
private fun StudyTaskRowCard(
    task: StudyTaskEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priorityLevel) {
        1 -> Color(0xFFC62828)
        2 -> HijriAmber
        else -> SaudiGreenPrimary
    }
    val priorityLabel = when (task.priorityLevel) {
        1 -> "أولوية قصوى"
        2 -> "متوسطة"
        else -> "عادية"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("study_task_checkbox_${task.id}")
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TahsiliPurple.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = task.subjectTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = TahsiliPurple,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_study_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف المهمة",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddStudyTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("تحصيلي") }
    var days by remember { mutableStateOf("7") }
    var priority by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مهمة مذاكرة جديدة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة (مثال: مراجعة تجميعات الكيمياء)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input")
                )

                Text("القسم الدراسي:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("تحصيلي", "قدرات", "اختبارات المدرسة", "STEP").forEach { tag ->
                        FilterChip(
                            selected = subject == tag,
                            onClick = { subject = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter { ch -> ch.isDigit() } },
                    label = { Text("الهدف خلال كم يوم؟") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("مستوى الأولوية:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "قصوى", 2 to "متوسطة", 3 to "عادية").forEach { (lvl, label) ->
                        FilterChip(
                            selected = priority == lvl,
                            onClick = { priority = lvl },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = days.toLongOrNull() ?: 7L
                    onConfirm(title, subject, d, priority)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_task_btn")
            ) {
                Text("إضافة للجدول")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
