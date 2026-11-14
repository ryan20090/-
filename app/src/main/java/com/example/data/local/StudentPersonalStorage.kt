package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CalendarRegion
import com.example.model.ComputerizedExamBooking
import com.example.model.GradeLevel
import com.example.model.ShortSchoolQuizItem
import org.json.JSONArray
import org.json.JSONObject

class StudentPersonalStorage(private val prefs: SharedPreferences?) {

    var selectedGrade: GradeLevel
        get() = GradeLevel.fromId(prefs?.getString(KEY_GRADE_ID, GradeLevel.HIGH_3.id))
        set(value) {
            prefs?.edit()?.putString(KEY_GRADE_ID, value.id)?.apply()
        }

    var selectedRegion: CalendarRegion
        get() = CalendarRegion.fromId(prefs?.getString(KEY_REGION_ID, CalendarRegion.GENERAL.id))
        set(value) {
            prefs?.edit()?.putString(KEY_REGION_ID, value.id)?.apply()
        }

    var hsGrade1: String
        get() = prefs?.getString(KEY_HS_1, "98.0") ?: "98.0"
        set(value) {
            prefs?.edit()?.putString(KEY_HS_1, value)?.apply()
        }

    var hsGrade2: String
        get() = prefs?.getString(KEY_HS_2, "98.5") ?: "98.5"
        set(value) {
            prefs?.edit()?.putString(KEY_HS_2, value)?.apply()
        }

    var hsGrade3: String
        get() = prefs?.getString(KEY_HS_3, "99.0") ?: "99.0"
        set(value) {
            prefs?.edit()?.putString(KEY_HS_3, value)?.apply()
        }

    var highSchoolGpa: String
        get() = prefs?.getString(KEY_UNIFIED_HS_GPA, "98.6") ?: "98.6"
        set(value) {
            prefs?.edit()?.putString(KEY_UNIFIED_HS_GPA, value)?.apply()
        }

    var quduratScore: String
        get() = prefs?.getString(KEY_UNIFIED_QUDURAT, "92") ?: "92"
        set(value) {
            prefs?.edit()?.putString(KEY_UNIFIED_QUDURAT, value)?.apply()
        }

    var tahsiliScore: String
        get() = prefs?.getString(KEY_UNIFIED_TAHSILI, "90") ?: "90"
        set(value) {
            prefs?.edit()?.putString(KEY_UNIFIED_TAHSILI, value)?.apply()
        }

    var stepScore: String
        get() = prefs?.getString(KEY_UNIFIED_STEP, "85") ?: "85"
        set(value) {
            prefs?.edit()?.putString(KEY_UNIFIED_STEP, value)?.apply()
        }

    var targetWeightedGoal: String
        get() = prefs?.getString(KEY_TARGET_GOAL, "94.0") ?: "94.0"
        set(value) {
            prefs?.edit()?.putString(KEY_TARGET_GOAL, value)?.apply()
        }

    var customWeightHs: Int
        get() = prefs?.getInt(KEY_CUSTOM_W_HS, 30) ?: 30
        set(value) {
            prefs?.edit()?.putInt(KEY_CUSTOM_W_HS, value)?.apply()
        }

    var customWeightQud: Int
        get() = prefs?.getInt(KEY_CUSTOM_W_QUD, 30) ?: 30
        set(value) {
            prefs?.edit()?.putInt(KEY_CUSTOM_W_QUD, value)?.apply()
        }

    var customWeightTah: Int
        get() = prefs?.getInt(KEY_CUSTOM_W_TAH, 40) ?: 40
        set(value) {
            prefs?.edit()?.putInt(KEY_CUSTOM_W_TAH, value)?.apply()
        }

    var customWeightStep: Int
        get() = prefs?.getInt(KEY_CUSTOM_W_STEP, 0) ?: 0
        set(value) {
            prefs?.edit()?.putInt(KEY_CUSTOM_W_STEP, value)?.apply()
        }

    var tahsiliRemainingPages: String
        get() = prefs?.getString(KEY_TAHSILI_PAGES, "280") ?: "280"
        set(value) {
            prefs?.edit()?.putString(KEY_TAHSILI_PAGES, value)?.apply()
        }

    fun getComputerizedBooking(): ComputerizedExamBooking? {
        val raw = prefs?.getString(KEY_COMPUTERIZED_BOOKING, null) ?: return defaultComputerizedBooking()
        if (raw == "NONE") return null
        return runCatching {
            val obj = JSONObject(raw)
            ComputerizedExamBooking(
                examType = obj.optString("examType", "قدرات محوسب"),
                examTitle = obj.optString("examTitle", "اختبار القدرات المحوسب"),
                examDateIso = obj.optString("examDateIso", "2026-12-20"),
                hijriDateDisplay = obj.optString("hijriDateDisplay", "11 رجب 1448 هـ"),
                gregorianDateDisplay = obj.optString("gregorianDateDisplay", "20 ديسمبر 2026 م")
            )
        }.getOrNull()
    }

    fun saveComputerizedBooking(booking: ComputerizedExamBooking?) {
        if (booking == null) {
            prefs?.edit()?.putString(KEY_COMPUTERIZED_BOOKING, "NONE")?.apply()
            return
        }
        val obj = JSONObject().apply {
            put("examType", booking.examType)
            put("examTitle", booking.examTitle)
            put("examDateIso", booking.examDateIso)
            put("hijriDateDisplay", booking.hijriDateDisplay)
            put("gregorianDateDisplay", booking.gregorianDateDisplay)
        }
        prefs?.edit()?.putString(KEY_COMPUTERIZED_BOOKING, obj.toString())?.apply()
    }

    fun getShortSchoolQuizzes(): List<ShortSchoolQuizItem> {
        val raw = prefs?.getString(KEY_SHORT_QUIZZES, null) ?: return defaultShortQuizzes()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ShortSchoolQuizItem(
                            id = obj.optString("id"),
                            subjectName = obj.optString("subjectName"),
                            examDateIso = obj.optString("examDateIso"),
                            hijriDateDisplay = obj.optString("hijriDateDisplay"),
                            gregorianDateDisplay = obj.optString("gregorianDateDisplay"),
                            note = obj.optString("note")
                        )
                    )
                }
            }
        }.getOrElse { defaultShortQuizzes() }
    }

    fun saveShortSchoolQuizzes(list: List<ShortSchoolQuizItem>) {
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("subjectName", item.subjectName)
                put("examDateIso", item.examDateIso)
                put("hijriDateDisplay", item.hijriDateDisplay)
                put("gregorianDateDisplay", item.gregorianDateDisplay)
                put("note", item.note)
            }
            arr.put(obj)
        }
        prefs?.edit()?.putString(KEY_SHORT_QUIZZES, arr.toString())?.apply()
    }

    companion object {
        const val PREFS_FILE_NAME = "masar_student_personal_store_v2"
        const val KEY_GRADE_ID = "selected_grade_id"
        const val KEY_REGION_ID = "selected_region_id"
        private const val KEY_HS_1 = "hs_grade_1"
        private const val KEY_HS_2 = "hs_grade_2"
        private const val KEY_HS_3 = "hs_grade_3"
        private const val KEY_UNIFIED_HS_GPA = "unified_hs_gpa"
        private const val KEY_UNIFIED_QUDURAT = "unified_qudurat"
        private const val KEY_UNIFIED_TAHSILI = "unified_tahsili"
        private const val KEY_UNIFIED_STEP = "unified_step"
        private const val KEY_TARGET_GOAL = "target_weighted_goal"
        private const val KEY_CUSTOM_W_HS = "custom_w_hs"
        private const val KEY_CUSTOM_W_QUD = "custom_w_qud"
        private const val KEY_CUSTOM_W_TAH = "custom_w_tah"
        private const val KEY_CUSTOM_W_STEP = "custom_w_step"
        private const val KEY_TAHSILI_PAGES = "tahsili_remaining_pages"
        private const val KEY_COMPUTERIZED_BOOKING = "computerized_exam_booking"
        private const val KEY_SHORT_QUIZZES = "short_school_quizzes"

        fun create(context: Context): StudentPersonalStorage {
            val sp = context.applicationContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            return StudentPersonalStorage(sp)
        }

        fun fromContext(context: Context): StudentPersonalStorage = create(context)

        private fun defaultComputerizedBooking() = ComputerizedExamBooking(
            examType = "قدرات محوسب",
            examTitle = "موعدي القادم في اختبار القدرات المحوسب",
            examDateIso = "2026-12-18",
            hijriDateDisplay = "9 رجب 1448 هـ",
            gregorianDateDisplay = "18 ديسمبر 2026 م"
        )

        private fun defaultShortQuizzes() = listOf(
            ShortSchoolQuizItem(
                id = "quiz_math_1",
                subjectName = "اختبار فترة ثانية — الرياضيات",
                examDateIso = "2026-11-18",
                hijriDateDisplay = "8 جمادى الآخرة 1448 هـ",
                gregorianDateDisplay = "18 نوفمبر 2026 م",
                note = "الفصل الثالث: التفاضل والتكامل"
            ),
            ShortSchoolQuizItem(
                id = "quiz_phys_1",
                subjectName = "اختبار قصير — الفيزياء",
                examDateIso = "2026-12-01",
                hijriDateDisplay = "21 جمادى الآخرة 1448 هـ",
                gregorianDateDisplay = "1 ديسمبر 2026 م",
                note = "الكهرباء والمغناطيسية"
            )
        )
    }
}
