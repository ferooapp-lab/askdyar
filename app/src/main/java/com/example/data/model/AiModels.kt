package com.example.data.model

import com.example.data.local.NoteEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AiQuoteResponse(
    val text: String,
    val author: String,
    val theme: String,
    val isSimulated: Boolean = true
)

data class DayMood(
    val dayLabel: String,
    val dateStr: String,
    val moodScore: Int, // 0 - 100
    val emoji: String,
    val noteCount: Int
)

data class MoodBreakdown(
    val emotionName: String,
    val percentage: Int, // 0 - 100
    val emoji: String
)

data class MoodAnalysisResult(
    val days: List<DayMood>,
    val breakdowns: List<MoodBreakdown>,
    val overallScore: Int, // e.g. 88
    val summaryInsight: String,
    val advice: String,
    val isSimulated: Boolean = true
)

data class MoodInsightDay(
    val dayKey: String,
    val shortLabel: String,
    val fullDateLabel: String,
    val moodScore: Int, // 0 - 100
    val emoji: String,
    val moodLabel: String,
    val noteCount: Int,
    val hasActivity: Boolean
)

data class MoodInsights30DaySummary(
    val days: List<MoodInsightDay>, // 30 days oldest -> today
    val consistencyPercent: Int, // 0 - 100
    val activeDaysCount: Int,
    val totalNotesLast30Days: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val averageMoodScore: Int,
    val dominantEmotion: String,
    val breakdowns: List<MoodBreakdown>,
    val consistencyInsight: String
)

object MoodInsightsCalculator {
    fun build30DayInsights(
        notes: List<NoteEntity>,
        checkInHistory: Map<String, Triple<String, String, Int>> = emptyMap(),
        nowMillis: Long = System.currentTimeMillis()
    ): MoodInsights30DaySummary {
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val shortFormat = SimpleDateFormat("d", Locale("tr"))
        val fullFormat = SimpleDateFormat("dd MMM EEE", Locale("tr"))

        val days = mutableListOf<MoodInsightDay>()
        var totalNotes = 0
        var activeDays = 0

        var passionHits = 0
        var peaceHits = 0
        var longingHits = 0
        var joyHits = 0

        for (offset in 29 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -offset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + 24L * 60L * 60L * 1000L - 1L
            val dayKey = keyFormat.format(dayCal.time)

            val dayNotes = notes.filter { note ->
                val ts = maxOf(note.createdAt, note.updatedAt)
                ts in dayStart..dayEnd
            }
            totalNotes += dayNotes.size

            val checkIn = checkInHistory[dayKey]
            val hasActivity = dayNotes.isNotEmpty() || checkIn != null
            if (hasActivity) activeDays++

            var noteDelta = 0
            dayNotes.forEach { n ->
                val combined = "${n.title} ${n.content} ${n.tags}".lowercase(Locale("tr"))
                if (combined.contains("aşk") || combined.contains("sev") || combined.contains("tutku") || combined.contains("kalp")) {
                    passionHits += 2
                    noteDelta += 6
                }
                if (combined.contains("huzur") || combined.contains("şükür") || combined.contains("iyi ki") || combined.contains("sabah")) {
                    peaceHits += 2
                    noteDelta += 5
                }
                if (combined.contains("özlem") || combined.contains("hasret") || combined.contains("gece") || combined.contains("bekle")) {
                    longingHits += 2
                    noteDelta += 2
                }
                if (combined.contains("mutlu") || combined.contains("gül") || combined.contains("neşe") || combined.contains("harika")) {
                    joyHits += 2
                    noteDelta += 7
                }
                if (combined.contains("kırgın") || combined.contains("üzgün") || combined.contains("ağla")) {
                    noteDelta -= 8
                }
            }

            if (checkIn != null) {
                when {
                    checkIn.first.contains("Aşık", true) || checkIn.first.contains("Tutkulu", true) -> passionHits += 2
                    checkIn.first.contains("Huzurlu", true) -> peaceHits += 2
                    checkIn.first.contains("Özlem", true) -> longingHits += 2
                    else -> joyHits += 1
                }
            }

            val score = when {
                checkIn != null && dayNotes.isNotEmpty() -> {
                    ((checkIn.third + (82 + noteDelta).coerceIn(58, 99)) / 2).coerceIn(55, 99)
                }
                checkIn != null -> checkIn.third.coerceIn(55, 99)
                dayNotes.isNotEmpty() -> (80 + noteDelta + (dayNotes.size * 2)).coerceIn(58, 99)
                else -> {
                    // Gentle baseline wave so empty days visually distinguish from active days
                    val wave = ((29 - offset) % 5) * 2
                    (66 + wave).coerceIn(60, 76)
                }
            }

            val (emoji, moodLabel) = when {
                checkIn != null -> checkIn.second to checkIn.first
                dayNotes.isNotEmpty() && score >= 92 -> "😍" to "Zirve Romantizm"
                dayNotes.isNotEmpty() && score >= 84 -> "💖" to "Sevgi Dolu"
                dayNotes.isNotEmpty() && score >= 74 -> "🥰" to "Sıcak & Huzurlu"
                dayNotes.isNotEmpty() -> "💌" to "Düşünceli"
                else -> "✨" to "Not Kaydı Yok"
            }

            days.add(
                MoodInsightDay(
                    dayKey = dayKey,
                    shortLabel = if (offset == 0) "Bugün" else shortFormat.format(dayCal.time),
                    fullDateLabel = fullFormat.format(dayCal.time),
                    moodScore = score,
                    emoji = emoji,
                    moodLabel = moodLabel,
                    noteCount = dayNotes.size,
                    hasActivity = hasActivity
                )
            )
        }

        // Compute current streak (from today backwards) and longest streak in 30 days
        var currentStreak = 0
        for (i in days.lastIndex downTo 0) {
            if (days[i].hasActivity) currentStreak++
            else if (i < days.lastIndex) break
        }
        var longestStreak = 0
        var running = 0
        days.forEach { d ->
            if (d.hasActivity) {
                running++
                if (running > longestStreak) longestStreak = running
            } else {
                running = 0
            }
        }

        val consistencyPercent = ((activeDays * 100f) / 30f).toInt().coerceIn(0, 100)
        val activeScores = days.filter { it.hasActivity }.map { it.moodScore }
        val avgMoodScore = if (activeScores.isNotEmpty()) {
            activeScores.average().toInt().coerceIn(50, 99)
        } else {
            days.map { it.moodScore }.average().toInt().coerceIn(50, 99)
        }

        val totalHits = (passionHits + peaceHits + longingHits + joyHits).coerceAtLeast(1)
        val breakdowns = if (passionHits + peaceHits + longingHits + joyHits == 0) {
            listOf(
                MoodBreakdown("Tutku & Aşk", 40, "🔥"),
                MoodBreakdown("Huzur & Şefkat", 30, "🕊️"),
                MoodBreakdown("Özlem & Hasret", 15, "💌"),
                MoodBreakdown("Neşe & Mutluluk", 15, "☀️")
            )
        } else {
            val p = (passionHits * 100 / totalHits).coerceAtLeast(5)
            val h = (peaceHits * 100 / totalHits).coerceAtLeast(5)
            val l = (longingHits * 100 / totalHits).coerceAtLeast(5)
            val j = (100 - p - h - l).coerceAtLeast(5)
            listOf(
                MoodBreakdown("Tutku & Aşk", p, "🔥"),
                MoodBreakdown("Huzur & Şefkat", h, "🕊️"),
                MoodBreakdown("Özlem & Hasret", l, "💌"),
                MoodBreakdown("Neşe & Mutluluk", j, "☀️")
            )
        }

        val dominant = breakdowns.maxByOrNull { it.percentage }?.let { "${it.emoji} ${it.emotionName}" } ?: "💖 Sevgi Dolu"
        val insight = when {
            activeDays >= 20 -> "Olağanüstü romantik tutarlılık! Son 30 günün $activeDays gününde aşk notu veya duygu kaydı bıraktın."
            activeDays >= 8 -> "Güzel bir ritim yakaladın: Son 30 günde $activeDays aktif gün ve %$consistencyPercent romantik tutarlılık sağladın."
            activeDays >= 1 -> "Son 30 günde $activeDays günde kayıt bıraktın. Her gün kısa bir aşk notu ekleyerek romantik serini büyütebilirsin."
            else -> "Henüz son 30 güne ait günlük aşk notun yok. İlk notunu yazarak 30 günlük duygu eğrini başlatabilirsin!"
        }

        return MoodInsights30DaySummary(
            days = days,
            consistencyPercent = consistencyPercent,
            activeDaysCount = activeDays,
            totalNotesLast30Days = totalNotes,
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            averageMoodScore = avgMoodScore,
            dominantEmotion = dominant,
            breakdowns = breakdowns,
            consistencyInsight = insight
        )
    }
}
