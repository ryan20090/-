package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.saudiacademic.masar.R
import com.example.data.AcademicRepository
import com.example.data.local.OfficialSaudiCalendarSeed
import com.example.data.local.UniversityAdmissionsSeed
import com.example.model.CalendarRegion
import com.example.model.EventCategory
import com.example.model.UniversityFormulaType
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مسار 1448", appName)
    }

    @Test
    fun `verify v2_0 calendar dates, seconds countdown, masarat gpa, and university admissions`() {
        // 1. General vs Makkah/Madinah/Jeddah/Taif week calculation on Nov 15, 2026
        val nov15 = LocalDate.of(2026, 11, 15)
        val generalWeek = AcademicRepository.computeAcademicWeek(nov15, CalendarRegion.GENERAL)
        assertEquals(12, generalWeek.weekNumber)
        assertEquals(19, generalWeek.totalSemesterWeeks)

        val westernWeek = AcademicRepository.computeAcademicWeek(nov15, CalendarRegion.WESTERN_HOLY_CITIES)
        assertEquals(11, westernWeek.weekNumber)
        assertEquals(18, westernWeek.totalSemesterWeeks)

        // 2. Verify seed dates for General region
        val generalEvents = OfficialSaudiCalendarSeed.defaultAcademicEvents1448(CalendarRegion.GENERAL)
        val qudurat = generalEvents.first { it.id == "qiyas_qudurat_paper_1" }
        assertEquals("2026-12-10", qudurat.startDateIso)
        assertEquals("2026-12-14", qudurat.endDateIso)
        assertEquals("1 - 5 رجب 1448 هـ", qudurat.hijriDateDisplay)

        val foundingDay = generalEvents.first { it.id == "moe_1448_founding_day" }
        assertEquals("12 - 15 رمضان 1448 هـ", foundingDay.hijriDateDisplay)

        val eidFitr = generalEvents.first { it.id == "moe_1448_eid_fitr" }
        assertEquals("2027-02-26", eidFitr.startDateIso)
        assertEquals("2027-03-13", eidFitr.endDateIso)
        assertEquals("19 رمضان - 5 شوال 1448 هـ", eidFitr.hijriDateDisplay)

        // 3. Verify seed dates for Western Holy Cities region
        val westernEvents = OfficialSaudiCalendarSeed.defaultAcademicEvents1448(CalendarRegion.WESTERN_HOLY_CITIES)
        val westernStart = westernEvents.first { it.id == "moe_1448_start" }
        assertEquals("2026-08-30", westernStart.startDateIso)
        assertEquals("17 ربيع الأول 1448 هـ", westernStart.hijriDateDisplay)

        val westernAdha = westernEvents.first { it.id == "moe_1448_eid_adha" }
        assertEquals("2027-04-30", westernAdha.startDateIso)
        assertEquals("23 ذو القعدة - 16 ذو الحجة 1448 هـ", westernAdha.hijriDateDisplay)

        // 4. Verify Tahsili estimated tag, Period 2 end date (12 June 2027), and seconds countdown
        val tahsiliP2 = generalEvents.first { it.id == "qiyas_tahsili_period_2" }
        assertEquals("2027-06-12", tahsiliP2.endDateIso)
        assertTrue(tahsiliP2.title.contains("(موعد تقديري لحين الإعلان الرسمي من قياس)"))

        val tahsiliP1 = generalEvents.first { it.id == "qiyas_tahsili_period_1" }
        val countdown = AcademicRepository.buildCountdownItem(
            event = tahsiliP1,
            nowDateTime = LocalDateTime.of(2026, 11, 13, 12, 30, 15)
        )
        assertEquals(EventCategory.TAHSILI, countdown.categoryEnum)
        assertTrue(countdown.daysRemaining > 190)
        assertEquals(45L, countdown.secondsRemaining)

        // 5. Verify Masarat Cumulative GPA (20% Grade 1 + 40% Grade 2 + 40% Grade 3)
        val masaratGpa = AcademicRepository.calculateMasaratCumulativeGpa(95.0, 98.0, 100.0)
        // 95*0.2 + 98*0.4 + 100*0.4 = 19.0 + 39.2 + 40.0 = 98.2
        assertEquals(98.2, masaratGpa, 0.01)

        // 6. Verify Reverse Tahsili Target Calculator
        val reqTahsili = AcademicRepository.calculateRequiredTahsiliScore(
            hsGpa = 100.0,
            quduratScore = 90.0,
            stepScore = 0.0,
            targetWeighted = 93.0,
            hsWeight = 30,
            quduratWeight = 30,
            tahsiliWeight = 40,
            stepWeight = 0
        )
        // (93*100 - 3000 - 2700) / 40 = 3600 / 40 = 90.0
        assertEquals(90.0, reqTahsili, 0.01)

        // 7. Verify University Formulas & Admissions Evaluator
        val kfupmScore = UniversityFormulaType.KFUPM_50_50.calculate(99.0, 96.0, 94.0, 85.0)
        assertEquals(95.0, kfupmScore, 0.01)

        val admissions = UniversityAdmissionsSeed.defaultUniversityAdmissions()
        assertTrue(admissions.size >= 15)
        val evals = AcademicRepository.evaluateUniversityAdmissions(
            admissions = admissions,
            hsGpa = 99.0,
            qudurat = 95.0,
            tahsili = 95.0,
            step = 90.0
        )
        assertTrue(evals.any { it.isEligibleByLastYear })

        // 8. Verify Jetpack Glance AppWidget Snapshot (Academic Week, Next Vacation, Tahsili Days)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val widgetSnapshot = com.example.widget.MasarGlanceAppWidget.buildSnapshot(
            context = context,
            now = LocalDateTime.of(2026, 11, 15, 10, 0)
        )
        assertEquals("الأسبوع الدراسي 12 من 19", widgetSnapshot.weekBadgeText)
        assertTrue(widgetSnapshot.nextVacationDaysText.contains("متبقي"))
        assertTrue(widgetSnapshot.tahsiliDaysCount > 180)
        assertTrue(widgetSnapshot.tahsiliDaysText.contains("متبقي"))
    }
}

