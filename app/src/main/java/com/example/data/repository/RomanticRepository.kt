package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Compatibility facade bridging application services to [FirestoreRepository].
 * All persistence, authentication, moderation, monetization, backup, and chat
 * now live exclusively in Firebase Auth + Cloud Firestore.
 */
class RomanticRepository(
    context: Context,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val firestoreRepo: FirestoreRepository = FirestoreRepository(
        context = context.applicationContext,
        db = firestore,
        auth = auth
    )
}
