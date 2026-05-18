# Healthcare Android App

Ứng dụng chăm sóc sức khỏe **Android Native** viết bằng **Java + XML**, dùng **Firebase** cho xác thực, dữ liệu thời gian thực, lưu trữ tệp và dữ liệu seed demo.

> Tên thư mục dự án vẫn là `AI-Powered Healthcare App Design`, nhưng phần đã xây dựng hiện tại là một app Android native. Trong source hiện chưa có module AI/backend riêng.

## Tổng quan

```text
Android app (Java + XML + ViewBinding)
        |
        | Firebase SDK
        v
Firebase Authentication
Cloud Firestore
Firebase Storage
```

App không có backend server riêng. Các thao tác đăng nhập, đăng ký, đọc/ghi hồ sơ, lịch hẹn, thanh toán, thông báo, cấu hình người dùng và tệp đính kèm đều đi qua Firebase.

## Tính năng đã xây dựng

- Onboarding 3 màn hình, đăng nhập email/password, đăng nhập Google, đăng ký, quên mật khẩu và đổi mật khẩu.
- Trang Home hiển thị bệnh viện gần đây, bác sĩ nổi bật, lối tắt đặt lịch, hồ sơ y tế, tìm bác sĩ và bảo hiểm.
- Tìm kiếm bác sĩ/bệnh viện với bộ lọc, chip chuyên khoa, khoảng giá, đánh giá và kinh nghiệm.
- Chi tiết bác sĩ, chi tiết bệnh viện, danh sách bác sĩ theo bệnh viện.
- Luồng đặt lịch: chọn dịch vụ, ngày, giờ, bệnh nhân/người thân, triệu chứng, thanh toán và xác nhận.
- Thanh toán tạo đồng thời bản ghi `payments` và `appointments` bằng Firestore `WriteBatch`.
- Quản lý lịch hẹn: xem theo trạng thái, check-in, hủy lịch, đổi lịch, mở hóa đơn PDF và đánh giá sau khám.
- Check-in bằng QR/appointment id, cập nhật trạng thái hàng đợi và hỗ trợ màn hình scan cho nhân viên.
- Hồ sơ y tế, chi tiết hồ sơ, mở link đính kèm và upload tệp lên Firebase Storage.
- Thông báo, đánh dấu tất cả là đã đọc.
- Bảo hiểm y tế.
- Hồ sơ cá nhân: xem/cập nhật thông tin, upload ảnh đại diện, đăng xuất.
- Quản lý người thân, phương thức thanh toán, cài đặt app, lịch sử đăng nhập, hỗ trợ khách hàng.
- Dashboard admin dạng giao diện native code: xem collection, cập nhật trạng thái bác sĩ, tạo voucher, tạo content banner và mở màn hình staff scan.
- Nhắc lịch khám trước 1 giờ bằng `AlarmManager` + `BroadcastReceiver`.
- Tạo hóa đơn PDF bằng `PdfDocument`.

## Cấu trúc dự án

```text
AI-Powered Healthcare App Design/
|-- android/                         Android native app
|   |-- build.gradle                 Root Gradle config, AGP 8.2.2
|   |-- settings.gradle              Project name: HealthcareApp
|   |-- gradlew / gradlew.bat        Gradle Wrapper
|   `-- app/
|       |-- build.gradle             App config, SDK 34, Firebase, Glide, ZXing
|       |-- google-services.json     Firebase config đang dùng để chạy app
|       |-- google-services.json.example
|       `-- src/main/
|           |-- AndroidManifest.xml
|           |-- java/com/healthcare/app/
|           |   |-- activity/        27 Activity
|           |   |-- adapter/         6 RecyclerView adapter
|           |   |-- model/           8 model class
|           |   |-- receiver/        ReminderReceiver
|           |   `-- util/            ReceiptGenerator, ReminderScheduler
|           `-- res/
|               |-- layout/          32 XML layout/item/dialog
|               |-- drawable/
|               |-- menu/
|               |-- values/
|               `-- xml/
|-- firebase-seed/                   Công cụ seed dữ liệu Firestore
|   |-- seed-data.html               Seed bằng trình duyệt
|   |-- seed-data.html.example
|   |-- seed-firestore.js            Seed bằng Node.js/firebase-admin
|   `-- package.json
|-- How_To_Run.md                    Hướng dẫn chạy chi tiết
|-- USE_CASES.md                     Use case nghiệp vụ
|-- implementation_plan.md           Kế hoạch triển khai
`-- README.md
```

## Tech stack

| Nhóm | Công nghệ |
| --- | --- |
| Android | Java 17, Android SDK 34, minSdk 24, targetSdk 34 |
| UI | XML layout, ViewBinding, Material Components, AndroidX, RecyclerView, ViewPager2 |
| Auth | Firebase Authentication, Email/Password, Google Sign-In |
| Database | Cloud Firestore |
| Storage | Firebase Storage |
| Image loading | Glide 4.16 |
| QR | ZXing Android Embedded 4.3 |
| PDF/Reminder | `PdfDocument`, `AlarmManager`, `BroadcastReceiver` |
| Build | Gradle Wrapper, Android Gradle Plugin 8.2.2, Google Services plugin 4.4.0 |

## Màn hình chính

| Màn hình | Activity |
| --- | --- |
| Onboarding | `OnboardingActivity` |
| Login / Register / Forgot / Reset Password | `LoginActivity`, `RegisterActivity`, `ForgotPasswordActivity`, `ResetPasswordActivity` |
| Home | `HomeActivity` |
| Search | `SearchActivity` |
| Doctor / Hospital Detail | `DoctorDetailActivity`, `HospitalDetailActivity` |
| Booking / Payment / Confirmation | `BookingActivity`, `PaymentActivity`, `ConfirmationActivity` |
| Appointments / Check-in / Reschedule / Review | `AppointmentsActivity`, `CheckInActivity`, `RescheduleActivity`, `ReviewActivity` |
| Medical Records / Record Detail | `MedicalRecordsActivity`, `RecordDetailActivity` |
| Notifications | `NotificationsActivity` |
| Insurance | `InsuranceActivity` |
| Profile | `ProfileActivity` |
| Family Members | `FamilyMembersActivity` |
| Payment Methods | `PaymentMethodsActivity` |
| App Settings | `AppSettingsActivity` |
| Support | `SupportActivity` |
| Admin Dashboard / Staff Scan | `AdminDashboardActivity`, `StaffScanActivity` |

Một vài màn hình như `PaymentMethodsActivity`, `AppSettingsActivity`, `SupportActivity`, `AdminDashboardActivity` và `ReviewActivity` đang dựng UI bằng Java code thay vì XML layout riêng.

## Firestore collections hiện có

| Collection | Mục đích |
| --- | --- |
| `appointments` | Lịch hẹn, trạng thái, QR/check-in |
| `doctors` | Danh sách bác sĩ |
| `family_members` | Người thân của user |
| `hospitals` | Danh sách bệnh viện |
| `insurance` | Thông tin bảo hiểm |
| `medical_records` | Hồ sơ y tế |
| `notifications` | Thông báo trong app |
| `payment_methods` | Phương thức thanh toán đã lưu |
| `payments` | Giao dịch thanh toán |
| `user_settings` | Cài đặt notification/email/dark mode |
| `users` | Hồ sơ người dùng, avatar, thông tin liên hệ |

Một số màn hình trong code có thể tạo thêm collection khi dùng các tính năng tương ứng, ví dụ `reviews`, `support_tickets`, `vouchers`, `content_banners` hoặc subcollection `users/{uid}/login_history`. Các collection này chưa xuất hiện trong Firebase Console hiện tại nếu chưa có document nào được tạo.

## Luồng đặt lịch

```text
Search/Home
   -> DoctorDetailActivity
   -> BookingActivity
      - chọn dịch vụ
      - chọn ngày/giờ
      - chọn bệnh nhân hoặc người thân
      - nhập triệu chứng
   -> PaymentActivity
      - chọn phương thức thanh toán
      - ghi payments + appointments bằng WriteBatch
      - tạo notification
      - lên lịch reminder trước 1 giờ
   -> ConfirmationActivity
      - hiển thị thông tin xác nhận
      - mở Appointments hoặc tạo hóa đơn PDF
```

## Chạy dự án

### Yêu cầu

- Android Studio
- JDK 17 trở lên
- Android SDK 34
- Firebase project có Authentication, Cloud Firestore và Storage
- File `android/app/google-services.json`

Kiểm tra Java:

```powershell
java -version
```

### Cách 1: chạy bằng Android Studio

1. Mở **Android Studio**.
2. Chọn **File > Open**.
3. Chọn thư mục:

```text
AI-Powered Healthcare App Design/android
```

4. Đợi **Gradle Sync** hoàn tất.
5. Kiểm tra file Firebase config đã nằm đúng vị trí:

```text
android/app/google-services.json
```

6. Chọn emulator hoặc thiết bị thật.
7. Nhấn **Run** hoặc dùng phím tắt **Shift + F10**.

Nếu chưa có emulator:

1. Vào **Tools > Device Manager**.
2. Chọn **Create Virtual Device**.
3. Chọn thiết bị, ví dụ Pixel 6.
4. Chọn system image Android API 34.
5. Tạo emulator rồi chạy app.

### Cách 2: build APK bằng Gradle

Trên Windows PowerShell:

```powershell
cd android
.\gradlew.bat assembleDebug
```

APK debug sau khi build nằm tại:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

### Cách 3: cài APK vào thiết bị/emulator bằng adb

Sau khi đã build APK:

```powershell
cd android
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Nếu máy không nhận `adb`, mở terminal trong Android Studio hoặc thêm Android SDK `platform-tools` vào PATH.

### Đăng nhập demo

```text
Email: sarah.williams@email.com
Password: password123
```

Tài khoản này chỉ dùng được khi đã tạo user tương ứng trong Firebase Authentication và seed dữ liệu theo UID thật của user.

### Seed dữ liệu trước khi chạy thử đầy đủ

App vẫn có thể mở nếu chưa seed, nhưng các màn hình như Home, Search, Appointments, Medical Records, Notifications và Insurance cần dữ liệu Firestore để hiển thị đầy đủ.

Các bước nhanh:

1. Tạo hoặc kiểm tra user demo trong Firebase Authentication.
2. Lấy UID của user demo.
3. Mở `firebase-seed/seed-data.html` bằng trình duyệt.
4. Nhập Firebase config và UID.
5. Chạy seed để tạo hospitals, doctors, appointments, medical records, notifications và insurance.

Có thể dùng script Node.js thay thế:

```powershell
cd firebase-seed
npm install
node seed-firestore.js
```

Với cách Node.js, cần sửa `USER_UID` trong `seed-firestore.js` thành UID thật trước khi chạy.

### Lỗi thường gặp

| Lỗi | Cách xử lý |
| --- | --- |
| Gradle Sync failed | Mở đúng thư mục `android/`, kiểm tra JDK 17 và kết nối internet để tải dependencies |
| `google-services.json` missing | Đặt file Firebase config vào `android/app/google-services.json` |
| Không đăng nhập Google được | Cập nhật `default_web_client_id` trong `android/app/src/main/res/values/strings.xml` |
| App đăng nhập được nhưng không có dữ liệu | Seed Firestore bằng UID thật của user đang đăng nhập |
| Không nhận emulator/thiết bị | Kiểm tra Device Manager, bật USB debugging nếu dùng máy thật |

## Seed dữ liệu demo

Thư mục `firebase-seed/` có 2 cách seed:

- `seed-data.html`: mở bằng trình duyệt, nhập Firebase config/UID rồi seed trực tiếp.
- `seed-firestore.js`: dùng Node.js với `firebase-admin`.

Dữ liệu seed hiện bao gồm:

- 5 bệnh viện
- 5 bác sĩ
- 1 user demo nếu điền UID thật
- 4 lịch hẹn
- 4 hồ sơ y tế
- 5 thông báo
- 1 bảo hiểm

Tài khoản demo được ghi trong script seed:

```text
Email: sarah.williams@email.com
Password: password123
```

Lưu ý: cần tạo user này trong Firebase Authentication trước, sau đó dùng UID thật để seed các collection gắn với user.

## Cấu hình cần chú ý

- `android/app/google-services.json` đang được app dùng để kết nối Firebase. File `.example` chỉ là mẫu.
- Google Sign-In cần cấu hình `default_web_client_id` trong `android/app/src/main/res/values/strings.xml`; hiện đang để placeholder `YOUR_WEB_CLIENT_ID_HERE`.
- Các quyền đã khai báo: Internet, network state, camera, exact alarm và post notifications.
- App có sẵn `FileProvider` để mở hóa đơn PDF/tệp nội bộ.
- Một số dữ liệu ngày trong seed đang là dữ liệu demo, không tự cập nhật theo ngày hiện tại.

## Ghi chú về phần web/package root

Ở root có `package.json` theo dạng Vite/React prototype từ công cụ thiết kế, nhưng source app đang được triển khai chính trong `android/`. README này mô tả phần Android native đã xây dựng hiện tại.
