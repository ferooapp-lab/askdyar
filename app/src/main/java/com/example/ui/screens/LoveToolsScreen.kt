package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.LoveQuotesViewModel

@Composable
fun LoveToolsScreen(
    viewModel: LoveQuotesViewModel,
    onNavigateToStudio: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onOpenMusicSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val counterInfo by viewModel.loveCounterInfo.collectAsState()
    val mission by viewModel.currentMission.collectAsState()
    val compatibilityResult by viewModel.compatibilityResult.collectAsState()
    val isAmbientPlaying by viewModel.isAmbientPlaying.collectAsState()
    val currentTrackTitle by viewModel.currentMusicTrackTitle.collectAsState()

    val coupons by viewModel.loveCoupons.collectAsState()
    val bucketList by viewModel.bucketList.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val zodiac1 by viewModel.selectedZodiac1.collectAsState()
    val zodiac2 by viewModel.selectedZodiac2.collectAsState()
    val zodiacResult by viewModel.zodiacResult.collectAsState()
    val letterTemplate by viewModel.selectedLetterTemplate.collectAsState()
    val partnerName by viewModel.letterPartnerName.collectAsState()
    val myName by viewModel.letterMyName.collectAsState()
    val trait by viewModel.letterTrait.collectAsState()
    val letterText by viewModel.generatedLetter.collectAsState()
    val ourSong by viewModel.ourSong.collectAsState()

    val loveStreak by viewModel.loveStreak.collectAsState()
    val savedLoveLanguage by viewModel.savedLoveLanguage.collectAsState()
    val specialCountdowns by viewModel.specialCountdowns.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val quotesWithFavorites by viewModel.quotesWithFavorites.collectAsState()
    val savedFavoriteQuotes by viewModel.savedFavoriteQuotes.collectAsState()

    val missYouTotal by viewModel.missYouTotalCount.collectAsState()
    val missYouToday by viewModel.missYouTodayCount.collectAsState()
    val loveJarReasons by viewModel.loveJarReasons.collectAsState()
    val timeCapsules by viewModel.timeCapsules.collectAsState()
    val truthDareCount by viewModel.truthDareCompletedCount.collectAsState()

    var activeSection by rememberSaveable { mutableStateOf<LoveSpaceSection?>(null) }
    var showEditCounterDialog by remember { mutableStateOf(false) }

    val totalFavsCount = remember(quotesWithFavorites, allNotes, savedFavoriteQuotes) {
        quotesWithFavorites.count { it.isFavorite } + allNotes.count { it.isFavorite } + savedFavoriteQuotes.size
    }

    BackHandler(enabled = activeSection != null) {
        activeSection = null
    }

    AnimatedContent(
        targetState = activeSection,
        modifier = modifier
            .fillMaxSize()
            .testTag("love_tools_screen"),
        transitionSpec = {
            if (targetState != null) {
                (slideInHorizontally { it / 5 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 6 } + fadeOut())
            } else {
                (slideInHorizontally { -it / 5 } + fadeIn()) togetherWith
                    (slideOutHorizontally { it / 6 } + fadeOut())
            }
        },
        label = "loveSpaceSectionTransition"
    ) { selectedSection ->
        if (selectedSection == null) {
            // Sleek Overview Hub: Hero + Quick Pulse + 5 Structured Sub-Sections
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LoveSpaceHeroCard(
                        counterInfo = counterInfo,
                        loveStreak = loveStreak,
                        missYouTotal = missYouTotal,
                        capsulesCount = timeCapsules.size,
                        jarCount = loveJarReasons.size,
                        favoritesCount = totalFavsCount,
                        onEditCounter = { showEditCounterDialog = true },
                        onNavigateToFavorites = onNavigateToFavorites
                    )
                }

                item {
                    QuickMissYouPulseBar(
                        partnerName = counterInfo.partnerName,
                        todayCount = missYouToday,
                        onSendPing = { viewModel.sendMissYouPing() }
                    )
                }

                item {
                    LoveSpaceDirectoryGrid(
                        onSelectSection = { section -> activeSection = section }
                    )
                }
            }
        } else {
            // Focused Sub-Section Screen with Sticky Header + Quick Pill Switcher
            Column(modifier = Modifier.fillMaxSize()) {
                LoveSpaceSubSectionHeaderBar(
                    activeSection = selectedSection,
                    onBackToHub = { activeSection = null },
                    onSelectSection = { section -> activeSection = section }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedSection) {
                        LoveSpaceSection.BIZ_VE_SAYAC -> {
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "1",
                                    title = "Birliktelik Sayacı",
                                    subtitle = "Birlikte geçen gün, saat ve dakika"
                                )
                            }
                            item {
                                LoveCounterCard(
                                    counterInfo = counterInfo,
                                    onEditClick = { showEditCounterDialog = true }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "2",
                                    title = "Kilometre Taşları",
                                    subtitle = "Aşk yolculuğunuzdaki özel duraklar"
                                )
                            }
                            item {
                                MilestonesTimelineCard(currentDays = counterInfo.calculateDays())
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "3",
                                    title = "Özel Günler & Sevgi Rozetleri",
                                    subtitle = "Yaklaşan yıldönümleri ve başarı rozetleri"
                                )
                            }
                            item {
                                SpecialCountdownsAndBadgesSection(
                                    streakDays = loveStreak,
                                    notesCount = allNotes.size,
                                    favoritesCount = totalFavsCount,
                                    countdowns = specialCountdowns,
                                    onAddCountdown = { t, e, d -> viewModel.addSpecialCountdown(t, e, d) },
                                    onDeleteCountdown = { id -> viewModel.deleteSpecialCountdown(id) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "4",
                                    title = "\"Seni Özledim\" Canlı Kalp",
                                    subtitle = "Partnerine anlık sevgi sinyali gönder"
                                )
                            }
                            item {
                                MissYouLiveCard(
                                    partnerName = counterInfo.partnerName,
                                    totalCount = missYouTotal,
                                    todayCount = missYouToday,
                                    onSendPing = { viewModel.sendMissYouPing() }
                                )
                            }
                        }

                        LoveSpaceSection.OYUNLAR_VE_CARK -> {
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "1",
                                    title = "Randevu Karar Çarkı",
                                    subtitle = "Bugün ne yapsak derdine eğlenceli çözüm"
                                )
                            }
                            item {
                                DateNightWheelSection()
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "2",
                                    title = "Çiftler İçin Aşk Zarı",
                                    subtitle = "Sürpriz eylem ve yer kombinasyonu"
                                )
                            }
                            item {
                                RomanticDiceSection()
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "3",
                                    title = "Doğruluk mu Cesaret mi",
                                    subtitle = "İlişkiye heyecan katan romantik görevler"
                                )
                            }
                            item {
                                TruthOrDareCard(
                                    completedCount = truthDareCount,
                                    onCompleteChallenge = { viewModel.completeTruthOrDare() }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "4",
                                    title = "2 Kişilik Çift Quizi",
                                    subtitle = "Birbirinizi ne kadar iyi tanıyorsunuz?"
                                )
                            }
                            item {
                                CoupleTriviaQuizCard()
                            }
                        }

                        LoveSpaceSection.MEKTUP_VE_KAPSUL -> {
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "1",
                                    title = "Aşk Kavanozu",
                                    subtitle = "Seni sevmemin nedenleri bir arada"
                                )
                            }
                            item {
                                LoveJarCard(
                                    reasons = loveJarReasons,
                                    onAddReason = { viewModel.addLoveJarReason(it) },
                                    onSaveToFavorites = { reason ->
                                        viewModel.saveToFavoritesLibrary(
                                            title = "Aşk Kavanozu Notu",
                                            text = reason,
                                            author = counterInfo.myName,
                                            category = "Aşk Kavanozu",
                                            sourceLabel = "Aşk Kavanozu"
                                        )
                                    }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "2",
                                    title = "Geleceğe Zaman Kapsülü",
                                    subtitle = "İleri bir tarihte açılacak gizli anılar"
                                )
                            }
                            item {
                                TimeCapsuleCard(
                                    capsules = timeCapsules,
                                    onCreateCapsule = { title, msg, author, delayMs ->
                                        viewModel.createTimeCapsule(title, msg, author, delayMs)
                                    },
                                    onDeleteCapsule = { id -> viewModel.deleteTimeCapsule(id) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "3",
                                    title = "Aşk Mektubu Sihirbazı",
                                    subtitle = "Duygularını özel bir mektuba dök"
                                )
                            }
                            item {
                                LoveLetterWizardSection(
                                    selectedTemplate = letterTemplate,
                                    partnerName = partnerName,
                                    myName = myName,
                                    trait = trait,
                                    letterText = letterText,
                                    onUpdateLetter = { t, m, p, tr -> viewModel.updateLoveLetter(t, m, p, tr) },
                                    onSaveToNotes = { text ->
                                        viewModel.saveNote(
                                            title = "Özel Aşk Mektubum",
                                            content = text,
                                            author = myName,
                                            tags = "Mektup, Özel"
                                        )
                                    }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "4",
                                    title = "Hediye & Sürpriz Bulucu",
                                    subtitle = "Partnerine özel yaratıcı sürpriz fikirleri"
                                )
                            }
                            item {
                                GiftAndSurpriseFinderCard(
                                    onSaveIdeaToNotes = { title, desc ->
                                        viewModel.saveNote(
                                            title = "Sürpriz Fikri: $title",
                                            content = desc,
                                            author = "Aşk Alanı",
                                            tags = "Sürpriz, Hediye"
                                        )
                                    }
                                )
                            }
                        }

                        LoveSpaceSection.UYUM_VE_HUZUR -> {
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "1",
                                    title = "Aşk & İsim Uyumluluk Ölçer",
                                    subtitle = "İsimlerinizin enerjisini keşfet"
                                )
                            }
                            item {
                                CompatibilityTestCard(
                                    compatibilityResult = compatibilityResult,
                                    onCalculate = { n1, n2 -> viewModel.calculateLoveCompatibility(n1, n2) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "2",
                                    title = "Burç Uyumu Rehberi",
                                    subtitle = "Yıldızların ilişkiniz hakkındaki yorumu"
                                )
                            }
                            item {
                                ZodiacCompatibilitySection(
                                    selectedZodiac1 = zodiac1,
                                    selectedZodiac2 = zodiac2,
                                    result = zodiacResult,
                                    onSelectSigns = { s1, s2 -> viewModel.updateZodiacSigns(s1, s2) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "3",
                                    title = "5 Sevgi Dili Keşif Testi",
                                    subtitle = "Sevgiyi nasıl alıp verdiğini öğren"
                                )
                            }
                            item {
                                LoveLanguageQuizSection(
                                    savedLanguage = savedLoveLanguage,
                                    onSaveResult = { viewModel.saveLoveLanguageResult(it) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "4",
                                    title = "Çift Nefes Senkronizasyonu",
                                    subtitle = "Birlikte sakinleşin ve kalp ritminizi eşitleyin"
                                )
                            }
                            item {
                                CoupleBreathingSection()
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "5",
                                    title = "Müzik & Bizim Şarkımız",
                                    subtitle = "Romantik fon müziği ve ilişkinizin şarkısı"
                                )
                            }
                            item {
                                AmbientMusicToggleCard(
                                    isAmbientPlaying = isAmbientPlaying,
                                    onToggleAmbientMusic = { viewModel.toggleAmbientMusic() },
                                    currentTrackTitle = currentTrackTitle,
                                    onOpenMusicStudio = onOpenMusicSheet
                                )
                            }
                            item {
                                OurSongCard(
                                    ourSong = ourSong,
                                    onSaveSong = { title, artist -> viewModel.updateOurSong(title, artist) }
                                )
                            }
                        }

                        LoveSpaceSection.SOZ_KUPON_VE_GOREV -> {
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "1",
                                    title = "1.200+ Sonsuz Söz Motoru",
                                    subtitle = "Moduna göre anında romantik söz üret"
                                )
                            }
                            item {
                                InfiniteQuoteEngineSection(
                                    onSaveToNotes = { moodTitle, text ->
                                        viewModel.saveNote(
                                            title = "Özel Üretim: $moodTitle",
                                            content = text,
                                            author = "Sonsuz Söz Motoru",
                                            tags = "$moodTitle, Romantik"
                                        )
                                    },
                                    onSaveToFavorites = { moodTitle, text, author ->
                                        viewModel.saveToFavoritesLibrary(
                                            title = "Özel Üretim: $moodTitle",
                                            text = text,
                                            author = author,
                                            category = moodTitle,
                                            sourceLabel = "Söz Motoru"
                                        ) { _, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onOpenInStudio = { text, author ->
                                        viewModel.loadCustomQuoteIntoStudio(text, author)
                                        onNavigateToStudio()
                                    }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "2",
                                    title = "Günlük Aşk Görevi",
                                    subtitle = "Bugün partnerini mutlu edecek küçük bir adım"
                                )
                            }
                            item {
                                DailyMissionCard(
                                    mission = mission,
                                    onPickRandomMission = { viewModel.pickRandomMission() }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "3",
                                    title = "Aşk Kuponları",
                                    subtitle = "Partnerine hediye edebileceğin özel kuponlar"
                                )
                            }
                            item {
                                LoveCouponsSection(
                                    coupons = coupons,
                                    onToggleRedeem = { viewModel.toggleCouponRedeemed(it) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "4",
                                    title = "Çift Kova Listesi",
                                    subtitle = "Birlikte gerçekleştireceğiniz hayaller"
                                )
                            }
                            item {
                                CoupleBucketListSection(
                                    items = bucketList,
                                    onToggleDone = { viewModel.toggleBucketItem(it) }
                                )
                            }
                            item {
                                LoveSubSectionItemHeader(
                                    stepNumber = "5",
                                    title = "Günün Çift Sorusu",
                                    subtitle = "Derin ve anlamlı sohbetler başlat"
                                )
                            }
                            item {
                                CoupleQuestionCard(
                                    question = currentQuestion,
                                    onNextQuestion = { viewModel.pickNextQuestion() },
                                    onSaveToNotes = { answer ->
                                        viewModel.saveNote(
                                            title = "Günün Çift Sorusu Notu",
                                            content = answer,
                                            author = "Biz",
                                            tags = "Soru, Hatıra"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditCounterDialog) {
        EditLoveCounterDialog(
            counterInfo = counterInfo,
            onDismiss = { showEditCounterDialog = false },
            onSave = { myNameIn, partnerNameIn, startTimeMs, titleIn ->
                viewModel.updateLoveCounter(myNameIn, partnerNameIn, startTimeMs, titleIn)
                showEditCounterDialog = false
            }
        )
    }
}
