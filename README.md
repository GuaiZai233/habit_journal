# habbit_journal

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-14+-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Go](https://img.shields.io/badge/Go-1.20+-00ADD8?logo=go&logoColor=white)](https://golang.org/)
[![Docker](https://img.shields.io/badge/Docker-Latest-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MPL%202.0-brightgreen)](LICENSE)

完全采用 Vibe Coding 而成的好习惯记录应用。

## 架构
基于最新 `CLAUDE.md` 的实现：

- Android 三页：主页 / 日历 / 设置
- 主页卡片：展示“距离上次好习惯已过 X 天”
- 日历页：有记录显示圆点，次数大于 1 显示数字
- 设置页：服务器地址、手动同步、CSV 导出、许可证与 GitHub 地址
- Go 后端：自托管，提供日志增量同步 API

## Android

关键目录：

- `app/src/main/java/com/example/habbitjournal/feature/home`
- `app/src/main/java/com/example/habbitjournal/feature/calendar`
- `app/src/main/java/com/example/habbitjournal/feature/settings`
- `app/src/main/java/com/example/habbitjournal/data/repository/GoodHabitRepositoryImpl.kt`

构建：

```bash
./gradlew :app:assembleDebug
```

## Backend

关键接口：

- `GET /api/v1/health`
- `PUT /api/v1/logs/{date}`
- `GET /api/v1/logs?from=yyyy-mm-dd&to=yyyy-mm-dd`
- `POST /api/v1/sync/push`
- `GET /api/v1/sync/pull?since=RFC3339`

启动：

```bash
cd server
docker compose up -d
```

## 数据表

`good_habit_logs`

- `record_date` (PK)
- `count`
- `updated_at`
- `deleted_at`
