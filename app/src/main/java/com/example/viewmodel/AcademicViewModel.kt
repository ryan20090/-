package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AcademicRepository
import com.example.data.local.MasarDatabase
import com.example.data.local.StudentPersonalStorage
import com.example.model.AcademicEventEntity
import com.example.model.AcademicWeekInfo
import com.example.model.CalendarRegion
import com.example.model.ComputerizedExamBooking
import com.example.model.CountdownItem
import com.example.model.EventCategory
import com.example.model.GradeLevel
import com.example.model.MajorFieldCategory
import com.example.model.PlatformSyncEntity
import com.example.model.ShortSchoolQuizItem
import com.example.model.StudyTaskEntity
import com.example.model.UniversityAdmissionEntity
import com.example.model.UniversityFormulaType
import com.example.model.UniversityMajorEvaluation
import com.example.model.WeightedScenarioEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeightedCalcState(
    val hsGrade1: String = "98.0",
    val hsGrade2: String = "98.5",
    val hsGrade3: String = "99.0",
    val highSchoolGpa: String = "98.60",
    val quduratScore: String = "92",
    val tahsiliScore: String = "90",
    val stepScore: String = "85",
    val targetGoalScore: String = "94.0",
    val hsWeight: Int = 30,
    val quduratWeight: Int = 30,
    val tahsiliWeight: Int = 40,
    val stepWeight: Int = 0,
    val presetName: String = "المعادلة العامة (30% ث + 30% ق + 40% ت)"
) {
    val hsDouble: Double get() = highSchoolGpa.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    val quduratDouble: Double get() = quduratScore.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    val tahsiliDouble: Double get() = tahsiliScore.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    val stepDouble: Double get() = stepScore.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    val targetGoalDouble: Double get() = targetGoalScore.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 90.0

    val masaratCumulativeGpa: Double
        get() = AcademicRepository.calculateMasaratCumulativeGpa(
            grade1Score = hsGrade1.toDoubleOrNull() ?: 0.0,
            grade2Score = hsGrade2.toDoubleOrNull() ?: 0.0,
            grade3Score = hsGrade3.toDoubleOrNull() ?: 0.0
        )

    val formattedMasaratGpa: String
        get() = String.format(Locale.ENGLISH, "%.2f%%", masaratCumulativeGpa)

    val computedScore: Double
        get() {
            val totalWeight = (hsWeight + quduratWeight + tahsiliWeight + stepWeight).coerceAtLeast(1)
            return ((hsDouble * hsWeight) + (quduratDouble * quduratWeight) +
                (tahsiliDouble * tahsiliWeight) + (stepDouble * stepWeight)) / totalWeight.toDouble()
        }

    val formattedScore: String
        get() = String.format(Locale.ENGLISH, "%.2f%%", computedScore)

    val requiredTahsiliForGoal: Double
        get() = AcademicRepository.calculateRequiredTahsiliScore(
            hsGpa = hsDouble,
            quduratScore = quduratDouble,
            stepScore = stepDouble,
            targetWeighted = targetGoalDouble,
            hsWeight = hsWeight,
            quduratWeight = quduratWeight,
            tahsiliWeight = tahsiliWeight.coerceAtLeast(1),
            stepWeight = stepWeight
        )

    val allUniversityFormulasScores: List<Pair<UniversityFormulaType, Double>>
        get() = UniversityFormulaType.entries.map { formula ->
            formula to formula.calculate(hsDouble, quduratDouble, tahsiliDouble, stepDouble)
        }
}

data class QiyasSpeedTimerState(
    val questionCountInput: String = "25",
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false
) {
    val currentQuestionIndex: Int
        get() {
            val totalQ = (totalSeconds / 60).coerceAtLeast(1)
            val elapsedQ = ((totalSeconds - remainingSeconds) / 60) + 1
            return elapsedQ.coerceIn(1, totalQ)
        }

    val formattedRemaining: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return String.format(Locale.ENGLISH, "%02d:%02d", mins, secs)
        }
}

data class AcademicUiState(
    val selectedGrade: GradeLevel = GradeLevel.HIGH_3,
    val selectedRegion: CalendarRegion = CalendarRegion.GENERAL,
    val referenceDbBadge: String = MasarDatabase.MONTHLY_UPDATE_DATE_TEXT,
    val weekInfo: AcademicWeekInfo = AcademicRepository.calculateAcademicWeekInfo(LocalDate.now()),
    val nextVacation: CountdownItem? = null,
    val upcomingVacations: List<CountdownItem> = emptyList(),
    val nextSchoolExam: CountdownItem? = null,
    val yearEndCountdown: CountdownItem? = null,
    val primaryTahsiliExam: CountdownItem? = null,
    val secondTahsiliExam: CountdownItem? = null,
    val tahsiliRegistration: CountdownItem? = null,
    val personalComputerizedCountdown: CountdownItem? = null,
    val computerizedBooking: ComputerizedExamBooking? = null,
    val shortSchoolQuizzes: List<ShortSchoolQuizItem> = emptyList(),
    val tahsiliRemainingPagesInput: String = "280",
    val tahsiliDailyPagesTarget: String = "1.5 صفحة / درس يومياً",
    val speedTimer: QiyasSpeedTimerState = QiyasSpeedTimerState(),
    val allCountdowns: List<CountdownItem> = emptyList(),
    val qiyasAndCustomCountdowns: List<CountdownItem> = emptyList(),
    val selectedHomeFilter: EventCategory? = null,
    val studyTasks: List<StudyTaskEntity> = emptyList(),
    val platformSyncs: List<PlatformSyncEntity> = emptyList(),
    val weightedScenarios: List<WeightedScenarioEntity> = emptyList(),
    val weightedCalc: WeightedCalcState = WeightedCalcState(),
    val universityEvaluations: List<UniversityMajorEvaluation> = emptyList(),
    val filterOnlyEligibleMajors: Boolean = false,
    val selectedMajorField: MajorFieldCategory = MajorFieldCategory.ALL,
    val selectedUniversityRegion: String? = null,
    val isSyncing: Boolean = false,
    val syncBannerMessage: String? = null
)

class AcademicViewModel(
    private val repository: AcademicRepository,
    private val personalStorage: StudentPersonalStorage = StudentPersonalStorage(null)
) : ViewModel() {

    private val _selectedGrade = MutableStateFlow(personalStorage.selectedGrade)
    private val _selectedRegion = MutableStateFlow(personalStorage.selectedRegion)
    private val _selectedHomeFilter = MutableStateFlow<EventCategory?>(null)
    private val _weightedCalc = MutableStateFlow(
        WeightedCalcState(
            hsGrade1 = personalStorage.hsGrade1,
            hsGrade2 = personalStorage.hsGrade2,
            hsGrade3 = personalStorage.hsGrade3,
            highSchoolGpa = personalStorage.highSchoolGpa,
            quduratScore = personalStorage.quduratScore,
            tahsiliScore = personalStorage.tahsiliScore,
            stepScore = personalStorage.stepScore,
            targetGoalScore = personalStorage.targetWeightedGoal,
            hsWeight = personalStorage.customWeightHs,
            quduratWeight = personalStorage.customWeightQud,
            tahsiliWeight = personalStorage.customWeightTah,
            stepWeight = personalStorage.customWeightStep
        )
    )
    private val _computerizedBooking = MutableStateFlow(personalStorage.getComputerizedBooking())
    private val _shortQuizzes = MutableStateFlow(personalStorage.getShortSchoolQuizzes())
    private val _tahsiliPagesInput = MutableStateFlow(personalStorage.tahsiliRemainingPages)
    private val _speedTimer = MutableStateFlow(QiyasSpeedTimerState())
    private val _filterOnlyEligible = MutableStateFlow(false)
    private val _selectedMajorField = MutableStateFlow(MajorFieldCategory.ALL)
    private val _selectedUniRegion = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _syncBannerMessage = MutableStateFlow<String?>(null)
    private val _syncedHijriToday = MutableStateFlow<String?>(null)
    private val _clockTick = MutableStateFlow(LocalDateTime.now())

    private val _minuteVibrationEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val minuteVibrationEvents: SharedFlow<Unit> = _minuteVibrationEvents.asSharedFlow()

    private var speedTimerJob: Job? = null

    val uiState: StateFlow<AcademicUiState> = combine(
        _clockTick,
        repository.allEventsFlow,
        repository.universityAdmissionsFlow,
        repository.studyTasksFlow,
        repository.platformSyncsFlow
    ) { now, events, admissions, tasks, syncs ->
         RawDataBundle(now, events, admissions, tasks, syncs)
    }.combine(repository.weightedScenariosFlow) { bundle, scenarios ->
        buildFullUiState(bundle, scenarios)
    }.combine(_selectedGrade) { state, _ -> state }
        .combine(_selectedRegion) { state, _ -> state }
        .combine(_selectedHomeFilter) { state, _ -> state }
        .combine(_weightedCalc) { state, _ -> state }
        .combine(_computerizedBooking) { state, _ -> state }
        .combine(_shortQuizzes) { state, _ -> state }
        .combine(_tahsiliPagesInput) { state, _ -> state }
        .combine(_speedTimer) { state, _ -> state }
        .combine(_filterOnlyEligible) { state, _ -> state }
        .combine(_selectedMajorField) { state, _ -> state }
        .combine(_selectedUniRegion) { state, _ -> state }
        .combine(_isSyncing) { state, _ -> state }
        .combine(_syncBannerMessage) { state, _ -> state }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AcademicUiState(
                selectedGrade = _selectedGrade.value,
                selectedRegion = _selectedRegion.value,
                weightedCalc = _weightedCalc.value
            )
        )

    private data class RawDataBundle(
        val now: LocalDateTime,
        val events: List<AcademicEventEntity>,
        val admissions: List<UniversityAdmissionEntity>,
        val tasks: List<StudyTaskEntity>,
        val syncs: List<PlatformSyncEntity>
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded(_selectedRegion.value)
            triggerAutoSync(silent = true)
        }
        // 1-second live countdown ticker for seconds precision
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _clockTick.value = LocalDateTime.now()
            }
        }
    }

    private fun buildFullUiState(
        bundle: RawDataBundle,
        scenarios: List<WeightedScenarioEntity>
    ): AcademicUiState {
        val now = bundle.now
        val today = now.toLocalDate()
        val grade = _selectedGrade.value
        val region = _selectedRegion.value
        val calc = _weightedCalc.value

        val weekInfo = AcademicRepository.calculateAcademicWeekInfo(
            today = today,
            overrideHijriToday = _syncedHijriToday.value,
            region = region
        )

        val allItems = bundle.events.map { AcademicRepository.buildCountdownItem(it, now) }

        val gradeFiltered = allItems.filter { item ->
            if (grade.isHighSchool) {
                true
            } else {
                !item.event.highSchoolOnly || item.event.isCustom
            }
        }

        val upcomingVacations = allItems.filter {
            it.categoryEnum == EventCategory.VACATION && !it.isPast
        }
        val nextVacation = upcomingVacations.firstOrNull()

        val upcomingSchoolExams = allItems.filter {
            it.categoryEnum == EventCategory.SCHOOL_EXAM && !it.isPast
        }
        val nextSchoolExam = upcomingSchoolExams.firstOrNull()

        val yearEndItem = allItems.find { it.event.id == "moe_1448_summer_vacation" }
            ?: allItems.lastOrNull()

        val primaryTahsili = allItems.find { it.event.id == "qiyas_tahsili_period_1" }
            ?: allItems.firstOrNull { it.categoryEnum == EventCategory.TAHSILI && !it.isPast }
        val secondTahsili = allItems.find { it.event.id == "qiyas_tahsili_period_2" }
        val tahsiliReg = allItems.find { it.event.id == "qiyas_tahsili_reg_open" }

        // Personal Computerized Exam (Qudurat or STEP) live countdown by seconds
        val booking = _computerizedBooking.value
        val personalCompCountdown = booking?.let { b ->
            val entity = AcademicEventEntity(
                id = "personal_comp_booking",
                title = b.examTitle,
                subtitle = "موعد اختبار الطالب المحوسب (${b.examType})",
                category = if (b.examType.contains("STEP")) EventCategory.STEP.name else EventCategory.QUDURAT.name,
                startDateIso = b.examDateIso,
                endDateIso = null,
                hijriDateDisplay = b.hijriDateDisplay,
                gregorianDateDisplay = b.gregorianDateDisplay,
                sourcePlatform = "المصدر: حجز الطالب في قياس",
                highSchoolOnly = true
            )
            AcademicRepository.buildCountdownItem(entity, now)
        }

        // Short school quizzes with days remaining
        val quizzesWithDays = _shortQuizzes.value.map { q ->
            val qDate = runCatching { LocalDate.parse(q.examDateIso) }.getOrDefault(today)
            val daysRem = ChronoUnit.DAYS.between(today, qDate).coerceAtLeast(0L)
            q.copy(daysRemaining = daysRem)
        }.sortedBy { it.examDateIso }

        // Tahsili study planner daily target
        val pagesCount = _tahsiliPagesInput.value.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 280.0
        val daysToTahsiliP1 = (primaryTahsili?.daysRemaining ?: 190L).coerceAtLeast(1L)
        val dailyRate = pagesCount / daysToTahsiliP1.toDouble()
        val dailyTargetStr = String.format(
            Locale.ENGLISH,
            "%.1f صفحة أو درس يومياً (خلال %d يوم متبقي للفترة الأولى)",
            dailyRate,
            daysToTahsiliP1
        )

        val qiyasAndCustom = allItems.filter {
            it.categoryEnum in setOf(
                EventCategory.TAHSILI,
                EventCategory.QUDURAT,
                EventCategory.STEP,
                EventCategory.CUSTOM
            )
        }

        // University Admissions Evaluation + Smart Filtering
        val allEvaluations = AcademicRepository.evaluateUniversityAdmissions(
            admissions = bundle.admissions,
            hsGpa = calc.hsDouble,
            qudurat = calc.quduratDouble,
            tahsili = calc.tahsiliDouble,
            step = calc.stepDouble
        )
        val filteredEvaluations = allEvaluations.filter { ev ->
            val passEligible = !_filterOnlyEligible.value || ev.isEligibleByLastYear
            val passField = _selectedMajorField.value == MajorFieldCategory.ALL ||
                ev.fieldEnum == _selectedMajorField.value
            val passRegion = _selectedUniRegion.value == null ||
                ev.admission.regionName == _selectedUniRegion.value
            passEligible && passField && passRegion
        }

        return AcademicUiState(
            selectedGrade = grade,
            selectedRegion = region,
            referenceDbBadge = MasarDatabase.MONTHLY_UPDATE_DATE_TEXT,
            weekInfo = weekInfo,
            nextVacation = nextVacation,
            upcomingVacations = upcomingVacations,
            nextSchoolExam = nextSchoolExam,
            yearEndCountdown = yearEndItem,
            primaryTahsiliExam = primaryTahsili,
            secondTahsiliExam = secondTahsili,
            tahsiliRegistration = tahsiliReg,
            personalComputerizedCountdown = personalCompCountdown,
            computerizedBooking = booking,
            shortSchoolQuizzes = quizzesWithDays,
            tahsiliRemainingPagesInput = _tahsiliPagesInput.value,
            tahsiliDailyPagesTarget = dailyTargetStr,
            speedTimer = _speedTimer.value,
            allCountdowns = gradeFiltered,
            qiyasAndCustomCountdowns = qiyasAndCustom,
            selectedHomeFilter = _selectedHomeFilter.value,
            studyTasks = bundle.tasks,
            platformSyncs = bundle.syncs,
            weightedScenarios = scenarios,
            weightedCalc = calc,
            universityEvaluations = filteredEvaluations,
            filterOnlyEligibleMajors = _filterOnlyEligible.value,
            selectedMajorField = _selectedMajorField.value,
            selectedUniversityRegion = _selectedUniRegion.value,
            isSyncing = _isSyncing.value,
            syncBannerMessage = _syncBannerMessage.value
        )
    }

    fun selectGrade(grade: GradeLevel) {
        _selectedGrade.value = grade
        personalStorage.selectedGrade = grade
        _clockTick.value = LocalDateTime.now()
    }

    fun selectRegion(region: CalendarRegion) {
        if (_selectedRegion.value == region) return
        _selectedRegion.value = region
        personalStorage.selectedRegion = region
        viewModelScope.launch {
            repository.updateRegionEvents(region)
            _clockTick.value = LocalDateTime.now()
        }
    }

    fun setHomeCategoryFilter(category: EventCategory?) {
        _selectedHomeFilter.value = category
        _clockTick.value = LocalDateTime.now()
    }

    fun triggerAutoSync(silent: Boolean = false) {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            if (!silent) {
                _syncBannerMessage.value = "جارٍ مزامنة التاريخ الهجري والتحقق من التوقيت..."
            }
            val result = repository.syncWithTrustedPlatforms(
                referenceDate = _clockTick.value.toLocalDate(),
                region = _selectedRegion.value
            )
            _syncedHijriToday.value = result.syncedHijriToday
            _isSyncing.value = false
            if (!silent) {
                _syncBannerMessage.value = result.message
            }
            _clockTick.value = LocalDateTime.now()
        }
    }

    // Computerized Exam Booking (Qudurat Computerized or STEP)
    fun saveComputerizedBooking(
        examType: String,
        examTitle: String,
        daysFromToday: Long
    ) {
        val targetDate = _clockTick.value.toLocalDate().plusDays(daysFromToday.coerceIn(1L, 365L))
        val hijri = AcademicRepository.formatLocalUmmAlQura(targetDate, includeWeekday = false)
        val greg = AcademicRepository.formatArabicGregorian(targetDate)
        val booking = ComputerizedExamBooking(
            examType = examType,
            examTitle = examTitle.ifBlank { "موعدي القادم في $examType" },
            examDateIso = targetDate.toString(),
            hijriDateDisplay = hijri,
            gregorianDateDisplay = greg
        )
        personalStorage.saveComputerizedBooking(booking)
        _computerizedBooking.value = booking
        _clockTick.value = LocalDateTime.now()
    }

    fun clearComputerizedBooking() {
        personalStorage.saveComputerizedBooking(null)
        _computerizedBooking.value = null
        _clockTick.value = LocalDateTime.now()
    }

    // Short School Quizzes (الاختبارات المدرسية القصيرة / الفترات)
    fun addShortSchoolQuiz(
        subjectName: String,
        note: String,
        daysFromToday: Long
    ) {
        if (subjectName.isBlank()) return
        val targetDate = _clockTick.value.toLocalDate().plusDays(daysFromToday.coerceIn(1L, 365L))
        val hijri = AcademicRepository.formatLocalUmmAlQura(targetDate, includeWeekday = false)
        val greg = AcademicRepository.formatArabicGregorian(targetDate)
        val newQuiz = ShortSchoolQuizItem(
            id = "quiz_${System.currentTimeMillis()}",
            subjectName = subjectName.trim(),
            examDateIso = targetDate.toString(),
            hijriDateDisplay = hijri,
            gregorianDateDisplay = greg,
            note = note.trim().ifBlank { "اختبار فترة مدرسي" }
        )
        val updated = _shortQuizzes.value + newQuiz
        personalStorage.saveShortSchoolQuizzes(updated)
        _shortQuizzes.value = updated
        _clockTick.value = LocalDateTime.now()
    }

    fun deleteShortSchoolQuiz(id: String) {
        val updated = _shortQuizzes.value.filterNot { it.id == id }
        personalStorage.saveShortSchoolQuizzes(updated)
        _shortQuizzes.value = updated
        _clockTick.value = LocalDateTime.now()
    }

    // Tahsili Study Planner (مُخطط ختم التحصيلي)
    fun updateTahsiliRemainingPages(pagesInput: String) {
        val cleaned = pagesInput.filter { it.isDigit() }
        personalStorage.tahsiliRemainingPages = cleaned
        _tahsiliPagesInput.value = cleaned
        _clockTick.value = LocalDateTime.now()
    }

    // Qiyas Speed Timer (مؤقت سرعة قياس — 60 ثانية لكل سؤال مع اهتزاز عند نهاية كل دقيقة)
    fun updateSpeedTimerQuestionCount(countInput: String) {
        val cleaned = countInput.filter { it.isDigit() }
        val qCount = (cleaned.toIntOrNull() ?: 1).coerceIn(1, 200)
        speedTimerJob?.cancel()
        _speedTimer.value = QiyasSpeedTimerState(
            questionCountInput = cleaned,
            totalSeconds = qCount * 60,
            remainingSeconds = qCount * 60,
            isRunning = false
        )
        _clockTick.value = LocalDateTime.now()
    }

    fun toggleSpeedTimerRunning() {
        val current = _speedTimer.value
        if (current.isRunning) {
            speedTimerJob?.cancel()
            _speedTimer.update { it.copy(isRunning = false) }
            _clockTick.value = LocalDateTime.now()
        } else {
            val startSecs = if (current.remainingSeconds <= 0) current.totalSeconds else current.remainingSeconds
            _speedTimer.update { it.copy(remainingSeconds = startSecs, isRunning = true) }
            speedTimerJob?.cancel()
            speedTimerJob = viewModelScope.launch {
                while (_speedTimer.value.isRunning && _speedTimer.value.remainingSeconds > 0) {
                    delay(1_000L)
                    val nextRem = (_speedTimer.value.remainingSeconds - 1).coerceAtLeast(0)
                    _speedTimer.update {
                        it.copy(
                            remainingSeconds = nextRem,
                            isRunning = nextRem > 0
                        )
                    }
                    // Vibrate gently at the end of every 60-second minute
                    if (nextRem % 60 == 0 && nextRem < _speedTimer.value.totalSeconds) {
                        _minuteVibrationEvents.tryEmit(Unit)
                    }
                }
            }
        }
    }

    fun resetSpeedTimer() {
        speedTimerJob?.cancel()
        _speedTimer.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false
            )
        }
        _clockTick.value = LocalDateTime.now()
    }

    fun addCustomExam(
        title: String,
        subtitle: String,
        category: EventCategory,
        daysFromToday: Long
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val targetDate = _clockTick.value.toLocalDate().plusDays(daysFromToday.coerceAtLeast(1))
            repository.addCustomExam(
                title = title,
                subtitle = subtitle,
                category = category,
                dateIso = targetDate.toString(),
                highSchoolOnly = false
            )
        }
    }

    fun deleteCustomExam(id: String) {
        viewModelScope.launch {
            repository.deleteCustomExam(id)
        }
    }

    fun addStudyTask(
        title: String,
        subjectTag: String,
        daysFromToday: Long,
        priorityLevel: Int
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val targetDate = _clockTick.value.toLocalDate().plusDays(daysFromToday.coerceAtLeast(1))
            repository.addStudyTask(
                title = title,
                subjectTag = subjectTag.ifBlank { "مذاكرة عامة" },
                targetDateIso = targetDate.toString(),
                priorityLevel = priorityLevel,
                forThirdHighSchool = _selectedGrade.value.isThirdHighSchool
            )
        }
    }

    fun toggleStudyTask(taskId: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleStudyTask(taskId, completed)
        }
    }

    fun deleteStudyTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteStudyTask(taskId)
        }
    }

    // High School Masarat Cumulative GPA (20% + 40% + 40%)
    fun updateMasaratGrades(g1: String, g2: String, g3: String) {
        personalStorage.hsGrade1 = g1
        personalStorage.hsGrade2 = g2
        personalStorage.hsGrade3 = g3
        _weightedCalc.update {
            it.copy(hsGrade1 = g1, hsGrade2 = g2, hsGrade3 = g3)
        }
        _clockTick.value = LocalDateTime.now()
    }

    fun adoptMasaratGpaIntoWeighted() {
        val computed = String.format(Locale.ENGLISH, "%.2f", _weightedCalc.value.masaratCumulativeGpa)
        personalStorage.highSchoolGpa = computed
        _weightedCalc.update { it.copy(highSchoolGpa = computed) }
        _clockTick.value = LocalDateTime.now()
    }

    fun updateWeightedInputs(
        hsGpa: String = _weightedCalc.value.highSchoolGpa,
        qudurat: String = _weightedCalc.value.quduratScore,
        tahsili: String = _weightedCalc.value.tahsiliScore,
        step: String = _weightedCalc.value.stepScore,
        targetGoal: String = _weightedCalc.value.targetGoalScore
    ) {
        personalStorage.highSchoolGpa = hsGpa
        personalStorage.quduratScore = qudurat
        personalStorage.tahsiliScore = tahsili
        personalStorage.stepScore = step
        personalStorage.targetWeightedGoal = targetGoal
        _weightedCalc.update {
            it.copy(
                highSchoolGpa = hsGpa,
                quduratScore = qudurat,
                tahsiliScore = tahsili,
                stepScore = step,
                targetGoalScore = targetGoal
            )
        }
        _clockTick.value = LocalDateTime.now()
    }

    fun applyWeightedPreset(
        presetName: String,
        hsWeight: Int,
        quduratWeight: Int,
        tahsiliWeight: Int,
        stepWeight: Int = 0
    ) {
        personalStorage.customWeightHs = hsWeight
        personalStorage.customWeightQud = quduratWeight
        personalStorage.customWeightTah = tahsiliWeight
        personalStorage.customWeightStep = stepWeight
        _weightedCalc.update {
            it.copy(
                presetName = presetName,
                hsWeight = hsWeight,
                quduratWeight = quduratWeight,
                tahsiliWeight = tahsiliWeight,
                stepWeight = stepWeight
            )
        }
        _clockTick.value = LocalDateTime.now()
    }

    fun setFilterOnlyEligibleMajors(onlyEligible: Boolean) {
        _filterOnlyEligible.value = onlyEligible
        _clockTick.value = LocalDateTime.now()
    }

    fun setMajorFieldFilter(field: MajorFieldCategory) {
        _selectedMajorField.value = field
        _clockTick.value = LocalDateTime.now()
    }

    fun setUniversityRegionFilter(regionName: String?) {
        _selectedUniRegion.value = regionName
        _clockTick.value = LocalDateTime.now()
    }

    fun saveCurrentWeightedScenario(customTitle: String) {
        val calc = _weightedCalc.value
        viewModelScope.launch {
            repository.saveWeightedScenario(
                title = customTitle.ifBlank { calc.presetName },
                hsGpa = calc.hsDouble,
                qudurat = calc.quduratDouble,
                tahsili = calc.tahsiliDouble,
                hsWeight = calc.hsWeight,
                quduratWeight = calc.quduratWeight,
                tahsiliWeight = calc.tahsiliWeight,
                result = calc.computedScore
            )
        }
    }

    fun deleteWeightedScenario(id: Int) {
        viewModelScope.launch {
            repository.deleteWeightedScenario(id)
        }
    }

    companion object {
        fun provideFactory(
            repository: AcademicRepository,
            personalStorage: StudentPersonalStorage = StudentPersonalStorage(null)
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AcademicViewModel(repository, personalStorage) as T
            }
        }
    }
}
