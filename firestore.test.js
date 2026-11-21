const assert = require('node:assert');
const fs = require('node:fs');
const { after, before, beforeEach, test } = require('node:test');
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require('@firebase/rules-unit-testing');

const PROJECT_ID = 'gen-lang-client-0250490157';
const DATABASE_ID = 'ai-studio-android-akszleri-92fafaeb-a4fd-4560-8f8f-157d95398800';
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || '127.0.0.1:8085').split(':');
const emulatorPort = parseInt(emulatorPortStr, 10);

let testEnv;

function tsPast(offsetMs = 1000) {
  return new Date(Date.now() - offsetMs);
}

function validUserPayload(uid, email, fullName = 'Elif Yilmaz') {
  const now = tsPast(2000);
  return {
    uid,
    fullName,
    email,
    partnerName: 'Can',
    isBlocked: false,
    isRestricted: false,
    canSubmitQuotes: true,
    canComment: true,
    canUploadMusic: true,
    canChat: true,
    restrictionReason: '',
    restrictedUntil: null,
    isVip: false,
    vipPlanName: '',
    isAdFree: false,
    hasUnlimitedBackup: false,
    hasDesignPack: false,
    hasVipBadgeAndAi: false,
    loveCoins: 30,
    unlockedItemIds: [],
    totalCoinPurchases: 0,
    totalVipOrPackPurchases: 0,
    estimatedRevenueTry: 0,
    themeMode: 'system',
    designTheme: 'ruby_velvet',
    dailyNotifEnabled: true,
    dailyNotifHour: 8,
    dailyNotifMinute: 30,
    dailyNotifTone: 'MIXED',
    loveCounterMyName: fullName,
    loveCounterPartnerName: 'Can',
    loveCounterStartDate: 1700000000000,
    loveCounterTitle: 'İlk Günümüzden Beri',
    redeemedCouponIds: [],
    doneBucketIds: [],
    readBroadcastIds: [],
    ourSongTitle: '',
    ourSongArtist: '',
    loveStreak: 1,
    streakLastDay: '2026-11-19',
    todayMoodLabel: '',
    todayMoodEmoji: '💖',
    todayMoodScore: 90,
    todayMoodDate: '',
    moodCheckInHistoryJson: '{}',
    loveLanguageName: '',
    missYouTotalCount: 0,
    missYouTodayCount: 0,
    missYouDay: '',
    truthDareCompletedCount: 0,
    lastSyncAt: 0,
    syncedNotesCount: 0,
    syncedFavsCount: 0,
    syncedRemindersCount: 0,
    lastBackupJson: '',
    createdAt: now,
    updatedAt: now,
  };
}

function validAdminSettingsPayload() {
  return {
    submissionsEnabled: true,
    dailyLimitPerUser: 3,
    minCharCount: 10,
    maxCharCount: 280,
    autoApprove: false,
    maintenanceMode: false,
    maintenanceTitle: 'Kısa Bir Aşk Molası 🛠️',
    maintenanceMessage: 'Bakım çalışması sürüyor.',
    maintenanceEstimatedTime: 'Yaklaşık 15 dakika',
    allowUserRegistration: true,
    allowCustomMusic: true,
    allowCommunityComments: true,
    announcementBannerText: '',
    adsEnabled: true,
    rewardCoinsPerAd: 15,
    interstitialCooldownSeconds: 180,
    adBlockerGuardEnabled: true,
    chatEnabled: true,
    chatClosedMessage: 'Sohbet kapalı',
    chatAllowGuests: true,
    chatRequireApproval: false,
    chatSlowModeSeconds: 0,
    chatMaxCharCount: 300,
    chatBlockedWordsCsv: 'küfür,hakaret,spam,reklam',
    updatedAt: tsPast(1000),
  };
}

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      databaseId: DATABASE_ID,
      rules: fs.readFileSync('firestore.rules', 'utf8'),
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

test('1. Unauthenticated requests are denied across all collections', async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection('users').doc('alice').get());
  await assertFails(unauthDb.collection('chat_messages').get());
  await assertFails(unauthDb.collection('community_quotes').get());
  await assertFails(unauthDb.collection('app_config').doc('admin_settings').get());
  await assertFails(unauthDb.collection('chat_messages').doc('m1').set({ message: 'hi' }));
});

test('2. User profile CRUD enforces ownership, PII isolation, and blocks self-escalation', async () => {
  const aliceDb = testEnv.authenticatedContext('alice', { email: 'alice@example.com' }).firestore();
  const bobDb = testEnv.authenticatedContext('bob', { email: 'bob@example.com' }).firestore();

  await assertSucceeds(aliceDb.collection('users').doc('alice').set(validUserPayload('alice', 'alice@example.com')));

  await assertFails(bobDb.collection('users').doc('alice').set(validUserPayload('alice', 'alice@example.com')));
  const vipSpoof = validUserPayload('bob', 'bob@example.com');
  vipSpoof.isVip = true;
  await assertFails(bobDb.collection('users').doc('bob').set(vipSpoof));

  await assertFails(bobDb.collection('users').doc('alice').get());
  await assertFails(bobDb.collection('users').get());

  await assertFails(aliceDb.collection('users').doc('alice').update({ isBlocked: true, updatedAt: tsPast(500) }));
  await assertFails(aliceDb.collection('users').doc('alice').update({ canChat: false, updatedAt: tsPast(500) }));

  await assertSucceeds(aliceDb.collection('users').doc('alice').update({
    partnerName: 'Mert',
    loveStreak: 5,
    lastSyncAt: 1700000050000,
    updatedAt: tsPast(500),
  }));
});

test('3. User subcollections enforce owner isolation, list queries, and schemas', async () => {
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const created = tsPast(3000);

  await assertSucceeds(aliceDb.collection('users').doc('alice').collection('notes').doc('n1').set({
    title: 'Sabah Notu',
    content: 'Seni çok seviyorum!',
    author: 'Alice',
    tags: 'Aşk, Sabah',
    voiceAudioPath: null,
    voiceDurationSeconds: 0,
    isFavorite: true,
    backgroundTheme: 'rose_velvet',
    filterStyle: 'normal',
    isSyncedWithCloud: true,
    createdAt: created,
    updatedAt: created,
  }));

  await assertSucceeds(aliceDb.collection('users').doc('alice').collection('notes').get());
  await assertSucceeds(aliceDb.collection('users').doc('alice').collection('reminders').get());

  await assertFails(bobDb.collection('users').doc('alice').collection('notes').doc('n1').get());
  await assertFails(bobDb.collection('users').doc('alice').collection('notes').get());
  await assertFails(bobDb.collection('users').doc('alice').collection('notes').doc('n1').delete());

  await assertFails(aliceDb.collection('users').doc('alice').collection('notes').doc('n2').set({
    title: 'Bad',
    content: 'Test',
    author: 'Alice',
    tags: '',
    voiceAudioPath: null,
    voiceDurationSeconds: 0,
    isFavorite: false,
    backgroundTheme: 'rose_velvet',
    filterStyle: 'normal',
    isSyncedWithCloud: true,
    rogueField: 'hack',
    createdAt: created,
    updatedAt: created,
  }));

  await assertFails(aliceDb.collection('users').doc('alice').collection('love_jar').doc('j1').set({
    reason: 'Gülüşün',
    createdAt: new Date(Date.now() + 600000),
  }));
});

test('4. Admin lookup, app_config, broadcasts, admin_quotes, and user moderation work only for admins', async () => {
  const adminDb = testEnv.authenticatedContext('admin_uid', {
    email: 'ferhat.krnn65@gmail.com',
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext('alice', { email: 'alice@example.com' }).firestore();

  await assertSucceeds(adminDb.collection('admins').doc('admin_uid').set({
    uid: 'admin_uid',
    email: 'ferhat.krnn65@gmail.com',
    createdAt: tsPast(1000),
  }));

  await assertFails(aliceDb.collection('admins').doc('alice').set({
    uid: 'alice',
    email: 'alice@example.com',
    createdAt: tsPast(1000),
  }));

  await assertFails(aliceDb.collection('app_config').doc('admin_settings').set(validAdminSettingsPayload()));
  await assertSucceeds(adminDb.collection('app_config').doc('admin_settings').set(validAdminSettingsPayload()));
  await assertSucceeds(aliceDb.collection('app_config').doc('admin_settings').get());

  await assertSucceeds(adminDb.collection('broadcasts').doc('b1').set({
    title: 'Hoş Geldiniz',
    message: 'Yeni özellikler yayında!',
    category: 'Duyuru',
    emoji: '📣',
    sentBy: 'Yönetici',
    sentAt: tsPast(1000),
  }));
  await assertSucceeds(aliceDb.collection('broadcasts').get());

  await assertFails(aliceDb.collection('admin_quotes').doc('aq1').set({
    text: 'Deneme admin sözüdür',
    author: 'Editör',
    sectionId: 'ask',
    subCategory: 'Özel',
    tagsCsv: 'Aşk',
    isFeaturedDaily: true,
    createdAt: tsPast(1000),
  }));
  await assertSucceeds(adminDb.collection('admin_quotes').doc('aq1').set({
    text: 'Gönülden gelen gerçek bir aşk sözüdür.',
    author: 'Yönetici',
    sectionId: 'ask',
    subCategory: 'Editör Seçimi',
    tagsCsv: 'Aşk',
    isFeaturedDaily: true,
    createdAt: tsPast(1000),
  }));

  await assertSucceeds(aliceDb.collection('users').doc('alice').set(validUserPayload('alice', 'alice@example.com')));
  await assertSucceeds(adminDb.collection('users').get());
  await assertSucceeds(adminDb.collection('users').doc('alice').update({
    isRestricted: true,
    canChat: false,
    restrictionReason: 'Spam',
    restrictedUntil: Date.now() + 86400000,
    updatedAt: tsPast(500),
  }));
});

test('5. Chat live stream, paginated Mesaj Geçmişi, mute enforcement, Premium badges & atomic likes', async () => {
  const adminDb = testEnv.authenticatedContext('admin_uid', {
    email: 'ferhat.krnn65@gmail.com',
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext('alice', { email: 'alice@example.com' }).firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore(); // Intentionally no email claims in token

  await assertSucceeds(aliceDb.collection('users').doc('alice').set(validUserPayload('alice', 'alice@example.com')));
  await assertSucceeds(aliceDb.collection('public_profiles').doc('alice').set({
    uid: 'alice',
    displayName: 'Alice',
    partnerName: 'Mert',
    role: 'USER',
    isPremium: false,
    vipPlanName: '',
    isBlocked: false,
    isMutedInChat: false,
    sharedQuotesCount: 0,
    receivedHeartsCount: 0,
    updatedAt: tsPast(1000),
  }));
  await assertSucceeds(bobDb.collection('public_profiles').get());

  await assertFails(aliceDb.collection('chat_messages').doc('spoof1').set({
    roomId: 'genel',
    senderUid: 'alice',
    senderName: 'Alice',
    senderRole: 'ADMIN',
    isPremiumSender: true,
    vipPlanName: 'VIP',
    message: 'Fake admin',
    replyToMessageId: null,
    replyToSenderName: null,
    replyToSnippet: null,
    status: 'PUBLISHED',
    isPinned: false,
    likesCount: 0,
    moderationNote: null,
    createdAt: tsPast(1000),
  }));

  for (let i = 1; i <= 3; i++) {
    await assertSucceeds(aliceDb.collection('chat_messages').doc(`msg_${i}`).set({
      roomId: 'genel',
      senderUid: 'alice',
      senderName: 'Alice',
      senderRole: 'USER',
      isPremiumSender: false,
      vipPlanName: '',
      message: `Romantik sohbet mesajı #${i}`,
      replyToMessageId: null,
      replyToSenderName: null,
      replyToSnippet: null,
      status: 'PUBLISHED',
      isPinned: false,
      likesCount: 0,
      moderationNote: null,
      createdAt: new Date(Date.now() - (5000 - i * 1000)),
    }));
  }

  // Published query + cursor pagination for Mesaj Geçmişi succeeds for signed-in users (even without email token claim)
  const page1Snap = await assertSucceeds(
    bobDb.collection('chat_messages')
      .where('roomId', '==', 'genel')
      .where('status', '==', 'PUBLISHED')
      .orderBy('createdAt', 'desc')
      .limit(2)
      .get()
  );
  assert.equal(page1Snap.docs.length, 2);
  const lastDoc = page1Snap.docs[page1Snap.docs.length - 1];
  const page2Snap = await assertSucceeds(
    bobDb.collection('chat_messages')
      .where('roomId', '==', 'genel')
      .where('status', '==', 'PUBLISHED')
      .orderBy('createdAt', 'desc')
      .startAfter(lastDoc)
      .limit(2)
      .get()
  );
  assert.equal(page2Snap.docs.length, 1);

  // Unfiltered chat query fails for non-admin (hidden/pending protection)
  await assertFails(bobDb.collection('chat_messages').get());

  // Atomic WriteBatch like + increment works once; duplicate or rogue increment fails
  const likeBatch = bobDb.batch();
  likeBatch.set(bobDb.collection('chat_messages').doc('msg_1').collection('likes').doc('bob'), {
    uid: 'bob',
    createdAt: tsPast(500),
  });
  likeBatch.update(bobDb.collection('chat_messages').doc('msg_1'), { likesCount: 1 });
  await assertSucceeds(likeBatch.commit());

  await assertFails(bobDb.collection('chat_messages').doc('msg_1').update({ likesCount: 10 }));

  // Admin pins message and mutes Alice -> Alice's subsequent chat send fails by server rule!
  await assertSucceeds(adminDb.collection('chat_messages').doc('msg_1').update({ isPinned: true }));
  await assertSucceeds(adminDb.collection('users').doc('alice').update({
    isRestricted: true,
    canChat: false,
    restrictionReason: 'Susturuldu',
    restrictedUntil: null,
    updatedAt: tsPast(500),
  }));

  await assertFails(aliceDb.collection('chat_messages').doc('msg_muted').set({
    roomId: 'genel',
    senderUid: 'alice',
    senderName: 'Alice',
    senderRole: 'USER',
    isPremiumSender: false,
    vipPlanName: '',
    message: 'Susturulmuşken yazma denemesi',
    replyToMessageId: null,
    replyToSenderName: null,
    replyToSnippet: null,
    status: 'PUBLISHED',
    isPinned: false,
    likesCount: 0,
    moderationNote: null,
    createdAt: tsPast(200),
  }));
});

test('6. Support messages & Community quotes enforce privacy and moderation queues', async () => {
  const adminDb = testEnv.authenticatedContext('admin_uid', {
    email: 'ferhat.krnn65@gmail.com',
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();

  await assertSucceeds(aliceDb.collection('support_messages').doc('s1').set({
    userId: 'alice',
    senderName: 'Alice',
    senderEmail: 'alice@example.com',
    topic: 'Öneri',
    message: 'Harika uygulama, teşekkürler!',
    adminReply: null,
    status: 'PENDING',
    isReadByAdmin: false,
    createdAt: tsPast(1000),
    repliedAt: null,
  }));

  await assertFails(bobDb.collection('support_messages').doc('s1').get());
  await assertFails(bobDb.collection('support_messages').get());
  const ownSupport = await assertSucceeds(
    aliceDb.collection('support_messages').where('userId', '==', 'alice').get()
  );
  assert.equal(ownSupport.docs.length, 1);

  await assertSucceeds(adminDb.collection('support_messages').doc('s1').update({
    adminReply: 'Çok teşekkür ederiz!',
    status: 'REPLIED',
    isReadByAdmin: true,
    repliedAt: tsPast(500),
  }));

  await assertSucceeds(aliceDb.collection('community_quotes').doc('cq1').set({
    authorUid: 'alice',
    text: 'Kalbimde sadece senin izin var sevgilim.',
    author: 'Alice',
    category: 'Aşk Sözleri',
    submitterName: 'Alice',
    status: 'PENDING',
    likesCount: 0,
    moderationNote: null,
    createdAt: tsPast(1000),
    approvedAt: null,
  }));

  await assertFails(bobDb.collection('community_quotes').doc('cq1').get());
  await assertSucceeds(adminDb.collection('community_quotes').doc('cq1').update({
    status: 'APPROVED',
    approvedAt: tsPast(500),
  }));
  await assertSucceeds(bobDb.collection('community_quotes').doc('cq1').get());
  await assertSucceeds(bobDb.collection('community_quotes').where('status', '==', 'APPROVED').get());
  await assertSucceeds(bobDb.collection('community_quotes').doc('cq1').collection('comments').doc('c1').set({
    authorUid: 'bob',
    authorName: 'Bob',
    commentText: 'Çok anlamlı bir söz!',
    createdAt: tsPast(300),
  }));
});

test('7. Private Emotional Journal (journal_entries) enforces strict owner-only privacy and date/mood schema', async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext('alice').firestore();
  const bobDb = testEnv.authenticatedContext('bob').firestore();
  const adminDb = testEnv.authenticatedContext('admin_uid', {
    email: 'ferhat.krnn65@gmail.com',
    email_verified: true,
  }).firestore();

  const created = tsPast(5000);
  const entryPayload = {
    userId: 'alice',
    title: 'Boğazda İlk Yürüyüşümüz',
    content: 'Soğuk bir Kasım akşamıydı ama ellerimiz hiç üşümedi. Seninle her anım özel.',
    moodId: 'romantic',
    dateKey: '2026-11-21',
    memoryTimestamp: 1795262400000,
    locationOrMoment: 'Ortaköy Sahili',
    isFavorite: true,
    isPrivateLocked: true,
    createdAt: created,
    updatedAt: created,
  };

  // Owner create & dateKey descending query (exact mirror of FirestoreRepository.observeJournalEntries)
  await assertSucceeds(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').set(entryPayload)
  );
  const aliceEntries = await assertSucceeds(
    aliceDb.collection('users').doc('alice').collection('journal_entries').orderBy('dateKey', 'desc').get()
  );
  assert.equal(aliceEntries.docs.length, 1);

  // Partial update on mutable fields succeeds
  await assertSucceeds(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').update({
      isFavorite: false,
      moodId: 'grateful',
      updatedAt: tsPast(500),
    })
  );

  // Immutable createdAt or userId tampering fails
  await assertFails(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').update({
      createdAt: tsPast(200),
      updatedAt: tsPast(100),
    })
  );
  await assertFails(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').update({
      userId: 'bob',
      updatedAt: tsPast(100),
    })
  );

  // Invalid dateKey, moodId, rogue ghost key, or future timestamp fails
  await assertFails(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_bad_date').set({
      ...entryPayload,
      dateKey: '21-11-2026',
    })
  );
  await assertFails(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_bad_mood').set({
      ...entryPayload,
      moodId: 'invalid_mood',
    })
  );
  await assertFails(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_ghost').set({
      ...entryPayload,
      ghostField: 'attack',
    })
  );

  // Strict privacy: unauthenticated, another user (Bob), and even Admin cannot read/modify/delete Alice's journal
  await assertFails(unauthDb.collection('users').doc('alice').collection('journal_entries').get());
  await assertFails(bobDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').get());
  await assertFails(bobDb.collection('users').doc('alice').collection('journal_entries').get());
  await assertFails(bobDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').delete());
  await assertFails(adminDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').get());
  await assertFails(adminDb.collection('users').doc('alice').collection('journal_entries').get());

  // Owner can delete their own entry
  await assertSucceeds(
    aliceDb.collection('users').doc('alice').collection('journal_entries').doc('je_1').delete()
  );
});

