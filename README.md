# Android Music Player - Bound & Foreground Service Demo (Java)

Dự án mẫu minh họa đầy đủ kỹ thuật **Foreground Service kết hợp Bound Service (bindService)** trong Android bằng ngôn ngữ Java.

---

## 🌟 Đúng chuẩn yêu cầu đề bài
1. **Dùng Bound Service (`bindService` & `IBinder`)**:
   - `MainActivity` liên kết trực tiếp với `MusicService` qua `ServiceConnection`.
   - Điều khiển tương tác thời gian thực: **Play**, **Pause**, **Next**, **Prev**, và chọn bài từ ListView 10 bài hát.
   - Khi vào lại ứng dụng, Activity tự bind lại và cập nhật chính xác bài đang phát và trạng thái Play/Pause.
2. **Foreground Service (`startForegroundService` + Notification)**:
   - Khi thoát app hoặc bấm Home ra màn hình chính, Service **vẫn tiếp tục phát nhạc bình thường**.
   - Thanh thông báo hiển thị đầy đủ Notification với tên bài hát, nghệ sĩ và các nút điều khiển trực tiếp (**Play/Pause**, **Next**, **Prev**).
3. **Quản lý vòng đời chuẩn Android**:
   - Khởi chạy bằng `startForegroundService()` để đảm bảo tiến trình sống độc lập.
   - Gắn kết bằng `bindService()` trong `onStart()` và `unbindService()` trong `onStop()` để tránh rò rỉ bộ nhớ (leak connection).

---

## 🚀 Hướng dẫn chạy
1. Clone hoặc tải project về máy:
   ```bash
   git clone https://github.com/caohoangphucs/android-music-service-demo.git
   ```
2. Mở bằng **Android Studio**.
3. Bấm **Run** để chạy trên máy ảo hoặc thiết bị thật (Android 7.0 - 14+).
