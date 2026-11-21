package com.example.data.firebase

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

object GoogleAuthManager {
    private fun getWebClientId(context: Context): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else ""
    }

    suspend fun trySilentGoogleSignIn(context: Context, auth: FirebaseAuth): Result<FirebaseUser> {
        val current = auth.currentUser
        if (current != null) return Result.success(current)
        val serverClientId = getWebClientId(context)
        if (serverClientId.isBlank()) {
            return Result.failure(IllegalStateException("Google OAuth istemci yapılandırması bulunamadı."))
        }
        return try {
            val credentialManager = CredentialManager.create(context)
            val option = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            val result = credentialManager.getCredential(context, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Oturum açılamadı."))
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Beklenmeyen kimlik bilgisi türü."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoCredentialException) {
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activityContext: Context, auth: FirebaseAuth): Result<FirebaseUser> {
        val serverClientId = getWebClientId(activityContext)
        if (serverClientId.isBlank()) {
            return Result.failure(IllegalStateException("Google OAuth web client ID eksik."))
        }
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            val result = credentialManager.getCredential(activityContext, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleCred = GoogleIdTokenCredential.createFrom(cred.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val user = authResult.user
                    ?: return Result.failure(IllegalStateException("Google hesabı ile oturum açılamadı."))
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Geçersiz Google kimlik bilgisi alındı."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Google ile giriş iptal edildi."))
        } catch (_: NoCredentialException) {
            Result.failure(Exception("Cihazda kayıtlı bir Google hesabı bulunamadı. Lütfen cihazınıza Google hesabı ekleyin."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Google ile giriş sırasında bir hata oluştu."))
        }
    }

    suspend fun signOut(context: Context, auth: FirebaseAuth) {
        withContext(NonCancellable) {
            try {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
            }
            auth.signOut()
        }
    }
}
