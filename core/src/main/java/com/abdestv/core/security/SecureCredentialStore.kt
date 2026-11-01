package com.abdestv.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureCredentialStore(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "abdes_tv_secure_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.applicationContext.getSharedPreferences("abdes_tv_credentials_fallback", Context.MODE_PRIVATE)
    }

    fun savePassword(profileId: Long, password: String) {
        prefs.edit().putString("pwd_$profileId", password).apply()
    }

    fun getPassword(profileId: Long): String {
        return prefs.getString("pwd_$profileId", "") ?: ""
    }

    fun deletePassword(profileId: Long) {
        prefs.edit().remove("pwd_$profileId").apply()
    }

    fun saveParentalPin(pin: String) {
        prefs.edit().putString("parental_pin", pin).apply()
    }

    fun getParentalPin(): String {
        return prefs.getString("parental_pin", "") ?: ""
    }
}
