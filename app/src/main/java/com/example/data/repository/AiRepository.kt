package com.example.data.repository

import android.util.Log
import com.example.data.local.NoteEntity
import com.example.data.model.AiQuoteResponse
import com.example.data.model.DayMood
import com.example.data.model.MoodAnalysisResult
import com.example.data.model.MoodBreakdown
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val TAG = "AiRepository"

interface AiRepository {
    suspend fun generateQuote(theme: String): Result<AiQuoteResponse>
    suspend fun generateAcrosticPoem(partnerName: String): Result<String>
    suspend fun generateSituationMessage(situation: String): Result<AiQuoteResponse>
    suspend fun analyzeMoodTrends(notes: List<NoteEntity>): Result<MoodAnalysisResult>
    fun getBaselineMoodAnalysis(notes: List<NoteEntity>): MoodAnalysisResult
}

class AiRepositoryImpl : AiRepository {

    // 1. Structured JSON schema for AI quote generation
    private val quoteSchema = Schema.obj(
        properties = mapOf(
            "quote" to Schema.string(description = "Yapay zeka tarafından üretilen romantik ve etkileyici Türkçe aşk sözü"),
            "author" to Schema.string(description = "Söz için yazar veya tema imzası, örn: Gemini AI (Hüzünlü Aşk)")
        )
    )

    // 2. Structured JSON schema for 7-day mood analysis
    private val moodAnalysisSchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer(description = "Genel romantizm ve aşk uyum skoru (60-99 arası)"),
            "summaryInsight" to Schema.string(description = "Kullanıcının aşk notlarına ve duygu trendine dair Gemini analizi ve içgörüsü"),
            "advice" to Schema.string(description = "Çift için romantik, sıcak ve uygulanabilir ilişki tavsiyesi"),
            "passionPercentage" to Schema.integer(description = "Tutku ve aşk yüzdesi"),
            "peacePercentage" to Schema.integer(description = "Huzur ve şefkat yüzdesi"),
            "longingPercentage" to Schema.integer(description = "Özlem ve hasret yüzdesi"),
            "joyPercentage" to Schema.integer(description = "Sabah neşesi ve sevinç yüzdesi")
        )
    )

    // Lazy initialization of Firebase AI Logic models (gemini-3.8-flash)
    private val quoteModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quoteSchema
            }
        )
    }

    private val plainTextModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    private val moodModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = moodAnalysisSchema
            }
        )
    }

    override suspend fun generateQuote(theme: String): Result<AiQuoteResponse> {
        val cleanTheme = theme.trim().ifBlank { "romantik aşk" }
        return try {
            val prompt = "Sen derin duygulara sahip usta bir aşk şairisin. '$cleanTheme' temasına uygun, tamamen Türkçe, dokunaklı, samimi, klişelerden uzak ve etkileyici tek bir aşk sözü oluştur. Yanıtı JSON şemasına uygun ver."
            val response = withTimeout(15_000L) {
                quoteModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                val json = JSONObject(text)
                val quoteText = json.getString("quote")
                val author = json.optString("author", "Gemini AI (${cleanTheme.replaceFirstChar { it.uppercase() }})")
                Result.success(
                    AiQuoteResponse(
                        text = quoteText,
                        author = author,
                        theme = cleanTheme,
                        isSimulated = false
                    )
                )
            } else {
                fallbackQuote(cleanTheme)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Gemini quote generation timed out after 15s, using local fallback")
            fallbackQuote(cleanTheme)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini AI generation fallback activated: ${e.message}")
            fallbackQuote(cleanTheme)
        }
    }

    override suspend fun generateAcrosticPoem(partnerName: String): Result<String> {
        val cleanName = partnerName.trim()
        val upper = cleanName.uppercase(Locale("tr")).filter { it.isLetter() }
        if (upper.isBlank()) {
            return Result.failure(Exception("Lütfen harflerden oluşan geçerli bir isim girin"))
        }
        return try {
            val prompt = "'$upper' isminin baş harfleriyle (her satır sırasıyla bu harflerle başlayacak şekilde) sevgiliye yazılmış romantik, şiirsel, akıcı bir Türkçe akrostiş aşk şiiri yaz. Sadece şiir dizelerini döndür."
            val response = withTimeout(25_000L) {
                plainTextModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success(com.example.data.model.AcrosticPoemBuilder.buildFallbackAcrostic(cleanName))
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Acrostic poem generation timed out after 25s, using local fallback")
            Result.success(com.example.data.model.AcrosticPoemBuilder.buildFallbackAcrostic(cleanName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Acrostic fallback activated: ${e.message}")
            Result.success(com.example.data.model.AcrosticPoemBuilder.buildFallbackAcrostic(cleanName))
        }
    }

    override suspend fun generateSituationMessage(situation: String): Result<AiQuoteResponse> {
        val cleanSit = situation.trim().ifBlank { "barışma ve sevgi" }
        return try {
            val prompt = "Bir ilişki koçu ve romantik şair olarak, '$cleanSit' durumu için sevgiliye gönderilebilecek çok samimi, içten, doğal ve etkileyici tek bir Türkçe mesaj yaz. Yanıtı JSON şemasına uygun ver."
            val response = withTimeout(25_000L) {
                quoteModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                val json = JSONObject(text)
                val msg = json.getString("quote")
                val author = json.optString("author", "AI İlişki Koçu")
                Result.success(
                    AiQuoteResponse(
                        text = msg,
                        author = author,
                        theme = cleanSit,
                        isSimulated = false
                    )
                )
            } else {
                fallbackSituationMessage(cleanSit)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Situation message generation timed out after 25s, using local fallback")
            fallbackSituationMessage(cleanSit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Situation message fallback activated: ${e.message}")
            fallbackSituationMessage(cleanSit)
        }
    }

    override suspend fun analyzeMoodTrends(notes: List<NoteEntity>): Result<MoodAnalysisResult> {
        val daysList = buildSevenDaysList(notes)
        val notesSummary = if (notes.isNotEmpty()) {
            notes.take(8).joinToString("; ") { n ->
                "Başlık: ${n.title}, İçerik: ${n.content.take(80)}, Etiketler: ${n.tags}"
            }
        } else {
            "Kullanıcı henüz özel not kaydetmedi. Genel aşk ve romantizm trendi değerlendirilecek."
        }

        return try {
            val prompt = "Kullanıcının aşk notları ve son günlerdeki duygu kayıtları: [$notesSummary]. Bu verileri inceleyerek çiftin aşk endeksi skorunu (60-99), duygu dağılım yüzdelerini (tutku, huzur, özlem, neşe toplamı 100 olacak şekilde), kapsamlı bir Gemini içgörüsünü ve haftalık romantik tavsiyeyi JSON olarak üret."
            val response = withTimeout(12_000L) {
                moodModel.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                val json = JSONObject(text)
                val score = json.optInt("overallScore", (daysList.map { it.moodScore }.average()).toInt()).coerceIn(60, 99)
                val insight = json.optString("summaryInsight", "İlişkinizde yüksek bağlılık, içten bir huzur ve romantik çekim hakim.")
                val advice = json.optString("advice", "Bu hafta sonu baş başa sakin bir akşam yürüyüşü yapıp birbirinize en mutlu anınızı anlatın.")

                val passion = json.optInt("passionPercentage", 42)
                val peace = json.optInt("peacePercentage", 30)
                val longing = json.optInt("longingPercentage", 18)
                val joy = json.optInt("joyPercentage", 10)

                val breakdowns = listOf(
                    MoodBreakdown("Tutku & Aşk", passion, "🔥"),
                    MoodBreakdown("Huzur & Şefkat", peace, "🕊️"),
                    MoodBreakdown("Özlem & Hasret", longing, "💌"),
                    MoodBreakdown("Sabah Neşesi", joy, "☀️")
                )

                Result.success(
                    MoodAnalysisResult(
                        days = daysList,
                        breakdowns = breakdowns,
                        overallScore = score,
                        summaryInsight = insight,
                        advice = advice,
                        isSimulated = false
                    )
                )
            } else {
                fallbackMoodAnalysis(notes, daysList)
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Mood analysis timed out after 12s, applying local analytical fallback")
            fallbackMoodAnalysis(notes, daysList)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Mood analysis using fallback: ${e.message}")
            fallbackMoodAnalysis(notes, daysList)
        }
    }

    override fun getBaselineMoodAnalysis(notes: List<NoteEntity>): MoodAnalysisResult {
        val daysList = buildSevenDaysList(notes)
        return fallbackMoodAnalysis(notes, daysList).getOrNull() ?: MoodAnalysisResult(
            days = daysList,
            breakdowns = listOf(
                MoodBreakdown("Tutku & Aşk", 42, "🔥"),
                MoodBreakdown("Huzur & Şefkat", 30, "🕊️"),
                MoodBreakdown("Özlem & Hasret", 18, "💌"),
                MoodBreakdown("Sabah Neşesi", 10, "☀️")
            ),
            overallScore = 88,
            summaryInsight = "Haftalık duygu durumu projeksiyonuna göre kalbinizde sevgilinize karşı sıcak ve besleyici bir sevgi enerjisi hakim.",
            advice = "Bu hafta sonu baş başa sakin bir akşam yürüyüşü yapıp birbirinize bu haftanın en mutlu anını anlatmayı deneyin.",
            isSimulated = true
        )
    }

    private fun buildSevenDaysList(notes: List<NoteEntity>): List<DayMood> {
        val daysList = mutableListOf<DayMood>()
        val dayFormat = SimpleDateFormat("EEE", Locale("tr"))
        val dateFormat = SimpleDateFormat("dd MMM", Locale("tr"))
        val hasRecentNotes = notes.any { System.currentTimeMillis() - it.updatedAt < 7L * 24 * 60 * 60 * 1000 }

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dayName = dayFormat.format(cal.time)
            val dateLabel = dateFormat.format(cal.time)

            val baseScore = if (hasRecentNotes) 84 else 78
            val variation = when (i) {
                0 -> 94 // Today
                1 -> 90
                2 -> 86
                3 -> 88
                4 -> 82
                5 -> 85
                else -> 80
            }
            val score = (baseScore * 0.4 + variation * 0.6).toInt().coerceIn(60, 99)

            val emoji = when {
                score >= 92 -> "😍"
                score >= 88 -> "💖"
                score >= 84 -> "🥰"
                score >= 80 -> "✨"
                else -> "🌹"
            }

            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
            }.timeInMillis
            val dayEnd = dayStart + 24 * 60 * 60 * 1000
            val countOnDay = notes.count { it.updatedAt in dayStart..dayEnd }

            daysList.add(
                DayMood(
                    dayLabel = dayName.replaceFirstChar { it.uppercase() },
                    dateStr = dateLabel,
                    moodScore = score,
                    emoji = emoji,
                    noteCount = countOnDay
                )
            )
        }
        return daysList
    }

    private fun fallbackQuote(theme: String): Result<AiQuoteResponse> {
        val cleanTheme = theme.lowercase(Locale.getDefault())
        val (text, author) = when {
            cleanTheme.contains("sad") || cleanTheme.contains("hüzün") || cleanTheme.contains("kırık") || cleanTheme.contains("acı") -> {
                Pair(
                    "Sessizliğin bile adını fısıldadığı bu gecede, yokluğun kalbimin en derin yerinde sızlayan tatlı bir hatıra gibi.",
                    "Gemini AI (Hüzünlü Aşk)"
                )
            }
            cleanTheme.contains("morning") || cleanTheme.contains("sabah") || cleanTheme.contains("joy") || cleanTheme.contains("neşe") || cleanTheme.contains("günaydın") -> {
                Pair(
                    "Güneş bugün penceremden değil, senin kalbime düşen sıcacık tebessümünden doğdu. Günün neşeyle dolsun birtanem.",
                    "Gemini AI (Sabah Neşesi)"
                )
            }
            cleanTheme.contains("tutku") || cleanTheme.contains("passion") || cleanTheme.contains("ateş") || cleanTheme.contains("derin") -> {
                Pair(
                    "Seninle olmak bir fırtınanın ortasında en huzurlu limana sığınmak gibi; hem alev alev hem de sonsuz bir dinginlik.",
                    "Gemini AI (Tutkulu Aşk)"
                )
            }
            cleanTheme.contains("özlem") || cleanTheme.contains("mesafe") || cleanTheme.contains("hasret") -> {
                Pair(
                    "Aramızdaki kilometreler sadece toprağın mesafesidir sevgilim; kalbimin her ritmi doğrudan senin göğsünde yankılanıyor.",
                    "Gemini AI (Özlem & Hasret)"
                )
            }
            else -> {
                Pair(
                    "Dünyanın bütün karmaşası içinde senin gözlerine sığındım; sen benim en güzel gerçeğim, en tatlı mucizemsin.",
                    "Gemini AI (${theme.ifBlank { "Romantik" }})"
                )
            }
        }
        return Result.success(
            AiQuoteResponse(
                text = text,
                author = author,
                theme = theme.ifBlank { "Romantik" },
                isSimulated = true
            )
        )
    }

    private fun fallbackSituationMessage(situation: String): Result<AiQuoteResponse> {
        val clean = situation.lowercase(Locale("tr"))
        val candidates = when {
            clean.contains("barış") || clean.contains("özür") || clean.contains("kırgın") -> listOf(
                "Bazen kelimeler yanlış çıksa da kalbimde sana karşı hep en saf sevgi var. Kırgınlığımızı bir sarılmayla geride bırakalım mı? Seni çok seviyorum.",
                "Seni üzmeyi hiçbir zaman istemedim birtanem; haklı olmaktan çok seninle huzurlu ve mutlu olmak istiyorum. Gel barışalım."
            )
            clean.contains("buluşma") || clean.contains("heyecan") -> listOf(
                "Seni göreceğim saati iple çekiyorum; kalbim şimdiden ilk günkü gibi heyecanla çarpıyor. Birazdan görüşmek üzere!",
                "Bugün seninle buluşacak olmanın verdiği tatlı telaş içimi sıcacık yapıyor. Yanına gelmek için sabırsızlanıyorum."
            )
            clean.contains("mesafe") || clean.contains("özlem") || clean.contains("uzak") -> listOf(
                "Aramızda kilometreler olsa da başımı gökyüzüne kaldırdığımda aynı aya baktığımızı bilmek içimi ısıtıyor. Çok özledim seni.",
                "Şu an yanında olup elini tutmayı her şeyden çok isterdim; kavuşacağımız güne kadar kalbim hep senin yanında atıyor."
            )
            clean.contains("stres") || clean.contains("iş") || clean.contains("yorgun") -> listOf(
                "Bugünün ne kadar yorucu geçtiğini biliyorum sevgilim; derin bir nefes al, ben her zaman senin yanındayım. Akşam biraz dinlenip baş başa dertleşelim.",
                "Zor bir günün ardından omuzlarındaki bütün yükü biraz olsun hafifletebilmek isterdim. Harika iş çıkarıyorsun, seninle gurur duyuyorum."
            )
            clean.contains("yıldönümü") || clean.contains("kutlama") -> listOf(
                "El ele yürüdüğümüz bu güzel yolculukta bana kattığın her anı, her gülüşü kalbimde hazine gibi saklıyorum. Birlikte nice güzel yıllarımıza!",
                "Hayatımın en doğru ve en mutlu kararı senin elini tutmaktı. Her geçen gün sevgimiz daha da büyüyor, yıldönümümüz kutlu olsun!"
            )
            clean.contains("iltifat") || clean.contains("sürpriz") -> listOf(
                "Durup dururken aklıma düştün ve şunu söylemek istedim: Gülüşün bu dünyadaki en güzel manzara, iyi ki hayatımdasın.",
                "Bugün de her zamanki gibi kalbimin en güzel yerindesin. Varlığın bana her gün yeniden umut ve mutluluk veriyor."
            )
            else -> listOf(
                "Şu an içinde bulunduğumuz bu anda bile kalbimin en sakin ve en sıcak limanı sensin. Seni her halinle çok seviyorum.",
                "Her ne olursa olsun omuz omuza olduğumuz sürece aşamayacağımız hiçbir şey yok sevgilim. İyi ki yanımdasın."
            )
        }
        return Result.success(
            AiQuoteResponse(
                text = candidates.random(),
                author = "AI İlişki Koçu ($situation)",
                theme = situation,
                isSimulated = true
            )
        )
    }

    private fun fallbackMoodAnalysis(notes: List<NoteEntity>, daysList: List<DayMood>): Result<MoodAnalysisResult> {
        val avgScore = (daysList.map { it.moodScore }.average()).toInt()
        val breakdowns = listOf(
            MoodBreakdown("Tutku & Aşk", 42, "🔥"),
            MoodBreakdown("Huzur & Şefkat", 30, "🕊️"),
            MoodBreakdown("Özlem & Hasret", 18, "💌"),
            MoodBreakdown("Sabah Neşesi", 10, "☀️")
        )
        val summary = if (notes.isNotEmpty()) {
            "Son 7 günde eklenen aşk notları incelendiğinde; ilişkinizde yüksek bağlılık, içten bir huzur ve romantik çekimin zirvede olduğu görülüyor."
        } else {
            "Haftalık duygu durumu projeksiyonuna göre kalbinizde sevgilinize karşı sıcak ve besleyici bir sevgi enerjisi hakim."
        }
        val advice = "Bu hafta sonu baş başa sakin bir akşam yürüyüşü yapıp birbirinize bu haftanın en mutlu anını anlatmayı deneyin."

        return Result.success(
            MoodAnalysisResult(
                days = daysList,
                breakdowns = breakdowns,
                overallScore = avgScore,
                summaryInsight = summary,
                advice = advice,
                isSimulated = true
            )
        )
    }
}
