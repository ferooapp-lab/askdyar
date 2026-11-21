package com.example.data.model

import java.util.Locale

// =========================================================================
// 1. 1,200+ DİNAMİK & ÇALIŞAN AŞK SÖZÜ MOTORU (Sonsuz İlham Üreticisi)
// =========================================================================
enum class DynamicMood(val label: String, val emoji: String) {
    TUTKU("Derin Tutku", "🔥"),
    ILK_ASK("İlk Aşk Heyecanı", "💘"),
    OZLEM("Özlem & Hasret", "💌"),
    GUNAYDIN("Günaydın Enerjisi", "☀️"),
    IYI_GECELER("İyi Geceler Huzuru", "🌙"),
    YILDONUMU("Yıldönümü Kutlaması", "🥂"),
    BARISMA("Özür & Barışma", "🕊️"),
    ILTIFAT("Zarif İltifat", "🌹"),
    DOGUM_GUNU("Doğum Günü Özel", "🎂"),
    OMURLUK("Ömürlük Bağlılık", "💍")
}

object RomanticUniverseGenerator {
    val endearments = listOf(
        "Sevgilim",
        "Birtanem",
        "Canözüm",
        "Ruhumun eşi",
        "Ömrüm",
        "Kalbim",
        "Hayat ışığım",
        "Meleğim",
        "Gözbebeğim",
        "Tek gerçeğim"
    )

    private val templatesByMood: Map<DynamicMood, List<String>> = mapOf(
        DynamicMood.TUTKU to listOf(
            "{h}, gözlerine her baktığımda kalbim ilk günkü gibi hızlanıyor; seninle her anım ateşten bir şiir gibi.",
            "Dünyanın bütün gürültüsü sussa da içimde sadece senin adın yankılanıyor {h}, ruhum sana mühürlü.",
            "{h}, nefesin nefesime karıştığında zaman duruyor; sen benim en derin tutkum, en güzel mucizemsin.",
            "Ellerini tuttuğum an bütün evren avuçlarımda toplanıyor {h}, sensiz geçen saniye bile eksik.",
            "{h}, senin sevgin damarlarımda dolaşan en sıcak hayat iksiri; her atışta sana yeniden aşık oluyorum.",
            "Karanlık gecelerimi aydınlatan tek alev sensin {h}, kalbimin tahtında sadece sen oturuyorsun.",
            "{h}, bir gülüşünle içimdeki bütün fırtınaları dindirip kalbime baharı getiriyorsun.",
            "Sana duyduğum sevda kelimelere sığmayacak kadar derin, denizlerden daha engin {h}.",
            "{h}, sen benim aklımdan hiç çıkmayan en tatlı telaşım, en vazgeçilmez zaafımsın.",
            "Varlığınla hayatıma anlam katan, her dokunuşuyla ruhumu ısıtan tek kişisin {h}.",
            "{h}, bütün yollar sana çıkıyor, bütün şarkılar seni fısıldıyor, kalbim sadece senin için çarpıyor.",
            "Seninle bir ömür değil, bin ömür bile az gelir bana {h}; çünkü aşkım her gün büyüyerek çoğalıyor."
        ),
        DynamicMood.ILK_ASK to listOf(
            "{h}, seni ilk gördüğüm andan beri kalbimde kelebekler uçuşuyor; heyecanım hiç dinmiyor.",
            "Mesajını her gördüğümde yüzümde engel olamadığım o tatlı tebessüm beliriyor {h}.",
            "{h}, seninle tanışmak hayatımın en güzel tesadüfü değil, en hayırlı kaderiymiş.",
            "Sesini ilk duyduğum gündeki o masum heyecanı hâlâ ilk günkü tazeliğiyle taşıyorum {h}.",
            "{h}, adını duyduğumda bile içim kıpır kıpır oluyor; sen benim en güzel başlangıcımsın.",
            "Dünya seninle tanıştıktan sonra bambaşka renklere büründü {h}, her yer bahar bahçe.",
            "{h}, gözlerindeki o saf ışıltı bütün kalbimi tek bir bakışta fethetti.",
            "Seninle konuşurken saatlerin nasıl aktığını unutuyorum {h}, zaman bizimle akmıyor sanki.",
            "{h}, kalbimin kapısını hiç çalmadan açıp içine yerleşen en tatlı misafirimsin.",
            "Her yeni günde seni biraz daha tanımak ve daha çok sevmek dünyanın en güzel duygusu {h}.",
            "{h}, aklımın ucundan bile geçmezdi birinin gülüşüne bu kadar hayran kalacağım.",
            "Seninle el ele yürüdüğüm ilk günü ömrümün sonuna kadar kalbimde saklayacağım {h}."
        ),
        DynamicMood.OZLEM to listOf(
            "{h}, aramızda kilometreler olsa da kalbim her saniye senin göğsünde atıyor.",
            "Bugün her zamankinden daha çok özledim kokunu, sesini ve sımsıkı sarılmanı {h}.",
            "{h}, yokluğunda zaman geçmek bilmiyor; gözlerim hep yollarını, kalbim hep sesini arıyor.",
            "Aynı gökyüzüne bakıp aynı aya fısıldadığımız her gece özlemim biraz daha büyüyor {h}.",
            "{h}, bir an önce kollarına kavuşup saatlerce hiç konuşmadan sadece sarılmak istiyorum.",
            "Sensiz içtiğim kahvenin bile tadı eksik kalıyor {h}, aklım fikrim hep sende.",
            "{h}, ne zaman bir sevgi şarkısı çalsa içim sızlıyor; çünkü her notasında senin hasretin var.",
            "Mesafeler sadece haritalar içindir {h}, benim gönlümde sana olan uzaklık sıfır.",
            "{h}, rüzgâr bile bugün senin adını fısıldıyor kulağıma; çabuk gel, çok özledim.",
            "Her gece uyumadan önce kavuşacağımız o güzel günün hayaliyle gözlerimi kapatıyorum {h}.",
            "{h}, sana sarılamadığım her gün ömrümden çalınmış gibi; kavuşmamız bayram olacak.",
            "Kalbimdeki yerin o kadar büyük ki {h}, yanında değilken bile her an benimlesin."
        ),
        DynamicMood.GUNAYDIN to listOf(
            "Günaydın {h}! Güneş bugün penceremden değil, senin sıcacık tebessümünden doğdu.",
            "{h}, gözlerimi açtığım an aklıma düşen ilk güzel düşünce sensin; günün harika geçsin!",
            "Günaydın {h}, yeni başlayan bu günde kalbinden huzur, yüzünden neşe hiç eksik olmasın.",
            "Sabah kahvemin en tatlı eşlikçisi, uyanır uyanmaz gülümseme sebebimsin {h}. Günaydın!",
            "{h}, günaydın! Bugün de bütün dualarımın başında senin sağlığın ve mutluluğun var.",
            "Uyan {h}, senin o güzel enerjin olmadan bu şehrin sabahı aydınlanmıyor.",
            "Günaydın {h}, seni dün çok sevdim, bugün daha da çok seviyorum; harika bir gün bizim olsun.",
            "Gözlerinin ışıltısı kadar parlak, kalbin kadar temiz bir sabah diliyorum sana {h}.",
            "{h}, her sabah senin varlığına şükrederek uyanmak bu dünyanın en büyük zenginliği. Günaydın!",
            "Günaydın {h}! Bugün karşına çıkan her şey en az senin kadar tatlı ve güzel olsun.",
            "Sabahın serinliğinde içimi ısıtan tek şey senin sevgin {h}, musmutlu bir gün geçir.",
            "{h}, günaydın ömrümün baharı; bu mesajım yanağına kondurduğum sıcacık bir sabah öpücüğü olsun."
        ),
        DynamicMood.IYI_GECELER to listOf(
            "İyi geceler {h}. Bu gece bütün yıldızlara senin adını fısıldadım, huzurla uyu.",
            "{h}, gözlerini kapattığında bütün yorgunluğun uçup gitsin, rüyalarında benimle buluş.",
            "Gece sessizliğe büründüğünde kalbimin tek sesi sen oluyorsun {h}. Tatlı rüyalar!",
            "{h}, melekler başucunda, dualarım hep seninle olsun; huzur dolu uykular dilerim.",
            "İyi geceler {h}, yastığına başını koyduğunda kalbimin sıcaklığını yanında hisset.",
            "Gecenin karanlığını aydınlatan ay ışığım sensin {h}. En güzel rüyalar seninle olsun.",
            "{h}, bugün de seninle geçen her an için şükrederek kapatıyorum gözlerimi. İyi geceler!",
            "Rüyanda saçlarını okşayan hafif bir esinti hissedersen bil ki o benim sevgimdir {h}.",
            "{h}, uyu ve dinlen; yarın sabah yeniden sevgimizle aydınlanacak güzel bir güne uyanacağız.",
            "Kalbim kalbine emanet {h}. Gece boyunca bütün güzel dileklerim uykunu korusun.",
            "İyi geceler {h}, rüyaların bile senin gülüşün kadar tatlı ve huzurlu geçsin.",
            "Her gecenin sonunda aklıma düşen son dua, her sabahın başında açan ilk çiçek sensin {h}."
        ),
        DynamicMood.YILDONUMU to listOf(
            "Nice mutlu yıllara {h}! Seninle geçen her gün ömrümün en değerli hazinesine dönüştü.",
            "{h}, el ele yürüdüğümüz bu güzel yolda birlikte nice güzel yıldönümlerimiz olsun.",
            "Hayatımın en doğru kararı seni sevmekmiş {h}; iyi ki yollarımız kesişmiş, kutlu olsun!",
            "{h}, zaman geçse de kalbimdeki yerin hep ilk günkü gibi taze ve eşsiz. Seni seviyorum!",
            "Birlikte güldüğümüz, büyüdüğümüz ve omuz omuza durduğumuz her an için teşekkürler {h}.",
            "{h}, seninle yaşlanmak fikri bile kalbimi heyecanla dolduruyor; nice senelerimize!",
            "Her geçen yıl sana olan sevgimi azaltmadı, aksine köklerini daha da derinleştirdi {h}.",
            "{h}, yıldönümümüz kutlu olsun! Aşkımız her mevsim yeniden çiçek açan bir bahçe gibi.",
            "Seninle kurduğumuz bu sıcacık dünyayı hiçbir şeye değişmem {h}, iyi ki benimsin.",
            "{h}, bugüne kadar biriktirdiğimiz tüm anılarımıza ve gelecek güzel günlerimize kadeh kaldırıyorum!",
            "Birlikte aştığımız her yol, paylaştığımız her tebessüm bizi daha da güçlendirdi {h}.",
            "{h}, hayat kitabımın en güzel sayfaları seninle yazıldı; nice unutulmaz bölümlere!"
        ),
        DynamicMood.BARISMA to listOf(
            "{h}, seni kırdığım veya üzdüğüm için kalbim çok pişman; bizim sevgimiz her kırgınlıktan büyük.",
            "Beni affet {h}; gözlerindeki bir damla hüzne bile sebep olmak dünyada isteyeceğim son şey.",
            "{h}, seninle konuşmadan geçen her dakika bana ağır geliyor, gel sımsıkı sarılıp barışalım.",
            "Hatalar insanlar içindir ama bizim aşkımız her şeyi onaracak kadar güçlü {h}. Özür dilerim.",
            "{h}, sesini duymadan içim rahat etmiyor; bir tebessümünle dünyamı yeniden aydınlat lütfen.",
            "Kırgınlıklarımızın arasına sevgi köprüsü kuralım {h}, çünkü ben sensiz yarım kalıyorum.",
            "{h}, haklı veya haksız olmak değil, seninle mutlu olmak tek derdim. Gel barışalım birtanem.",
            "Sana olan sevgim anlık kızgınlıklardan katbekat fazla {h}; kalbini yeniden kazanmak istiyorum.",
            "{h}, elimi tut ve geçmişi geride bırakalım; senin gülüşün olmadan günüm aydınlanmıyor.",
            "Biliyorum bazen yanlış kelimeler seçebiliyorum {h}, ama kalbimde sana karşı hep saf sevgi var.",
            "{h}, bugün küslüğe değil sevgimize şans verelim; seni her şeyden çok seviyorum.",
            "Bir sarılmayla bütün buzları eritebiliriz {h}, kapında özür ve çiçeklerle bekliyorum."
        ),
        DynamicMood.ILTIFAT to listOf(
            "{h}, dünyanın en güzel manzarası senin gülerken kısılan o ışıl ışıl gözlerin.",
            "Senin zarafetin ve kalbinin güzelliği yanında bütün kelimeler eksik kalıyor {h}.",
            "{h}, sesin kulağıma geldiğinde içimde en sevdiğim melodi çalmaya başlıyor sanki.",
            "Yanındayken en sıradan sokaklar bile senin ışığınla bir masal diyarına dönüşüyor {h}.",
            "{h}, kalbinin temizliği yüzüne öylesine yansımış ki baktıkça huzur doluyorum.",
            "Sen benim sadece sevdiğim değil, her halinle hayranlık duyduğum en özel insansın {h}.",
            "{h}, gülüşün karanlık bir odada yakılan bin mum gibi etrafındaki her şeyi güzelleştiriyor.",
            "Senin gibi ince ruhlu ve merhametli birine aşık olmak hayatımın en büyük şansı {h}.",
            "{h}, baktığın her yeri güzelleştiren o eşsiz enerjine her gün yeniden hayran kalıyorum.",
            "Konuştuğunda kelimeler değil, adeta kalbinden dökülen inciler dökülüyor dudaklarından {h}.",
            "{h}, stilin, duruşun ve o masum bakışlarınla girdiğin her ortamın yıldızı sensin.",
            "Seni anlatmaya ne şairlerin kalemi yeter ne de sözlüklerdeki en süslü kelimeler {h}."
        ),
        DynamicMood.DOGUM_GUNU to listOf(
            "İyi ki doğdun {h}! Doğduğun gün sadece senin değil, benim de en şanslı günüm olmuş.",
            "{h}, yeni yaşın sana kalbin kadar güzel mutluluklar, sağlık ve hep bizimle dolu anılar getirsin!",
            "Doğum günün kutlu olsun {h}! Hayatıma kattığın bütün renkler ve neşe için minnettarım.",
            "{h}, pastandaki her mumun ışığında birlikte gerçekleşecek en güzel dileklerimizi diliyorum.",
            "Dünyaya gelişinle etrafına ışık saçtığın gibi benim de dünyamı aydınlattın {h}. İyi ki varsın!",
            "{h}, yeni yaşında da elini hiç bırakmadan her hayalini birlikte kutlamak en büyük arzum.",
            "İyi ki varsın {h}, iyi ki doğdun; sensiz bir dünyanın ne kadar eksik olacağını düşünemiyorum bile.",
            "{h}, bugün bütün evren senin için gülümsüyor; en özel yaşın en güzel sürprizlerle dolsun!",
            "Hayatımın en kıymetli hediyesi bizzat sensin {h}. Doğum günün kutlu olsun!",
            "{h}, yüzündeki tebessüm hiç solmasın, her yeni yaşında biraz daha mutlu olalım birlikte.",
            "Sen doğduğun gün gökyüzünden en parlak yıldız kalbime inmiş olmalı {h}. Mutlu yıllar!",
            "{h}, nice sağlıklı, huzurlu, kahkaha dolu ve aşkımızla taçlanan yaşların olsun!"
        ),
        DynamicMood.OMURLUK to listOf(
            "{h}, benim dünyadaki tek yuvam senin kalbin; nereye gidersem gideyim evim hep sensin.",
            "Saçlarımıza aklar düşse bile elini ilk günkü aşkla tutacağıma söz veriyorum {h}.",
            "{h}, seninle bir ömür paylaşmak değil mesele; seninle geçen her anı ömre bedel yaşamak.",
            "Hayatımın bütün mevsimlerini, baharını da kışını da sadece seninle yaşamak istiyorum {h}.",
            "{h}, iyi günde de zor günde de omuz omuza duracağım en güvenilir limanımsın.",
            "Dualarımın en güzel cevabı, ömrümün sonuna kadar yanımda görmek istediğim tek insansın {h}.",
            "{h}, aşkımız gelip geçici bir heves değil, kökü sonsuzluğa uzanan ulu bir çınar gibi.",
            "Birlikte kurduğumuz her hayal, geleceğe bıraktığımız en güzel imza olacak {h}.",
            "{h}, senin yanında kendim olabiliyor, senin kollarında bütün dünyadan güvende hissediyorum.",
            "Ömrümün geri kalan her sabahında gözlerimi senin gözlerine açmak istiyorum {h}.",
            "{h}, kalplerimiz arasındaki bu görünmez bağ hiçbir fırtınada kopmayacak kadar sağlam.",
            "Bana 'evet' dediğin ve kalbini açtığın için sonsuz teşekkürler {h}, ömürlüğüm sensin."
        )
    )

    val totalVariationsCount: Int
        get() = DynamicMood.values().size * endearments.size * 12 // 10 * 10 * 12 = 1,200 variations

    fun generate(
        mood: DynamicMood,
        endearmentIndex: Int,
        templateIndex: Int
    ): String {
        val list = templatesByMood[mood] ?: templatesByMood.values.first()
        val safeTemplate = list[templateIndex.mod(list.size)]
        val safeEndearment = endearments[endearmentIndex.mod(endearments.size)]

        // Replace {h} cleanly adjusting capitalization if at start of sentence
        return if (safeTemplate.startsWith("{h}")) {
            safeTemplate.replaceFirst("{h}", safeEndearment)
                .replace("{h}", safeEndearment.lowercase(Locale("tr")))
        } else {
            safeTemplate.replace("{h}", safeEndearment.lowercase(Locale("tr")))
        }
    }

    fun generateRandom(mood: DynamicMood? = null): Pair<DynamicMood, String> {
        val chosenMood = mood ?: DynamicMood.values().random()
        val eIdx = (endearments.indices).random()
        val tIdx = (0 until 12).random()
        return chosenMood to generate(chosenMood, eIdx, tIdx)
    }
}

// =========================================================================
// 2. RANDEVU KARAR ÇARKI (DATE NIGHT SPIN WHEEL)
// =========================================================================
data class DateWheelSlice(
    val id: Int,
    val title: String,
    val emoji: String,
    val detail: String,
    val colorHex: Long
)

object DateWheelData {
    val slices = listOf(
        DateWheelSlice(1, "Mum Işığında Yemek", "🕯️", "Evde veya favori restoranınızda romantik bir akşam yemeği hazırlayın.", 0xFFE91E63),
        DateWheelSlice(2, "Yıldız Yürüyüşü", "🌙", "Telefonları sessize alıp el ele sahil veya parkta sakin bir yürüyüş yapın.", 0xFF3F51B5),
        DateWheelSlice(3, "Sinema & Mısır", "🍿", "Sevdiğiniz filmden oluşan mini bir ev sineması ve bol patlamış mısır!", 0xFFFF9800),
        DateWheelSlice(4, "Birlikte Tatlı Yapın", "🍰", "Mutfakta birlikte kurabiye, sufle veya sıcak çikolata hazırlayıp eğlenin.", 0xFF9C27B0),
        DateWheelSlice(5, "Nostalji Gecesi", "📸", "İlk tanışma fotoğraflarınıza ve eski mesajlarınıza bakıp anıları tazeleyin.", 0xFF009688),
        DateWheelSlice(6, "Sürpriz Kahve/Piknik", "☕", "Termosa kahvenizi koyup en sevdiğiniz manzaraya karşı sohbet edin.", 0xFF795548),
        DateWheelSlice(7, "Masaj & Spa Saati", "💆", "Rahatlatıcı fon müziği eşliğinde birbirinize 15 dakikalık omuz masajı yapın.", 0xFFEC407A),
        DateWheelSlice(8, "Müzik & Dans", "💃", "Odanın ışıklarını loşlaştırıp 'Bizim Şarkımız' eşliğinde baş başa dans edin.", 0xFF673AB7)
    )
}

// =========================================================================
// 3. ROMANTİK AŞK ZARI (COUPLE DICE)
// =========================================================================
object RomanticDiceData {
    val actions = listOf(
        "Gözlerinin içine bakarak iltifat et" to "👀",
        "Alnından sıcacık öp ve sarıl" to "😘",
        "En sevdiğin ortak anınızı anlat" to "📖",
        "Elini öpüp ona teşekkür et" to "🤝",
        "Kulağına tatlı bir sır fısılda" to "🤫",
        "Ona 2 dizelik mini bir şiir uydur" to "✍️",
        "Sırt ve omuz masajı yap" to "💆",
        "Şarkı mırıldanıp dansa kaldır" to "🎶"
    )

    val durations = listOf(
        "30 saniye boyunca" to "⏱️",
        "İki kez üst üste" to "✨",
        "Gülümseyerek hemen şimdi" to "😊",
        "1 dakika kesintisiz" to "⏳",
        "Gözleri kapalıyken sürprizle" to "🎁",
        "En romantik ses tonunla" to "🎙️"
    )

    fun roll(): Triple<String, String, String> {
        val (act, actEmoji) = actions.random()
        val (dur, durEmoji) = durations.random()
        return Triple("$actEmoji $durEmoji", act, dur)
    }
}

// =========================================================================
// 4. 5 SEVGİ DİLİ TESTİ (LOVE LANGUAGE QUIZ)
// =========================================================================
enum class LoveLanguageType(
    val title: String,
    val emoji: String,
    val description: String,
    val partnerTip: String
) {
    WORDS(
        title = "Onaylayıcı Sözler",
        emoji = "💬",
        description = "Sevildiğini en çok içten iltifatlar, tatlı mesajlar ve 'seni seviyorum' cümleleriyle hissediyorsun.",
        partnerTip = "Partnerin sana sık sık güzel sözler yazmalı, notlar bırakmalı ve takdirini sesli dile getirmeli."
    ),
    TIME(
        title = "Kaliteli Zaman",
        emoji = "🕰️",
        description = "Senin için en kıymetli hediye dikkati tamamen sana verilmiş, baş başa geçirilen kaliteli saatlerdir.",
        partnerTip = "Telefonlardan uzak yürüyüşler, uzun sohbetler ve baş başa planlar kalbini fetheder."
    ),
    GIFTS(
        title = "Anlamlı Hediyeler",
        emoji = "🎁",
        description = "Maddi değerinden ziyade 'aklıma geldin' diyen küçük, düşünceli sürprizler ve hatıralar seni çok mutlu eder.",
        partnerTip = "Tek bir çiçek, sevdiğin bir çikolata veya el emeği küçük bir hatıra gününü aydınlatır."
    ),
    SERVICE(
        title = "Hizmet Davranışları",
        emoji = "☕",
        description = "Senin için sevgi sözden çok eylemdir; işini kolaylaştıran, senin için kahve hazırlayan özenli adımlar çok değerlidir.",
        partnerTip = "Yorgun olduğunda sana yardım etmesi veya senden önce düşünüp bir işi halletmesi sana aşkını hissettirir."
    ),
    TOUCH(
        title = "Fiziksel Temas",
        emoji = "🤗",
        description = "El ele tutuşmak, sımsıkı sarılmak ve şefkatli bir dokunuş senin için en güçlü güven ve sevgi bağıdır.",
        partnerTip = "Yürürken elini tutmak, film izlerken omzuna yaslanmak ve sık sık sarılmak bağınızı güçlendirir."
    )
}

data class LoveQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<Pair<String, LoveLanguageType>>
)

object LoveLanguageQuizData {
    val questions = listOf(
        LoveQuizQuestion(
            id = 1,
            question = "Zor ve yorucu bir günün ardından partnerinden ne görmek seni en çok rahatlatır?",
            options = listOf(
                "Bana 'Seninle gurur duyuyorum, harikasın' demesi" to LoveLanguageType.WORDS,
                "Telefonunu bırakıp benimle oturup çay eşliğinde dertleşmesi" to LoveLanguageType.TIME,
                "Bana sımsıkı sarılıp başımı omzuna yaslamama izin vermesi" to LoveLanguageType.TOUCH,
                "Ben dinlenirken yemeği veya kahveyi benim yerime hazırlaması" to LoveLanguageType.SERVICE,
                "Beni neşelendirmek için sevdiğim küçük bir tatlı alıp gelmesi" to LoveLanguageType.GIFTS
            )
        ),
        LoveQuizQuestion(
            id = 2,
            question = "Özel bir gününüzde hangi sürpriz kalbini daha hızlı çarptırır?",
            options = listOf(
                "İçinde duygu dolu, el yazısıyla yazılmış uzun bir aşk mektubu" to LoveLanguageType.WORDS,
                "Sadece ikimize özel, kalabalıktan uzak planlanmış bir hafta sonu" to LoveLanguageType.TIME,
                "Anlamı olan, özenle seçilmiş ve hatıra değeri yüksek bir hediye" to LoveLanguageType.GIFTS,
                "Bütün hazırlıkları benim yorulmamam için gizlice halletmiş olması" to LoveLanguageType.SERVICE,
                "Yıldızların altında el ele dans edip uzun uzun sarılmak" to LoveLanguageType.TOUCH
            )
        ),
        LoveQuizQuestion(
            id = 3,
            question = "İlişkide seni en çok ne incitir?",
            options = listOf(
                "Kaba sözler veya sevgisini kelimelerle hiç ifade etmemesi" to LoveLanguageType.WORDS,
                "Yanımdayken bile sürekli telefonuna bakması ve vakit ayırmaması" to LoveLanguageType.TIME,
                "Soğuk ve mesafeli durup sarılmaktan kaçınması" to LoveLanguageType.TOUCH,
                "Verdiği sözleri yerine getirmeyip bütün yükü bana bırakması" to LoveLanguageType.SERVICE,
                "Özel günleri tamamen unutup küçük bir jest bile yapmaması" to LoveLanguageType.GIFTS
            )
        ),
        LoveQuizQuestion(
            id = 4,
            question = "Sen sevgini karşı tarafa en çok nasıl gösterirsin?",
            options = listOf(
                "Ona sık sık tatlı mesajlar ve iltifatlar göndererek" to LoveLanguageType.WORDS,
                "Ona en güzel anlarda eşlik edip tüm ilgimi vererek" to LoveLanguageType.TIME,
                "Gittiğim yerlerden ona küçük hatıralar veya hediyeler alarak" to LoveLanguageType.GIFTS,
                "Onun işini kolaylaştıracak pratik sürprizler ve yardımlar yaparak" to LoveLanguageType.SERVICE,
                "Yanından geçerken bile elini tutup sarılarak" to LoveLanguageType.TOUCH
            )
        ),
        LoveQuizQuestion(
            id = 5,
            question = "Hafta sonu ideal romantik sabah senin için hangisidir?",
            options = listOf(
                "Sabah uyanınca kulağıma fısıldanan güzel bir 'Günaydın aşkım' mesajı" to LoveLanguageType.WORDS,
                "Saatlerce acele etmeden birlikte yapılan uzun bir kahvaltı sohbeti" to LoveLanguageType.TIME,
                "Masada beni bekleyen taze bir gül veya sevdiğim bir sürpriz" to LoveLanguageType.GIFTS,
                "Kalktığımda kahvaltının ve taze çayın çoktan hazırlanmış olması" to LoveLanguageType.SERVICE,
                "Yataktan kalkmadan önce sıcacık, huzurlu bir sarılma" to LoveLanguageType.TOUCH
            )
        )
    )

    fun evaluate(answers: List<LoveLanguageType>): LoveLanguageType {
        if (answers.isEmpty()) return LoveLanguageType.WORDS
        return answers.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: LoveLanguageType.WORDS
    }
}

// =========================================================================
// 5. İSİM AKROSTİŞ ŞİİR ÜRETECİ (TÜRKÇE ALFABE DESTEKLİ)
// =========================================================================
object AcrosticPoemBuilder {
    private val letterLines = mapOf(
        'A' to "Adın kalbime yazılmış en güzel aşk duasının baş harfi,",
        'B' to "Bakışlarınla aydınlanıyor ömrümün en karanlık geceleri,",
        'C' to "Canıma can katan o masum gülüşün dünyalara bedel,",
        'Ç' to "Çiçekler bile kıskanır senin yanımdayken açan zarafetini,",
        'D' to "Düşlerimin en tatlı gerçeği, huzurumun tek adresisin,",
        'E' to "Ellerini tuttuğum her an bütün mevsimler bahara döner,",
        'F' to "Fırtınalı denizlerin ortasında sığındığım en güvenli limansın,",
        'G' to "Gözlerinin derinliğinde kaybolmak en güzel özgürlüğüm,",
        'Ğ' to "Gönlümün en ince sızısına şifa olan tek sevgilisin,",
        'H' to "Her nefeste kalbimde yankılanan eşsiz bir mucizesin,",
        'I' to "Işıl ışıl parlayan gözlerinle umut doldurursun içime,",
        'İ' to "İyi ki varsın, iyi ki kalbimin tek sahibi sensin,",
        'J' to "Jüpiter bile kıskanır gökyüzünde bizim parlayan aşkımızı,",
        'K' to "Kalbimin bütün kapıları sadece senin sevginle açılır,",
        'L' to "Laleler, güller sönük kalır senin eşsiz güzelliğinin yanında,",
        'M' to "Masallardan çıkıp hayatıma doğan en saf meleğimsin,",
        'N' to "Nereye baksam senin izin, nereye dönsem senin kokun var,",
        'O' to "Olur da bir gün yorulursan, omzum sonsuza dek senin yuvan,",
        'Ö' to "Ömrümün kalan bütün sayfalarını sadece seninle yazmak isterim,",
        'P' to "Papatyalar gibi saf, yıldızlar gibi parlaktır sana sevdam,",
        'R' to "Ruhumun diğer yarısı, tamamlandığım en güzel yerimsin,",
        'S' to "Seninle geçen her saniye, yaşanmış bir ömre bedel sevgilim,",
        'Ş' to "Şiirler bile aciz kalır sana duyduğum bu tutkuyu anlatmaya,",
        'T' to "Tek bir tebessümünle bütün dünyamı yeniden kurarsın,",
        'U' to "Uzaklar bile ayıramaz aynı ritimde atan iki kalbi,",
        'Ü' to "Üşüdüğüm her kış gecesinde içimi ısıtan güneşimsin,",
        'V' to "Varlığın hayatımın en büyük şükrü, en tatlı mucizesi,",
        'Y' to "Yıldızlara adını fısıldadım bu gece, sonsuz aşkım seninle,",
        'Z' to "Zaman dursa da senin yanında, ben sana her an yeniden aşık olurum."
    )

    fun buildFallbackAcrostic(rawName: String): String {
        val clean = rawName.trim().uppercase(Locale("tr")).filter { it.isLetter() }
        if (clean.isEmpty()) return "Lütfen geçerli bir isim girin."
        val lines = clean.map { ch ->
            val verse = letterLines[ch] ?: "$ch harfiyle başlayan en güzel duygularım hep seninle,"
            "$ch — $verse"
        }
        return lines.joinToString("\n")
    }
}

// =========================================================================
// 6. ÖZEL GÜNLER GERİ SAYIMI & SEVGİ ROZETLERİ
// =========================================================================
data class SpecialCountdownEvent(
    val id: String,
    val title: String,
    val emoji: String,
    val targetTimestamp: Long
) {
    fun daysRemaining(): Long {
        val diff = targetTimestamp - System.currentTimeMillis()
        return if (diff > 0) (diff / (1000L * 60 * 60 * 24)) + 1 else 0L
    }
}

data class LoveBadge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean
)
