package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import kotlin.math.abs

enum class CalendarRegion(
    val id: String,
    val title: String,
    val subtitle: String
) {
    GENERAL(
        id = "general",
        title = "التقويم العام للمملكة",
        subtitle = "بقية مناطق المملكة • 19 أسبوعاً للفصل الأول"
    ),
    WESTERN_HOLY_CITIES(
        id = "makkah_madinah_jeddah_taif",
        title = "تقويم المدينة المنورة ومكة المكرمة وجدة والطائف",
        subtitle = "المدينة المنورة ومكة المكرمة وجدة والطائف • 18 أسبوعاً للفصل الأول"
    );

    val shortLabel: String get() = title

    companion object {
        fun fromId(id: String?): CalendarRegion =
            entries.find { it.id == id } ?: GENERAL
    }
}

enum class GradeStage(val arabicTitle: String) {
    ELEMENTARY("المرحلة الابتدائية"),
    INTERMEDIATE("المرحلة المتوسطة"),
    HIGH_SCHOOL("المرحلة الثانوية")
}

enum class GradeLevel(
    val id: String,
    val arabicName: String,
    val shortName: String,
    val stage: GradeStage,
    val isHighSchool: Boolean = false,
    val isThirdHighSchool: Boolean = false,
    val guidanceTip: String
) {
    ELEM_1("elem_1", "الأول الابتدائي", "أول ابتدائي", GradeStage.ELEMENTARY, false, false, "التركيز على التأسيس في القراءة والحساب والانضباط المدرسي"),
    ELEM_2("elem_2", "الثاني الابتدائي", "ثاني ابتدائي", GradeStage.ELEMENTARY, false, false, "تنمية مهارات الإملاء والحساب الذهني والمتابعة اليومية"),
    ELEM_3("elem_3", "الثالث الابتدائي", "ثالث ابتدائي", GradeStage.ELEMENTARY, false, false, "الاستعداد للاختبارات الوطنية (نافس) للصف الثالث الابتدائي"),
    ELEM_4("elem_4", "الرابع الابتدائي", "رابع ابتدائي", GradeStage.ELEMENTARY, false, false, "بداية الصفوف العليا والاعتماد على النفس في المذاكرة"),
    ELEM_5("elem_5", "الخامس الابتدائي", "خامس ابتدائي", GradeStage.ELEMENTARY, false, false, "تنظيم جدول المراجعة الأسبوعي للمواد العلمية واللغوية"),
    ELEM_6("elem_6", "السادس الابتدائي", "سادس ابتدائي", GradeStage.ELEMENTARY, false, false, "الاستعداد لاختبارات نهاية المرحلة الابتدائية واختبارات نافس"),
    INTER_1("inter_1", "الأول المتوسط", "أول متوسط", GradeStage.INTERMEDIATE, false, false, "التأقلم مع المرحلة المتوسطة وتعدد المواد العلمية"),
    INTER_2("inter_2", "الثاني المتوسط", "ثاني متوسط", GradeStage.INTERMEDIATE, false, false, "رفع المعدل الدراسي وتطوير مهارات البحث والمشاريع"),
    INTER_3("inter_3", "الثالث المتوسط", "ثالث متوسط", GradeStage.INTERMEDIATE, false, false, "الاستعداد لاختبارات نافس وتحديد المسار المناسب في المرحلة الثانوية"),
    HIGH_1("high_1", "الأول الثانوي (السنة المشتركة)", "أول ثانوي", GradeStage.HIGH_SCHOOL, true, false, "بداية احتساب المعدل التراكمي الثانوي (20%) وتحديد التخصص ومبادئ القدرات"),
    HIGH_2("high_2", "الثاني الثانوي", "ثاني ثانوي", GradeStage.HIGH_SCHOOL, true, false, "الفرصة الذهبية لإنجاز القدرات العامة وSTEP واحتساب 40% من المعدل التراكمي"),
    HIGH_3("high_3", "الثالث الثانوي (خريج)", "ثالث ثانوي", GradeStage.HIGH_SCHOOL, true, true, "عام الحسم: التركيز الكامل على الاختبار التحصيلي (الفترة الأولى والثانية) والنسبة الموزونة");

    val stageArabic: String get() = stage.arabicTitle
    val arabicTitle: String get() = arabicName

    companion object {
        fun fromId(id: String?): GradeLevel = entries.find { it.id == id } ?: HIGH_3
    }
}

enum class EventCategory(val arabicLabel: String) {
    TAHSILI("الاختبار التحصيلي"),
    QUDURAT("اختبار القدرات"),
    STEP("اختبار STEP"),
    VACATION("إجازة رسمية"),
    SCHOOL_EXAM("اختبارات مدرسية"),
    MILESTONE("تقويم دراسي"),
    CUSTOM("اختبار مخصص");

    companion object {
        val ACADEMIC_MILESTONE = MILESTONE
    }
}

enum class UniversityFormulaType(
    val arabicTitle: String,
    val universitiesExamples: String,
    val hsWeight: Int,
    val quduratWeight: Int,
    val tahsiliWeight: Int,
    val stepWeight: Int
) {
    STANDARD_30_30_40(
        arabicTitle = "معادلة (30% ثانوية + 30% قدرات + 40% تحصيلي)",
        universitiesExamples = "جامعة الملك سعود، الإمام محمد بن سعود، أم القرى، الجامعة الإسلامية، جامعة القصيم",
        hsWeight = 30,
        quduratWeight = 30,
        tahsiliWeight = 40,
        stepWeight = 0
    ),
    TAIBAH_40_30_30(
        arabicTitle = "معادلة (40% ثانوية + 30% قدرات + 30% تحصيلي)",
        universitiesExamples = "جامعة طيبة (المسار العلمي والطبي)، جامعة تبوك",
        hsWeight = 40,
        quduratWeight = 30,
        tahsiliWeight = 30,
        stepWeight = 0
    ),
    KAU_30_30_30_10STEP(
        arabicTitle = "معادلة جامعة الملك عبدالعزيز (30% ث + 30% ق + 30% ت + 10% STEP)",
        universitiesExamples = "جامعة الملك عبدالعزيز (المسارات العلمية والصحية والهندسية)",
        hsWeight = 30,
        quduratWeight = 30,
        tahsiliWeight = 30,
        stepWeight = 10
    ),
    KKU_30_30_35_5STEP(
        arabicTitle = "معادلة جامعة الملك خالد (30% ث + 30% ق + 35% ت + 5% STEP)",
        universitiesExamples = "جامعة الملك خالد (التخصصات الصحية والهندسية والحاسب)",
        hsWeight = 30,
        quduratWeight = 30,
        tahsiliWeight = 35,
        stepWeight = 5
    ),
    KKU_LANG_30_30_25_15STEP(
        arabicTitle = "معادلة جامعة الملك خالد للغات والترجمة (30% ث + 30% ق + 25% ت + 15% STEP)",
        universitiesExamples = "جامعة الملك خالد (تخصص اللغات والترجمة)",
        hsWeight = 30,
        quduratWeight = 30,
        tahsiliWeight = 25,
        stepWeight = 15
    ),
    KFUPM_50_50(
        arabicTitle = "معادلة جامعة الملك فهد للبترول والمعادن (50% قدرات + 50% تحصيلي)",
        universitiesExamples = "جامعة الملك فهد للبترول والمعادن (KFUPM)",
        hsWeight = 0,
        quduratWeight = 50,
        tahsiliWeight = 50,
        stepWeight = 0
    );

    val arabicLabel: String get() = arabicTitle
    val exampleUniversities: String get() = universitiesExamples

    fun calculate(hs: Double, qud: Double, tah: Double, step: Double): Double {
        return (hs * hsWeight + qud * quduratWeight + tah * tahsiliWeight + step * stepWeight) / 100.0
    }
}

enum class MajorFieldCategory(val arabicLabel: String) {
    ALL("جميع التخصصات"),
    MEDICAL("صحي وطبي"),
    ENGINEERING("هندسي"),
    TECH_CS("تقني وحاسب"),
    ADMIN_LAW("إداري وقانوني ولغات")
}

@Entity(tableName = "academic_events")
data class AcademicEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val startDateIso: String,
    val endDateIso: String?,
    val hijriDateDisplay: String,
    val gregorianDateDisplay: String,
    val sourcePlatform: String,
    val sourceUrl: String = "",
    val highSchoolOnly: Boolean = false,
    val thirdHighSchoolPriority: Boolean = false,
    val isCustom: Boolean = false,
    val registrationNote: String = "",
    val estimatedBadgeText: String = ""
)

@Entity(tableName = "university_admissions")
data class UniversityAdmissionEntity(
    @PrimaryKey val id: String,
    val universityName: String,
    val regionName: String,
    val majorName: String,
    val fieldCategory: String, // MajorFieldCategory.name
    val formulaType: String, // UniversityFormulaType.name
    val formulaDisplay: String,
    val stepRequirementNote: String,
    val lastYearMinWeighted: Double,
    val requiresStepScore: Boolean = false
) {
    val lastYearCohortHijri: String get() = "1447 هـ"
    val lastYearMinScore: Double get() = lastYearMinWeighted
    val sourcePlatform: String get() = "المصدر: أدلة القبول المعلنة للعام الماضي (تحديث شهري)"
}

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subjectTag: String,
    val targetDateIso: String,
    val hijriTargetDisplay: String,
    val priorityLevel: Int = 1,
    val isCompleted: Boolean = false,
    val forThirdHighSchool: Boolean = false
)

@Entity(tableName = "platform_sync_status")
data class PlatformSyncEntity(
    @PrimaryKey val platformId: String,
    val platformName: String,
    val authorityName: String,
    val officialUrl: String,
    val dataScope: String,
    val lastSyncedEpochMillis: Long,
    val syncStatusText: String,
    val isVerifiedOnline: Boolean,
    val syncedEventsCount: Int
) {
    val officialPortal: String get() = officialUrl
}

@Entity(tableName = "weighted_scenarios")
data class WeightedScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val highSchoolGpa: Double,
    val quduratScore: Double,
    val tahsiliScore: Double,
    val hsWeight: Int,
    val quduratWeight: Int,
    val tahsiliWeight: Int,
    val weightedResult: Double
) {
    val scenarioTitle: String get() = title
    val calculatedWeighted: Double get() = weightedResult
}

data class ShortSchoolQuizItem(
    val id: String,
    val subjectName: String,
    val examDateIso: String,
    val hijriDateDisplay: String,
    val gregorianDateDisplay: String,
    val note: String,
    val daysRemaining: Long = 0L
)

data class ComputerizedExamBooking(
    val examType: String, // "قدرات محوسب" or "اختبار STEP"
    val examTitle: String,
    val examDateIso: String,
    val hijriDateDisplay: String,
    val gregorianDateDisplay: String
)

data class CountdownItem(
    val event: AcademicEventEntity,
    val categoryEnum: EventCategory,
    val daysRemaining: Long,
    val hoursRemaining: Long,
    val minutesRemaining: Long,
    val secondsRemaining: Long,
    val durationDays: Long,
    val isOngoing: Boolean,
    val isPast: Boolean,
    val urgencyFraction: Float
)

data class UniversityMajorEvaluation(
    val admission: UniversityAdmissionEntity,
    val formulaEnum: UniversityFormulaType,
    val fieldEnum: MajorFieldCategory,
    val studentWeightedForMajor: Double,
    val differenceFromLastYear: Double,
    val isEligibleByLastYear: Boolean
) {
    val studentCalculatedScore: Double get() = studentWeightedForMajor
    val comparisonBadgeText: String
        get() = if (isEligibleByLastYear) {
            String.format(
                Locale.ENGLISH,
                "موزونتك الحالية أعلى من آخر نسبة قبول (+%.2f%%)",
                abs(differenceFromLastYear)
            )
        } else {
            String.format(
                Locale.ENGLISH,
                "تحتاج لرفع موزونتك بمقدار (+%.2f%%) للوصول لنسبة العام الماضي",
                abs(differenceFromLastYear)
            )
        }
}

data class AcademicWeekInfo(
    val academicYearLabel: String,
    val currentSemesterName: String,
    val currentWeekNumber: Int,
    val totalSemesterWeeks: Int,
    val semesterProgress: Float,
    val fullYearProgress: Float,
    val todayGregorianFormatted: String,
    val todayHijriFormatted: String,
    val statusBadgeText: String,
    val statusDescription: String,
    val isVacationNow: Boolean,
    val daysUntilSemesterEnd: Long,
    val region: CalendarRegion = CalendarRegion.GENERAL
) {
    val semesterName: String get() = currentSemesterName
    val weekNumber: Int get() = currentWeekNumber
    val isActiveStudyWeek: Boolean get() = !isVacationNow && currentWeekNumber > 0
    val statusSummary: String get() = statusDescription
    val progressFraction: Float get() = semesterProgress
    val hijriToday: String get() = todayHijriFormatted
    val gregorianToday: String get() = todayGregorianFormatted
}
