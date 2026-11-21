package com.example

import com.example.data.model.CardStyleOptions
import com.example.data.model.CompatibilityCalculator
import com.example.data.model.CuratedQuotes
import com.example.data.model.FilterPreset
import com.example.data.model.LoveCounterInfo
import com.example.data.model.QuoteSection
import com.example.data.model.RomanticMissions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSeparatedQuoteSections() {
        assertTrue("Aşk sözleri listesi dolu olmalı", CuratedQuotes.askSozleri.isNotEmpty())
        assertTrue("Romantik sözler listesi dolu olmalı", CuratedQuotes.romantikSozler.isNotEmpty())
        assertTrue("Günaydın mesajları listesi dolu olmalı", CuratedQuotes.gunaydinMesajlari.isNotEmpty())
        assertTrue("Gece mesajları listesi dolu olmalı", CuratedQuotes.geceMesajlari.isNotEmpty())
        assertTrue("Tüm sözler birleşik listesi dolu olmalı", CuratedQuotes.quotes.isNotEmpty())

        assertEquals(4, QuoteSection.values().size)
    }

    @Test
    fun testLoveCounterCalculation() {
        val counter = LoveCounterInfo(
            myName = "Ferhat",
            partnerName = "Canan",
            startTimestamp = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 10)
        )
        assertTrue(counter.calculateDays() >= 10)
    }

    @Test
    fun testCompatibilityCalculator() {
        val result = CompatibilityCalculator.calculate("Ferhat", "Canan")
        assertTrue(result.scorePercentage in 80..100)
        assertNotNull(result.statusTitle)
    }

    @Test
    fun testRomanticMissions() {
        assertTrue(RomanticMissions.missions.isNotEmpty())
    }

    @Test
    fun testThemePresetsAvailable() {
        assertTrue("Card theme presets should be available", CardStyleOptions.themePresets.isNotEmpty())
        val rubyPreset = CardStyleOptions.themePresets.find { it.id == "ruby_velvet" }
        assertNotNull("Ruby velvet theme should exist", rubyPreset)
    }

    @Test
    fun testFilterPresets() {
        assertEquals("normal", FilterPreset.NORMAL.id)
        assertEquals("warm_glow", FilterPreset.WARM_GLOW.id)
    }

    @Test
    fun testLoveCoupons() {
        val coupons = com.example.data.model.DefaultLoveCoupons.list
        assertTrue("En az 5 kupon olmalı", coupons.size >= 5)
        val first = coupons.first()
        assertNotNull(first.title)
        assertNotNull(first.emoji)
    }

    @Test
    fun testCoupleBucketList() {
        val list = com.example.data.model.DefaultCoupleBucketList.list
        assertTrue("Kova listesi dolu olmalı", list.size >= 5)
    }

    @Test
    fun testCoupleQuestions() {
        val questions = com.example.data.model.DefaultCoupleQuestions.questions
        assertTrue("Sorular dolu olmalı", questions.size >= 8)
    }

    @Test
    fun testZodiacCompatibility() {
        val sign1 = com.example.data.model.ZodiacData.signs[0] // Koç
        val sign2 = com.example.data.model.ZodiacData.signs[1] // Boğa
        val res = com.example.data.model.ZodiacData.calculateZodiacMatch(sign1, sign2)
        assertTrue(res.scorePercentage in 80..100)
        assertNotNull(res.statusTitle)
    }

    @Test
    fun testLoveLetterBuilder() {
        val t = com.example.data.model.LoveLetterTemplates.templates[0]
        val letter = com.example.data.model.LoveLetterTemplates.buildLetter(t, "Ali", "Ayşe", "gözlerine")
        assertTrue(letter.contains("Ayşe"))
        assertTrue(letter.contains("Ali"))
        assertTrue(letter.contains("gözlerine"))
    }

    @Test
    fun testAiQuoteGeneration() = kotlinx.coroutines.runBlocking {
        val aiRepo = com.example.data.repository.AiRepositoryImpl()
        val resSad = aiRepo.generateQuote("sad love")
        assertTrue(resSad.isSuccess)
        val quoteSad = resSad.getOrNull()
        assertNotNull(quoteSad)
        assertTrue(quoteSad!!.text.isNotBlank())
        assertTrue(quoteSad.isSimulated)

        val resMorning = aiRepo.generateQuote("morning joy")
        assertTrue(resMorning.isSuccess)
        val quoteMorning = resMorning.getOrNull()
        assertNotNull(quoteMorning)
        assertTrue(quoteMorning!!.text.isNotBlank())
    }

    @Test
    fun testAiMoodTrendsAnalysis() = kotlinx.coroutines.runBlocking {
        val aiRepo = com.example.data.repository.AiRepositoryImpl()
        val res = aiRepo.analyzeMoodTrends(emptyList())
        assertTrue(res.isSuccess)
        val analysis = res.getOrNull()
        assertNotNull(analysis)
        assertEquals(7, analysis!!.days.size)
        assertTrue(analysis.breakdowns.isNotEmpty())
        assertTrue(analysis.overallScore in 50..100)
        assertTrue(analysis.summaryInsight.isNotBlank())
        assertTrue(analysis.advice.isNotBlank())
    }

    @Test
    fun testRomanticUniverse1200Variations() {
        assertTrue(
            "En az 1000 benzersiz kombinasyon olmalı",
            com.example.data.model.RomanticUniverseGenerator.totalVariationsCount >= 1000
        )
        val generated = mutableSetOf<String>()
        for (mood in com.example.data.model.DynamicMood.values()) {
            for (e in com.example.data.model.RomanticUniverseGenerator.endearments.indices) {
                for (t in 0 until 12) {
                    val quote = com.example.data.model.RomanticUniverseGenerator.generate(mood, e, t)
                    assertTrue(quote.isNotBlank())
                    generated.add(quote)
                }
            }
        }
        assertTrue("Üretilen tüm sözler benzersiz ve 1000'den fazla olmalı", generated.size >= 1000)
    }

    @Test
    fun testPlayStoreCoupleGamesAndAcrostic() {
        assertEquals(8, com.example.data.model.DateWheelData.slices.size)
        val dice = com.example.data.model.RomanticDiceData.roll()
        assertTrue(dice.second.isNotBlank())
        assertTrue(dice.third.isNotBlank())

        val lang = com.example.data.model.LoveLanguageQuizData.evaluate(
            listOf(
                com.example.data.model.LoveLanguageType.TIME,
                com.example.data.model.LoveLanguageType.WORDS,
                com.example.data.model.LoveLanguageType.TIME
            )
        )
        assertEquals(com.example.data.model.LoveLanguageType.TIME, lang)

        val poem = com.example.data.model.AcrosticPoemBuilder.buildFallbackAcrostic("Zeynep")
        assertTrue(poem.lines().size == 6)
        assertTrue(poem.startsWith("Z —"))
    }

    @Test
    fun testAppDesignThemesAvailable() {
        val themes = com.example.ui.theme.AppDesignTheme.values().toList()
        assertEquals(6, themes.size)
        val titles = themes.map { it.title }
        assertTrue(titles.contains("Pastel Pembe"))
        assertTrue(titles.contains("Gece Yarısı Mavisi"))
        assertTrue(titles.contains("Romantik Kırmızı"))
    }

    @Test
    fun testExtendedLoveSpaceModels() {
        assertTrue(com.example.data.model.DefaultLoveJarReasons.initialReasons.size >= 15)

        val truthPrompt = com.example.data.model.CoupleTruthOrDareData.drawPrompt(
            com.example.data.model.TruthDareLevel.SWEET,
            true
        )
        assertTrue(truthPrompt.isTruth)
        assertEquals(com.example.data.model.TruthDareLevel.SWEET, truthPrompt.level)

        val now = 1_000_000L
        val lockedCapsule = com.example.data.model.LoveTimeCapsule(
            id = "tc1",
            title = "Test",
            message = "Gelecekten selam",
            author = "Ben",
            createdAt = now,
            unlockAt = now + 86_400_000L
        )
        assertTrue(!lockedCapsule.isUnlocked(now))
        assertTrue(lockedCapsule.isUnlocked(now + 90_000_000L))
        assertTrue(lockedCapsule.remainingText(now).contains("kaldı"))

        assertEquals(6, com.example.data.model.CoupleTriviaData.questions.size)

        val matches = com.example.data.model.GiftSurpriseFinderData.findMatches(
            com.example.data.model.GiftBudget.FREE,
            com.example.data.model.GiftSetting.HOME
        )
        assertTrue(matches.isNotEmpty())
        assertTrue(matches.all { it.budget == com.example.data.model.GiftBudget.FREE && it.setting == com.example.data.model.GiftSetting.HOME })

        val milestones = com.example.data.model.MilestonesTimelineCalculator.compute(120L)
        assertEquals(8, milestones.size)
        assertEquals(3, milestones.count { it.isReached })
    }

    @Test
    fun testSocialImageShareFormatsAndPalettes() {
        val post = com.example.util.SocialCardFormat.fromId("card")
        assertEquals(1080, post.widthPx)
        assertEquals(1350, post.heightPx)

        val square = com.example.util.SocialCardFormat.fromId("square")
        assertEquals(1080, square.widthPx)
        assertEquals(1080, square.heightPx)

        val story = com.example.util.SocialCardFormat.fromId("story")
        assertEquals(1080, story.widthPx)
        assertEquals(1920, story.heightPx)

        assertEquals(com.example.util.SocialCardFormat.POST_4_5, com.example.util.SocialCardFormat.fromId("unknown"))

        val paletteIds = com.example.util.SocialImageShareHelper.palettes.map { it.id }
        assertTrue(paletteIds.contains("pastel_pembe"))
        assertTrue(paletteIds.contains("gece_yarisi_mavisi"))
        assertTrue(paletteIds.contains("romantik_kirmizi"))
    }

    @Test
    fun testAiAcrosticAndSituationAssistant() = kotlinx.coroutines.runBlocking {
        val aiRepo = com.example.data.repository.AiRepositoryImpl()

        val acrosticRes = aiRepo.generateAcrosticPoem("Elif")
        assertTrue("Akrostiş üretimi başarılı sonuç döndürmeli", acrosticRes.isSuccess)
        val poem = acrosticRes.getOrNull().orEmpty()
        assertTrue("Akrostiş boş olmamalı", poem.isNotBlank())
        assertTrue("E harfi ile başlamalı", poem.startsWith("E"))

        val sitRes = aiRepo.generateSituationMessage("Barışma & Özür")
        assertTrue("İlişki durum asistanı başarılı sonuç döndürmeli", sitRes.isSuccess)
        val sitMsg = sitRes.getOrNull()
        assertNotNull(sitMsg)
        assertTrue("Durum mesajı boş olmamalı", sitMsg!!.text.isNotBlank())
    }

    @Test
    fun testAdminSettingsMaintenanceAndFeatureFlags() {
        val defaults = com.example.data.model.AdminSettings()
        assertTrue(!defaults.maintenanceMode)
        assertTrue(defaults.allowUserRegistration)
        assertTrue(defaults.allowCustomMusic)
        assertTrue(defaults.allowCommunityComments)

        val updated = defaults.copy(
            maintenanceMode = true,
            maintenanceTitle = "Planlı Bakım",
            maintenanceMessage = "Kısa süre sonra yayındayız.",
            maintenanceEstimatedTime = "30 dakika",
            announcementBannerText = "Yeni romantik sözler eklendi!"
        )
        assertTrue(updated.maintenanceMode)
        assertEquals("Planlı Bakım", updated.maintenanceTitle)
        assertEquals("30 dakika", updated.maintenanceEstimatedTime)
        assertEquals("Yeni romantik sözler eklendi!", updated.announcementBannerText)
    }

    @Test
    fun testAuthSessionRolesAndBuiltInMusicPresets() {
        val loggedOut = com.example.ui.AuthSessionState()
        assertTrue(!loggedOut.isLoggedIn)
        assertTrue(!loggedOut.isAdmin)

        val user = com.example.ui.AuthSessionState(
            role = "USER",
            userId = 1L,
            fullName = "Zeynep Yılmaz",
            email = "zeynep@example.com"
        )
        assertTrue(user.isLoggedIn)
        assertTrue(user.isUser)
        assertTrue(!user.isAdmin)
        assertEquals("Zeynep Yılmaz", user.displayName)

        val admin = com.example.ui.AuthSessionState(role = "ADMIN", fullName = "Yönetici")
        assertTrue(admin.isLoggedIn)
        assertTrue(admin.isAdmin)
        assertEquals("Yönetici", admin.displayName)

        assertTrue(com.example.util.BuiltInAmbientPresets.presets.size >= 3)
    }

    @Test
    fun testEngagementNotificationAndUserModerationModels() {
        val likeNotification = com.example.data.local.EngagementNotificationEntity(
            id = 1L,
            type = "LIKE",
            quoteId = 42L,
            quotePreview = "Kalbim seninle güzel...",
            actorName = "Aylin",
            message = "Aylin paylaştığın sözü beğendi."
        )
        assertEquals("LIKE", likeNotification.type)
        assertTrue(!likeNotification.isRead)
        assertEquals(42L, likeNotification.quoteId)

        val approvalNotification = com.example.data.local.EngagementNotificationEntity(
            type = "APPROVED",
            quoteId = 99L,
            quotePreview = "Her sabahın ilk ışığısın",
            actorName = "Yönetici",
            message = "Paylaştığın söz onaylandı!",
            isRead = true
        )
        assertEquals("APPROVED", approvalNotification.type)
        assertTrue(approvalNotification.isRead)

        val defaultUser = com.example.data.local.UserAccountEntity(
            fullName = "Ece Kaya",
            email = "ece@example.com",
            passwordHash = "hashed_secret"
        )
        assertTrue(!defaultUser.isBlocked)
        assertTrue(!defaultUser.isRestricted)
        assertTrue(defaultUser.canSubmitQuotes)
        assertTrue(defaultUser.canComment)
        assertTrue(defaultUser.canUploadMusic)

        val restrictedUser = defaultUser.copy(
            isRestricted = true,
            canSubmitQuotes = false,
            canComment = false,
            canUploadMusic = true,
            restrictionReason = "Topluluk kuralları ihlali",
            restrictedUntil = System.currentTimeMillis() + 86_400_000L
        )
        assertTrue(restrictedUser.isRestricted)
        assertTrue(!restrictedUser.canSubmitQuotes)
        assertTrue(!restrictedUser.canComment)
        assertTrue(restrictedUser.canUploadMusic)
        assertEquals("Topluluk kuralları ihlali", restrictedUser.restrictionReason)

        val bannedUser = defaultUser.copy(
            isBlocked = true,
            restrictionReason = "Kalıcı ban"
        )
        assertTrue(bannedUser.isBlocked)
        assertEquals("Kalıcı ban", bannedUser.restrictionReason)
    }

    @Test
    fun testVisualPostPresetsAndMoodInsights30Day() {
        // 1. Visual Post preset background images and bundled typography styles
        assertEquals(4, com.example.data.model.CardStyleOptions.backgroundImages.size)
        assertEquals(4, com.example.data.model.StudioTypographyPreset.values().size)
        assertEquals("com.instagram.android", com.example.util.SharePlatform.INSTAGRAM.packageName)
        assertEquals("com.whatsapp", com.example.util.SharePlatform.WHATSAPP.packageName)

        // 2. 30-day Mood Insights from saved daily love notes
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val notes = listOf(
            com.example.data.local.NoteEntity(
                id = 1L,
                title = "Bugünkü Aşk Notum",
                content = "Seninle her anım huzur ve mutluluk dolu, seni çok seviyorum.",
                tags = "Aşk, Huzur",
                createdAt = now,
                updatedAt = now
            ),
            com.example.data.local.NoteEntity(
                id = 2L,
                title = "Dünkü Özlem",
                content = "Gece yıldızlara bakıp özlemle seni düşündüm.",
                tags = "Özlem",
                createdAt = now - dayMs,
                updatedAt = now - dayMs
            ),
            com.example.data.local.NoteEntity(
                id = 3L,
                title = "Geçen Hafta",
                content = "İyi ki hayatımdasın sevgilim, kalbim seninle.",
                tags = "Şükür",
                createdAt = now - (5L * dayMs),
                updatedAt = now - (5L * dayMs)
            )
        )

        val emptySummary = com.example.data.model.MoodInsightsCalculator.build30DayInsights(emptyList())
        assertEquals(30, emptySummary.days.size)
        assertEquals(0, emptySummary.activeDaysCount)
        assertEquals(0, emptySummary.consistencyPercent)

        val summary = com.example.data.model.MoodInsightsCalculator.build30DayInsights(
            notes = notes,
            nowMillis = now
        )
        assertEquals(30, summary.days.size)
        assertEquals(3, summary.totalNotesLast30Days)
        assertEquals(3, summary.activeDaysCount)
        assertEquals(10, summary.consistencyPercent) // 3/30 = 10%
        assertTrue(summary.currentStreakDays >= 2)
        assertTrue(summary.longestStreakDays >= 2)
        assertTrue(summary.averageMoodScore in 50..99)
        assertTrue(summary.breakdowns.isNotEmpty())
        assertTrue(summary.consistencyInsight.contains("3 günde kayıt"))
    }

    @Test
    fun testStoryTemplateFramesAndCloudBackupStatus() {
        val frames = com.example.data.model.StoryTemplateFrame.values()
        assertEquals(6, frames.size)
        assertEquals(com.example.data.model.StoryTemplateFrame.INSTA_STORY_BORDER, com.example.data.model.StoryTemplateFrame.fromId("ig_story"))
        assertEquals(com.example.data.model.StoryTemplateFrame.WHATSAPP_STATUS, com.example.data.model.StoryTemplateFrame.fromId("wp_status"))
        assertTrue(!com.example.data.model.StoryTemplateFrame.POLAROID_MEMORY.isPremium)
        assertTrue(com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME.isPremium)
        assertEquals(30, com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME.coinCost)

        val never = com.example.data.model.CloudBackupStatus()
        assertEquals(com.example.data.model.CloudSyncState.NEVER_BACKED_UP, never.state)

        val pending = com.example.data.model.CloudBackupStatus(
            lastBackupTimestamp = 1_700_000_000_000L,
            syncedNotesCount = 3,
            unsyncedNotesCount = 2,
            hasSnapshot = true
        )
        assertEquals(com.example.data.model.CloudSyncState.PENDING_CHANGES, pending.state)

        val synced = pending.copy(unsyncedNotesCount = 0)
        assertEquals(com.example.data.model.CloudSyncState.SYNCED, synced.state)
    }

    @Test
    fun testMonetizationEntitlementsAndCatalog() {
        assertEquals(2, com.example.data.model.MonetizationCatalog.vipSubscriptions.size)
        assertEquals(4, com.example.data.model.MonetizationCatalog.oneTimePacks.size)
        assertEquals(3, com.example.data.model.MonetizationCatalog.coinPacks.size)
        assertEquals(4, com.example.data.model.MonetizationCatalog.featureComparison.size)

        val freeUser = com.example.data.model.EntitlementState()
        assertTrue(freeUser.shouldShowAds)
        assertTrue(!freeUser.hasUnlimitedCloudBackup)
        assertTrue(!freeUser.hasSpecialDesignPack)
        assertTrue(freeUser.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.INSTA_STORY_BORDER))
        assertTrue(!freeUser.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME))

        val premiumStudioTheme = CardStyleOptions.themePresets.first { it.isPremium }
        assertTrue(!freeUser.hasAccessToStudioTheme(premiumStudioTheme))

        val unlockedOneFrame = freeUser.copy(
            unlockedItemIds = setOf(com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME.id)
        )
        assertTrue(unlockedOneFrame.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME))
        assertTrue(!unlockedOneFrame.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.EDITORIAL_MAGAZINE))

        val designPackUser = freeUser.copy(hasDesignPack = true)
        assertTrue(designPackUser.hasSpecialDesignPack)
        assertTrue(designPackUser.hasAccessToStudioTheme(premiumStudioTheme))

        val unlimitedBackupUser = freeUser.copy(hasUnlimitedBackup = true)
        assertTrue(unlimitedBackupUser.hasUnlimitedCloudBackup)
        assertTrue(unlimitedBackupUser.shouldShowAds) // Backup pack alone keeps free tier unless ad-free or VIP

        val adFreeUser = freeUser.copy(isAdFree = true)
        assertTrue(!adFreeUser.shouldShowAds)

        val vipUser = freeUser.copy(isVip = true)
        assertTrue(!vipUser.shouldShowAds)
        assertTrue(vipUser.hasUnlimitedCloudBackup)
        assertTrue(vipUser.hasSpecialDesignPack)
        assertTrue(vipUser.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.GOLD_ROYAL_FRAME))
        assertTrue(vipUser.hasAccessToFrame(com.example.data.model.StoryTemplateFrame.EDITORIAL_MAGAZINE))
        assertTrue(vipUser.hasAccessToStudioTheme(premiumStudioTheme))
    }

    @Test
    fun testMorningMotivationalQuotesAndLookup() {
        assertTrue(CuratedQuotes.morningMotivationalQuotes.isNotEmpty())
        val mot = CuratedQuotes.pickMorningQuote("MOTIVATIONAL")
        assertTrue(CuratedQuotes.morningMotivationalQuotes.any { it.id == mot.id })

        val rom = CuratedQuotes.pickMorningQuote("ROMANTIC")
        assertTrue(rom.text.isNotBlank())

        val mix = CuratedQuotes.pickMorningQuote("MIXED")
        assertTrue(mix.text.isNotBlank())

        val foundMot = CuratedQuotes.findById("sabah_mot_1")
        assertNotNull(foundMot)
        assertEquals("Sabah İlhamı", foundMot?.author)
    }

    @Test
    fun testCloudBackupFreeLimitAndUnlimitedQuota() {
        val overFree = com.example.data.model.CloudBackupStatus(
            lastBackupTimestamp = 1_700_000_000_000L,
            totalLocalItemsCount = 15,
            backupLimit = 10,
            isUnlimited = false
        )
        assertTrue(overFree.isOverFreeLimit)

        val withinFree = overFree.copy(totalLocalItemsCount = 8)
        assertTrue(!withinFree.isOverFreeLimit)

        val unlimited = overFree.copy(isUnlimited = true)
        assertTrue(!unlimited.isOverFreeLimit)
    }

    @Test
    fun testTargetedAdBlockerEvaluator() {
        // Offline / general DNS down must NEVER trigger ad-blocker notice
        val offlineRes = com.example.util.AdBlockerEvaluator.evaluateHostResolution(
            generalDnsResolved = false,
            generalIp = null,
            adHosts = listOf("googleads.g.doubleclick.net" to null)
        )
        assertTrue(!offlineRes.isOnline)
        assertTrue(!offlineRes.adBlockerDetected)

        // Normal online user resolving ad servers cleanly must NOT trigger notice
        val normalRes = com.example.util.AdBlockerEvaluator.evaluateHostResolution(
            generalDnsResolved = true,
            generalIp = "142.250.184.142",
            adHosts = listOf(
                "googleads.g.doubleclick.net" to "142.250.184.2",
                "pagead2.googlesyndication.com" to "142.250.184.66"
            )
        )
        assertTrue(normalRes.isOnline)
        assertTrue(!normalRes.adBlockerDetected)

        // Sinkhole IP while online MUST detect active ad blocker
        val sinkholeRes = com.example.util.AdBlockerEvaluator.evaluateHostResolution(
            generalDnsResolved = true,
            generalIp = "142.250.184.142",
            adHosts = listOf(
                "googleads.g.doubleclick.net" to "0.0.0.0",
                "pagead2.googlesyndication.com" to "142.250.184.66"
            )
        )
        assertTrue(sinkholeRes.isOnline)
        assertTrue(sinkholeRes.adBlockerDetected)

        // Private DNS hostname detection
        assertTrue(com.example.util.AdBlockerEvaluator.isAdBlockPrivateDnsHost("dns.adguard-dns.com"))
        assertTrue(com.example.util.AdBlockerEvaluator.isAdBlockPrivateDnsHost("dns.nextdns.io"))
        assertTrue(!com.example.util.AdBlockerEvaluator.isAdBlockPrivateDnsHost("dns.google"))
    }

    @Test
    fun testCommunityChatModerationAndSettings() {
        // 1. Rooms & lookup
        assertEquals(4, com.example.data.model.ChatRoom.values().size)
        assertEquals(
            com.example.data.model.ChatRoom.SIIR_SOZ,
            com.example.data.model.ChatRoom.fromId("siir_soz")
        )
        assertEquals(
            com.example.data.model.ChatRoom.GENEL,
            com.example.data.model.ChatRoom.fromId("unknown_room")
        )

        // 2. Admin chat defaults & controls
        val settings = com.example.data.model.AdminSettings()
        assertTrue(settings.chatEnabled)
        assertTrue(settings.chatAllowGuests)
        assertTrue(!settings.chatRequireApproval)
        assertEquals(0, settings.chatSlowModeSeconds)
        assertEquals(300, settings.chatMaxCharCount)

        // 3. Profanity / blocked-word detection
        val blockedMatch = com.example.data.model.ChatModerationHelper.containsBlockedWord(
            text = "Burada reklam paylaşmak yasak mı?",
            blockedWordsCsv = "küfür, hakaret, reklam"
        )
        assertEquals("reklam", blockedMatch)

        val cleanMatch = com.example.data.model.ChatModerationHelper.containsBlockedWord(
            text = "Sevgilimle harika bir akşam geçirdik",
            blockedWordsCsv = "küfür, hakaret, reklam"
        )
        assertEquals(null, cleanMatch)

        // 4. Slow mode cooldown calculation
        val now = 1_700_000_030_000L
        val remaining = com.example.data.model.ChatModerationHelper.checkSlowModeRemainingSeconds(
            lastSentAtMillis = now - 8_000L,
            slowModeSeconds = 30,
            nowMillis = now
        )
        assertEquals(22, remaining)

        val expired = com.example.data.model.ChatModerationHelper.checkSlowModeRemainingSeconds(
            lastSentAtMillis = now - 35_000L,
            slowModeSeconds = 30,
            nowMillis = now
        )
        assertEquals(0, expired)

        // 5. Content validation
        assertNotNull(com.example.data.model.ChatModerationHelper.validateMessageContent("a", 300))
        assertNotNull(com.example.data.model.ChatModerationHelper.validateMessageContent("x".repeat(301), 300))
        assertEquals(null, com.example.data.model.ChatModerationHelper.validateMessageContent("Merhaba herkese!", 300))

        // 6. User chat mute permission & ChatMessageEntity
        val user = com.example.data.local.UserAccountEntity(
            fullName = "Arda",
            email = "arda@example.com",
            passwordHash = "hash"
        )
        assertTrue(user.canChat)
        val mutedUser = user.copy(isRestricted = true, canChat = false, restrictionReason = "Spam")
        assertTrue(!mutedUser.canChat)

        val adminMsg = com.example.data.local.ChatMessageEntity(
            roomId = com.example.data.model.ChatRoom.GENEL.id,
            senderName = "Yönetici 👑",
            senderRole = "ADMIN",
            message = "Hoş geldiniz",
            isPinned = true
        )
        assertEquals("PUBLISHED", adminMsg.status)
        assertTrue(adminMsg.isPinned)
    }

    @Test
    fun testChatHistoryMergerAndPremiumProfileModel() {
        val room = com.example.data.model.ChatRoom.GENEL.id
        val older1 = com.example.data.local.ChatMessageEntity(
            id = 10L,
            roomId = room,
            senderName = "Elif",
            senderRole = "USER",
            isVipSender = true,
            message = "Günaydın güzel insanlar",
            status = "PUBLISHED",
            likesCount = 1,
            createdAt = 1_000L
        )
        val older2 = com.example.data.local.ChatMessageEntity(
            id = 20L,
            roomId = room,
            senderName = "Emre",
            senderRole = "USER",
            message = "Kalplerimiz bir",
            status = "PUBLISHED",
            likesCount = 2,
            createdAt = 2_000L
        )
        val hiddenOld = com.example.data.local.ChatMessageEntity(
            id = 25L,
            roomId = room,
            senderName = "Gizli",
            message = "Gizlenmiş mesaj",
            status = "HIDDEN",
            createdAt = 2_500L
        )
        // Live snapshot brings an updated like count & pin on id=20 plus a brand new message id=30
        val live2Updated = older2.copy(likesCount = 9, isPinned = true)
        val live3 = com.example.data.local.ChatMessageEntity(
            id = 30L,
            roomId = room,
            senderName = "Zeynep",
            senderRole = "USER",
            isVipSender = true,
            message = "Yeni aşk sözü ekledim",
            status = "PUBLISHED",
            createdAt = 3_000L
        )
        val otherRoomMsg = older1.copy(id = 40L, roomId = com.example.data.model.ChatRoom.GECE_KOSESI.id)

        val merged = com.example.data.model.ChatHistoryMerger.mergePagedAndLive(
            loadedHistory = listOf(older2, older1, hiddenOld),
            liveMessages = listOf(live3, live2Updated, otherRoomMsg),
            roomId = room,
            includeAllStatuses = false
        )

        assertEquals(listOf(10L, 20L, 30L), merged.map { it.id })
        assertEquals(9, merged[1].likesCount)
        assertTrue(merged[1].isPinned)

        // Search filter with Turkish locale
        val filtered = com.example.data.model.ChatHistoryMerger.filterByQuery(merged, "elif")
        assertEquals(1, filtered.size)
        assertEquals(10L, filtered.first().id)

        // Deterministic positive Long IDs from Firestore doc IDs
        val idA1 = com.example.data.firebase.stableLongIdFromDocId("chat_doc_alpha_1")
        val idA2 = com.example.data.firebase.stableLongIdFromDocId("chat_doc_alpha_1")
        val idB = com.example.data.firebase.stableLongIdFromDocId("chat_doc_beta_2")
        assertEquals(idA1, idA2)
        assertTrue(idA1 > 0L)
        assertTrue(idA1 != idB)

        // Public profile Premium & stats
        val premiumProfile = com.example.data.firebase.FsPublicProfile(
            uid = "u_prem_1",
            displayName = "Elif",
            partnerName = "Mert",
            isPremium = true,
            vipPlanName = "Yıllık VIP",
            sharedQuotesCount = 7,
            receivedHeartsCount = 42
        )
        assertTrue(premiumProfile.isPremium)
        assertEquals(7, premiumProfile.sharedQuotesCount)
        assertEquals(42, premiumProfile.receivedHeartsCount)
    }

    @Test
    fun testEmotionalJournalDateGroupingAndPinSecurity() {
        val helper = com.example.data.model.JournalDateHelper
        val dayMs = 24L * 60L * 60L * 1000L
        val cal = java.util.Calendar.getInstance().apply {
            set(2025, java.util.Calendar.JANUARY, 15, 12, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayNoon = helper.normalizeDateMillis(cal.timeInMillis)
        val yesterdayNoon = helper.normalizeDateMillis(todayNoon - dayMs)
        val todayKey = helper.toDateKey(todayNoon)
        val yesterdayKey = helper.toDateKey(yesterdayNoon)

        assertEquals("2025-01-15", todayKey)
        assertEquals("2025-01-14", yesterdayKey)

        // Mood lookup & fallback
        assertEquals(com.example.data.model.JournalMood.ROMANTIC, com.example.data.model.JournalMood.fromId("romantic"))
        assertEquals(com.example.data.model.JournalMood.ROMANTIC, com.example.data.model.JournalMood.fromId("unknown_mood"))

        val entry1 = com.example.data.model.EmotionalJournalEntry(
            id = 1L,
            docId = "j1",
            title = "İlk Kahve",
            content = "Yağmurlu bir sabah, seninle el ele oturduk.",
            dateKey = todayKey,
            memoryTimestamp = todayNoon,
            moodId = "peaceful",
            locationOrMoment = "Galata",
            isFavorite = true,
            isPrivateLocked = true
        )
        val entry2 = com.example.data.model.EmotionalJournalEntry(
            id = 2L,
            docId = "j2",
            title = "Ocak Gecesi",
            content = "Soğuk akşamda senin gözlerinle ısındım.",
            dateKey = yesterdayKey,
            memoryTimestamp = yesterdayNoon,
            moodId = "romantic",
            isFavorite = false
        )

        val entries = listOf(entry2, entry1)
        val sections = helper.groupEntriesByDate(entries, nowMillis = todayNoon)
        assertEquals(2, sections.size)
        assertEquals(todayKey, sections.first().dateKey)
        assertTrue(sections.first().formattedHeader.startsWith("Bugün"))
        assertTrue(sections[1].formattedHeader.startsWith("Dün"))

        val selectedDayFiltered = helper.filterEntries(
            entries = entries,
            scope = com.example.data.model.JournalDateScope.SELECTED_DAY,
            selectedDateKey = todayKey,
            selectedMood = null,
            searchQuery = "",
            nowMillis = todayNoon
        )
        assertEquals(listOf(1L), selectedDayFiltered.map { it.id })

        val favoriteFiltered = helper.filterEntries(
            entries = entries,
            scope = com.example.data.model.JournalDateScope.FAVORITES,
            selectedDateKey = todayKey,
            selectedMood = null,
            searchQuery = "galata",
            nowMillis = todayNoon
        )
        assertEquals(listOf(1L), favoriteFiltered.map { it.id })

        // Salted PIN hashing verification
        val sec = com.example.data.model.JournalSecurityHelper
        assertTrue(sec.isValidPinFormat("2580"))
        assertTrue(!sec.isValidPinFormat("258"))
        assertTrue(!sec.isValidPinFormat("25a0"))

        val hashUser1 = sec.hashPin("u1", "2580")
        val hashUser2 = sec.hashPin("u2", "2580")
        assertTrue(hashUser1.isNotBlank())
        assertTrue(hashUser1 != hashUser2)
        assertTrue(sec.verifyPin("u1", "2580", hashUser1))
        assertTrue(!sec.verifyPin("u1", "0000", hashUser1))
    }
}
