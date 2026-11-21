package com.example.data.model

enum class QuoteSection(val id: String, val title: String, val subtitle: String, val emoji: String) {
    ASK_SOZLERI(
        id = "ask",
        title = "Aşk Sözleri",
        subtitle = "Tutkulu, derin ve unutulmaz aşk cümleleri",
        emoji = "💖"
    ),
    ROMANTIK_SOZLER(
        id = "romantik",
        title = "Romantik Sözler",
        subtitle = "Ruhu okşayan, duygusal ve zarif iltifatlar",
        emoji = "✨"
    ),
    GUNAYDIN_MESAJLARI(
        id = "gunaydin",
        title = "Günaydın Mesajları",
        subtitle = "Güne sevgilinin tebessümüyle başlatan sıcacık sözler",
        emoji = "☀️"
    ),
    GECE_MESAJLARI(
        id = "gece",
        title = "İyi Geceler Mesajları",
        subtitle = "Huzurlu uykular ve tatlı rüyalar dileyen aşk fısıltıları",
        emoji = "🌙"
    )
}

data class LoveQuote(
    val id: String,
    val text: String,
    val author: String,
    val section: QuoteSection,
    val subCategory: String = "",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

object CuratedQuotes {
    // 1. AŞK SÖZLERİ (Tutkulu, edebi, derin aşk mısraları)
    val askSozleri = listOf(
        LoveQuote(
            id = "ask_1",
            text = "Sen kocaman çöllerde bir kalabalık gibisin, kocaman denizlerde ender bir balık gibisin. Bir bulut gibisin dikeyine yükselen, gökkuşağı gibisin çok renkli ve derin.",
            author = "Özdemir Asaf",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("Şiir", "Aşk", "ÖzdemirAsaf")
        ),
        LoveQuote(
            id = "ask_2",
            text = "Öyle bir seveceksin ki, yüreğinden kimse ayıramayacak. Ve öyle bir bağlanacaksın ki, onsuz bir gün bile geçirmek istemeyeceksin.",
            author = "Can Yücel",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Tutkulu Aşk",
            tags = listOf("Bağlılık", "Tutku", "CanYücel")
        ),
        LoveQuote(
            id = "ask_3",
            text = "Seni sevmek, dünyada yapılacak en güzel ibadet gibi; içim huzurla, kalbim senin adınla doluyor.",
            author = "Cemal Süreya",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("CemalSüreya", "Huzur", "Sevda")
        ),
        LoveQuote(
            id = "ask_4",
            text = "Tahir olmak da ayıp değil Zühre olmak da, hattâ sevda yüzünden ölmek de ayıp değil. Seversin dünyayı doludizgin ama o bunun farkında değildir.",
            author = "Nâzım Hikmet",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("NazımHikmet", "Sevda", "Edebiyat")
        ),
        LoveQuote(
            id = "ask_5",
            text = "Aşk sandığın kadar değil, yandığın kadardır. Yanmayan ne bilsin vuslatı, sevdayı...",
            author = "Mevlana",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Derin Aşk",
            tags = listOf("Mevlana", "Tasavvuf", "Aşk")
        ),
        LoveQuote(
            id = "ask_6",
            text = "Seni ne kadar sevdiğimi bir bilseydin; gökyüzündeki yıldızları teker teker saçlarına taç yapardım.",
            author = "Ahmed Arif",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Şiirsel",
            tags = listOf("AhmedArif", "Yıldızlar", "Hasret")
        ),
        LoveQuote(
            id = "ask_7",
            text = "Sen benim yarım kalmış şiirlerimin en görkemli dizesi, ömrümün en hakiki sevdasısın.",
            author = "Attila İlhan",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Tutkulu Aşk",
            tags = listOf("Attilaİlhan", "Mısra", "Sevda")
        ),
        LoveQuote(
            id = "ask_8",
            text = "Birbirimize sarıldığımızda dünyanın bütün gürültüsü susuyor, sadece kalplerimizin aynı ritimde atışı kalıyor.",
            author = "Romantik İtiraf",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Kalp Kalbe",
            tags = listOf("Sarılmak", "Huzur", "Kalp")
        ),
        LoveQuote(
            id = "ask_9",
            text = "Seni her düşündüğümde kalbime bir gül dikiyorum; bilsen şimdi içimde koca bir gül bahçesi var.",
            author = "Aşk Fısıltısı",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Derin Aşk",
            tags = listOf("GülBahçesi", "Hayal", "Aşk")
        ),
        LoveQuote(
            id = "ask_10",
            text = "Gözlerin gözlerime değdiği an, zamanın durduğunu ve evrenin sadece bizim için döndüğünü hissettim.",
            author = "İlk Görüşte Aşk",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "İtiraf",
            tags = listOf("Gözler", "Zaman", "Büyü")
        ),
        LoveQuote(
            id = "ask_11",
            text = "Ben seninle nefes almayı, seninle yaşamayı ve senin kollarında yaşlanmayı sevdim sevgilim.",
            author = "Ömürlük Sevda",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Ömürlük",
            tags = listOf("Ömürlük", "Sadakat", "Aşk")
        ),
        LoveQuote(
            id = "ask_12",
            text = "Aşk, iki yalnızlığın birbirini koruması, birbirine dokunması ve birbirini selamlamasıdır.",
            author = "Rainer Maria Rilke",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Felsefi",
            tags = listOf("Rilke", "Yalnızlık", "Sevgi")
        ),
        LoveQuote(
            id = "ask_13",
            text = "Terketmedi sevdan beni, aç kaldım, susuz kaldım, hayın, karanlıktı gece. Can garip, can suskun, can paramparça... Ve ellerim kelepçede, tütünsüz, uykusuz kaldım, terketmedi sevdan beni.",
            author = "Ahmed Arif",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("AhmedArif", "Tutku", "Şiir")
        ),
        LoveQuote(
            id = "ask_14",
            text = "Seninle el ele verip bütün kainatı unutmak, sadece göğe bakmak istiyorum. İkimiz birden sevinebiliriz göğe bakalım.",
            author = "Turgut Uyar",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("TurgutUyar", "GöğeBakalım", "Aşk")
        ),
        LoveQuote(
            id = "ask_15",
            text = "Sen benim sarhoşluğumsun, ne ayıldım, ne ayılabilirim, ne ayılmak isterim. Başım ağır, dizlerim parçalanmış, üstüm başım çamur içinde, yanına koşuyorum.",
            author = "Nâzım Hikmet",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Edebi Şiir",
            tags = listOf("NazımHikmet", "Sarhoşluk", "Aşk")
        ),
        LoveQuote(
            id = "ask_16",
            text = "Ömrümün kalan bütün günlerini, her sabah senin gözlerinde uyanarak ve her akşam senin omzunda huzur bularak geçirmek istiyorum. Benimle evlenir misin?",
            author = "Evlilik Teklifi",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Evlilik & Teklif",
            tags = listOf("Evlilik", "Teklif", "Ömürlük")
        ),
        LoveQuote(
            id = "ask_17",
            text = "Aramızdaki yüzlerce kilometre sadece haritalar içindir. Kalbimin her atışında senin adın yankılanıyor sevgilim.",
            author = "Uzak Mesafe Aşkı",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Uzun Mesafe",
            tags = listOf("Mesafe", "Özlem", "Sadakat")
        ),
        LoveQuote(
            id = "ask_18",
            text = "Seni sevmekten başka hiçbir işe yaramayan, aklı fikri sadece sende olan tembel ama aşık bir kalbim var!",
            author = "Tatlı Aşk",
            section = QuoteSection.ASK_SOZLERI,
            subCategory = "Sevimli & Neşeli",
            tags = listOf("Tatlı", "Gülümseme", "Aşk")
        )
    )

    // 2. ROMANTİK SÖZLER (Zarif, duygusal, iltifat dolu romantik ifadeler)
    val romantikSozler = listOf(
        LoveQuote(
            id = "rom_1",
            text = "Gözlerinin kahvesinden koy ömrüme, kırk yılın hatırına sen kalayım.",
            author = "Zarif İltifat",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "İltifat",
            tags = listOf("Kahve", "Ömürlük", "Zarafet")
        ),
        LoveQuote(
            id = "rom_2",
            text = "Ben senin en çok bana yansıyan halini sevdim. Yanımdayken yüzünde açan o dingin tebessümü sevdim.",
            author = "Edip Cansever",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Duygusal",
            tags = listOf("Tebessüm", "Huzur", "Zarafet")
        ),
        LoveQuote(
            id = "rom_3",
            text = "Senin yanındayken dünya o kadar güzelleşiyor ki, en sıradan sokaklar bile cennet bahçesine dönüşüyor.",
            author = "Romantik İtiraf",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Büyüleyici",
            tags = listOf("Cennet", "Güzellik", "Huzur")
        ),
        LoveQuote(
            id = "rom_4",
            text = "Sesin kulağıma geldiğinde, göğsümde koca bir orkestranın en zarif kemanı çalmaya başlıyor.",
            author = "Müzikal Aşk",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "İltifat",
            tags = listOf("Sesin", "Melodi", "Zarafet")
        ),
        LoveQuote(
            id = "rom_5",
            text = "Ellerini tuttuğumda bütün kışlar bahara dönüyor, içimde rengarenk çiçekler açıyor sevgilim.",
            author = "Romantik Esinti",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Duygusal",
            tags = listOf("Ellerin", "Bahar", "Çiçekler")
        ),
        LoveQuote(
            id = "rom_6",
            text = "Dünyanın en güzel manzarası hangisi diye sorsalar, senin gülerken kısılan gözlerin derim.",
            author = "En Güzel Manzara",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "İltifat",
            tags = listOf("Gözlerin", "Gülüşün", "Manzara")
        ),
        LoveQuote(
            id = "rom_7",
            text = "Bütün mevsimleri seninle yaşamak istiyorum; baharda çiçek kokunu, kışın sıcacık avuçlarını.",
            author = "Mevsimler Boyu",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Birliktelik",
            tags = listOf("Mevsimler", "Gelecek", "Romantizm")
        ),
        LoveQuote(
            id = "rom_8",
            text = "Sen benim en çok sakındığım duam, kabul olmuş en güzel mucizemsin.",
            author = "Kalpten Dua",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Mucize",
            tags = listOf("Dua", "Mucize", "Şükür")
        ),
        LoveQuote(
            id = "rom_9",
            text = "Kokun sinmiş yastığıma, nereye dönsem sen varsın; iyi ki benimsin, iyi ki yanımdasın.",
            author = "Sevgi Bağı",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Duygusal",
            tags = listOf("Kokun", "Sevgi", "Varlığın")
        ),
        LoveQuote(
            id = "rom_10",
            text = "Seninle geçen her an, hayatımın en değerli hazinesi; kalbimin tek sahibisin.",
            author = "Aşk İncisi",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Değerli",
            tags = listOf("Hazine", "Kalbim", "Aşk")
        ),
        LoveQuote(
            id = "rom_11",
            text = "Bir kadının en güzel makyajı, sevdiği adamın ona hissettirdiği mutluluk ve dudaklarındaki tebessümdür.",
            author = "Zarif Aşk",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Zarafet",
            tags = listOf("Tebessüm", "Mutluluk", "Zarafet")
        ),
        LoveQuote(
            id = "rom_12",
            text = "Sen bana 'nasılsın' diye sorma sevgilim; sesini duyduğum an içimdeki bütün dertler eriyip bahara dönüyor.",
            author = "Şifa Kaynağım",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Duygusal",
            tags = listOf("Sesin", "Huzur", "Bahar")
        ),
        LoveQuote(
            id = "rom_13",
            text = "Sana sımsıkı sarıldığımda evime varmış gibi hissediyorum. Benim dünyadaki tek yuvam senin kalbin ve kollarındır.",
            author = "Güvenli Liman",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Huzurlu Yuva",
            tags = listOf("Sarılmak", "Yuva", "Güven")
        ),
        LoveQuote(
            id = "rom_14",
            text = "Eğer bir hatamla kalbini incittiysem özür dilerim birtanem. Bir tebessümünle dünyamı yeniden aydınlat, barışalım.",
            author = "Barışma Fısıltısı",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Özür & Barışma",
            tags = listOf("Özür", "Barışma", "Aşk")
        )
    )

    // 3. GÜNAYDIN MESAJLARI (Sabah sevgilisine gönderilecek tatlı, enerjik, romantik mesajlar)
    val gunaydinMesajlari = listOf(
        LoveQuote(
            id = "gun_1",
            text = "Günaydın ruhumun eşi, kalbimin en tatlı melodisi. Yeni doğan güneş senin gülüşünün yanında sönük kalır.",
            author = "Güne Başlarken",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Romantik Günaydın",
            tags = listOf("Günaydın", "Sabah", "Gülüşün")
        ),
        LoveQuote(
            id = "gun_2",
            text = "Gözlerimi açtığım an aklıma düşen ilk güzellik sensin. Günün senin kalbin gibi aydınlık ve huzurlu geçsin sevgilim.",
            author = "Aşk Uyanışı",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Huzurlu Sabah",
            tags = listOf("Huzur", "Aydınlık", "Günaydın")
        ),
        LoveQuote(
            id = "gun_3",
            text = "Günaydın biriciğim! Bugün de kalbimin her atışında senin adın, dualarımda senin mutluluğun var.",
            author = "Sabah Duası",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Duygusal Günaydın",
            tags = listOf("Kalp", "Mutluluk", "Sabah")
        ),
        LoveQuote(
            id = "gun_4",
            text = "Kahvemin kokusunda değil, senin varlığında uyanıyorum her sabah hayata. Günaydın ömrümün neşesi!",
            author = "Sabah Kahvesi",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Tatlı Günaydın",
            tags = listOf("Kahve", "Neşe", "Günaydın")
        ),
        LoveQuote(
            id = "gun_5",
            text = "Güneş yeryüzünü aydınlatırken, senin sevgin benim tüm karanlıklarımı aydınlatıyor. Musmutlu bir gün olsun birtanem.",
            author = "Güneş Işığım",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Işıltılı Sabah",
            tags = listOf("Güneş", "Aydınlık", "Birtanem")
        ),
        LoveQuote(
            id = "gun_6",
            text = "Dünyanın en şanslı insanıyım çünkü her sabah senin sevginle uyanıyorum. Günaydın en güzel rüyam!",
            author = "Şans Meleğim",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Romantik Günaydın",
            tags = listOf("Şans", "Rüya", "Günaydın")
        ),
        LoveQuote(
            id = "gun_7",
            text = "Uyan sevgilim, senin gülüşün olmadan bu şehir uyanamaz; günün güzelliklerle dolsun.",
            author = "Sabah Teveccühü",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Tatlı Günaydın",
            tags = listOf("Gülüşün", "Şehir", "Günaydın")
        ),
        LoveQuote(
            id = "gun_8",
            text = "Bugün kalbimin pencereleri ardına kadar sana açık. Harika bir gün geçirmen dileğiyle günaydın balım!",
            author = "Pencere Önü Çiçeğim",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Neşeli Günaydın",
            tags = listOf("Pencere", "Neşe", "Günaydın")
        ),
        LoveQuote(
            id = "gun_9",
            text = "Seni düşünerek uyandığım her sabah, ömrüme taptaze bir bahar gibi geliyor. Günaydın canözüm.",
            author = "Bahar Sabahı",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Bahar Sabahı",
            tags = listOf("Bahar", "Canözüm", "Günaydın")
        ),
        LoveQuote(
            id = "gun_10",
            text = "Yüzünden tebessümün, kalbinden sevgim hiç eksik olmasın. Günün en az senin kadar tatlı geçsin sevgilim, günaydın!",
            author = "Tatlı Dilekler",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Tatlı Günaydın",
            tags = listOf("Tebessüm", "Tatlı", "Günaydın")
        )
    )

    // 4. İYİ GECELER / GECE MESAJLARI (Huzurlu, rüya gibi, aşk dolu gece mesajları)
    val geceMesajlari = listOf(
        LoveQuote(
            id = "gece_1",
            text = "İyi geceler ömrüm. Rüyalarım seninle başlasın, sabahım yine senin kokunla aydınlansın.",
            author = "Gece Fısıltısı",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Huzurlu Gece",
            tags = listOf("İyiGeceler", "Rüya", "Huzur")
        ),
        LoveQuote(
            id = "gece_2",
            text = "Gökyüzündeki her bir yıldıza senin adını fısıldadım bu gece. Uyu sevgilim, melekler ve dualarım seni beklesin.",
            author = "Yıldızlı Gece",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Yıldızlı Rüyalar",
            tags = listOf("Yıldızlar", "Dua", "İyiGeceler")
        ),
        LoveQuote(
            id = "gece_3",
            text = "Gözlerini kapattığında kalbin kalbime emanet olsun. En tatlı rüyaların başrolü sen ol bu gece, iyi geceler.",
            author = "Emanet Kalbim",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Romantik Gece",
            tags = listOf("Kalbim", "Emanet", "İyiGeceler")
        ),
        LoveQuote(
            id = "gece_4",
            text = "Mesafeler bedenleri ayırsa da, aynı aya bakıp birbirimizi hissettiğimiz müddetçe her gece vuslattır. İyi geceler canım.",
            author = "Uzak Mesafe Gecesi",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Özlem Gecesi",
            tags = listOf("AyIşığı", "Vuslat", "Mesafeler")
        ),
        LoveQuote(
            id = "gece_5",
            text = "Gece olup sessizlik çöktüğünde içimdeki tek ses senin adın oluyor. Huzurla uyu ruhumun eşi, iyi uykular.",
            author = "Gece Sessizliği",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Huzurlu Gece",
            tags = listOf("Sessizlik", "Huzur", "RuhumunEşi")
        ),
        LoveQuote(
            id = "gece_6",
            text = "Karanlık geceyi aydınlatan ay gibi, hayatımın en karanlık anlarını aydınlatan ışığımsın. Tatlı rüyalar sevgilim.",
            author = "Ay Işığım",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Romantik Gece",
            tags = listOf("AyIşığı", "Aydınlık", "İyiGeceler")
        ),
        LoveQuote(
            id = "gece_7",
            text = "Seninle sonlanan her günüm bir şükür, seninle başlayacak her yarınım ise bir umut. Huzur dolu geceler birtanem.",
            author = "Şükür Sebebim",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Huzurlu Gece",
            tags = listOf("Şükür", "Umut", "İyiGeceler")
        ),
        LoveQuote(
            id = "gece_8",
            text = "Rüyanda yanına sokulup saçlarını okşayan bir esinti hissedersen bil ki o benim özlemimdir. İyi geceler meleğim.",
            author = "Özlem Rüzgarı",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Duygusal Gece",
            tags = listOf("Özlem", "Rüzgar", "Meleğim")
        ),
        LoveQuote(
            id = "gece_9",
            text = "Yastığa başını koyduğunda tüm yorgunluğun uçup gitsin, yerine kalbimin sıcaklığı gelsin. İyi uykular sevgilim.",
            author = "Sıcak Gece",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Sıcak Uykular",
            tags = listOf("Yastık", "Sıcaklık", "İyiGeceler")
        ),
        LoveQuote(
            id = "gece_10",
            text = "Gözlerin gözlerime uzak olsa da, kalbim her nefeste senin için çarpıyor. Gecen huzurla dolsun ömrüm.",
            author = "Gece Duası",
            section = QuoteSection.GECE_MESAJLARI,
            subCategory = "Romantik Gece",
            tags = listOf("Nefes", "Huzur", "İyiGeceler")
        )
    )

    // All quotes flattened for search or global favorites
    val quotes = askSozleri + romantikSozler + gunaydinMesajlari + geceMesajlari

    // Sabah motivasyonel & romantik bildirim sözleri
    val morningMotivationalQuotes = listOf(
        LoveQuote(
            id = "sabah_mot_1",
            text = "Yeni doğan her güneş, hayallerine ve kalbindeki sevgiye atılan taze bir adımdır. Bugün gülümsemek ve sevmek için harika bir sabah!",
            author = "Sabah İlhamı",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Sabah Motivasyonu",
            tags = listOf("Motivasyon", "Sabah", "Umut")
        ),
        LoveQuote(
            id = "sabah_mot_2",
            text = "Kalbinde iyilik ve sevgi taşıyan insanın yolu asla karanlıkta kalmaz. Bugün ışığını paylaşmaktan çekinme!",
            author = "Günün Motivasyonu",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Pozitif Başlangıç",
            tags = listOf("Işık", "Motivasyon", "Sevgi")
        ),
        LoveQuote(
            id = "sabah_mot_3",
            text = "Dünün yorgunluğunu geride bırak; bugün hem kendi kalbine hem de sevdiklerinin gününe umut olma vakti. Günaydın!",
            author = "Yeni Başlangıç",
            section = QuoteSection.GUNAYDIN_MESAJLARI,
            subCategory = "Sabah Motivasyonu",
            tags = listOf("Günaydın", "Umut", "Güç")
        ),
        LoveQuote(
            id = "sabah_mot_4",
            text = "Küçük bir tebessüm ve içten bir 'seni seviyorum', bazen bir insanın bütün gününü güzelleştirmeye yeter.",
            author = "Sevgi Notu",
            section = QuoteSection.ROMANTIK_SOZLER,
            subCategory = "Sabah Romantizmi",
            tags = listOf("Tebessüm", "Romantik", "Sabah")
        )
    )

    fun pickMorningQuote(tonePreference: String = "MIXED"): LoveQuote {
        val romanticPool = gunaydinMesajlari + askSozleri + romantikSozler
        return when (tonePreference) {
            "MOTIVATIONAL" -> morningMotivationalQuotes.random()
            "ROMANTIC" -> romanticPool.random()
            else -> (morningMotivationalQuotes + gunaydinMesajlari + romantikSozler).random()
        }
    }

    fun findById(quoteId: String): LoveQuote? {
        return (quotes + morningMotivationalQuotes).find { it.id == quoteId }
    }
}
