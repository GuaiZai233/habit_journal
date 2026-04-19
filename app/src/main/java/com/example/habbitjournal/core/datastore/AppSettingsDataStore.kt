package com.example.habbitjournal.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val serverUrlKey = stringPreferencesKey("server_url")
    private val githubUrlKey = stringPreferencesKey("github_url")

    val serverUrl: Flow<String> = context.appSettingsDataStore.data.map { prefs ->
        prefs[serverUrlKey] ?: ""
    }

    val githubUrl: Flow<String> = context.appSettingsDataStore.data.map { prefs ->
        prefs[githubUrlKey] ?: "https://github.com/"
    }

    suspend fun setServerUrl(url: String) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[serverUrlKey] = url.trim().trimEnd('/')
        }
    }

    suspend fun setGithubUrl(url: String) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[githubUrlKey] = url.trim()
        }
    }
}
