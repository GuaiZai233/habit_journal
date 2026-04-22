package com.example.habbitjournal.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class SettingsSection {
    SERVER,
    EXPORT,
    ABOUT,
    LICENSES,
}

private const val PROJECT_GITHUB_URL = "https://github.com/GuaiZai233/habit_journal"

private fun sectionDepth(section: SettingsSection?): Int {
    return when (section) {
        null -> 0
        SettingsSection.SERVER -> 1
        SettingsSection.EXPORT -> 1
        SettingsSection.ABOUT -> 1
        SettingsSection.LICENSES -> 2
    }
}
private val SaveSuccessGreen = Color(0xFF2E7D32)

private data class OpenSourceNotice(
    val name: String,
    val license: String,
)

private val OpenSourceNotices = listOf(
    OpenSourceNotice("AndroidX Core / Lifecycle / Activity / Navigation", "Apache-2.0"),
    OpenSourceNotice("Jetpack Compose UI / Material3 / Material Icons", "Apache-2.0"),
    OpenSourceNotice("Kotlin Coroutines", "Apache-2.0"),
    OpenSourceNotice("Kotlinx Serialization", "Apache-2.0"),
    OpenSourceNotice("Hilt (Dagger)", "Apache-2.0"),
    OpenSourceNotice("Room", "Apache-2.0"),
    OpenSourceNotice("DataStore", "Apache-2.0"),
    OpenSourceNotice("OkHttp", "Apache-2.0"),
)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSaveServerUrl: (String) -> Unit,
    onExportCsv: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var selectedSection by rememberSaveable { mutableStateOf<SettingsSection?>(null) }

    BackHandler(enabled = selectedSection != null) {
        selectedSection = null
    }

    AnimatedContent(
        targetState = selectedSection,
        transitionSpec = {
            val movingForward = sectionDepth(targetState) > sectionDepth(initialState)
            val enterFrom = if (movingForward) 1 else -1
            val exitTo = if (movingForward) -1 else 1

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
                onOpenExport = { selectedSection = SettingsSection.EXPORT },
                onOpenAbout = { selectedSection = SettingsSection.ABOUT },
            )
        } else {
            SettingsDetailLayout(
                title = when (currentSection) {
                    SettingsSection.SERVER -> "服务器设置"
                    SettingsSection.EXPORT -> "导出"
                    SettingsSection.ABOUT -> "关于"
                    SettingsSection.LICENSES -> "许可证"
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
                        SettingsMenuCard(
                            title = "项目地址",
                            description = PROJECT_GITHUB_URL,
                            onClick = { uriHandler.openUri(PROJECT_GITHUB_URL) },
                        )
                        SettingsMenuCard(
                            title = "许可证",
                            description = "查看本项目与第三方组件许可声明",
                            onClick = { selectedSection = SettingsSection.LICENSES },
                        )
                    }

                    SettingsSection.LICENSES -> {
                        Text("本项目许可证：MPL-2.0", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "下列第三方开源项目及组件按其各自许可证分发：",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OpenSourceNotices.forEach { notice ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(notice.name, style = MaterialTheme.typography.bodyMedium)
                                    Text("许可证：${notice.license}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMenu(
    onOpenServer: () -> Unit,
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
            title = "导出",
            description = "导出好习惯记录为 CSV",
            onClick = onOpenExport,
        )
        SettingsMenuCard(
            title = "关于",
            description = "查看项目信息与许可证",
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