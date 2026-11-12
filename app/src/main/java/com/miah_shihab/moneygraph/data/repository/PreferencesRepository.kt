package com.miah_shihab.moneygraph.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.miah_shihab.moneygraph.util.DEFAULT_CURRENCY_SYMBOL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_THEME_COLOR = longPreferencesKey("theme_color")
        val KEY_DARK_MODE = intPreferencesKey("dark_mode") // 0=System, 1=Light, 2=Dark
        val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val KEY_PIN = stringPreferencesKey("app_pin")
        val KEY_USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
        val KEY_GOOGLE_EMAIL = stringPreferencesKey("google_account_email")
        val KEY_LAST_BACKUP = longPreferencesKey("last_backup_timestamp")
    }

    val themeColorFlow: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_THEME_COLOR] ?: 0xFF4285F4L // Default to Material Google Blue
    }

    val darkModeFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: 0 // Default System
    }

    val currencySymbolFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_CURRENCY_SYMBOL] ?: DEFAULT_CURRENCY_SYMBOL
    }

    val pinFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_PIN] ?: ""
    }

    val useBiometricFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USE_BIOMETRIC] ?: false
    }

    val googleAccountEmailFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_GOOGLE_EMAIL] ?: ""
    }

    val lastBackupTimeFlow: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_BACKUP] ?: 0L
    }

    suspend fun setThemeColor(color: Long) {
        dataStore.edit { it[KEY_THEME_COLOR] = color }
    }

    suspend fun setDarkMode(mode: Int) {
        dataStore.edit { it[KEY_DARK_MODE] = mode }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { it[KEY_CURRENCY_SYMBOL] = symbol }
    }

    suspend fun setPin(pin: String) {
        dataStore.edit { it[KEY_PIN] = pin }
    }

    suspend fun setUseBiometric(enabled: Boolean) {
        dataStore.edit { it[KEY_USE_BIOMETRIC] = enabled }
    }

    suspend fun setGoogleAccountEmail(email: String) {
        dataStore.edit { it[KEY_GOOGLE_EMAIL] = email }
    }

    suspend fun setLastBackupTime(time: Long) {
        dataStore.edit { it[KEY_LAST_BACKUP] = time }
    }
}
