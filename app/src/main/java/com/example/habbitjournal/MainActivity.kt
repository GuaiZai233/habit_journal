package com.example.habbitjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.habbitjournal.core.ui.theme.HabbitJournalTheme
import com.example.habbitjournal.navigation.AppNavRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabbitJournalTheme {
                AppNavRoot()
            }
        }
    }
}
