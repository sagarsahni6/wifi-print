# WiFi Print System — Setup Guide

## Overview
A cross-platform Wi-Fi printing system: **Android phone → Wi-Fi → Windows PC → Printer**

---

## 📋 Prerequisites

### Desktop Server (Windows)
- Windows 10/11
- .NET 8 SDK or Runtime ([download](https://dotnet.microsoft.com/download/dotnet/8.0))
- At least one printer installed
- **Optional**: LibreOffice (for DOCX→PDF conversion) ([download](https://www.libreoffice.org/download/))

### Android Client
- Android 8.0+ (API 26)
- Android Studio Hedgehog or newer
- Android SDK Platform 34 + Build Tools 34.0.0
- Both devices on the **same Wi-Fi network**

---

## 🖥️ Desktop Server Setup

### 1. Build & Run
```bash
cd WifiPrintServer
dotnet restore
dotnet build
dotnet run --project WifiPrintServer
```

### 2. First Launch
- The server will:
  - Generate a self-signed HTTPS certificate (stored in `%LOCALAPPDATA%\WifiPrintServer\`)
  - Start listening on `https://0.0.0.0:5000`
  - Begin broadcasting via mDNS on the local network
  - Open the Dashboard window

### 3. Dashboard Overview
| Page | Description |
|------|-------------|
| **Dashboard** | Stats cards, PIN pairing, recent jobs |
| **Print Queue** | All jobs with status, progress, timestamps |
| **Printers** | All installed printers with capabilities |
| **Devices** | Paired Android devices |
| **Logs** | Real-time server event log |
| **Settings** | Port, server name, auto-start, tray |

### 4. Firewall
Allow port 5000 (or your configured port) through Windows Firewall:
```powershell
netsh advfirewall firewall add rule name="WiFi Print Server" dir=in action=allow protocol=TCP localport=5000
```

---

## 📱 Android App Setup

### 1. Open in Android Studio
- Open the `WifiPrintApp` folder in Android Studio
- Let Gradle sync complete
- Build and run on your device/emulator
- If you build from the command line, create `WifiPrintApp/local.properties` with:

```properties
sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

### 2. Connect to Server
1. Ensure phone and PC are on the **same Wi-Fi network**
2. Open the app → tap **"Connect"** or **"Find Server"**
3. The app will auto-discover the server via mDNS
4. If auto-discovery fails, tap **"Enter IP Manually"** and enter the PC's IP

### 3. Pair the Device
1. On the **PC Dashboard**, click **"Generate PIN"**
2. On the **Android app**, enter the 6-digit PIN
3. Tap **"Pair Device"**
4. The app receives a JWT token and saves it locally

### 4. Print a File
1. Tap **"Print"** in the bottom nav
2. Tap **"Choose File"** → select PDF, image, DOCX, or text file
3. Select a printer from the dropdown
4. Configure settings (copies, orientation, color, quality, duplex)
5. Tap **"Print"**
6. Monitor progress in the **Jobs** tab

---

## 🔌 API Reference

**Base URL**: `https://<server-ip>:5000`

### Authentication
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/pair` | POST | None | Exchange PIN for JWT token |
| `/api/auth/status` | GET | JWT | Validate current token |
| `/api/auth/devices` | GET | JWT | List paired devices |

### Printers
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/printers` | GET | JWT | List all installed printers |
| `/api/printers/{id}` | GET | JWT | Get printer details |

### Print Jobs
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/print` | POST | JWT | Upload file + create job (multipart) |
| `/api/jobs` | GET | JWT | List all jobs (filter: `?status=Pending`) |
| `/api/jobs/{id}` | GET | JWT | Get job details |
| `/api/jobs/{id}/cancel` | POST | JWT | Cancel a job |
| `/api/jobs/{id}/retry` | POST | JWT | Retry a failed job |

### WebSocket (SignalR)
| Endpoint | Description |
|----------|-------------|
| `/ws/status` | Real-time job updates (pass `?access_token=JWT`) |

### Health Check
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/status` | GET | None | Server health check |

---

## 📁 Project Structure

```
WIFI PRINT/
├── WifiPrintServer/           # Desktop server (.NET 8 / WPF)
│   ├── WifiPrintServer.sln
│   └── WifiPrintServer/
│       ├── Program.cs         # Kestrel + WPF host
│       ├── App.xaml(.cs)      # WPF app + system tray
│       ├── MainWindow.xaml    # Dashboard UI
│       ├── Controllers/       # REST API endpoints
│       ├── Services/          # Business logic
│       ├── Models/            # Data models
│       └── Hubs/              # SignalR hub
│
└── WifiPrintApp/              # Android client (Kotlin)
    └── app/src/main/java/com/wifiprint/app/
        ├── WifiPrintApp.kt    # Application + Hilt
        ├── di/                # Dependency injection
        ├── data/              # Models, API, DB, Repository
        ├── discovery/         # mDNS/NSD discovery
        ├── ui/                # Compose screens
        └── workers/           # WorkManager upload
```

---

## 🔐 Security Notes
- Communication uses **HTTPS with self-signed certificate**
- Devices pair via **desktop approval** or legacy PIN flow
- All API calls after pairing use **JWT bearer tokens**
- The Android app now pins the server certificate fingerprint after first approval
- Tokens expire after 365 days by default
- Device-management endpoints are restricted to local desktop access
- Job APIs are scoped to the authenticated device by default

## ✅ Verification

### Windows Server
```bash
dotnet build WifiPrintServer/WifiPrintServer.sln
dotnet test WifiPrintServer/WifiPrintServer.sln
```

### Android App
```bash
cd WifiPrintApp
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## 🤖 CI
- GitHub Actions workflow: `.github/workflows/ci.yml`
- Server job: restore, build, and test the .NET solution on Windows
- Android job: install SDK 34, assemble debug, run unit tests, and run lint

## ⚠️ Troubleshooting

| Issue | Solution |
|-------|----------|
| Server not discovered | Ensure same Wi-Fi network, check firewall |
| "Certificate error" | App trusts self-signed certs by default |
| DOCX won't convert | Install LibreOffice on the server PC |
| Upload timeout | Check file size (max 100MB default) |
| Printer not found | Verify printer is installed in Windows |
