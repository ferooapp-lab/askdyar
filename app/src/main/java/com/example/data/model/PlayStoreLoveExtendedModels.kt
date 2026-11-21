package com.example.data.model

// =========================================================================
// 1. AŞK KAVANOZU ("Bana Neden Aşıksın?" Sebep Kartları)
// =========================================================================
object DefaultLoveJarReasons {
    val initialReasons = listOf(
        "Gülerken gözlerinin içinin parlamasına bayılıyorum.",
        "En zor anımda bile tek bir sarılmanla beni sakinleştirebiliyorsun.",
        "Sabahları uykulu sesinle 'günaydın' demen bütün günümü güzelleştiriyor.",
        "Benim en garip şakalarıma bile içtenlikle güldüğün için.",
        "Yanındayken kendimi dünyadaki en güvende insan gibi hissediyorum.",
        "Küçük detayları bile hatırlayıp beni şaşırtmana hayranım.",
        "Kalbinin merhameti ve insanlara olan nezaketin beni sana daha çok bağlıyor.",
        "Birlikte susarken bile sıkılmadığım tek insan sensin.",
        "Sesini duyduğumda kalp atışlarım hâlâ ilk günkü gibi hızlanıyor.",
        "Beni olduğum gibi kabul edip her halimle sevdiğin için.",
        "Birlikte hayal kurarken gözlerindeki heyecanı görmek çok güzel.",
        "Yorulduğumda omuzuma yaslanacak en sıcak yuva olduğun için.",
        "Kokun hafızama kazınmış en huzurlu melodi gibi.",
        "Bana her gün daha iyi bir insan olma ilhamı veriyorsun.",
        "Çünkü seninle geçen sıradan bir Salı akşamı bile bayram gibi geliyor."
    )
}

// =========================================================================
// 2. ÇİFTLER İÇİN DOĞRULUK MU CESARET Mİ? (3 Seviyeli)
// =========================================================================
enum class TruthDareLevel(val label: String, val emoji: String) {
    SWEET("Tatlı & Masum", "🌸"),
    DEEP("Derin & Duygusal", "💫"),
    PASSIONATE("Tutkulu & Eğlenceli", "🔥")
}

data class TruthDarePrompt(
    val isTruth: Boolean,
    val level: TruthDareLevel,
    val prompt: String
)

object CoupleTruthOrDareData {
    val prompts = listOf(
        // SWEET - TRUTH
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Benimle ilgili ilk dikkatini çeken şey neydi?"),
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Benden duyduğun ve hiç unutamadığın iltifat hangisi?"),
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Sence benim en tatlı ve komik alışkanlığım nedir?"),
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Birlikte geçirdiğimiz en komik an hangisiydi?"),
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Benim yerime gizlice yaptığın küçük bir iyilik oldu mu?"),
        TruthDarePrompt(true, TruthDareLevel.SWEET, "Telefonunda bana koyduğun en komik veya tatlı lakap ne?"),
        // SWEET - DARE
        TruthDarePrompt(false, TruthDareLevel.SWEET, "Şu an elimi öpüp bana 3 farklı kelimeyle iltifat et."),
        TruthDarePrompt(false, TruthDareLevel.SWEET, "20 saniye boyunca hiç gülmeden gözlerimin içine bak!"),
        TruthDarePrompt(false, TruthDareLevel.SWEET, "Bana bizim için uydurduğun 2 dizelik mini bir şarkı söyle."),
        TruthDarePrompt(false, TruthDareLevel.SWEET, "En sevdiğimiz fotoğrafımızı bul ve neden sevdiğini anlat."),
        TruthDarePrompt(false, TruthDareLevel.SWEET, "Bana 1 dakika boyunca sıcacık bir omuz masajı yap."),
        TruthDarePrompt(false, TruthDareLevel.SWEET, "Bir sonraki buluşmamızda yapacağımız tatlı bir sürprize söz ver."),

        // DEEP - TRUTH
        TruthDarePrompt(true, TruthDareLevel.DEEP, "Bana gerçekten aşık olduğunu ilk ne zaman anladın?"),
        TruthDarePrompt(true, TruthDareLevel.DEEP, "İlişkimizde senin için en kutsal ve vazgeçilmez değer hangisi?"),
        TruthDarePrompt(true, TruthDareLevel.DEEP, "5 yıl sonra bizim için kurduğun en büyük ortak hayal nedir?"),
        TruthDarePrompt(true, TruthDareLevel.DEEP, "Sana kendimi en çok sevilmiş hissettirdiğim an hangisiydi?"),
        TruthDarePrompt(true, TruthDareLevel.DEEP, "Benimle ilgili en çok gurur duyduğun özelliğim ne?"),
        TruthDarePrompt(true, TruthDareLevel.DEEP, "Zor bir gününde sadece benim nasıl bir desteğimi istersin?"),
        // DEEP - DARE
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Gözlerimi kapatıp ellerimi tut ve kalbinden geçen en samimi teşekkürü söyle."),
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Telefona 30 saniyelik bana özel gelecek hatırası ses kaydı/notu bırak."),
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Şu an bana 1 dakikalık kesintisiz, derin ve huzurlu bir sarılma hediye et."),
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Birlikte yaşlanınca yapmak istediğin ilk şeyi tüm detaylarıyla anlat."),
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Bana hiç söylemediğin ama duymamı istediğin derin bir sırrını paylaş."),
        TruthDarePrompt(false, TruthDareLevel.DEEP, "Aşk Zaman Kapsülü bölümüne benim için küçük bir gelecek notu yaz."),

        // PASSIONATE - TRUTH
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "Beni en çekici bulduğun an veya kıyafet hangisi?"),
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "Birlikte çıkmak istediğin en romantik ve baş başa tatil neresi?"),
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "Sana göre mükemmel bir romantik akşamın olmazsa olmaz 3 şeyi nedir?"),
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "İlk öpücüğümüzü veya ilk el ele tutuşmamızı tek kelimeyle nasıl anlatırsın?"),
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "Bana en çok ne zaman 'keşke zaman dursa' diye bakıyorsun?"),
        TruthDarePrompt(true, TruthDareLevel.PASSIONATE, "Kulağıma fısıldamamı en çok sevdiğin cümle hangisi?"),
        // PASSIONATE - DARE
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Kulağıma eğil ve bu gece için çok romantik bir iltifat fısılda."),
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Beni ellerimden tutup en romantik şarkımızda 30 saniye dansa kaldır."),
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Alnımdan, yanaklarımdan ve ellerimden sırasıyla öpüp seni seviyorum de."),
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Şu an bana WhatsApp'tan aşk dolu gizli bir randevu daveti gönder."),
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Gözlerim kapalıyken 15 saniye boyunca saçlarımı ve yüzümü okşa."),
        TruthDarePrompt(false, TruthDareLevel.PASSIONATE, "Bugün bana istediğin bir romantik Aşk Kuponu hakkı armağan et.")
    )

    fun drawPrompt(level: TruthDareLevel, isTruth: Boolean?): TruthDarePrompt {
        val pool = prompts.filter { it.level == level && (isTruth == null || it.isTruth == isTruth) }
        return if (pool.isNotEmpty()) pool.random() else prompts.random()
    }
}

// =========================================================================
// 3. AŞK ZAMAN KAPSÜLÜ (Geleceğe Kilitli Mektup)
// =========================================================================
data class LoveTimeCapsule(
    val id: String,
    val title: String,
    val message: String,
    val author: String,
    val createdAt: Long,
    val unlockAt: Long
) {
    fun isUnlocked(now: Long = System.currentTimeMillis()): Boolean = now >= unlockAt

    fun remainingText(now: Long = System.currentTimeMillis()): String {
        val diff = unlockAt - now
        if (diff <= 0) return "Kilidi Açıldı! 🔓"
        val minutes = (diff / (1000L * 60))
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> "$days gün kaldı 🔒"
            hours > 0 -> "$hours saat kaldı 🔒"
            else -> "${minutes.coerceAtLeast(1)} dk kaldı 🔒"
        }
    }
}

// =========================================================================
// 4. BİRBİRİMİZİ NE KADAR TANIYORUZ? (2 Kişilik Çift Quizi)
// =========================================================================
data class CoupleTriviaQuestion(
    val id: Int,
    val question: String,
    val options: List<String>
)

object CoupleTriviaData {
    val questions = listOf(
        CoupleTriviaQuestion(
            1,
            "İdeal bir hafta sonu akşamında partnerin hangisini seçerdi?",
            listOf("Evde film & kahve", "Dışarıda şık akşam yemeği", "Sahilde uzun yürüyüş", "Arkadaşlarla eğlence")
        ),
        CoupleTriviaQuestion(
            2,
            "Partnerinin en çok mutlu olduğu sürpriz türü hangisidir?",
            listOf("El yazısı aşk mektubu", "Sürpriz küçük hediye", "Baş başa kısa tatil", "Sevdiği yemeği yapmak")
        ),
        CoupleTriviaQuestion(
            3,
            "Partnerin stresli olduğunda en çok neye ihtiyaç duyar?",
            listOf("Sıkıca sarılmaya", "Sessizce dinlenilmeye", "Tatlı/kahve molasına", "Komik bir sohbete")
        ),
        CoupleTriviaQuestion(
            4,
            "Sizin ilişkinizi en iyi tanımlayan mevsim hangisi olurdu?",
            listOf("Çiçek açan İlkbahar", "Sıcak ve enerjik Yaz", "Romantik Sonbahar", "Şömine başı Kış")
        ),
        CoupleTriviaQuestion(
            5,
            "Birlikte çıkılacak hayal tatilinde ilk tercih ne olur?",
            listOf("Deniz & güneş koyları", "Tarihi Avrupa sokakları", "Doğa & dağ evi", "Karlı kayak oteli")
        ),
        CoupleTriviaQuestion(
            6,
            "Partnerin sabah uyandığında ilk neyi görmek ister?",
            listOf("Tatlı bir günaydın mesajı", "Taze demlenmiş kahve", "Gülümseyen bir öpücük", "Hazırlanmış güzel kahvaltı")
        )
    )
}

// =========================================================================
// 5. AKILLI HEDİYE & SÜRPRİZ BULUCU
// =========================================================================
enum class GiftBudget(val label: String) {
    FREE("Ücretsiz 💝"),
    AFFORDABLE("Ekonomik ☕"),
    SPECIAL("Özel 💎")
}

enum class GiftSetting(val label: String) {
    HOME("Evde 🏡"),
    OUTDOOR("Dışarıda 🌆"),
    LONG_DISTANCE("Uzak Mesafe 📱")
}

data class GiftSurpriseIdea(
    val title: String,
    val emoji: String,
    val budget: GiftBudget,
    val setting: GiftSetting,
    val description: String,
    val stepTip: String
)

object GiftSurpriseFinderData {
    val ideas = listOf(
        GiftSurpriseIdea(
            "365 Neden Aşk Kavanozu",
            "🫙",
            GiftBudget.FREE,
            GiftSetting.HOME,
            "Küçük kağıtlara onu sevmenin sebeplerini, ortak şakalarınızı ve güzel anılarınızı yazıp bir kavanozda topla.",
            "İlk kağıda: 'Bu kavanozdaki her söz kalbimin bir parçası' yaz."
        ),
        GiftSurpriseIdea(
            "Yıldızlar Altında Balkon / Salon Pikniği",
            "🕯️",
            GiftBudget.FREE,
            GiftSetting.HOME,
            "Yere yumuşak minderler ser, ışıkları kapat, mumları yak ve sevdiğiniz şarkı listesini aç.",
            "Telefonları başka odaya bırakıp 1 saat sadece sohbet edin."
        ),
        GiftSurpriseIdea(
            "Gün Doğumu veya Gün Batımı Yürüyüşü",
            "🌅",
            GiftBudget.FREE,
            GiftSetting.OUTDOOR,
            "Termosa evde yaptığın sıcak kahve veya çayı koy, manzaralı bir tepede ya da sahilde sürpriz mola ver.",
            "Cebine onun için yazdığın mini bir not gizle."
        ),
        GiftSurpriseIdea(
            "Sürpriz Sesli Mektup & Dijital Kart",
            "🎙️",
            GiftBudget.FREE,
            GiftSetting.LONG_DISTANCE,
            "Uygulamadaki Notlarım bölümünden ona 1 dakikalık sesli aşk mektubu kaydet ve Kart Stüdyosu'ndan özel görsel gönder.",
            "Gece tam 00:00'da veya sabah uyanmadan önce ilet."
        ),
        GiftSurpriseIdea(
            "Kişisel Fotoğraf & Anı Kitapçığı",
            "📸",
            GiftBudget.AFFORDABLE,
            GiftSetting.HOME,
            "En sevdiğiniz 10 fotoğrafı bastırıp arkalarına o gün hissettiklerini el yazınla ekle.",
            "Son sayfayı gelecekte çekeceğiniz fotoğraf için boş bırak."
        ),
        GiftSurpriseIdea(
            "Favori Kahve + Çiçek Buluşması",
            "💐",
            GiftBudget.AFFORDABLE,
            GiftSetting.OUTDOOR,
            "Hiç beklemediği bir iş/okul çıkışında elinde en sevdiği tek bir çiçek ve favori içeceğiyle onu karşıla.",
            "Karton bardağın üzerine küçük bir kalp çiz."
        ),
        GiftSurpriseIdea(
            "Sürpriz Online Sipariş & Mesaj",
            "🍰",
            GiftBudget.AFFORDABLE,
            GiftSetting.LONG_DISTANCE,
            "Uzakta bile olsan, tam stresli bir anında sevdiği tatlıyı veya kahveyi adresine sürpriz gönder.",
            "Sipariş notuna uygulamadan seçtiğin romantik bir sözü eklet."
        ),
        GiftSurpriseIdea(
            "Butik Spa veya Baş Başa Akşam Yemeği",
            "🥂",
            GiftBudget.SPECIAL,
            GiftSetting.OUTDOOR,
            "Özel bir masa ayırt, şık giyinin ve telefonlardan uzak yıldönümü tadında bir akşam planla.",
            "Masanın üzerine önceden küçük bir hediye kutusu bırak."
        ),
        GiftSurpriseIdea(
            "Özel İsim İşlemeli Hatıra & Hediye Kutusu",
            "🎁",
            GiftBudget.SPECIAL,
            GiftSetting.HOME,
            "İkinizin baş harflerini taşıyan bir bileklik/kolye ile kokulu mum ve mektubunu şık bir kutuda sun.",
            "Kutu kapağının içine ikinizin tarihini yaz."
        ),
        GiftSurpriseIdea(
            "Sürpriz Hafta Sonu Bileti",
            "✈️",
            GiftBudget.SPECIAL,
            GiftSetting.LONG_DISTANCE,
            "Uzun süredir kavuşamıyorsanız, haber vermeden sürpriz buluşma bileti alıp geri sayım başlat!",
            "Aşk Alanı'ndaki Özel Günler Geri Sayımı'na tarihi hemen ekle."
        )
    )

    fun findMatches(budget: GiftBudget?, setting: GiftSetting?): List<GiftSurpriseIdea> {
        return ideas.filter {
            (budget == null || it.budget == budget) &&
                (setting == null || it.setting == setting)
        }.ifEmpty { ideas }
    }
}

// =========================================================================
// 6. İLİŞKİ KİLOMETRE TAŞLARI ZAMAN TÜNELİ
// =========================================================================
data class RelationshipMilestone(
    val title: String,
    val emoji: String,
    val targetDays: Long,
    val isReached: Boolean,
    val daysDelta: Long,
    val progress: Float
)

object MilestonesTimelineCalculator {
    private val milestonesSpec = listOf(
        Triple("İlk Hafta Kıvılcımı", "✨", 7L),
        Triple("İlk Ay Kutlaması", "🌙", 30L),
        Triple("100. Gün Yıldızı", "💯", 100L),
        Triple("6. Ay Yarım Yıl", "🌹", 180L),
        Triple("1. Yıl Büyük Kutlama", "🎂", 365L),
        Triple("500. Gün Altın Bağ", "🏆", 500L),
        Triple("2. Yıl Ömürlük Adım", "💍", 730L),
        Triple("1000. Gün Efsanesi", "👑", 1000L)
    )

    fun compute(currentDays: Long): List<RelationshipMilestone> {
        val safeDays = currentDays.coerceAtLeast(0L)
        return milestonesSpec.map { (title, emoji, target) ->
            val reached = safeDays >= target
            val progress = (safeDays.toFloat() / target.toFloat()).coerceIn(0f, 1f)
            val delta = if (reached) safeDays - target else target - safeDays
            RelationshipMilestone(
                title = title,
                emoji = emoji,
                targetDays = target,
                isReached = reached,
                daysDelta = delta,
                progress = progress
            )
        }
    }
}
