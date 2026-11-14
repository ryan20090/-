package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.AcademicRepository
import com.example.data.local.OfficialSaudiCalendarSeed
import com.example.data.local.StudentPersonalStorage
import com.example.model.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime

data class MasarWidgetSnapshot(
    val regionTitle: String,
    val semesterName: String,
    val weekBadgeText: String,
    val weekStatusSubtitle: String,
    val hijriToday: String,
    val progressPercent: Int,
    val nextVacationTitle: String,
    val nextVacationHijri: String,
    val nextVacationDaysText: String,
    val nextVacationDaysCount: Long,
    val tahsiliTitle: String,
    val tahsiliHijri: String,
    val tahsiliDaysText: String,
    val tahsiliDaysCount: Long
)

class MasarGlanceAppWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = buildSnapshot(context)
        provideContent {
            GlanceTheme {
                MasarGlanceWidgetContent(snapshot = snapshot)
            }
        }
    }

    companion object {
        fun buildSnapshot(
            context: Context,
            now: LocalDateTime = LocalDateTime.now()
        ): MasarWidgetSnapshot {
            val storage = StudentPersonalStorage.create(context)
            val region = storage.selectedRegion
            val today = now.toLocalDate()

            val weekInfo = AcademicRepository.calculateAcademicWeekInfo(
                today = today,
                overrideHijriToday = null,
                region = region
            )

            val events = OfficialSaudiCalendarSeed.defaultAcademicEvents1448(region)
                .map { AcademicRepository.buildCountdownItem(it, now) }

            val nextVacation = events.firstOrNull {
                it.categoryEnum == EventCategory.VACATION && !it.isPast
            }
            val tahsiliP1 = events.firstOrNull { it.event.id == "qiyas_tahsili_period_1" }
                ?: events.firstOrNull { it.categoryEnum == EventCategory.TAHSILI && !it.isPast }

            val weekBadge = if (weekInfo.isActiveStudyWeek) {
                "الأسبوع الدراسي ${weekInfo.currentWeekNumber} من ${weekInfo.totalSemesterWeeks}"
            } else {
                weekInfo.statusBadgeText
            }

            val vacTitle = nextVacation?.event?.title ?: "إجازة الخريف الدراسية"
            val vacHijri = nextVacation?.event?.hijriDateDisplay ?: "12 ربيع الآخر 1448 هـ"
            val vacDaysCount = nextVacation?.daysRemaining ?: 0L
            val vacDaysText = when {
                nextVacation == null -> "مكتملة"
                nextVacation.isOngoing -> "إجازة جارية الآن"
                else -> "متبقي ${nextVacation.daysRemaining} يوم"
            }

            val tahTitle = "الاختبار التحصيلي (الفترة 1)"
            val tahHijri = tahsiliP1?.event?.hijriDateDisplay ?: "16 - 20 ذو الحجة 1448 هـ"
            val tahDaysCount = tahsiliP1?.daysRemaining ?: 0L
            val tahDaysText = if (tahsiliP1 != null) {
                "متبقي ${tahsiliP1.daysRemaining} يوم"
            } else {
                "قريباً"
            }

            return MasarWidgetSnapshot(
                regionTitle = region.title,
                semesterName = weekInfo.currentSemesterName,
                weekBadgeText = weekBadge,
                weekStatusSubtitle = weekInfo.statusDescription,
                hijriToday = weekInfo.todayHijriFormatted,
                progressPercent = (weekInfo.semesterProgress * 100).toInt().coerceIn(0, 100),
                nextVacationTitle = vacTitle,
                nextVacationHijri = vacHijri,
                nextVacationDaysText = vacDaysText,
                nextVacationDaysCount = vacDaysCount,
                tahsiliTitle = tahTitle,
                tahsiliHijri = tahHijri,
                tahsiliDaysText = tahDaysText,
                tahsiliDaysCount = tahDaysCount
            )
        }
    }
}

class RefreshMasarWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        MasarGlanceAppWidget().updateAll(context)
    }
}

@Composable
fun MasarGlanceWidgetContent(snapshot: MasarWidgetSnapshot) {
    val bgDeepEmerald = ColorProvider(Color(0xFF064B36))
    val cardEmeraldSurface = ColorProvider(Color(0xFF0B6B4F))
    val vacationCardBg = ColorProvider(Color(0xFF00695C))
    val tahsiliCardBg = ColorProvider(Color(0xFF4527A0))
    val goldAccent = ColorProvider(Color(0xFFF3C969))
    val whiteColor = ColorProvider(Color(0xFFFFFFFF))
    val softWhiteColor = ColorProvider(Color(0xFFD7EFE5))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(bgDeepEmerald)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Top Header: App Title + Region + Academic Week Pill + Refresh Action
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "مسار 1448 • ${snapshot.regionTitle}",
                        style = TextStyle(
                            color = goldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = snapshot.hijriToday,
                        style = TextStyle(
                            color = softWhiteColor,
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }

                Box(
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .background(cardEmeraldSurface)
                        .clickable(actionRunCallback<RefreshMasarWidgetAction>())
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "تحديث ↻",
                        style = TextStyle(
                            color = goldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Prominent Current Academic Week Banner
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(14.dp)
                    .background(cardEmeraldSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = snapshot.weekBadgeText,
                            style = TextStyle(
                                color = whiteColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = snapshot.semesterName,
                            style = TextStyle(
                                color = softWhiteColor,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(10.dp)
                            .background(bgDeepEmerald)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "إنجاز ${snapshot.progressPercent}%",
                            style = TextStyle(
                                color = goldAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Bottom Dual Countdown Cards: Next Vacation & Tahsili Exam (in Days)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Card 1: Next Official Vacation Countdown
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(14.dp)
                        .background(vacationCardBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "أقرب إجازة رسمية",
                            style = TextStyle(
                                color = goldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = snapshot.nextVacationTitle,
                            style = TextStyle(
                                color = whiteColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(3.dp))
                        Text(
                            text = snapshot.nextVacationDaysText,
                            style = TextStyle(
                                color = goldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = snapshot.nextVacationHijri,
                            style = TextStyle(
                                color = softWhiteColor,
                                fontSize = 9.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Card 2: Tahsili Exam Countdown in Days
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .cornerRadius(14.dp)
                        .background(tahsiliCardBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "الاختبار التحصيلي",
                            style = TextStyle(
                                color = goldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = snapshot.tahsiliTitle,
                            style = TextStyle(
                                color = whiteColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = GlanceModifier.height(3.dp))
                        Text(
                            text = snapshot.tahsiliDaysText,
                            style = TextStyle(
                                color = goldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = snapshot.tahsiliHijri,
                            style = TextStyle(
                                color = softWhiteColor,
                                fontSize = 9.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
