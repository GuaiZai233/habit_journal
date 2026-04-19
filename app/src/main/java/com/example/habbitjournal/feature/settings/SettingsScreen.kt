package com.example.habbitjournal.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveServerUrl: (String) -> Unit,
    onSaveGithubUrl: (String) -> Unit,
    onSyncNow: () -> Unit,
    onExportCsv: () -> Unit,
) {
    var serverUrl by remember(uiState.serverUrl) { mutableStateOf(uiState.serverUrl) }
    var githubUrl by remember(uiState.githubUrl) { mutableStateOf(uiState.githubUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("设置", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("服务器地址") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(onClick = { onSaveServerUrl(serverUrl) }, modifier = Modifier.fillMaxWidth()) {
            Text("保存服务器地址")
        }

        OutlinedTextField(
            value = githubUrl,
            onValueChange = { githubUrl = it },
            label = { Text("GitHub 地址") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(onClick = { onSaveGithubUrl(githubUrl) }, modifier = Modifier.fillMaxWidth()) {
            Text("保存 GitHub 地址")
        }

        Button(onClick = onSyncNow, modifier = Modifier.fillMaxWidth()) {
            Text("立即同步")
        }
        if (uiState.syncMessage.isNotBlank()) {
            Text(uiState.syncMessage)
        }

        Button(onClick = onExportCsv, modifier = Modifier.fillMaxWidth()) {
            Text("导出 CSV")
        }
        if (uiState.csvContent.isNotBlank()) {
            Text("CSV 内容（可复制）：", style = MaterialTheme.typography.titleSmall)
            Text(uiState.csvContent, style = MaterialTheme.typography.bodySmall)
        }

        Text("开源许可证：Apache-2.0")
        Text("GitHub：${uiState.githubUrl}")
    }
}
