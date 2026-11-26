# Android Music Service Demo (Java)

Dự án mẫu minh họa **Service trong Android** (Foreground Service & Background Service) phát nhạc nền bằng Java.

---

## 🌟 Chức năng chính
1. **Background Service**:
   - Khởi chạy Service phát nhạc thông thường (`startService()`).
   - Nhạc phát trong background nhưng không hiển thị Notification cố định (có nguy cơ bị hệ điều hành giải phóng khi thiếu RAM).
2. **Foreground Service**:
   - Khởi chạy Service mức ưu tiên cao (`startForegroundService()` + `startForeground()`).
   - Hiển thị Notification liên tục trên thanh thông báo, đảm bảo tiến trình phát nhạc không bị hệ thống tự động tắt khi ẩn app hoặc tắt màn hình.
   - Hỗ trợ đầy đủ quyền Media Playback và Notification trên Android 13/14 (`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`).
3. **Stop Service**:
   - Dừng phát nhạc và hủy giải phóng tài nguyên MediaPlayer.

---

## 🛠 Cấu trúc Source Code
- `app/src/main/AndroidManifest.xml`: Khai báo quyền, Foreground Service type `mediaPlayback`.
- `MusicService.java`: Điều khiển vòng đời Service, quản lý `MediaPlayer` và cấu hình `NotificationChannel`.
- `MainActivity.java`: Giao diện điều khiển (Start Background / Start Foreground / Stop) và cơ chế xin quyền động runtime permission.
- `res/raw/demo_music.mp3`: File âm thanh demo đi kèm sẵn trong project.

---

## 🚀 Hướng dẫn mở và chạy trên Android Studio
1. Mở **Android Studio** -> Chọn **Open** -> Trỏ đến thư mục `android-music-service-demo`.
2. Đợi Gradle sync hoàn tất.
3. Nhấn **Run (Shift + F10)** trên máy ảo hoặc thiết bị Android thật (hỗ trợ Android 7.0 - Android 14+).
