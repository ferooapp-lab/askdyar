package com.example.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

enum class OperationType {
    CREATE, UPDATE, DELETE, LIST, GET, WRITE
}

data class FirestoreErrorInfo(
    val error: String,
    val operationType: OperationType,
    val path: String?,
    val authUid: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val providers: List<String>
)

fun handleFirestoreError(
    exception: Throwable,
    operationType: OperationType,
    path: String?,
    auth: FirebaseAuth
): String {
    val user = auth.currentUser
    val providers = user?.providerData
        ?.mapNotNull { it.providerId.takeIf { id -> id.isNotBlank() } }
        ?: emptyList()

    val info = FirestoreErrorInfo(
        error = exception.message ?: exception.toString(),
        operationType = operationType,
        path = path,
        authUid = user?.uid,
        email = user?.email,
        emailVerified = user?.isEmailVerified,
        providers = providers
    )

    val json = JSONObject().apply {
        put("error", info.error)
        put("operationType", info.operationType.name)
        put("path", info.path)
        put("authInfo", JSONObject().apply {
            put("userId", info.authUid)
            put("email", info.email)
            put("emailVerified", info.emailVerified)
            put("providers", org.json.JSONArray(info.providers))
        })
    }.toString()

    Log.e("FirestoreError", json, exception)
    return json
}
