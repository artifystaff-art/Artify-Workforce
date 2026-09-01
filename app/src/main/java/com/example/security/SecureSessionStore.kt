package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

/**
 * Device-bound session storage backed by Android Keystore (EncryptedSharedPreferences).
 * Deliberately excluded from Auto Backup / device transfer (see backup_rules.xml and
 * data_extraction_rules.xml) so a session can never silently reappear on a different
 * physical device — a new device must always go through Civil ID verification again.
 */
class SecureSessionStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    fun saveSession(
        employeeId: String,
        refreshToken: String,
        accessToken: String,
        accessTokenExpiresAtEpochSeconds: Long,
        employeeFullName: String,
        employeeCode: String,
        role: String,
        assignedProjectId: String?,
        isDemo: Boolean
    ) {
        prefs.edit()
            .putString(KEY_EMPLOYEE_ID, employeeId)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_ACCESS_TOKEN_EXP, accessTokenExpiresAtEpochSeconds)
            .putString(KEY_EMPLOYEE_NAME, employeeFullName)
            .putString(KEY_EMPLOYEE_CODE, employeeCode)
            .putString(KEY_ROLE, role)
            .putString(KEY_ASSIGNED_PROJECT_ID, assignedProjectId)
            .putBoolean(KEY_IS_DEMO, isDemo)
            .apply()
    }

    fun updateAccessToken(accessToken: String, expiresAtEpochSeconds: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_ACCESS_TOKEN_EXP, expiresAtEpochSeconds)
            .apply()
    }

    fun hasCachedSession(): Boolean = prefs.contains(KEY_EMPLOYEE_ID) && prefs.contains(KEY_REFRESH_TOKEN)

    fun cachedEmployeeId(): String? = prefs.getString(KEY_EMPLOYEE_ID, null)
    fun cachedEmployeeName(): String? = prefs.getString(KEY_EMPLOYEE_NAME, null)
    fun cachedEmployeeCode(): String? = prefs.getString(KEY_EMPLOYEE_CODE, null)
    fun cachedRole(): String? = prefs.getString(KEY_ROLE, null)
    fun cachedAssignedProjectId(): String? = prefs.getString(KEY_ASSIGNED_PROJECT_ID, null)
    fun cachedRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun cachedAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun cachedAccessTokenExpiry(): Long = prefs.getLong(KEY_ACCESS_TOKEN_EXP, 0L)
    fun cachedIsDemo(): Boolean = prefs.getBoolean(KEY_IS_DEMO, false)

    /** Clears the session (logout) but keeps the device_id so re-registration reuses it. */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_EMPLOYEE_ID)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_ACCESS_TOKEN_EXP)
            .remove(KEY_EMPLOYEE_NAME)
            .remove(KEY_EMPLOYEE_CODE)
            .remove(KEY_ROLE)
            .remove(KEY_ASSIGNED_PROJECT_ID)
            .remove(KEY_IS_DEMO)
            .apply()
    }

    companion object {
        private const val FILE_NAME = "artify_secure_session"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_EMPLOYEE_ID = "employee_id"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_ACCESS_TOKEN_EXP = "access_token_exp"
        private const val KEY_EMPLOYEE_NAME = "employee_name"
        private const val KEY_EMPLOYEE_CODE = "employee_code"
        private const val KEY_ROLE = "role"
        private const val KEY_ASSIGNED_PROJECT_ID = "assigned_project_id"
        private const val KEY_IS_DEMO = "is_demo"

        @Volatile private var instance: SecureSessionStore? = null
        fun getInstance(context: Context): SecureSessionStore =
            instance ?: synchronized(this) {
                instance ?: SecureSessionStore(context.applicationContext).also { instance = it }
            }
    }
}
