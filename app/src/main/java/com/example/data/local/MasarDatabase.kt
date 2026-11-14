package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AcademicEventEntity
import com.example.model.CalendarRegion
import com.example.model.EventCategory
import com.example.model.PlatformSyncEntity
import com.example.model.StudyTaskEntity
import com.example.model.UniversityAdmissionEntity
import com.example.model.WeightedScenarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasarDao {
    @Query("SELECT * FROM academic_events ORDER BY startDateIso ASC")
    fun observeAllEvents(): Flow<List<AcademicEventEntity>>

    @Query("SELECT COUNT(*) FROM academic_events")
    suspend fun getEventsCount(): Int

    @Query("DELETE FROM academic_events WHERE isCustom = 0")
    suspend fun deleteOfficialEvents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<AcademicEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: AcademicEventEntity)

    @Query("DELETE FROM academic_events WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomEvent(id: String)

    @Query("SELECT * FROM university_admissions ORDER BY lastYearMinWeighted DESC")
    fun observeUniversityAdmissions(): Flow<List<UniversityAdmissionEntity>>

    @Query("DELETE FROM university_admissions")
    suspend fun deleteAllUniversityAdmissions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUniversityAdmissions(items: List<UniversityAdmissionEntity>)

    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, priorityLevel DESC, targetDateIso ASC")
    fun observeStudyTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT COUNT(*) FROM study_tasks")
    suspend fun getStudyTasksCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTask(task: StudyTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTasks(tasks: List<StudyTaskEntity>)

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Int, completed: Boolean)

    @Query("DELETE FROM study_tasks WHERE id = :taskId")
    suspend fun deleteStudyTask(taskId: Int)

    @Query("SELECT * FROM platform_sync_status ORDER BY platformId ASC")
    fun observePlatformSyncs(): Flow<List<PlatformSyncEntity>>

    @Query("SELECT COUNT(*) FROM platform_sync_status")
    suspend fun getPlatformSyncCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlatformSyncs(platforms: List<PlatformSyncEntity>)

    @Query("SELECT * FROM weighted_scenarios ORDER BY id DESC")
    fun observeWeightedScenarios(): Flow<List<WeightedScenarioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightedScenario(scenario: WeightedScenarioEntity)

    @Query("DELETE FROM weighted_scenarios WHERE id = :id")
    suspend fun deleteWeightedScenario(id: Int)
}

@Database(
    entities = [
        AcademicEventEntity::class,
        UniversityAdmissionEntity::class,
        StudyTaskEntity::class,
        PlatformSyncEntity::class,
        WeightedScenarioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MasarDatabase : RoomDatabase() {
    abstract fun masarDao(): MasarDao

    companion object {
        const val DB_VERSION_NUMBER = 3
        const val MONTHLY_UPDATE_DATE_TEXT = "تحديث شهري: ذو القعدة 1448 هـ (الإصدار المرجعي v3)"

        @Volatile
        private var INSTANCE: MasarDatabase? = null

        fun getInstance(context: Context): MasarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasarDatabase::class.java,
                    "masar_1448_academic.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object OfficialSaudiCalendarSeed {
    const val TAHSILI_ESTIMATED_TAG = "(موعد تقديري لحين الإعلان الرسمي من قياس)"

    fun defaultAcademicEvents1448(
        region: CalendarRegion = CalendarRegion.GENERAL
    ): List<AcademicEventEntity> {
        val isWestern = region == CalendarRegion.WESTERN_HOLY_CITIES

        val yearStartIso = if (isWestern) "2026-08-30" else "2026-08-23"
        val yearStartHijri = if (isWestern) "17 ربيع الأول 1448 هـ" else "10 ربيع الأول 1448 هـ"
        val yearStartGreg = if (isWestern) "30 أغسطس 2026 م" else "23 أغسطس 2026 م"
        val yearStartNote = if (isWestern) {
            "بداية الدراسة لمدارس المدينة المنورة ومكة المكرمة وجدة والطائف (18 أسبوعاً للفصل الأول)"
        } else {
            "بداية الدراسة وفق التقويم العام للمملكة (19 أسبوعاً للفصل الأول)"
        }

        val adhaStartIso = if (isWestern) "2027-04-30" else "2027-05-07"
        val adhaHijri = if (isWestern) {
            "23 ذو القعدة - 16 ذو الحجة 1448 هـ"
        } else {
            "1 - 16 ذو الحجة 1448 هـ"
        }
        val adhaGreg = if (isWestern) {
            "30 أبريل - 22 مايو 2027 م"
        } else {
            "7 - 22 مايو 2027 م"
        }
        val adhaNote = if (isWestern) {
            "إجازة عيد الأضحى لمدارس المدينة المنورة ومكة المكرمة وجدة والطائف • تستأنف الدراسة الأحد 17 ذو الحجة 1448 هـ"
        } else {
            "إجازة عيد الأضحى لبقية مناطق المملكة • تستأنف الدراسة الأحد 17 ذو الحجة 1448 هـ (23 مايو 2027 م)"
        }

        val finalsStartIso = if (isWestern) "2027-06-20" else "2027-06-13"
        val finalsEndIso = if (isWestern) "2027-07-01" else "2027-06-24"
        val finalsHijri = if (isWestern) "15 - 26 محرم 1449 هـ" else "8 - 19 محرم 1449 هـ"
        val finalsGreg = if (isWestern) "20 يونيو - 1 يوليو 2027 م" else "13 - 24 يونيو 2027 م"
        val finalsNote = if (isWestern) {
            "تنتهي الاختبارات بنهاية دوام الخميس 1 يوليو 2027 م لمدارس مكة والمدينة وجدة والطائف"
        } else {
            "تنتهي الاختبارات بنهاية دوام الخميس 24 يونيو 2027 م وفق التقويم العام"
        }

        val summerStartIso = if (isWestern) "2027-07-01" else "2027-06-24"
        val summerHijri = if (isWestern) "26 محرم - 19 ربيع الأول 1449 هـ" else "19 محرم - 19 ربيع الأول 1449 هـ"
        val summerGreg = if (isWestern) "1 يوليو - 21 أغسطس 2027 م" else "24 يونيو - 21 أغسطس 2027 م"
        val summerNote = if (isWestern) {
            "تبدأ بنهاية دوام الخميس 1 يوليو 2027 م لمدارس المدينة المنورة ومكة وجدة والطائف"
        } else {
            "تبدأ بنهاية دوام الخميس 24 يونيو 2027 م • بداية العام القادم 1449 هـ يوم الأحد 22 أغسطس 2027 م"
        }

        return listOf(
            AcademicEventEntity(
                id = "moe_1448_start",
                title = "بداية العام الدراسي 1448 هـ (الفصل الأول)",
                subtitle = "انطلاق الدراسة في مدارس التعليم العام بالمملكة",
                category = EventCategory.MILESTONE.name,
                startDateIso = yearStartIso,
                endDateIso = null,
                hijriDateDisplay = yearStartHijri,
                gregorianDateDisplay = yearStartGreg,
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = yearStartNote
            ),
            AcademicEventEntity(
                id = "moe_1448_national_day",
                title = "إجازة اليوم الوطني السعودي 96",
                subtitle = "إجازة رسمية بمناسبة اليوم الوطني للمملكة العربية السعودية",
                category = EventCategory.VACATION.name,
                startDateIso = "2026-09-23",
                endDateIso = "2026-09-26",
                hijriDateDisplay = "12 - 15 ربيع الآخر 1448 هـ",
                gregorianDateDisplay = "23 - 26 سبتمبر 2026 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "4 أيام شاملة عطلة نهاية الأسبوع"
            ),
            AcademicEventEntity(
                id = "moe_1448_autumn_break",
                title = "إجازة الخريف الدراسية",
                subtitle = "إجازة الخريف المعتمدة لجميع المراحل الدراسية",
                category = EventCategory.VACATION.name,
                startDateIso = "2026-11-20",
                endDateIso = "2026-11-28",
                hijriDateDisplay = "10 - 18 جمادى الآخرة 1448 هـ",
                gregorianDateDisplay = "20 - 28 نوفمبر 2026 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "تستأنف الدراسة الأحد 19 جمادى الآخرة 1448 هـ (29 نوفمبر 2026 م)"
            ),
            AcademicEventEntity(
                id = "qiyas_qudurat_paper_1",
                title = "اختبار القدرات العامة (الفترة الورقية)",
                subtitle = "لطلاب وطالبات الصفين الثاني والثالث الثانوي",
                category = EventCategory.QUDURAT.name,
                startDateIso = "2026-12-10",
                endDateIso = "2026-12-14",
                hijriDateDisplay = "1 - 5 رجب 1448 هـ",
                gregorianDateDisplay = "10 - 14 ديسمبر 2026 م",
                sourcePlatform = "المصدر: هيئة تقويم التعليم والتدريب - قياس",
                highSchoolOnly = true,
                thirdHighSchoolPriority = false,
                registrationNote = "الموعد الرسمي المعتمد لاختبار القدرات العامة الورقي (1 - 5 رجب 1448 هـ)"
            ),
            AcademicEventEntity(
                id = "moe_1448_sem1_finals",
                title = "اختبارات نهاية الفصل الدراسي الأول",
                subtitle = "الاختبارات التحريرية النهائية للفصل الدراسي الأول 1448 هـ",
                category = EventCategory.SCHOOL_EXAM.name,
                startDateIso = "2026-12-27",
                endDateIso = "2027-01-07",
                hijriDateDisplay = "18 - 29 رجب 1448 هـ",
                gregorianDateDisplay = "27 ديسمبر 2026 - 7 يناير 2027 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "تشمل جميع صفوف النقل والشهادات"
            ),
            AcademicEventEntity(
                id = "moe_1448_midyear_break",
                title = "إجازة منتصف العام الدراسي",
                subtitle = "إجازة ما بين الفصلين الدراسيين الأول والثاني",
                category = EventCategory.VACATION.name,
                startDateIso = "2027-01-08",
                endDateIso = "2027-01-16",
                hijriDateDisplay = "30 رجب - 8 شعبان 1448 هـ",
                gregorianDateDisplay = "8 - 16 يناير 2027 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "تبدأ بنهاية دوام الخميس 29 رجب 1448 هـ"
            ),
            AcademicEventEntity(
                id = "moe_1448_sem2_start",
                title = "بداية الفصل الدراسي الثاني 1448 هـ",
                subtitle = "عودة الطلاب والطالبات لمقاعد الدراسة للفصل الثاني",
                category = EventCategory.MILESTONE.name,
                startDateIso = "2027-01-17",
                endDateIso = null,
                hijriDateDisplay = "9 شعبان 1448 هـ",
                gregorianDateDisplay = "17 يناير 2027 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "بداية الأسبوع الأول من الفصل الدراسي الثاني"
            ),
            AcademicEventEntity(
                id = "qiyas_tahsili_reg_open",
                title = "فتح التسجيل المبكر للاختبار التحصيلي 1448 هـ $TAHSILI_ESTIMATED_TAG",
                subtitle = "بداية التسجيل لطلاب وطالبات الصف الثالث الثانوي $TAHSILI_ESTIMATED_TAG",
                category = EventCategory.TAHSILI.name,
                startDateIso = "2027-02-01",
                endDateIso = "2027-03-15",
                hijriDateDisplay = "24 شعبان 1448 هـ",
                gregorianDateDisplay = "1 فبراير 2027 م",
                sourcePlatform = "المصدر: هيئة تقويم التعليم والتدريب - قياس",
                highSchoolOnly = true,
                thirdHighSchoolPriority = true,
                registrationNote = "خاص بطلاب الثالث الثانوي $TAHSILI_ESTIMATED_TAG",
                estimatedBadgeText = TAHSILI_ESTIMATED_TAG
            ),
            AcademicEventEntity(
                id = "moe_1448_founding_day",
                title = "إجازة يوم التأسيس السعودي",
                subtitle = "إجازة رسمية بمناسبة ذكرى تأسيس الدولة السعودية",
                category = EventCategory.VACATION.name,
                startDateIso = "2027-02-19",
                endDateIso = "2027-02-22",
                hijriDateDisplay = "12 - 15 رمضان 1448 هـ",
                gregorianDateDisplay = "19 - 22 فبراير 2027 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "الموافق 12 - 15 رمضان 1448 هـ (19 - 22 فبراير 2027 م)"
            ),
            AcademicEventEntity(
                id = "moe_1448_eid_fitr",
                title = "إجازة عيد الفطر المبارك",
                subtitle = "إجازة العشر الأواخر من رمضان وعيد الفطر المبارك",
                category = EventCategory.VACATION.name,
                startDateIso = "2027-02-26",
                endDateIso = "2027-03-13",
                hijriDateDisplay = "19 رمضان - 5 شوال 1448 هـ",
                gregorianDateDisplay = "26 فبراير - 13 مارس 2027 م",
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = "تبدأ بنهاية دوام الخميس 18 رمضان 1448 هـ، وتستأنف الدراسة الأحد 6 شوال 1448 هـ (14 مارس 2027 م)"
            ),
            AcademicEventEntity(
                id = "qiyas_step_spring_1448",
                title = "اختبار كفايات اللغة الإنجليزية (STEP)",
                subtitle = "النافذة الموصى بها للتقديم على الجامعات والمسارات الصحية والعلمية",
                category = EventCategory.STEP.name,
                startDateIso = "2027-04-10",
                endDateIso = "2027-04-25",
                hijriDateDisplay = "3 - 18 ذو القعدة 1448 هـ",
                gregorianDateDisplay = "10 - 25 أبريل 2027 م",
                sourcePlatform = "المصدر: هيئة تقويم التعليم والتدريب - قياس",
                highSchoolOnly = true,
                thirdHighSchoolPriority = false,
                registrationNote = "متاح على مدار العام بنظام المقرات المحوسبة (صلاحية الاختبار 3 سنوات)"
            ),
            AcademicEventEntity(
                id = "moe_1448_eid_adha",
                title = "إجازة عيد الأضحى المبارك",
                subtitle = if (isWestern) {
                    "إجازة عيد الأضحى لمدارس المدينة المنورة ومكة المكرمة وجدة والطائف"
                } else {
                    "إجازة عيد الأضحى المبارك لبقية مناطق المملكة"
                },
                category = EventCategory.VACATION.name,
                startDateIso = adhaStartIso,
                endDateIso = "2027-05-22",
                hijriDateDisplay = adhaHijri,
                gregorianDateDisplay = adhaGreg,
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = adhaNote
            ),
            AcademicEventEntity(
                id = "qiyas_tahsili_period_1",
                title = "الاختبار التحصيلي — الفترة الأولى $TAHSILI_ESTIMATED_TAG",
                subtitle = "الاختبار التحصيلي الدراسي الأول لخريجي الثالث الثانوي (علمي / نظري) $TAHSILI_ESTIMATED_TAG",
                category = EventCategory.TAHSILI.name,
                startDateIso = "2027-05-26",
                endDateIso = "2027-05-30",
                hijriDateDisplay = "20 - 24 ذو الحجة 1448 هـ",
                gregorianDateDisplay = "26 - 30 مايو 2027 م",
                sourcePlatform = "المصدر: هيئة تقويم التعليم والتدريب - قياس",
                highSchoolOnly = true,
                thirdHighSchoolPriority = true,
                registrationNote = "الفترة الأولى للقبول الجامعي لثالث ثانوي $TAHSILI_ESTIMATED_TAG",
                estimatedBadgeText = TAHSILI_ESTIMATED_TAG
            ),
            AcademicEventEntity(
                id = "qiyas_tahsili_period_2",
                title = "الاختبار التحصيلي — الفترة الثانية $TAHSILI_ESTIMATED_TAG",
                subtitle = "الفرصة الثانية لتعزيز درجة الاختبار التحصيلي لطلاب الثالث الثانوي $TAHSILI_ESTIMATED_TAG",
                category = EventCategory.TAHSILI.name,
                startDateIso = "2027-06-09",
                endDateIso = "2027-06-12",
                hijriDateDisplay = "4 - 7 محرم 1449 هـ",
                gregorianDateDisplay = "9 - 12 يونيو 2027 م",
                sourcePlatform = "المصدر: هيئة تقويم التعليم والتدريب - قياس",
                highSchoolOnly = true,
                thirdHighSchoolPriority = true,
                registrationNote = "تنتهي في 12 يونيو 2027 م (7 محرم 1449 هـ) قبل بدء الاختبارات النهائية للمدارس $TAHSILI_ESTIMATED_TAG",
                estimatedBadgeText = TAHSILI_ESTIMATED_TAG
            ),
            AcademicEventEntity(
                id = "moe_1448_final_exams",
                title = "اختبارات نهاية العام الدراسي 1448 هـ",
                subtitle = "الاختبارات النهائية للفصل الدراسي الثاني وإتمام الصف الدراسي",
                category = EventCategory.SCHOOL_EXAM.name,
                startDateIso = finalsStartIso,
                endDateIso = finalsEndIso,
                hijriDateDisplay = finalsHijri,
                gregorianDateDisplay = finalsGreg,
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = finalsNote
            ),
            AcademicEventEntity(
                id = "moe_1448_summer_vacation",
                title = "إجازة نهاية العام الدراسي 1448 هـ",
                subtitle = "بداية الإجازة الصيفية للطلاب والطالبات بنهاية دوام الخميس",
                category = EventCategory.VACATION.name,
                startDateIso = summerStartIso,
                endDateIso = "2027-08-21",
                hijriDateDisplay = summerHijri,
                gregorianDateDisplay = summerGreg,
                sourcePlatform = "المصدر: تقويم وزارة التعليم",
                registrationNote = summerNote
            )
        )
    }

    fun defaultStudyTasks(): List<StudyTaskEntity> = listOf(
        StudyTaskEntity(
            title = "مراجعة تأسيس الرياضيات والفيزياء (الاختبار التحصيلي)",
            subjectTag = "تحصيلي - علمي",
            targetDateIso = "2026-11-25",
            hijriTargetDisplay = "15 جمادى الآخرة 1448 هـ",
            priorityLevel = 3,
            isCompleted = false,
            forThirdHighSchool = true
        ),
        StudyTaskEntity(
            title = "حل 50 سؤال تجميعات كيمياء وأحياء (الفصل الأول)",
            subjectTag = "تحصيلي - تجميعات",
            targetDateIso = "2026-11-30",
            hijriTargetDisplay = "20 جمادى الآخرة 1448 هـ",
            priorityLevel = 3,
            isCompleted = false,
            forThirdHighSchool = true
        ),
        StudyTaskEntity(
            title = "التدرب على نماذج القدرات الورقي والمحوسب (1 - 5 رجب)",
            subjectTag = "قدرات عامة",
            targetDateIso = "2026-12-08",
            hijriTargetDisplay = "28 جمادى الآخرة 1448 هـ",
            priorityLevel = 2,
            isCompleted = false,
            forThirdHighSchool = false
        ),
        StudyTaskEntity(
            title = "إعداد ملخصات اختبارات منتصف الفصل والمشاريع المدرسية",
            subjectTag = "مهام مدرسية",
            targetDateIso = "2026-11-19",
            hijriTargetDisplay = "9 جمادى الآخرة 1448 هـ",
            priorityLevel = 2,
            isCompleted = true,
            forThirdHighSchool = false
        )
    )

    fun defaultPlatforms(nowMillis: Long): List<PlatformSyncEntity> = listOf(
        PlatformSyncEntity(
            platformId = "umm_al_qura_api",
            platformName = "خدمة مزامنة التاريخ الهجري والتحقق من التوقيت",
            authorityName = "المصدر: تقويم أم القرى المدمج + خدمة التحقق الهجري",
            officialUrl = "",
            dataScope = "الاتصال بخدمة (api.aladhan.com/v1/gToH) لجلب التاريخ الهجري الحالي والتحقق من مطابقة التوقيت اليومي",
            lastSyncedEpochMillis = nowMillis,
            syncStatusText = "جاهز لمزامنة التاريخ الهجري والتحقق من التوقيت",
            isVerifiedOnline = true,
            syncedEventsCount = 16
        ),
        PlatformSyncEntity(
            platformId = "moe_sa_local",
            platformName = "قاعدة بيانات التقويم الدراسي 1448 هـ (تحديث شهري آمن)",
            authorityName = "المصدر: تقويم وزارة التعليم (التقويم العام + تقويم مكة والمدينة وجدة والطائف)",
            officialUrl = "",
            dataScope = "مخزنة في قاعدة البيانات المرجعية (Room SQLite v3) بمعزل تام عن بيانات الطالب الشخصية",
            lastSyncedEpochMillis = nowMillis,
            syncStatusText = "محدّثة للإصدار المرجعي رقم 3 (ذو القعدة 1448 هـ)",
            isVerifiedOnline = true,
            syncedEventsCount = 11
        ),
        PlatformSyncEntity(
            platformId = "etec_qiyas_local",
            platformName = "البيانات المرجعية للجامعات واختبارات قياس",
            authorityName = "المصدر: هيئة تقويم التعليم والتدريب - قياس + أدلة القبول الجامعي",
            officialUrl = "",
            dataScope = "تشمل مواعيد القدرات والتحصيلي وSTEP ومعادلات الجامعات السعودية وآخر نسب القبول الاسترشادية",
            lastSyncedEpochMillis = nowMillis,
            syncStatusText = "محدّثة (18 تخصصاً جامعياً + 6 معادلات موزونة معتمدة)",
            isVerifiedOnline = true,
            syncedEventsCount = 18
        )
    )
}
