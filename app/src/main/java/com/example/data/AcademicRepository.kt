package com.example.data

import com.example.data.local.MasarDao
import com.example.data.local.OfficialSaudiCalendarSeed
import com.example.data.local.UniversityAdmissionsSeed
import com.example.data.remote.TrustedPlatformsNetwork
import com.example.model.AcademicEventEntity
import com.example.model.AcademicWeekInfo
import com.example.model.CalendarRegion
import com.example.model.CountdownItem
import com.example.model.EventCategory
import com.example.model.MajorFieldCategory
import com.example.model.PlatformSyncEntity
import com.example.model.StudyTaskEntity
import com.example.model.UniversityAdmissionEntity
import com.example.model.UniversityFormulaType
import com.example.model.UniversityMajorEvaluation
import com.example.model.WeightedScenarioEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class SyncResultSummary(
    val success: Boolean,
    val message: String,
    val syncedHijriToday: String,
    val timestampMillis: Long
)

class AcademicRepository(private val dao: MasarDao) {

    val allEventsFlow: Flow<List<AcademicEventEntity>> = dao.observeAllEvents()
    val universityAdmissionsFlow: Flow<List<UniversityAdmissionEntity>> = dao.observeUniversityAdmissions()
    val studyTasksFlow: Flow<List<StudyTaskEntity>> = dao.observeStudyTasks()
    val platformSyncsFlow: Flow<List<PlatformSyncEntity>> = dao.observePlatformSyncs()
    val weightedScenariosFlow: Flow<List<WeightedScenarioEntity>> = dao.observeWeightedScenarios()

    suspend fun ensureSeeded(region: CalendarRegion = CalendarRegion.GENERAL) = withContext(Dispatchers.IO) {
        // Refresh monthly reference data (academic_events & university_admissions)
        // while keeping student personal data completely untouched.
        dao.deleteOfficialEvents()
        dao.upsertEvents(OfficialSaudiCalendarSeed.defaultAcademicEvents1448(region))

        dao.deleteAllUniversityAdmissions()
        dao.upsertUniversityAdmissions(UniversityAdmissionsSeed.defaultUniversityAdmissions())

        if (dao.getStudyTasksCount() == 0) {
            dao.insertStudyTasks(OfficialSaudiCalendarSeed.defaultStudyTasks())
        }
        if (dao.getPlatformSyncCount() == 0) {
            dao.upsertPlatformSyncs(
                OfficialSaudiCalendarSeed.defaultPlatforms(System.currentTimeMillis())
            )
        }
    }

    suspend fun updateRegionEvents(region: CalendarRegion) = withContext(Dispatchers.IO) {
        dao.deleteOfficialEvents()
        dao.upsertEvents(OfficialSaudiCalendarSeed.defaultAcademicEvents1448(region))
    }

    suspend fun syncWithTrustedPlatforms(
        referenceDate: LocalDate = LocalDate.now(),
        region: CalendarRegion = CalendarRegion.GENERAL
    ): SyncResultSummary = withContext(Dispatchers.IO) {
        val nowMillis = System.currentTimeMillis()
        val ddMmYyyy = referenceDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH))

        var hijriTodayText = formatLocalUmmAlQura(referenceDate)
        var remoteSuccess = false

        try {
            val response = TrustedPlatformsNetwork.ummAlQuraApi.convertGregorianToHijri(ddMmYyyy)
            val hijri = response.data?.hijri
            if (response.code == 200 && hijri != null) {
                val day = hijri.day ?: ""
                val monthAr = hijri.month?.ar ?: ""
                val year = hijri.year ?: "1448"
                val weekdayAr = hijri.weekday?.ar ?: ""
                if (day.isNotBlank() && monthAr.isNotBlank()) {
                    hijriTodayText = if (weekdayAr.isNotBlank()) {
                        "$weekdayAr $day $monthAr $year هـ"
                    } else {
                        "$day $monthAr $year هـ"
                    }
                    remoteSuccess = true
                }
            }
        } catch (_: Exception) {
            remoteSuccess = false
        }

        val officialEvents = OfficialSaudiCalendarSeed.defaultAcademicEvents1448(region)
        dao.deleteOfficialEvents()
        dao.upsertEvents(officialEvents)

        val updatedPlatforms = listOf(
            PlatformSyncEntity(
                platformId = "umm_al_qura_api",
                platformName = "خدمة مزامنة التاريخ الهجري والتحقق من التوقيت",
                authorityName = "المصدر: تقويم أم القرى المدمج + خدمة التحقق من التوقيت الهجري",
                officialUrl = "",
                dataScope = "الاتصال بخدمة (api.aladhan.com/v1/gToH) لجلب التاريخ الهجري الحالي والتحقق من مطابقة التوقيت اليومي",
                lastSyncedEpochMillis = nowMillis,
                syncStatusText = if (remoteSuccess) {
                    "تمت مزامنة التاريخ الهجري عبر الشبكة بنجاح ($hijriTodayText)"
                } else {
                    "تم التحقق من التاريخ الهجري عبر محرك أم القرى المدمج بدون إنترنت ($hijriTodayText)"
                },
                isVerifiedOnline = remoteSuccess,
                syncedEventsCount = officialEvents.size
            ),
            PlatformSyncEntity(
                platformId = "moe_sa_local",
                platformName = "قاعدة بيانات التقويم الدراسي 1448 هـ (محلية ومعتمدة)",
                authorityName = "المصدر: تقويم وزارة التعليم (${region.title})",
                officialUrl = "",
                dataScope = "تواريخ الفصول الدراسية والإجازات والاختبارات مخزنة محلياً في قاعدة البيانات المرجعية بمعزل عن بيانات الطالب",
                lastSyncedEpochMillis = nowMillis,
                syncStatusText = "مفعّل حالياً: ${region.title}",
                isVerifiedOnline = true,
                syncedEventsCount = 11
            ),
            PlatformSyncEntity(
                platformId = "etec_qiyas_local",
                platformName = "البيانات المرجعية للجامعات واختبارات قياس",
                authorityName = "المصدر: هيئة تقويم التعليم والتدريب - قياس + أدلة القبول",
                officialUrl = "",
                dataScope = "مواعيد القدرات الورقية (10 - 14 ديسمبر 2026 م)، مواعيد التحصيلي التقديرية، و18 تخصصاً جامعياً",
                lastSyncedEpochMillis = nowMillis,
                syncStatusText = "محدّثة للإصدار المرجعي v3.0 (تحديث شهري آمن)",
                isVerifiedOnline = true,
                syncedEventsCount = 18
            )
        )
        dao.upsertPlatformSyncs(updatedPlatforms)

        SyncResultSummary(
            success = true,
            message = if (remoteSuccess) {
                "تمت مزامنة التاريخ الهجري والتحقق من التوقيت بنجاح ($hijriTodayText)"
            } else {
                "تم التحقق من التوقيت والتاريخ الهجري محلياً عبر تقويم أم القرى ($hijriTodayText)"
            },
            syncedHijriToday = hijriTodayText,
            timestampMillis = nowMillis
        )
    }

    suspend fun addCustomExam(
        title: String,
        subtitle: String,
        category: EventCategory,
        dateIso: String,
        highSchoolOnly: Boolean
    ) = withContext(Dispatchers.IO) {
        val parsedDate = runCatching { LocalDate.parse(dateIso) }.getOrDefault(LocalDate.now().plusDays(14))
        val hijriStr = formatLocalUmmAlQura(parsedDate, includeWeekday = false)
        val gregStr = formatArabicGregorian(parsedDate)
        val customEvent = AcademicEventEntity(
            id = "custom_${System.currentTimeMillis()}",
            title = title.trim(),
            subtitle = subtitle.trim().ifEmpty { "موعد اختبار مخصص أضافه الطالب" },
            category = category.name,
            startDateIso = parsedDate.toString(),
            endDateIso = null,
            hijriDateDisplay = hijriStr,
            gregorianDateDisplay = gregStr,
            sourcePlatform = "المصدر: جدول الطالب الشخصي",
            sourceUrl = "",
            highSchoolOnly = highSchoolOnly,
            thirdHighSchoolPriority = category == EventCategory.TAHSILI,
            isCustom = true,
            registrationNote = "تذكير شخصي محفوظ محلياً"
        )
        dao.upsertEvent(customEvent)
    }

    suspend fun deleteCustomExam(id: String) = withContext(Dispatchers.IO) {
        dao.deleteCustomEvent(id)
    }

    suspend fun addStudyTask(
        title: String,
        subjectTag: String,
        targetDateIso: String,
        priorityLevel: Int,
        forThirdHighSchool: Boolean
    ) = withContext(Dispatchers.IO) {
        val parsedDate = runCatching { LocalDate.parse(targetDateIso) }.getOrDefault(LocalDate.now().plusDays(3))
        val hijriStr = formatLocalUmmAlQura(parsedDate, includeWeekday = false)
        dao.insertStudyTask(
            StudyTaskEntity(
                title = title.trim(),
                subjectTag = subjectTag.trim(),
                targetDateIso = parsedDate.toString(),
                hijriTargetDisplay = hijriStr,
                priorityLevel = priorityLevel,
                isCompleted = false,
                forThirdHighSchool = forThirdHighSchool
            )
        )
    }

    suspend fun toggleStudyTask(taskId: Int, completed: Boolean) = withContext(Dispatchers.IO) {
        dao.setTaskCompleted(taskId, completed)
    }

    suspend fun deleteStudyTask(taskId: Int) = withContext(Dispatchers.IO) {
        dao.deleteStudyTask(taskId)
    }

    suspend fun saveWeightedScenario(
        title: String,
        hsGpa: Double,
        qudurat: Double,
        tahsili: Double,
        hsWeight: Int,
        quduratWeight: Int,
        tahsiliWeight: Int,
        result: Double
    ) = withContext(Dispatchers.IO) {
        dao.insertWeightedScenario(
            WeightedScenarioEntity(
                title = title,
                highSchoolGpa = hsGpa,
                quduratScore = qudurat,
                tahsiliScore = tahsili,
                hsWeight = hsWeight,
                quduratWeight = quduratWeight,
                tahsiliWeight = tahsiliWeight,
                weightedResult = result
            )
        )
    }

    suspend fun deleteWeightedScenario(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteWeightedScenario(id)
    }

    companion object {
        private val arabicHijriMonths = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )

        private val arabicGregorianMonths = arrayOf(
            "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
        )

        private val arabicWeekdays = mapOf(
            1 to "الاثنين",
            2 to "الثلاثاء",
            3 to "الأربعاء",
            4 to "الخميس",
            5 to "الجمعة",
            6 to "السبت",
            7 to "الأحد"
        )

        fun formatLocalUmmAlQura(date: LocalDate, includeWeekday: Boolean = true): String {
            return try {
                val hijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(date)
                val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
                val monthIndex = (hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1).coerceIn(0, 11)
                val year = hijrahDate.get(ChronoField.YEAR)
                val weekday = arabicWeekdays[date.dayOfWeek.value] ?: ""
                if (includeWeekday && weekday.isNotEmpty()) {
                    "$weekday $day ${arabicHijriMonths[monthIndex]} $year هـ"
                } else {
                    "$day ${arabicHijriMonths[monthIndex]} $year هـ"
                }
            } catch (_: Exception) {
                "1448 هـ"
            }
        }

        fun formatArabicGregorian(date: LocalDate, includeWeekday: Boolean = false): String {
            val day = date.dayOfMonth
            val monthName = arabicGregorianMonths[(date.monthValue - 1).coerceIn(0, 11)]
            val year = date.year
            val weekday = arabicWeekdays[date.dayOfWeek.value] ?: ""
            return if (includeWeekday && weekday.isNotEmpty()) {
                "$weekday $day $monthName $year م"
            } else {
                "$day $monthName $year م"
            }
        }

        fun computeAcademicWeek(
            today: LocalDate,
            region: CalendarRegion = CalendarRegion.GENERAL,
            overrideHijriToday: String? = null
        ): AcademicWeekInfo = calculateAcademicWeekInfo(today, overrideHijriToday, region)

        fun calculateAcademicWeekInfo(
            today: LocalDate,
            overrideHijriToday: String? = null,
            region: CalendarRegion = CalendarRegion.GENERAL
        ): AcademicWeekInfo {
            val isWestern = region == CalendarRegion.WESTERN_HOLY_CITIES

            val sem1Start = if (isWestern) LocalDate.of(2026, 8, 30) else LocalDate.of(2026, 8, 23)
            val totalSem1Weeks = if (isWestern) 18 else 19
            val nationalDayEnd = LocalDate.of(2026, 9, 26)
            val autumnBreakStart = LocalDate.of(2026, 11, 20)
            val autumnBreakEnd = LocalDate.of(2026, 11, 28)
            val sem1End = LocalDate.of(2027, 1, 7)
            val midYearEnd = LocalDate.of(2027, 1, 16)
            val sem2Start = LocalDate.of(2027, 1, 17)
            val eidFitrStart = LocalDate.of(2027, 2, 26)
            val eidFitrEnd = LocalDate.of(2027, 3, 13)
            val eidAdhaStart = if (isWestern) LocalDate.of(2027, 4, 30) else LocalDate.of(2027, 5, 7)
            val eidAdhaEnd = LocalDate.of(2027, 5, 22)
            val sem2End = if (isWestern) LocalDate.of(2027, 7, 1) else LocalDate.of(2027, 6, 24)

            val todayGreg = formatArabicGregorian(today, includeWeekday = true)
            val todayHijri = overrideHijriToday ?: formatLocalUmmAlQura(today, includeWeekday = true)

            val totalYearDays = ChronoUnit.DAYS.between(sem1Start, sem2End).coerceAtLeast(1)
            val elapsedYearDays = ChronoUnit.DAYS.between(sem1Start, today).coerceIn(0, totalYearDays)
            val fullYearProgress = (elapsedYearDays.toFloat() / totalYearDays.toFloat()).coerceIn(0f, 1f)

            return when {
                today.isBefore(sem1Start) -> {
                    val daysToStart = ChronoUnit.DAYS.between(today, sem1Start)
                    AcademicWeekInfo(
                        academicYearLabel = "العام الدراسي 1448 هـ (2026 - 2027 م)",
                        currentSemesterName = "الاستعداد للعام الدراسي 1448 هـ (${region.title})",
                        currentWeekNumber = 1,
                        totalSemesterWeeks = totalSem1Weeks,
                        semesterProgress = 0f,
                        fullYearProgress = 0f,
                        todayGregorianFormatted = todayGreg,
                        todayHijriFormatted = todayHijri,
                        statusBadgeText = "قبل بداية الدراسة",
                        statusDescription = "يتبقى $daysToStart يوم على انطلاق العام الدراسي 1448 هـ وفق ${region.title}",
                        isVacationNow = true,
                        daysUntilSemesterEnd = ChronoUnit.DAYS.between(today, sem1End),
                        region = region
                    )
                }

                !today.isAfter(sem1End) -> {
                    val rawDaysSinceSem1 = ChronoUnit.DAYS.between(sem1Start, today).coerceAtLeast(0)
                    val nationalDayDeduction = if (today.isAfter(nationalDayEnd)) 4L else 0L
                    val effectiveStudyDays = when {
                        today.isAfter(autumnBreakEnd) ->
                            (rawDaysSinceSem1 - nationalDayDeduction - 7L).coerceAtLeast(0L)
                        !today.isBefore(autumnBreakStart) ->
                            (ChronoUnit.DAYS.between(sem1Start, autumnBreakStart) - nationalDayDeduction).coerceAtLeast(0L)
                        else ->
                            (rawDaysSinceSem1 - nationalDayDeduction).coerceAtLeast(0L)
                    }

                    val weekNum = ((effectiveStudyDays / 7).toInt() + 1).coerceIn(1, totalSem1Weeks)
                    val semProgress = (weekNum.toFloat() / totalSem1Weeks.toFloat()).coerceIn(0.05f, 1f)
                    val isInAutumnBreak = !today.isBefore(autumnBreakStart) && !today.isAfter(autumnBreakEnd)
                    val isWeekend = today.dayOfWeek.value == 5 || today.dayOfWeek.value == 6

                    val nextWeekAfterAutumn = if (isWestern) 12 else 13
                    val badge = when {
                        isInAutumnBreak -> "إجازة الخريف الرسمية"
                        isWeekend -> "عطلة نهاية الأسبوع — الأسبوع $weekNum"
                        else -> "الأسبوع الدراسي $weekNum من $totalSem1Weeks"
                    }
                    val desc = when {
                        isInAutumnBreak -> "أنت الآن في إجازة الخريف، وتستأنف الدراسة في الأسبوع $nextWeekAfterAutumn من $totalSem1Weeks"
                        else -> "أنت في الأسبوع $weekNum من $totalSem1Weeks (${region.title})"
                    }

                    AcademicWeekInfo(
                        academicYearLabel = "العام الدراسي 1448 هـ (2026 - 2027 م)",
                        currentSemesterName = "الفصل الدراسي الأول 1448 هـ",
                        currentWeekNumber = weekNum,
                        totalSemesterWeeks = totalSem1Weeks,
                        semesterProgress = semProgress,
                        fullYearProgress = fullYearProgress,
                        todayGregorianFormatted = todayGreg,
                        todayHijriFormatted = todayHijri,
                        statusBadgeText = badge,
                        statusDescription = desc,
                        isVacationNow = isInAutumnBreak,
                        daysUntilSemesterEnd = ChronoUnit.DAYS.between(today, sem1End).coerceAtLeast(0),
                        region = region
                    )
                }

                !today.isAfter(midYearEnd) -> {
                    val daysLeft = ChronoUnit.DAYS.between(today, sem2Start).coerceAtLeast(0)
                    AcademicWeekInfo(
                        academicYearLabel = "العام الدراسي 1448 هـ (2026 - 2027 م)",
                        currentSemesterName = "إجازة منتصف العام الدراسي 1448 هـ",
                        currentWeekNumber = totalSem1Weeks,
                        totalSemesterWeeks = totalSem1Weeks,
                        semesterProgress = 1f,
                        fullYearProgress = fullYearProgress,
                        todayGregorianFormatted = todayGreg,
                        todayHijriFormatted = todayHijri,
                        statusBadgeText = "إجازة منتصف العام",
                        statusDescription = "يتبقى $daysLeft يوم على بداية الفصل الدراسي الثاني",
                        isVacationNow = true,
                        daysUntilSemesterEnd = 0,
                        region = region
                    )
                }

                !today.isAfter(sem2End) -> {
                    val daysSinceSem2 = ChronoUnit.DAYS.between(sem2Start, today).coerceAtLeast(0)
                    val totalWeeks = if (isWestern) 21 else 20
                    val weekNum = ((daysSinceSem2 / 7).toInt() + 1).coerceIn(1, totalWeeks)
                    val isInEidFitr = !today.isBefore(eidFitrStart) && !today.isAfter(eidFitrEnd)
                    val isInEidAdha = !today.isBefore(eidAdhaStart) && !today.isAfter(eidAdhaEnd)
                    val isVacation = isInEidFitr || isInEidAdha
                    val badge = when {
                        isInEidFitr -> "إجازة عيد الفطر المبارك"
                        isInEidAdha -> "إجازة عيد الأضحى المبارك"
                        else -> "الأسبوع الدراسي $weekNum من $totalWeeks"
                    }
                    AcademicWeekInfo(
                        academicYearLabel = "العام الدراسي 1448 هـ (2026 - 2027 م)",
                        currentSemesterName = "الفصل الدراسي الثاني 1448 هـ",
                        currentWeekNumber = weekNum,
                        totalSemesterWeeks = totalWeeks,
                        semesterProgress = (weekNum.toFloat() / totalWeeks.toFloat()).coerceIn(0.05f, 1f),
                        fullYearProgress = fullYearProgress,
                        todayGregorianFormatted = todayGreg,
                        todayHijriFormatted = todayHijri,
                        statusBadgeText = badge,
                        statusDescription = "أنت في الأسبوع $weekNum من الفصل الدراسي الثاني (${region.title})",
                        isVacationNow = isVacation,
                        daysUntilSemesterEnd = ChronoUnit.DAYS.between(today, sem2End).coerceAtLeast(0),
                        region = region
                    )
                }

                else -> {
                    AcademicWeekInfo(
                        academicYearLabel = "العام الدراسي 1448 هـ (2026 - 2027 م)",
                        currentSemesterName = "الإجازة الصيفية 1448 - 1449 هـ",
                        currentWeekNumber = 20,
                        totalSemesterWeeks = 20,
                        semesterProgress = 1f,
                        fullYearProgress = 1f,
                        todayGregorianFormatted = todayGreg,
                        todayHijriFormatted = todayHijri,
                        statusBadgeText = "إجازة نهاية العام الدراسي",
                        statusDescription = "تم إتمام العام الدراسي 1448 هـ بنجاح، إجازة سعيدة!",
                        isVacationNow = true,
                        daysUntilSemesterEnd = 0,
                        region = region
                    )
                }
            }
        }

        fun buildCountdownItem(
            event: AcademicEventEntity,
            nowDateTime: LocalDateTime
        ): CountdownItem {
            val today = nowDateTime.toLocalDate()
            val startDate = runCatching { LocalDate.parse(event.startDateIso) }.getOrDefault(today)
            val endDate = event.endDateIso?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: startDate
            val startDateTime = startDate.atTime(7, 0, 0)

            val isOngoing = !today.isBefore(startDate) && !today.isAfter(endDate)
            val isPast = today.isAfter(endDate)

            val totalSeconds = ChronoUnit.SECONDS.between(nowDateTime, startDateTime).coerceAtLeast(0L)
            val daysRem = if (isOngoing || isPast) 0L else (totalSeconds / 86_400L)
            val hoursRem = if (isOngoing || isPast) 0L else ((totalSeconds % 86_400L) / 3_600L)
            val minsRem = if (isOngoing || isPast) 0L else ((totalSeconds % 3_600L) / 60L)
            val secsRem = if (isOngoing || isPast) 0L else (totalSeconds % 60L)

            val durationDays = (ChronoUnit.DAYS.between(startDate, endDate) + 1).coerceAtLeast(1L)
            val urgency = when {
                isOngoing -> 1f
                isPast -> 0f
                daysRem >= 180 -> 0.15f
                else -> (1f - (daysRem.toFloat() / 180f)).coerceIn(0.15f, 0.98f)
            }

            val cat = runCatching { EventCategory.valueOf(event.category) }.getOrDefault(EventCategory.MILESTONE)
            return CountdownItem(
                event = event,
                categoryEnum = cat,
                daysRemaining = daysRem,
                hoursRemaining = hoursRem,
                minutesRemaining = minsRem,
                secondsRemaining = secsRem,
                durationDays = durationDays,
                isOngoing = isOngoing,
                isPast = isPast,
                urgencyFraction = urgency
            )
        }

        /**
         * حاسبة المعدل التراكمي للثانوية لنظام المسارات:
         * 20% للصف الأول الثانوي + 40% للصف الثاني الثانوي + 40% للصف الثالث الثانوي
         */
        fun calculateMasaratCumulativeGpa(
            grade1Score: Double,
            grade2Score: Double,
            grade3Score: Double
        ): Double {
            val g1 = grade1Score.coerceIn(0.0, 100.0)
            val g2 = grade2Score.coerceIn(0.0, 100.0)
            val g3 = grade3Score.coerceIn(0.0, 100.0)
            return (g1 * 0.20) + (g2 * 0.40) + (g3 * 0.40)
        }

        /**
         * ميزة "كم أحتاج في التحصيلي؟" (حاسبة الهدف العكسي):
         * تحسب الحد الأدنى للدرجة المطلوبة في الاختبار التحصيلي للوصول إلى النسبة الموزونة الطموحة.
         */
        fun calculateRequiredTahsiliScore(
            hsGpa: Double,
            quduratScore: Double,
            stepScore: Double,
            targetWeighted: Double,
            hsWeight: Int = 30,
            quduratWeight: Int = 30,
            tahsiliWeight: Int = 40,
            stepWeight: Int = 0
        ): Double {
            if (tahsiliWeight <= 0) return 0.0
            val existingContribution =
                (hsGpa * hsWeight + quduratScore * quduratWeight + stepScore * stepWeight) / 100.0
            val neededFromTahsili = targetWeighted - existingContribution
            return (neededFromTahsili * 100.0) / tahsiliWeight.toDouble()
        }

        fun evaluateUniversityAdmissions(
            admissions: List<UniversityAdmissionEntity>,
            hsGpa: Double,
            qudurat: Double,
            tahsili: Double,
            step: Double
        ): List<UniversityMajorEvaluation> {
            return admissions.map { item ->
                val formula = runCatching { UniversityFormulaType.valueOf(item.formulaType) }
                    .getOrDefault(UniversityFormulaType.STANDARD_30_30_40)
                val field = runCatching { MajorFieldCategory.valueOf(item.fieldCategory) }
                    .getOrDefault(MajorFieldCategory.ALL)
                val studentWeighted = formula.calculate(hsGpa, qudurat, tahsili, step)
                val diff = studentWeighted - item.lastYearMinWeighted
                UniversityMajorEvaluation(
                    admission = item,
                    formulaEnum = formula,
                    fieldEnum = field,
                    studentWeightedForMajor = studentWeighted,
                    differenceFromLastYear = diff,
                    isEligibleByLastYear = diff >= 0.0
                )
            }
        }
    }
}
