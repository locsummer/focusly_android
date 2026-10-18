# Focusly: Screen Time (Android)

Ứng dụng quản lý thời gian sử dụng thiết bị và tập trung cho nền tảng Android, được phát triển bằng **Kotlin & Jetpack Compose** theo mô hình **Clean Architecture + MVVM**.

## 1. Tính năng cốt lõi
1. **Screen Time Tracking**: Đo lường tổng thời gian dùng máy trong ngày, số lần mở máy (device unlocks) và chi tiết từng ứng dụng thông qua `UsageStatsManager`.
2. **App Blocking & Routine Schedules**: Chặn ứng dụng gây xao nhãng theo thời gian biểu hoặc khi bật chế độ Focus bằng `AccessibilityService` (`FocuslyAccessibilityService`) và `SYSTEM_ALERT_WINDOW`.
3. **App Usage Limits**: Đặt hạn mức số phút tối đa cho từng app; cảnh báo thông minh tại mốc 80% và khóa ứng dụng tại mốc 100%.
4. **Focus Time / Pomodoro Timer**: Đếm giờ tập trung (Pomodoro, Short Break, Deep Work) sử dụng `ForegroundService` (`FocusTimerService`) với sticky notification và Strict Mode chặn các app gây mất tập trung.
5. **Daily Insights & Reports**: Biểu đồ trực quan xu hướng dùng máy trong tuần, phân tích tiến độ thay đổi thói quen.
6. **Smart Reminders**: Worker nền kiểm tra hạn mức bằng `WorkManager` và gửi cảnh báo `NotificationCompat`.

## 2. Kiến trúc & Công nghệ
- **Ngôn ngữ**: Kotlin 2.0+
- **Giao diện**: Jetpack Compose (Material 3)
- **Kiến trúc**: Clean Architecture (Data, Domain, UI), MVVM, StateFlow, Coroutines
- **Cơ sở dữ liệu**: Room Database (`AppDatabase`, `AppLimitDao`, `BlockScheduleDao`, `FocusSessionDao`)
- **Tác vụ nền**:
  - `AccessibilityService`: Bắt sự kiện chuyển ứng dụng `TYPE_WINDOW_STATE_CHANGED` tức thì.
  - `ForegroundService`: Chạy bộ đếm giờ Pomodoro với `specialUse` subtype.
  - `WorkManager`: Chạy kiểm tra định kỳ 15 phút.

## 3. Cấu trúc thư mục
```
focusly_android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/focusly/app/
│       │   ├── FocuslyApplication.kt
│       │   ├── data/ (local/dao, local/entity, repository)
│       │   ├── domain/ (model, manager)
│       │   ├── service/ (FocuslyAccessibilityService, FocusTimerService)
│       │   ├── receiver/ (BootReceiver)
│       │   ├── worker/ (UsageLimitWorker)
│       │   └── ui/ (dashboard, blocking, focus, statistics, permissions, overlay, theme)
│       └── res/ (values, xml)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 4. Hướng dẫn chạy trên Android Studio
1. Mở thư mục `focusly_android` bằng **Android Studio (Iguana, Jellyfish, Koala hoặc mới hơn)**.
2. Đồng bộ Gradle (Sync Project with Gradle Files).
3. Chạy trên thiết bị thật hoặc máy ảo (Android 8.0 - API 26 trở lên, khuyến nghị Android 12+).
4. Khi mở app lần đầu, màn hình **Permission Setup** sẽ hướng dẫn cấp 3 quyền:
   - **Usage Access** (`PACKAGE_USAGE_STATS`)
   - **Display over other apps** (`SYSTEM_ALERT_WINDOW`)
   - **Accessibility Service** (`Focusly App Blocker Service`)
