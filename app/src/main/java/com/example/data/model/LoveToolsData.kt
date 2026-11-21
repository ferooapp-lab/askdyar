package com.example.data.model

data class LoveCounterInfo(
    val myName: String = "Ben",
    val partnerName: String = "Sevgilim",
    val startTimestamp: Long = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120), // default 120 days ago
    val celebrationTitle: String = "İlk Günümüzden Beri"
) {
    fun calculateDays(): Long {
        val diff = System.currentTimeMillis() - startTimestamp
        return if (diff > 0) diff / (1000 * 60 * 60 * 24) else 0
    }

    fun calculateHours(): Long {
        val diff = System.currentTimeMillis() - startTimestamp
        return if (diff > 0) (diff / (1000 * 60 * 60)) % 24 else 0
    }

    fun calculateMinutes(): Long {
        val diff = System.currentTimeMillis() - startTimestamp
        return if (diff > 0) (diff / (1000 * 60)) % 60 else 0
    }

    fun calculateNextMilestone(): Pair<String, Long> {
        val currentDays = calculateDays()
        val milestones = listOf(
            50L to "50. Sevgi Günü",
            100L to "100. Aşk Günü",
            200L to "200. Birliktelik Günü",
            365L to "1. Yıldönümü 💍",
            500L to "500. Altın Gün",
            730L to "2. Yıldönümü 🥂",
            1000L to "1000. Büyük Sevda Günü 💖"
        )
        for ((days, label) in milestones) {
            if (currentDays < days) {
                return Pair(label, days - currentDays)
            }
        }
        val nextYear = ((currentDays / 365) + 1) * 365
        return Pair("${nextYear / 365}. Yıldönümü", nextYear - currentDays)
    }
}

data class RomanticMission(
    val id: Int,
    val title: String,
    val description: String,
    val tip: String,
    val emoji: String
)

object RomanticMissions {
    val missions = listOf(
        RomanticMission(
            id = 1,
            title = "Sebepsiz Bir Şarkı Hediye Et",
            description = "Ona 'Bu şarkıyı dinlerken aklıma sen geldin' diyerek en sevdiğiniz romantik şarkının linkini gönder.",
            tip = "Müzik, kalbin en kestirme yoludur 🎶",
            emoji = "🎵"
        ),
        RomanticMission(
            id = 2,
            title = "Gözlerinin İçine Bakarak Teşekkür Et",
            description = "Bugün ona sarıl ve sadece 'Hayatımda olduğun için çok teşekkür ederim' de.",
            tip = "İçten bir teşekkür, paha biçilemez bir hediyedir ✨",
            emoji = "💖"
        ),
        RomanticMission(
            id = 3,
            title = "Geçmişten Bir Hatırayı Canlandır",
            description = "Birlikte ilk çekildiğiniz fotoğrafı ya da ilk mesajlaştığınız günü hatırlatan tatlı bir not paylaş.",
            tip = "İlk günkü heyecanı tazelemek kalbi ısıtır 📸",
            emoji = "💌"
        ),
        RomanticMission(
            id = 4,
            title = "Sıcak Bir İçecek Sürprizi",
            description = "Ona sevdiği kahveyi, çayı veya çikolatayı hazırla ya da küçük bir tatlı hediye et.",
            tip = "Küçük sürprizler büyük sevgilerin kanıtıdır ☕",
            emoji = "☕"
        ),
        RomanticMission(
            id = 5,
            title = "En Çok Sevdiğin 3 Özelliğini Yaz",
            description = "Onda en çok hayran olduğun, aşık olduğun 3 özelliği bir mesaja dök ve gönder.",
            tip = "Fark edilmek ve takdir edilmek aşkı güçlendirir 🌹",
            emoji = "🌹"
        ),
        RomanticMission(
            id = 6,
            title = "Günün Fotoğrafını Gönder",
            description = "Günün ortasında ona özel sıcacık bir gülümseme selfiesi gönderip 'Seni özledim' de.",
            tip = "Göz teması ve tebessüm mesafeleri eritir ✨",
            emoji = "🥰"
        ),
        RomanticMission(
            id = 7,
            title = "Birlikte Hayal Kurun",
            description = "Gelecekte birlikte gitmek istediğiniz bir tatil yerini veya birlikte yapacağınız bir hayali konuşun.",
            tip = "Ortak hayaller, ortak geleceğin tohumlarıdır 🏖️",
            emoji = "✈️"
        )
    )
}

data class CompatibilityResult(
    val name1: String,
    val name2: String,
    val scorePercentage: Int,
    val statusTitle: String,
    val interpretation: String,
    val advice: String
)

object CompatibilityCalculator {
    fun calculate(name1: String, name2: String): CompatibilityResult {
        val clean1 = name1.trim().lowercase()
        val clean2 = name2.trim().lowercase()
        if (clean1.isBlank() || clean2.isBlank()) {
            return CompatibilityResult(
                name1 = name1,
                name2 = name2,
                scorePercentage = 100,
                statusTitle = "Sonsuz Aşk",
                interpretation = "İsimleriniz yan yana bile bir şiir gibi duruyor.",
                advice = "Birbirinize her gün sevginizi fısıldamayı unutmayın."
            )
        }

        // Deterministic pseudo-algorithm based on character values
        val combinedHash = (clean1 + clean2).fold(0) { acc, c -> acc * 31 + c.code }
        val score = 80 + (kotlin.math.abs(combinedHash) % 20) // Always high & encouraging (80% - 99%)

        val (title, interp, advice) = when {
            score >= 95 -> Triple(
                "Ruh İkizi & Sonsuz Aşk 💖",
                "Yıldızlarınız ve kalpleriniz kusursuz bir ahenkle çarpıyor. Birbirinizi tamamlayan nadide iki parçasınız.",
                "Bu eşsiz bağı korumak için birlikte bol bol yeni hatıralar biriktirin."
            )
            score >= 90 -> Triple(
                "Tutkulu & Derin Bağ ✨",
                "Aranızdaki çekim kuvveti ve romantizm çok güçlü. Birbirinizin varlığında huzur ve heyecan bir arada.",
                "Küçük sürprizlerle birbirinizi şımartmaya devam edin."
            )
            score >= 85 -> Triple(
                "Şiirsel Sevda & Sadakat 🌹",
                "Saygı, sevgi ve zarafetle örülmüş sağlam bir ilişki potansiyeli. Birbirinizin en kıymetli limanısınız.",
                "Duygularınızı daha sık kelimelere dökerek aşkınızı tazeleyin."
            )
            else -> Triple(
                "Ahenkli & Tatlı Romantizm 💌",
                "Birbirinize kattığınız neşe ve renk hayatı güzelleştiriyor. Birlikteyken zamanın nasıl geçtiği anlaşılmıyor.",
                "Birbirinizi dinlemek ve sarılmak ilişkinizin gizli tılsımı."
            )
        }

        return CompatibilityResult(
            name1 = name1.trim().replaceFirstChar { it.uppercase() },
            name2 = name2.trim().replaceFirstChar { it.uppercase() },
            scorePercentage = score,
            statusTitle = title,
            interpretation = interp,
            advice = advice
        )
    }
}

// 4. ROMANTİK AŞK KUPONLARI
data class LoveCoupon(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isRedeemed: Boolean = false
)

object DefaultLoveCoupons {
    val list = listOf(
        LoveCoupon("c_1", "Sırt & Omuz Masajı", "15 dakikalık huzurlu ve rahatlatıcı masaj garantisi.", "💆"),
        LoveCoupon("c_2", "Baş Başa Akşam Yemeği", "Mekanı ve tüm menüyü sevgilinin belirlediği özel gece.", "🍝"),
        LoveCoupon("c_3", "Tartışmada Haklı Çıkma", "Ufak anlaşmazlıklarda 'Sen haklısın sevgilim' joker kartı.", "⚖️"),
        LoveCoupon("c_4", "Yatağa Sürpriz Kahvaltı", "Taze çay/kahve ile yatakta şımartılma seansı.", "☕"),
        LoveCoupon("c_5", "Sınırsız Sarılma & Şefkat", "Günün her anında geçerli 10 dakikalık kesintisiz sarılma.", "🤗"),
        LoveCoupon("c_6", "Film Gecesi & Mısır", "Filmi sevgilinin seçtiği, patlamış mısırlı sinema gecesi.", "🎬"),
        LoveCoupon("c_7", "Gece Yıldız Yürüyüşü", "El ele sakin bir gece yürüyüşü ve derin aşk sohbeti.", "🌙"),
        LoveCoupon("c_8", "Özel İtiraf & Şiir", "Gözlerinin içine bakılarak sana özel okunacak mısralar.", "💌")
    )
}

// 5. ÇİFT BUCKET LIST (BİRLİKTE YAPILACAKLAR LİSTESİ)
data class CoupleBucketItem(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val isDone: Boolean = false
)

object DefaultCoupleBucketList {
    val list = listOf(
        CoupleBucketItem("b_1", "Birlikte gündoğumunu izlemek", "Romantizm", "🌅"),
        CoupleBucketItem("b_2", "Yağmur altında ıslanıp dans etmek", "Çılgınlık", "🌧️"),
        CoupleBucketItem("b_3", "Birlikte pasta veya kurabiye pişirmek", "Mutfak", "🍰"),
        CoupleBucketItem("b_4", "İlk buluştuğumuz yere tekrar gitmek", "Nostalji", "📍"),
        CoupleBucketItem("b_5", "Çadır kurup gece yıldızları seyretmek", "Macera", "⛺"),
        CoupleBucketItem("b_6", "Dönme dolaba binip zirvede sarılmak", "Eğlence", "🎡"),
        CoupleBucketItem("b_7", "Birbirimize mektup yazıp 1 yıl sonra açmak", "Hatıra", "✉️"),
        CoupleBucketItem("b_8", "Birlikte bir fidan dikip büyütmek", "Anı", "🌱"),
        CoupleBucketItem("b_9", "Plansız hafta sonu yolculuğuna çıkmak", "Seyahat", "✈️")
    )
}

// 6. GÜNÜN ROMANTİK SORUSU (ÇİFT SOHBET KARTLARI)
data class CoupleQuestion(
    val id: Int,
    val question: String,
    val category: String,
    val emoji: String
)

object DefaultCoupleQuestions {
    val questions = listOf(
        CoupleQuestion(1, "Beni ilk gördüğün an aklından geçen ilk düşünce tam olarak neydi?", "İlk Günler", "👀"),
        CoupleQuestion(2, "Birlikte geçirdiğimiz ve kahkahalarla güldüğümüz en unutamadığın an hangisi?", "Hatıralar", "😂"),
        CoupleQuestion(3, "Benimle ilgili öğrendiğinde seni en çok mutlu eden özelliğim ne oldu?", "Keşif", "✨"),
        CoupleQuestion(4, "İlişkimizde kendini en çok huzurlu ve güvende hissettiğin an hangisiydi?", "Duygular", "🛡️"),
        CoupleQuestion(5, "Gelecekte birlikte gerçekleştirmek istediğin en büyük hayalimiz nedir?", "Gelecek", "🏡"),
        CoupleQuestion(6, "Sence birbirimize duyduğumuz sevgiyi en iyi anlatan şarkı hangisi?", "Müzik", "🎶"),
        CoupleQuestion(7, "Seni en çok ne yaptığımda gerçekten sevildiğini hissediyorsun?", "Sevgi Dili", "🥰"),
        CoupleQuestion(8, "Birlikte yaşlanıp 80 yaşına geldiğimizde nasıl bir çift olacağız?", "Yaşlanmak", "👵"),
        CoupleQuestion(9, "Günün hangi anında aklına daha çok düşüyorum?", "Aşk", "💭"),
        CoupleQuestion(10, "Bana henüz söylemediğin ama içinden geçen tatlı bir itirafın var mı?", "İtiraf", "🤫")
    )
}

// 7. BURÇ UYUMU MODÜLÜ
data class ZodiacSign(
    val name: String,
    val symbol: String,
    val element: String,
    val dates: String
)

object ZodiacData {
    val signs = listOf(
        ZodiacSign("Koç", "♈", "Ateş", "21 Mar - 19 Nis"),
        ZodiacSign("Boğa", "♉", "Toprak", "20 Nis - 20 May"),
        ZodiacSign("İkizler", "♊", "Hava", "21 May - 20 Haz"),
        ZodiacSign("Yengeç", "♋", "Su", "21 Haz - 22 Tem"),
        ZodiacSign("Aslan", "♌", "Ateş", "23 Tem - 22 Ağu"),
        ZodiacSign("Başak", "♍", "Toprak", "23 Ağu - 22 Eyl"),
        ZodiacSign("Terazi", "♎", "Hava", "23 Eyl - 22 Eki"),
        ZodiacSign("Akrep", "♏", "Su", "23 Eki - 21 Kas"),
        ZodiacSign("Yay", "♐", "Ateş", "22 Kas - 21 Ara"),
        ZodiacSign("Oğlak", "♑", "Toprak", "22 Ara - 19 Oca"),
        ZodiacSign("Kova", "♒", "Hava", "20 Oca - 18 Şub"),
        ZodiacSign("Balık", "♓", "Su", "19 Şub - 20 Mar")
    )

    fun calculateZodiacMatch(sign1: ZodiacSign, sign2: ZodiacSign): CompatibilityResult {
        val sameElement = sign1.element == sign2.element
        val score = when {
            sameElement -> 96
            (sign1.element == "Ateş" && sign2.element == "Hava") || (sign1.element == "Hava" && sign2.element == "Ateş") -> 93
            (sign1.element == "Toprak" && sign2.element == "Su") || (sign1.element == "Su" && sign2.element == "Toprak") -> 94
            else -> 87
        }

        val (title, interp, advice) = when {
            score >= 95 -> Triple(
                "Kozmik Ruh İkizleri ✨",
                "${sign1.name} (${sign1.element}) ve ${sign2.name} (${sign2.element}) elementlerinin harika uyumu. Birbirinizi içten içe tamamlayan büyülü bir enerjiye sahipsiniz.",
                "Bu göksel bağı beslemek için birlikte romantik maceralara atılın."
            )
            score >= 90 -> Triple(
                "Tutkulu & Manyetik Çekim 💖",
                "${sign1.name} ve ${sign2.name} yan yana geldiğinde aralarındaki kıvılcım hemen fark edilir. Birbirinize kattığınız heyecan çok yüksek.",
                "Açık iletişimle birbirinizin hassas yönlerine özen gösterin."
            )
            else -> Triple(
                "Dengeleyici & Derin Sevda 🌹",
                "Farklı enerjilerin birbirini beslediği, zenginleştirici bir aşk bağı. Birlikte olgunlaşan ve güçlenen bir birliktelik.",
                "Birbirinizin farklılıklarını birer zenginlik olarak kucaklayın."
            )
        }

        return CompatibilityResult(
            name1 = "${sign1.symbol} ${sign1.name}",
            name2 = "${sign2.symbol} ${sign2.name}",
            scorePercentage = score,
            statusTitle = title,
            interpretation = interp,
            advice = advice
        )
    }
}

// 8. AŞK MEKTUBU SİHİRBAZI & ŞABLONLAR
data class LoveLetterTemplate(
    val id: String,
    val title: String,
    val tone: String,
    val emoji: String,
    val baseTemplate: String
)

object LoveLetterTemplates {
    val templates = listOf(
        LoveLetterTemplate(
            id = "t_1",
            title = "Tutkulu & Derin Aşk",
            tone = "Tutkulu",
            emoji = "🔥",
            baseTemplate = "Canım {partner},\n\nHayatıma girdiğin günden beri dünyamın renkleri değişti. Gözlerindeki ışıltı, kalbimin en derin odalarını aydınlatıyor. En çok senin {trait} özelliğine hayranım. Seninle nefes almak, senin kollarında huzur bulmak bu hayattaki en büyük lütfum. Seni sonsuza dek seveceğim.\n\nSeni Çok Seven,\n{my_name}"
        ),
        LoveLetterTemplate(
            id = "t_2",
            title = "Şiirsel & Duygusal Mektup",
            tone = "Duygusal",
            emoji = "💌",
            baseTemplate = "Sevgilim {partner},\n\nSeni sevmek, baharın ilk tomurcuğunu koklamak gibi taze ve içten. Sesini duyduğum an içimdeki bütün fırtınalar yerini dingin bir denize bırakıyor. Varlığın, ömrüme yazılmış en güzel şiir mısrasıdır. İyi ki varsın, iyi ki kalbimin sahibisin.\n\nÖmür Boyu Kalbinde,\n{my_name}"
        ),
        LoveLetterTemplate(
            id = "t_3",
            title = "Yıldönümü & Teşekkür",
            tone = "Kutlama",
            emoji = "🥂",
            baseTemplate = "Biriciğim {partner},\n\nBirlikte geçirdiğimiz her gün, aşkımızın ne kadar kıymetli olduğunu bana bir kez daha hatırlatıyor. Birlikte güldüğümüz, el ele aştığımız tüm yollar için sana kalpten teşekkür ederim. Yanında olmak dünyanın en güvenli yuvası. Nice mutlu senelerimize sevgilim!\n\nSonsuz Sevgiyle,\n{my_name}"
        ),
        LoveLetterTemplate(
            id = "t_4",
            title = "Tatlı & Eğlenceli İtiraf",
            tone = "Eğlenceli",
            emoji = "🥰",
            baseTemplate = "Hey dünyanın en tatlı insanı {partner}!\n\nBiliyor musun, seni düşünürken kendi kendime gülümsemekten etraftakiler beni deli sanıyor olabilir! En sevdiğim kahveden, en sevdiğim filmden bile daha güzelsin. İyi ki hayatıma girdin, iyi ki benim birtanemsin!\n\nSenin Delin,\n{my_name}"
        ),
        LoveLetterTemplate(
            id = "t_5",
            title = "Özür & Barışma Mektubu",
            tone = "Barışma",
            emoji = "🕊️",
            baseTemplate = "Kıymetlim {partner},\n\nEğer seni kırdıysam veya üzdüysem kalbimin en derin yerinden özür dilerim. Seni üzmek, dünyada isteyeceğim son şey bile olamaz. Bizim sevgimiz her türlü kırgınlıktan çok daha büyük. Lütfen bana sımsıkı sarıl ve her şeyi yeniden başlatalım.\n\nPişman Aşığın,\n{my_name}"
        )
    )

    fun buildLetter(template: LoveLetterTemplate, myName: String, partnerName: String, trait: String): String {
        return template.baseTemplate
            .replace("{partner}", if (partnerName.isNotBlank()) partnerName else "Sevgilim")
            .replace("{my_name}", if (myName.isNotBlank()) myName else "Ben")
            .replace("{trait}", if (trait.isNotBlank()) trait else "masum gülüşüne")
    }
}

