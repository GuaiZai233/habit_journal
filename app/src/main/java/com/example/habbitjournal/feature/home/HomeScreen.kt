package com.example.habbitjournal.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddTodayLog: () -> Unit,
    onSyncNow: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("好习惯日志", style = MaterialTheme.typography.titleLarge)
                val dayText = uiState.daysSinceLast?.toString() ?: "-"
                Text("距离上次好习惯已过 $dayText 天", style = MaterialTheme.typography.headlineSmall)
                Text("最近记录日期：${uiState.lastRecordDateText}")
            }
        }
        Button(onClick = onAddTodayLog, modifier = Modifier.fillMaxWidth()) {
            Text("今天完成 +1")
        }
        Button(
            onClick = onSyncNow,
            enabled = !uiState.isSyncing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("同步中...")
                }
            } else {
                Text("立即同步")
            }
        }
        if (uiState.syncMessage.isNotBlank()) {
            Text(uiState.syncMessage)
        }
    }
}