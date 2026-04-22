package com.example.habbitjournal.feature.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val WeekLabels = listOf("日", "一", "二", "三", "四", "五", "六")

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onIncreaseCount: () -> Unit,
    onDecreaseCount: () -> Unit,
    onUpdateCount: (Int) -> Unit,
    onSaveCount: () -> Unit,
) {
    val placeholderCellColor = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        Color(0xFF2B2E34)
    } else {
        Color(0xFFF1F2F6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onPrevMonth) { Text("上一月") }
            Text(uiState.month.toString(), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onNextMonth) { Text("下一月") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeekLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        AnimatedContent(
            targetState = uiState.month,
            transitionSpec = {
                val forward = targetState > initialState
                val enterFrom = if (forward) 1 else -1
                val exitTo = if (forward) -1 else 1
                (slideInHorizontally(
                    animationSpec = tween(280),
                    initialOffsetX = { fullWidth -> fullWidth * enterFrom },
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(280),
                    targetOffsetX = { fullWidth -> fullWidth * exitTo },
                )).using(SizeTransform(clip = false))
            },
            label = "calendar-month-switch",
            modifier = Modifier.weight(1f),
        ) { month ->
            MonthGrid(
                month = month,
                dailyCount = uiState.dailyCount,
                placeholderColor = placeholderCellColor,
                onDayClick = onDayClick,
            )
        }

        AnimatedContent(
            targetState = uiState.selectedDate,
            transitionSpec = {
                if (targetState == null) {
                    // 面板关闭：向下滑出
                    fadeIn() togetherWith slideOutVertically(targetOffsetY = { it }) + fadeOut()
                } else {
                    // 只要有选中日期（打开或切换），都执行从下往上弹出，旧的向下滑出
                    (slideInVertically(initialOffsetY = { it }) + fadeIn()) togetherWith 
                    (slideOutVertically(targetOffsetY = { it }) + fadeOut())
                }
            },
            label = "editor-anim",
        ) { date ->
            if (date != null) {
                // 缓存最新打开的值，防止在关闭动画中计数器突然清零或错位
                var cachedCount by remember { mutableStateOf(uiState.selectedDateCount) }
                if (date == uiState.selectedDate) {
                    cachedCount = uiState.selectedDateCount
                }
                
                SelectedDateEditor(
                    selectedDate = date,
                    count = cachedCount,
                    onIncreaseCount = onIncreaseCount,
                    onDecreaseCount = onDecreaseCount,
                    onUpdateCount = onUpdateCount,
                    onSaveCount = onSaveCount,
                    onCancel = onClearSelection,
                )
            } else {
                // 空状态，用于动画过渡时占位
                Box(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    dailyCount: Map<Int, Int>,
    placeholderColor: Color,
    onDayClick: (Int) -> Unit,
) {
    val weekRows = buildWeekRows(month)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weekRows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                week.forEach { day ->
                    val count = if (day != null) dailyCount[day] ?: 0 else 0
                    DayCell(
                        day = day,
                        count = count,
                        placeholderColor = placeholderColor,
                        onDayClick = { if (day != null) onDayClick(day) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    count: Int,
    placeholderColor: Color,
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (day == null) placeholderColor else MaterialTheme.colorScheme.surfaceVariant)
            .then(if (day != null) Modifier.clickable { onDayClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Text(day.toString(), style = MaterialTheme.typography.labelMedium)
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                if (count > 1) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private fun buildWeekRows(month: YearMonth): List<List<Int?>> {
    val daysInMonth = month.lengthOfMonth()
    val firstDay = month.atDay(1).dayOfWeek
    val leadingEmpty = sundayBasedIndex(firstDay)
    val baseCells = List(leadingEmpty) { null } + (1..daysInMonth).toList()
    val totalCells = if (baseCells.size <= 35) 35 else 42
    val paddedCells = baseCells + List(totalCells - baseCells.size) { null }
    return paddedCells.chunked(7)
}

private fun sundayBasedIndex(dayOfWeek: DayOfWeek): Int {
    return dayOfWeek.value % 7
}

@Composable
private fun SelectedDateEditor(
    selectedDate: LocalDate,
    count: Int,
    onIncreaseCount: () -> Unit,
    onDecreaseCount: () -> Unit,
    onUpdateCount: (Int) -> Unit,
    onSaveCount: () -> Unit,
    onCancel: () -> Unit,
) {
    val countText = remember { mutableStateOf(count.toString()) }

    LaunchedEffect(count) {
        countText.value = count.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "编辑日期：$selectedDate",
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onDecreaseCount) {
                Text("−")
            }

            OutlinedTextField(
                value = countText.value,
                onValueChange = { newValue ->
                    countText.value = newValue
                    newValue.toIntOrNull()?.let { onUpdateCount(it) }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            Button(onClick = onIncreaseCount) {
                Text("+")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text("取消")
            }
            Button(
                onClick = onSaveCount,
                modifier = Modifier.weight(1f),
            ) {
                Text("保存")
            }
        }
    }
}