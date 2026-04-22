package com.example.habbitjournal.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class SettingsSection {
    SERVER,
    SYNC,
    EXPORT,
    ABOUT,
}

private const val PROJECT_GITHUB_URL = "https://github.com/GuaiZai233/habit_journal"
private val SaveSuccessGreen = Color(0xFF2E7D32)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveServerUrl: (String) -> Unit,
    onSyncNow: () -> Unit,
    onExportCsv: () -> Unit,
) {
    var selectedSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }

    BackHandler(enabled = selectedSection != null) {
        selectedSection = null
    }

    AnimatedContent(
        targetState = selectedSection,
        transitionSpec = {
            val openingDetail = initialState == null && targetState != null
            val enterFrom = if (openingDetail) 1 else -1
            val exitTo = if (openingDetail) -1 else 1

            (slideInHorizontally(
                animationSpec = tween(280),
                initialOffsetX = { fullWidth -> fullWidth * enterFrom },
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(280),
                targetOffsetX = { fullWidth -> fullWidth * exitTo },
            )).using(SizeTransform(clip = false))
        },
        label = "settings-section-switch",
    ) { currentSection ->
        if (currentSection == null) {
            SettingsMenu(
                onOpenServer = { selectedSection = SettingsSection.SERVER },
                onOpenSync = { selectedSection = SettingsSection.SYNC },
                onOpenExport = { selectedSection = SettingsSection.EXPORT },
                onOpenAbout = { selectedSection = SettingsSection.ABOUT },
            )
        } else {
            SettingsDetailLayout(
                title = when (currentSection) {
                    SettingsSection.SERVER -> "服务器设置"
                    SettingsSection.SYNC -> "同步设置"
                    SettingsSection.EXPORT -> "导出"
                    SettingsSection.ABOUT -> "关于"
                },
            ) {
                when (currentSection) {
                    SettingsSection.SERVER -> {
                        var serverUrl by remember(uiState.serverUrl) { mutableStateOf(uiState.serverUrl) }

                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            label = { Text("服务器地址") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Button(
                            onClick = { onSaveServerUrl(serverUrl) },
                            enabled = !uiState.isSavingServer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (uiState.isSavingServer) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("检测连通性...")
                            } else {
                                Text("确认")
                            }
                        }

                        if (uiState.serverSaveMessage.isNotBlank()) {
                            val color = when (uiState.serverSaveSucceeded) {
                                true -> SaveSuccessGreen
                                false -> MaterialTheme.colorScheme.error
                                null -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(
                                uiState.serverSaveMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                            )
                        }
                    }

                    SettingsSection.SYNC -> {
                        Button(onClick = onSyncNow, modifier = Modifier.fillMaxWidth()) {
                            Text("立即同步")
                        }
                        if (uiState.syncMessage.isNotBlank()) {
                            Text(uiState.syncMessage)
                        }
                    }

                    SettingsSection.EXPORT -> {
                        Button(onClick = onExportCsv, modifier = Modifier.fillMaxWidth()) {
                            Text("导出 CSV")
                        }
                        if (uiState.csvContent.isNotBlank()) {
                            Text("CSV 内容（可复制）：", style = MaterialTheme.typography.titleSmall)
                            Text(uiState.csvContent, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    SettingsSection.ABOUT -> {
                        Text("开源许可证：MPL-2.0")
                        Text("GitHub：$PROJECT_GITHUB_URL")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenu(
    onOpenServer: () -> Unit,
    onOpenSync: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("设置", style = MaterialTheme.typography.titleLarge)

        SettingsMenuCard(
            title = "服务器设置",
            description = "配置服务器地址",
            onClick = onOpenServer,
        )
        SettingsMenuCard(
            title = "同步设置",
            description = "手动触发云端同步并查看结果",
            onClick = onOpenSync,
        )
        SettingsMenuCard(
            title = "导出",
            description = "导出好习惯记录为 CSV",
            onClick = onOpenExport,
        )
        SettingsMenuCard(
            title = "关于",
            description = "查看许可证和项目信息",
            onClick = onOpenAbout,
        )
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
            Text("＞", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SettingsDetailLayout(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }

        content()
    }
}