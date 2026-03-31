# TrustEngine e-Certificate Verifier

A modern Android application for verifying Indonesian e-KTP (electronic ID card) identity using NFC, camera capture, and generating branded PDF certificates.

## Features

- **NFC e-KTP Reading**: Tap your e-KTP card to read identity data
- **Camera Capture**: Capture KTP photo and selfie using CameraX
- **Identity Verification**: Integration with Verihubs API for verification
- **PDF Certificate Generation**: Generate branded PDF certificates with TrustEngine styling
- **Modern Architecture**: Built with MVVM, Jetpack Compose, and Hilt DI

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with StateFlow
- **Dependency Injection**: Hilt
- **Camera**: CameraX
- **NFC**: Android NFC API
- **Networking**: Retrofit + OkHttp
- **PDF Generation**: iText 7
- **Image Loading**: Coil

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Minimum Android API 24 (Android 7.0)
- Target Android API 34

## Setup Instructions

### 1. Clone and Open Project

```bash
git clone <repository-url>
cd TrustEngine-Android
```

Open the project in Android Studio.

### 2. Configure Verihubs API

Create a `local.properties` file in the project root (if not exists) and add your Verihubs API key:

```properties
VERIHUBS_API_KEY=your_api_key_here
```

Or modify the `VerihubsClient.kt` file to include your API key directly for testing.

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run on Device

Connect an Android device with NFC capability and run:

```bash
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/trustengine/verifier/
├── data/
│   ├── remote/
│   │   ├── VerihubsApiService.kt    # API interface
│   │   └── VerihubsClient.kt        # Retrofit client
│   ├── local/                       # Room database (optional)
│   └── repository/                  # Data repositories
├── domain/
│   └── model/
│       ├── EKTPData.kt              # e-KTP data model
│       ├── CaptureData.kt           # Photo capture data
│       ├── VerificationResult.kt    # Verification result
│       └── CertificateData.kt       # Certificate data
├── nfc/
│   └── NFCEKTPReader.kt             # NFC reader implementation
├── pdf/
│   └── CertificatePDFGenerator.kt   # PDF generation
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt            # Home screen
│   │   ├── NFCScreen.kt             # NFC scanning screen
│   │   ├── CameraScreen.kt          # Camera capture screen
│   │   ├── VerificationScreen.kt    # Verification progress
│   │   └── CertificateScreen.kt     # Certificate display
│   ├── theme/
│   │   ├── Color.kt                 # Brand colors
│   │   ├── Type.kt                  # Typography
│   │   └── Theme.kt                 # App theme
│   └── VerificationViewModel.kt     # Shared ViewModel
├── di/
│   └── AppModule.kt                 # Hilt modules
├── MainActivity.kt                  # Main activity
└── TrustEngineApplication.kt        # Application class
```

## Usage

1. **Start Verification**: Tap "Start Verification" on the home screen
2. **NFC Scan**: Hold your e-KTP card against the back of your phone
3. **Capture Photos**: Take a photo of your KTP and a selfie
4. **Verification**: Wait for identity verification to complete
5. **Certificate**: Download or share your e-Certificate PDF

## Permissions

The app requires the following permissions:

- `NFC`: For reading e-KTP cards
- `CAMERA`: For capturing KTP and selfie photos
- `INTERNET`: For API communication
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`: For saving PDF certificates

## Customization

### Brand Colors

Modify the colors in `ui/theme/Color.kt`:

```kotlin
val TrustEngineDarkBlue = Color(0xFF0A2540)
val TrustEngineAccent = Color(0xFF00D4AA)
```

### PDF Template

Customize the PDF layout in `pdf/CertificatePDFGenerator.kt`.

### API Endpoints

Update the API base URL in `data/remote/VerihubsClient.kt`:

```kotlin
private const val BASE_URL = "https://api.verihubs.com/"
```

## Testing

### Unit Tests

```bash
./gradlew test
```

### Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

## Demo Mode

The app includes a demo mode for testing without actual NFC hardware. Tap "Simulate NFC Read (Demo)" on the NFC screen to use mock data.

## Troubleshooting

### NFC Not Working

1. Ensure NFC is enabled in device settings
2. Check that the device supports NFC
3. Hold the e-KTP card steady against the NFC antenna (usually middle-back of phone)

### Camera Issues

1. Grant camera permissions when prompted
2. Ensure no other apps are using the camera
3. Clean the camera lens

### PDF Generation Fails

1. Check storage permissions
2. Ensure sufficient storage space
3. Verify the app has write access to external storage

## License

Copyright © 2024 TrustEngine. All rights reserved.

## Support

For support, contact support@trustengine.id or visit https://trustengine.id