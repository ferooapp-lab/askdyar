package com.example.data.model

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class JournalMood(
    val id: String,
    val label: String,
    val emoji: String,
    val toneHint: String
) {
    ROMANTIC("romantic", "Aşık", "💘", "Kalbim seninle çarpıyor"),
    PEACEFUL("peaceful", "Huzurlu", "🕊️", "Yanında dünya çok sakin"),
    GRATEFUL("grateful", "Minnettar", "✨", "İyi ki hayatımdasın"),
    NOSTALGIC("nostalgic", "Özlem Dolu", "🌙", "Birlikte geçirdiğimiz her anı özlüyorum"),
    EXCITED("excited", "Heyecanlı", "🦋", "İlk günkü heyecan hiç bitmiyor"),
    MELANCHOLIC("melancholic", "Hüzünlü", "🌧️", "Sensiz geçen zaman biraz ağır");

    companion object {
        fun fromId(raw: String?): JournalMood {
            return values().firstOrNull { it.id == raw } ?: ROMANTIC
        }
    }
}

enum class JournalDateScope(val id: String, val label: String) {
    ALL("all", "Tüm Tarihler"),
    SELECTED_DAY("selected_day", "Seçili Gün"),
    THIS_MONTH("this_month", "Bu Ay"),
    FAVORITES("favorites", "Özel Anılar")
}

data class EmotionalJournalEntry(
    val id: Long = 0L,
    val docId: String = "",
    val title: String = "",
    val content: String = "",
    val moodId: String = JournalMood.ROMANTIC.id,
    val dateKey: String = JournalDateHelper.toDateKey(System.currentTimeMillis()),
    val memoryTimestamp: Long = JournalDateHelper.normalizeDateMillis(System.currentTimeMillis()),
    val locationOrMoment: String = "",
    val isFavorite: Boolean = false,
    val isPrivateLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val mood: JournalMood get() = JournalMood.fromId(moodId)
}

data class JournalCalendarDay(
    val dateKey: String,
    val dayNumber: String,
    val shortMonth: String,
    val shortWeekday: String,
    val isToday: Boolean,
    val entriesCount: Int
)

data class JournalDateSection(
    val dateKey: String,
    val formattedHeader: String,
    val entries: List<EmotionalJournalEntry>
)

object JournalDateHelper {

    private val trLocale = Locale("tr", "TR")

    fun toDateKey(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(millis))
    }

    fun normalizeDateMillis(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis.coerceAtLeast(1L)
    }

    fun parseDateKeyToNoonMillis(dateKey: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val parsed = sdf.parse(dateKey)
            if (parsed != null) normalizeDateMillis(parsed.time) else normalizeDateMillis(System.currentTimeMillis())
        } catch (_: Exception) {
            normalizeDateMillis(System.currentTimeMillis())
        }
    }

    fun currentMonthPrefix(nowMillis: Long = System.currentTimeMillis()): String {
        return toDateKey(nowMillis).take(7) // yyyy-MM
    }

    fun formatFullTurkishDate(dateKey: String, nowMillis: Long = System.currentTimeMillis()): String {
        val todayKey = toDateKey(nowMillis)
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayKey = toDateKey(yesterdayCal.timeInMillis)
        val millis = parseDateKeyToNoonMillis(dateKey)
        val formatted = SimpleDateFormat("d MMMM yyyy • EEEE", trLocale).format(Date(millis))
        return when (dateKey) {
            todayKey -> "Bugün • $formatted"
            yesterdayKey -> "Dün • $formatted"
            else -> formatted
        }
    }

    fun buildCalendarRibbonDays(
        entries: List<EmotionalJournalEntry>,
        daysBack: Int = 20,
        nowMillis: Long = System.currentTimeMillis()
    ): List<JournalCalendarDay> {
        val todayKey = toDateKey(nowMillis)
        val countsByDate = entries.groupingBy { it.dateKey }.eachCount()
        val keySet = linkedSetOf<String>()

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        for (i in 0..daysBack) {
            keySet.add(toDateKey(cal.timeInMillis))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        entries.forEach { entry ->
            if (entry.dateKey.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                keySet.add(entry.dateKey)
            }
        }

        val dayFmt = SimpleDateFormat("d", trLocale)
        val monthFmt = SimpleDateFormat("MMM", trLocale)
        val weekFmt = SimpleDateFormat("EEE", trLocale)

        return keySet
            .sortedDescending()
            .map { key ->
                val date = Date(parseDateKeyToNoonMillis(key))
                JournalCalendarDay(
                    dateKey = key,
                    dayNumber = dayFmt.format(date),
                    shortMonth = monthFmt.format(date).replace(".", "").take(3),
                    shortWeekday = weekFmt.format(date).replace(".", "").take(3),
                    isToday = (key == todayKey),
                    entriesCount = countsByDate[key] ?: 0
                )
            }
    }

    fun groupEntriesByDate(
        entries: List<EmotionalJournalEntry>,
        nowMillis: Long = System.currentTimeMillis()
    ): List<JournalDateSection> {
        return entries
            .groupBy { it.dateKey }
            .entries
            .sortedByDescending { it.key }
            .map { (dateKey, dayItems) ->
                JournalDateSection(
                    dateKey = dateKey,
                    formattedHeader = formatFullTurkishDate(dateKey, nowMillis),
                    entries = dayItems.sortedWith(
                        compareByDescending<EmotionalJournalEntry> { it.memoryTimestamp }
                            .thenByDescending { it.updatedAt }
                    )
                )
            }
    }

    fun filterEntries(
        entries: List<EmotionalJournalEntry>,
        scope: JournalDateScope,
        selectedDateKey: String?,
        selectedMood: JournalMood?,
        searchQuery: String,
        nowMillis: Long = System.currentTimeMillis()
    ): List<EmotionalJournalEntry> {
        val monthPrefix = currentMonthPrefix(nowMillis)
        val cleanQuery = searchQuery.trim()
        return entries.filter { entry ->
            val matchesScope = when (scope) {
                JournalDateScope.ALL -> true
                JournalDateScope.SELECTED_DAY -> selectedDateKey == null || entry.dateKey == selectedDateKey
                JournalDateScope.THIS_MONTH -> entry.dateKey.startsWith(monthPrefix)
                JournalDateScope.FAVORITES -> entry.isFavorite
            }
            val matchesMood = selectedMood == null || entry.moodId == selectedMood.id
            val matchesSearch = cleanQuery.isBlank() ||
                entry.title.contains(cleanQuery, ignoreCase = true) ||
                entry.content.contains(cleanQuery, ignoreCase = true) ||
                entry.locationOrMoment.contains(cleanQuery, ignoreCase = true) ||
                entry.mood.label.contains(cleanQuery, ignoreCase = true)
            matchesScope && matchesMood && matchesSearch
        }
    }
}

object JournalSecurityHelper {
    fun isValidPinFormat(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }

    fun hashPin(uid: String, pin: String): String {
        val raw = "aska_dair_journal_vault_v1:${uid.trim()}:${pin.trim()}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(uid: String, candidatePin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank() || !isValidPinFormat(candidatePin)) return false
        val expected = hashPin(uid, candidatePin)
        if (expected.length != storedHash.length) return false
        var diff = 0
        for (i in expected.indices) {
            diff = diff or (expected[i].code xor storedHash[i].code)
        }
        return diff == 0
    }
}
