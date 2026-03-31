# AI-Powered Healthcare App

Ứng dụng chăm sóc sức khỏe **Android Native** (Java) sử dụng **Firebase** (Authentication, Cloud Firestore, Cloud Storage).

---

## Mục lục

1. [Tổng quan kiến trúc](#tổng-quan-kiến-trúc)
2. [Cấu trúc dự án](#cấu-trúc-dự-án)
3. [Yêu cầu cài đặt](#yêu-cầu-cài-đặt)
4. [Hướng dẫn cài đặt & chạy](#hướng-dẫn-cài-đặt--chạy)
5. [Tài khoản mặc định](#tài-khoản-mặc-định)
6. [Cấu trúc Firestore Database](#cấu-trúc-firestore-database)
7. [Màn hình ứng dụng](#màn-hình-ứng-dụng)
8. [Tech Stack](#tech-stack)

---

## Tổng quan kiến trúc

```
┌──────────────────┐                    ┌──────────────────────────────────────┐
│                  │    Firebase SDK     │            Firebase (Google)         │
│   Android App    │ ◄───────────────►  │                                      │
│   (Java + XML)   │                    │  🔐 Authentication (Email/Password)  │
│                  │                    │  📦 Cloud Firestore (NoSQL Database) │
│   Material 3 UI  │                    │  📁 Cloud Storage (Files/Images)     │
│                  │                    │                                      │
└──────────────────┘                    └──────────────────────────────────────┘
   Android Studio                           Firebase Console (web)
```

**Không cần backend server riêng** — Firebase xử lý tất cả.

---

## Cấu trúc dự án

```
AI-Powered Healthcare App Design/
│
├── android/                              ← Android Native App
│   ├── build.gradle                      ← Root build (AGP 8.2.2, Google Services plugin)
│   ├── app/build.gradle                  ← App build (Firebase BOM, Glide, ZXing)
│   ├── settings.gradle                   ← Project: HealthcareApp
│   ├── gradle.properties                 ← JVM args, AndroidX
│   ├── local.properties                  ← Android SDK path
│   ├── gradlew.bat / gradlew            ← Gradle Wrapper (8.5)
│   ├── gradle/wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── app/src/main/
│       ├── AndroidManifest.xml
│       ├── google-services.json          ← ⚠️ BẠN CẦN TẠO FILE NÀY (xem hướng dẫn)
│       ├── java/com/healthcare/app/
│       │   ├── activity/                 ← 18 Activity classes
│       │   │   ├── OnboardingActivity    ← Màn hình giới thiệu (LAUNCHER)
│       │   │   ├── LoginActivity         ← Firebase Auth đăng nhập
│       │   │   ├── RegisterActivity      ← Firebase Auth đăng ký
│       │   │   ├── ForgotPasswordActivity← Firebase Auth reset password
│       │   │   ├── ResetPasswordActivity ← Đặt lại mật khẩu
│       │   │   ├── HomeActivity          ← Trang chủ (Firestore: doctors, hospitals)
│       │   │   ├── SearchActivity        ← Tìm kiếm (local filter)
│       │   │   ├── DoctorDetailActivity  ← Chi tiết bác sĩ (Firestore)
│       │   │   ├── HospitalDetailActivity← Chi tiết bệnh viện (Firestore)
│       │   │   ├── BookingActivity       ← Đặt lịch khám (Firestore)
│       │   │   ├── PaymentActivity       ← Thanh toán (Firestore)
│       │   │   ├── ConfirmationActivity  ← Xác nhận đặt lịch
│       │   │   ├── AppointmentsActivity  ← Danh sách lịch hẹn (Firestore query)
│       │   │   ├── CheckInActivity       ← Check-in + QR Code (Firestore update)
│       │   │   ├── MedicalRecordsActivity← Hồ sơ y tế (Firestore query)
│       │   │   ├── NotificationsActivity ← Thông báo (Firestore batch update)
│       │   │   ├── InsuranceActivity     ← Bảo hiểm (Firestore)
│       │   │   └── ProfileActivity       ← Hồ sơ + Firebase Auth signOut
│       │   ├── adapter/                  ← 5 RecyclerView Adapters
│       │   └── model/                    ← 7 POJO Model classes (Firestore @DocumentId)
│       └── res/
│           ├── layout/                   ← 24 XML layouts
│           ├── drawable/                 ← 35 drawable XMLs
│           ├── menu/bottom_nav_menu.xml  ← Bottom Navigation (5 tabs)
│           ├── mipmap-*/                 ← App launcher icons
│           └── values/                   ← colors.xml, themes.xml, strings.xml
│
├── firebase-seed/                        ← Script seed data lên Firestore
│   ├── package.json                      ← Dependency: firebase-admin
│   ├── seed-firestore.js                 ← Script Node.js seed data
│   └── serviceAccountKey.json            ← ⚠️ BẠN CẦN TẢI FILE NÀY (xem hướng dẫn)
│
└── README.md                             ← File này
```

---

## Yêu cầu cài đặt

| # | Phần mềm | Phiên bản | Mục đích | Link tải |
|---|----------|-----------|----------|----------|
| 1 | **Java JDK** | 17+ | Build Android app | [Adoptium](https://adoptium.net/) |
| 2 | **Android Studio** | Latest | IDE phát triển Android | [Android Studio](https://developer.android.com/studio) |
| 3 | **Node.js** | 18+ | Chạy script seed data | [Node.js](https://nodejs.org/) |
| 4 | **Tài khoản Google** | — | Tạo Firebase project | [Firebase Console](https://console.firebase.google.com/) |

> **KHÔNG CẦN**: Docker, Oracle, Maven, Spring Boot — Firebase thay thế tất cả backend!

---

## Hướng dẫn cài đặt & chạy

### Thứ tự thực hiện

```
Bước 1           Bước 2              Bước 3           Bước 4            Bước 5
Tạo Firebase → Tải google-       → Seed data     → Tạo user       → Chạy app
Project         services.json       lên Firestore    trong Auth       trên emulator
(5 phút)        (2 phút)            (2 phút)         (2 phút)         (3 phút)
```

---

### Bước 1: Tạo Firebase Project

1. Vào **https://console.firebase.google.com/**
2. Đăng nhập bằng **tài khoản Google**
3. Click **"Create a project"**
4. Đặt tên: `healthcare-app` → **Continue**
5. Tắt Google Analytics → **Create project** → **Continue**

**Bật Authentication:**
1. Menu trái → **Build → Authentication** → **Get started**
2. Tab **Sign-in method** → Bật **Email/Password** → **Save**

**Tạo Firestore Database:**
1. Menu trái → **Build → Firestore Database** → **Create database**
2. Chọn **Start in test mode** → **Next**
3. Chọn location **asia-southeast1** (Singapore) → **Enable**

---

### Bước 2: Tải google-services.json

1. Trong Firebase Console → **Project Settings** (biểu tượng bánh răng)
2. Cuộn xuống phần **Your apps** → Click icon **Android**
3. Nhập package name: `com.healthcare.app` → **Register app**
4. Click **Download google-services.json**
5. Copy file vào:
   ```
   android/app/google-services.json
   ```
6. Click **Next** → **Next** → **Continue to console**

---

### Bước 3: Seed data lên Firestore

**3a. Tải Service Account Key:**
1. Firebase Console → **Project Settings → Service accounts**
2. Click **Generate new private key** → **Generate key**
3. Lưu file thành:
   ```
   firebase-seed/serviceAccountKey.json
   ```

**3b. Chạy script seed:**
```bash
cd firebase-seed
npm install
node seed-firestore.js
```

Kết quả:
```
Seeding Firestore data...
Adding hospitals...
  ✓ 4 hospitals added
Adding doctors...
  ✓ 5 doctors added
```

---

### Bước 4: Tạo user test trong Firebase Auth

1. Firebase Console → **Authentication → Users**
2. Click **Add user**
3. Nhập:
   - **Email**: `sarah.williams@email.com`
   - **Password**: `password123`
4. Click **Add user**
5. **Copy UID** của user vừa tạo (cột User UID)
6. Mở file `firebase-seed/seed-firestore.js`
7. Thay `REPLACE_WITH_ACTUAL_UID` bằng UID thực
8. Chạy lại:
   ```bash
   node seed-firestore.js
   ```
   Lần này sẽ thêm appointments, records, notifications, insurance.

---

### Bước 5: Chạy app Android

1. Mở **Android Studio**
2. **File → Open** → chọn thư mục `android/`
3. Đợi **Gradle Sync** hoàn tất
4. Tạo emulator: **Tools → Device Manager → Create Virtual Device → Pixel 6 → API 34**
5. Nhấn **Run ▶** (Shift + F10)
6. Đăng nhập: `sarah.williams@email.com` / `password123`

---

## Tài khoản mặc định

| Thông tin | Giá trị |
|-----------|---------|
| **Email** | `sarah.williams@email.com` |
| **Password** | `password123` |
| **Patient ID** | `PT-123456` |

---

## Cấu trúc Firestore Database

```
📁 Firestore Database
│
├── 📁 users (collection)
│   └── 📄 {uid} → { name, email, phone, address, patientId, uid }
│
├── 📁 hospitals (collection)
│   ├── 📄 hospital_1 → { name, image, rating, reviewCount, specialties, distance, ... }
│   ├── 📄 hospital_2
│   └── ...
│
├── 📁 doctors (collection)
│   ├── 📄 doctor_1 → { name, specialization, hospitalId, hospitalName, rating, consultationFee, ... }
│   ├── 📄 doctor_2
│   └── ...
│
├── 📁 appointments (collection)
│   └── 📄 auto-id → { appointmentId, userId, doctorId, doctorName, hospital, date, time, status, ... }
│
├── 📁 medical_records (collection)
│   └── 📄 auto-id → { recordId, userId, date, type, title, doctor, hospital, details }
│
├── 📁 notifications (collection)
│   └── 📄 auto-id → { notificationId, userId, type, title, message, time, isRead }
│
├── 📁 insurance (collection)
│   └── 📄 auto-id → { userId, provider, policyNumber, coverage, validUntil, status }
│
└── 📁 payments (collection)
    └── 📄 auto-id → { userId, doctorId, amount, paymentMethod, status, paymentId }
```

---

## Màn hình ứng dụng

| # | Màn hình | Activity | Firebase Service |
|---|----------|----------|-----------------|
| 1 | Onboarding | `OnboardingActivity` | — |
| 2 | Login | `LoginActivity` | Auth: `signInWithEmailAndPassword` |
| 3 | Register | `RegisterActivity` | Auth: `createUserWithEmailAndPassword` + Firestore |
| 4 | Forgot Password | `ForgotPasswordActivity` | Auth: `sendPasswordResetEmail` |
| 5 | Reset Password | `ResetPasswordActivity` | Auth: `updatePassword` |
| 6 | Home | `HomeActivity` | Firestore: `doctors`, `hospitals` |
| 7 | Search | `SearchActivity` | Firestore: `doctors`, `hospitals` + local filter |
| 8 | Doctor Detail | `DoctorDetailActivity` | Firestore: `doctors/{id}` |
| 9 | Hospital Detail | `HospitalDetailActivity` | Firestore: `hospitals/{id}` + `doctors` query |
| 10 | Booking | `BookingActivity` | Firestore: `doctors/{id}` |
| 11 | Payment | `PaymentActivity` | Firestore: `payments.add()` |
| 12 | Confirmation | `ConfirmationActivity` | Firestore: `doctors/{id}` |
| 13 | Appointments | `AppointmentsActivity` | Firestore: `appointments` query by userId + status |
| 14 | Check-In | `CheckInActivity` | Firestore: `appointments` update queueStatus |
| 15 | Medical Records | `MedicalRecordsActivity` | Firestore: `medical_records` query by userId + type |
| 16 | Notifications | `NotificationsActivity` | Firestore: `notifications` + WriteBatch markAllRead |
| 17 | Insurance | `InsuranceActivity` | Firestore: `insurance` query by userId |
| 18 | Profile | `ProfileActivity` | Firestore: `users/{uid}` + Auth: `signOut` |

---

## Tech Stack

| Layer | Công nghệ |
|-------|-----------|
| **Android** | Java 17, Android SDK 34, Material Design 3, ViewBinding |
| **Auth** | Firebase Authentication (Email/Password) |
| **Database** | Cloud Firestore (NoSQL) |
| **Images** | Glide 4.16 |
| **QR Code** | ZXing 4.3 |
| **Build** | Gradle 8.5, AGP 8.2.2 |

---

## Xử lý sự cố thường gặp

| Vấn đề | Giải pháp |
|--------|-----------|
| `google-services.json not found` | Tải file từ Firebase Console và đặt vào `android/app/` |
| Gradle sync lỗi | Đảm bảo có internet, Android Studio tự tải dependencies |
| Login thất bại | Kiểm tra đã tạo user trong Firebase Auth chưa |
| Không hiện data | Kiểm tra đã chạy seed script và Firestore rules là test mode |
| Build lỗi SDK | Click **Install missing SDK** khi Android Studio hiện link |
| `FirebaseApp is not initialized` | Đảm bảo `google-services.json` đúng vị trí và Gradle sync OK |
