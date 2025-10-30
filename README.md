# DONORlk - Blood Donation Management System

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-brightgreen.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com/)

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Building the Project](#building-the-project)
- [User Roles](#user-roles)
- [Firebase Setup](#firebase-setup)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

DONORlk is a comprehensive blood donation management system built for Android that connects blood donors with donation centers. The application streamlines the blood donation process by providing features for donors, administrators, sub-admins, and operators to efficiently manage blood donations, reservations, and donation history.

## ✨ Features

### For Donors
- 🔐 **User Authentication** - Secure login and registration with Google Sign-In integration
- 👤 **Profile Management** - Create and manage donor profiles with blood type information
- 📍 **Location Services** - Find nearby donation centers using Google Maps integration
- 📅 **Reservation System** - Book appointments at donation centers
- 📊 **Donation History** - Track personal donation records
- 🔔 **Donation Forms** - Submit donation information easily

### For Administrators
- 👨‍💼 **Admin Dashboard** - Comprehensive overview of the system
- 🏥 **Center Management** - Add and manage donation centers
- 👥 **User Management** - Create and manage sub-admins and operators
- 📈 **Analytics** - View donation statistics and trends

### For Sub-Admins
- 🏢 **Center Operations** - Manage specific donation center operations
- 📋 **Appointment Management** - Handle donor reservations

### For Operators
- 💉 **Donation Recording** - Record completed donations
- ✅ **Verification** - Verify donor information

## 🛠 Tech Stack

### Android Development
- **Language:** Kotlin 2.0.21
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 35 (Android 14+)
- **Build System:** Gradle 8.9.2 with Kotlin DSL

### Core Libraries
- **AndroidX Core KTX:** 1.16.0
- **AppCompat:** 1.7.1
- **Material Components:** 1.12.0
- **ConstraintLayout:** 2.2.1
- **Navigation Component:** 2.9.1

### Firebase Services
- **Firebase Authentication:** 24.0.0
- **Firebase Firestore:** 26.0.0
- **Firebase Cloud Functions**
- **Google Services:** 4.4.3

### Google Services
- **Google Sign-In**
- **Google Maps SDK:** 18.2.0
- **Google Play Services Location:** 21.0.1
- **Maps Utils:** 3.4.0

### Additional Libraries
- **Credentials API:** 1.5.0 (for authentication)
- **ExifInterface:** 1.4.1 (for image handling)
- **CircleImageView:** 3.1.0 (for profile pictures)
- **Android Mail:** 1.6.7 (for email functionality)

### Testing
- **JUnit:** 4.13.2
- **AndroidX Test:** 1.2.1
- **Espresso:** 3.6.1

## 🏗 Architecture

The project follows the **MVC (Model-View-Controller)** architecture pattern:

```
app/src/main/java/com/example/donorlk/
├── controllers/          # Activity controllers handling business logic
├── models/              # Data models and business entities
├── views/               # Custom views and UI components
├── adapters/            # RecyclerView adapters and list management
└── MainActivity.kt      # Application entry point
```

## ⚙️ Prerequisites

Before you begin, ensure you have the following installed:
- **Android Studio:** Arctic Fox or later
- **JDK:** 11 or higher
- **Gradle:** 8.9.2 (included via wrapper)
- **Git:** For version control
- **Firebase Account:** For backend services
- **Google Maps API Key:** For location features

## 📥 Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AshenKavinda/DONORlk.git
   cd DONORlk
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory and open it

3. **Sync Gradle:**
   - Android Studio will automatically detect the project
   - Click "Sync Now" when prompted

4. **Add Firebase Configuration:**
   - Download `google-services.json` from your Firebase Console
   - Place it in the `app/` directory

5. **Configure Google Maps:**
   - Obtain a Google Maps API key
   - Add it to `AndroidManifest.xml` (see [Configuration](#configuration))

## 📁 Project Structure

```
DONORlk/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/donorlk/
│   │   │   │   ├── controllers/          # Activity controllers
│   │   │   │   ├── models/              # Data models
│   │   │   │   ├── views/               # Custom views
│   │   │   │   ├── adapters/            # List adapters
│   │   │   │   └── MainActivity.kt      # Entry point
│   │   │   ├── res/                     # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml      # App manifest
│   │   ├── androidTest/                 # Instrumented tests
│   │   └── test/                        # Unit tests
│   ├── build.gradle.kts                 # App-level build configuration
│   └── google-services.json             # Firebase configuration
├── functions/                           # Firebase Cloud Functions
├── admin-email-function/                # Admin email Cloud Function
├── gradle/
│   ├── libs.versions.toml              # Dependency version catalog
│   └── wrapper/                         # Gradle wrapper
├── build.gradle.kts                     # Project-level build configuration
├── settings.gradle.kts                  # Project settings
├── firebase.json                        # Firebase configuration
├── gradlew                              # Gradle wrapper script (Unix)
├── gradlew.bat                          # Gradle wrapper script (Windows)
└── README.md                            # This file
```

## 🔧 Configuration

### Google Maps API Key

Replace the placeholder API key in `app/src/main/AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

### Firebase Configuration

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Register your app with package name: `com.example.donorlk`
4. Download `google-services.json` and place it in the `app/` directory
5. Enable the following Firebase services:
   - **Authentication** (Email/Password and Google Sign-In)
   - **Cloud Firestore**
   - **Cloud Functions**

### Local Properties

Create a `local.properties` file in the project root (if not exists):

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

## 🔨 Building the Project

### Debug Build

```bash
# Windows
.\gradlew assembleDebug

# Unix/Mac
./gradlew assembleDebug
```

### Release Build

```bash
# Windows
.\gradlew assembleRelease

# Unix/Mac
./gradlew assembleRelease
```

### Install on Device

```bash
# Windows
.\gradlew installDebug

# Unix/Mac
./gradlew installDebug
```

### Run Tests

```bash
# Unit tests
.\gradlew test

# Instrumented tests
.\gradlew connectedAndroidTest
```

## 👥 User Roles

### 1. Donor
- Register and login to the system
- Complete profile with blood type and personal information
- View nearby donation centers on a map
- Make reservations for blood donations
- View donation history
- Receive notifications

### 2. Administrator
- Full system access
- Create and manage sub-admins
- Add and manage donation centers
- View system-wide analytics
- Manage operators
- Edit user accounts

### 3. Sub-Admin
- Manage specific donation center operations
- View center-specific data
- Handle donor reservations
- Coordinate with operators

### 4. Operator
- Record completed donations
- Verify donor information
- Update donation records
- Manage daily operations at donation centers

## 🔥 Firebase Setup

### Firestore Collections

The app uses the following Firestore collections:

- `users` - User profiles and authentication data
- `donors` - Donor-specific information
- `admins` - Administrator accounts
- `operators` - Operator accounts
- `donation_centers` - Donation center details
- `reservations` - Appointment bookings
- `donations` - Donation records

### Cloud Functions

Deploy Firebase Cloud Functions:

```bash
cd functions
npm install
firebase deploy --only functions
```

### Security Rules

Configure Firestore security rules in Firebase Console to protect user data.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Branch Strategy
- `main` - Production-ready code
- `frontend-dev` - Frontend development (current)
- `feature/*` - New features
- `bugfix/*` - Bug fixes

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic

## 📞 Contact

**Project Owner:** AshenKavinda

**Repository:** [https://github.com/AshenKavinda/DONORlk](https://github.com/AshenKavinda/DONORlk)

---

## 🙏 Acknowledgments

- Firebase for backend services
- Google Maps for location services
- Material Design for UI components
- All contributors who help improve this project

## 📝 Version History

- **v1.0** - Initial release
  - User authentication with Google Sign-In
  - Donation center management
  - Reservation system
  - Multi-role user support
  - Location-based services

---

**Made with ❤️ for blood donors and healthcare professionals**
