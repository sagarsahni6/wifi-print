# Printora — Privacy-First Cloud & Local Print Platform

Printora is a modern, enterprise-grade, **privacy-focused** wireless and cloud printing ecosystem. It enables phones, tablets, and web browsers to print directly to any Windows PC-connected printer with **zero cloud data retention** and **automatic immediate cleanup**.

```
📱 Android Device / 🌐 Web Browser
           │
           │  (TLS / AES-256 Encrypted Transit — No Cloud Storage)
           ▼
☁️ Cloudflare Tunnel Relay (Optional Remote Access)
   OR  ⚡ Local Wi-Fi Subnet (Direct Socket Fast-Path)
           │
           ▼
🖥️ Printora Server (Windows Host PC)
   ├── 🛡️ In-Memory Decryption & Spooling
   ├── 🖨️ Windows Print Dispatch
   └── 🧹 Instant Automatic File Shredding / Cleanup
           │
           ▼
🖨️ Physical Printer (Paper Output)
```

---

## 🛡️ Privacy & Security Commitments

Printora is engineered from the ground up for strict privacy, data sovereignty, and zero-trace operation:

| Privacy Pillar | How Printora Protects You |
|----------------|---------------------------|
| **Zero Cloud Storage** | The cloud is strictly an ephemeral encrypted transit pipe. **Zero files, images, or documents are ever stored or cached in the cloud.** |
| **Instant Auto-Cleanup** | Uploaded documents and converted temporary files are **automatically deleted immediately upon print completion or cancellation**. |
| **Complete Data Sovereignty** | Print job metadata, SQLite databases, and server configurations reside 100% locally on your own Host PC (`%LOCALAPPDATA%\Printora`). |
| **End-to-End Protection** | Connections use TLS encryption, AES-256 payload transfers, and certificate fingerprint pinning to prevent man-in-the-middle attacks. |
| **Zero Account / No Tracking** | No account creation, no external telemetry, and no tracking cookies. You maintain full ownership of your printing hardware and data. |
| **Host File Safeguard** | The auto-cleanup engine strictly manages temporary spool uploads; personal desktop files printed via drag-and-drop are always kept safe. |

---

## 🌟 Key Highlights

- ☁️ **Privacy-Preserving Cloud Relay**: Print securely from outside your home or office network via Cloudflare Zero-Trust Tunnels without port-forwarding or external data storage.
- ⚡ **LAN Fast-Path Connect**: Instant auto-discovery via mDNS and UDP beacons with zero-latency direct socket printing on local networks.
- 🧹 **Zero-Trace Auto-Cleanup**: Automatically purges spool documents and converted files the moment your document leaves the print queue.
- 📱 **Native Android Client**: Built with Jetpack Compose, Material 3, dynamic theming, document scanner, camera QR pairing, batch printing, and live queue tracking.
- 🌐 **Web Print Studio**: Print directly from any phone or browser by scanning a dynamic QR code — no app installation required. Includes rate limiting, file type validation, and PIN protection.
- 🔒 **Desktop Admin Control**: New connecting devices require explicit approval or a 6-digit dynamic PIN generated directly on the Host PC dashboard.
- 🔄 **Seamless Data Migration**: Automatic transparent migration from legacy `%LOCALAPPDATA%\SpoolDrop` and `WifiPrintServer` directories to `%LOCALAPPDATA%\Printora\`.

---

## 📋 System Requirements

### Printora Server (Windows Desktop)
- Windows 10 / 11 (64-bit)
- .NET 8 SDK or Runtime ([Download .NET 8](https://dotnet.microsoft.com/download/dotnet/8.0))
- At least one printer configured in Windows
- *(Optional)* LibreOffice for server-side DOCX/PPTX to PDF conversion ([Download](https://www.libreoffice.org/download/))

### Printora Android App
- Android 8.0+ (API 26+)
- Google Play Services (for CameraX QR Scanner)

---

## 🖥️ Printora Desktop Server Setup

### Option 1: One-Click Installer (Recommended)
Download and run `PrintoraServer-Setup.exe` or execute `release-package\Install-Server.bat`.
- Automatically installs to `%LOCALAPPDATA%\Programs\PrintoraServer\`
- Creates Start Menu and Desktop shortcuts for **Printora Server**
- Configures inbound Windows Firewall permissions for port 5000
- Migrates existing databases, certificates, and settings seamlessly

### Option 2: Build & Run from Source
```powershell
cd WifiPrintServer
dotnet restore
dotnet build WifiPrintServer.sln
dotnet run --project WifiPrintServer
```

### Dashboard Overview
| Page | Purpose |
|------|---------|
| **Dashboard** | Real-time statistics, active connections, Cloud Relay status, recent jobs |
| **Print Queue** | Live job status, reordering, retrying, pausing, and cancelling jobs |
| **Printers** | Installed printer discovery, paper sizes, color capabilities, and status |
| **Devices** | Paired Android phones and authorized mobile devices |
| **Web Print** | Interactive QR code generator, printable station posters, and protection rules |
| **Logs** | Real-time event log viewer with search and log level filters |
| **Settings** | Port configuration, Cloud Relay settings, Privacy & Auto-Cleanup toggle, tray minimization |

### Firewall Configuration
If running manually, ensure TCP port 5000 is permitted through Windows Firewall:
```powershell
netsh advfirewall firewall add rule name="Printora Server" dir=in action=allow protocol=TCP localport=5000
```

---

## 📱 Printora Android App Setup

### 1. Build and Install
1. Open the `WifiPrintApp` directory in Android Studio (Ladybug or newer).
2. Allow Gradle sync to complete with JDK 17+.
3. Build and deploy to your Android device or emulator:
```powershell
cd WifiPrintApp
.\gradlew.bat assembleDebug
```

### 2. Connect to Server
1. **Local Wi-Fi Auto-Discovery**: Open Printora. If your phone and PC are on the same Wi-Fi, Printora Server is detected automatically.
2. **Cloud Relay Connect**: Paste your Printora Cloud Relay URL (`https://*.trycloudflare.com` or custom domain) to print remotely over cellular/LTE.
3. **QR Code Pairing**: Tap the QR icon and scan the connection QR code displayed on the Printora PC Dashboard.

### 3. Print Documents & Photos
1. Tap **Print** in the bottom navigation.
2. Select files (PDF, images, text documents) or capture a new scan using the built-in Document Scanner.
3. Choose your target printer and set copies, orientation, page range, color mode, and duplex options.
4. Tap **Send to Printora**. The file is printed and automatically purged immediately upon completion.

---

## 🔌 Core API Endpoints

**Base URL**: `https://<server-ip>:5000` or `https://<cloud-relay-tunnel>.trycloudflare.com`

### Authentication & Pairing
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/request` | POST | None | Request pairing approval from desktop user |
| `/api/auth/pair` | POST | None | Exchange 6-digit PIN for JWT token |
| `/api/auth/status` | GET | JWT | Validate authentication session |
| `/api/auth/devices` | GET | JWT | List authorized client devices |

### Printers & Capabilities
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/printers` | GET | JWT | List all installed printers with status |
| `/api/printers/{id}` | GET | JWT | Fetch detailed printer capabilities and paper trays |

### Print Jobs & Document Uploads
| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/print` | POST | JWT | Upload document and submit print job (multipart) |
| `/api/jobs` | GET | JWT | List all print jobs (supports status filter) |
| `/api/jobs/{id}` | GET | JWT | Get live status and metadata for a specific job |
| `/api/jobs/{id}/cancel` | POST | JWT | Cancel a queued or active job (triggers auto-cleanup) |
| `/api/jobs/{id}/retry` | POST | JWT | Retry a failed job |

### Web Print Studio (Public Browser Gateway)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/web-print` | GET | Responsive browser UI for mobile scanning & printing |
| `/api/web-print/upload` | POST | Anti-abuse protected upload endpoint for web clients |

### Real-Time Updates
| Endpoint | Protocol | Description |
|----------|----------|-------------|
| `/ws/status` | SignalR | Real-time queue, printer status, and job event streaming |

---

## 📁 Repository Structure

```
WIFI PRINT/
├── WifiPrintServer/                # Printora Windows Desktop Server & Services
│   ├── WifiPrintServer.sln         # Visual Studio Solution (.NET 8)
│   ├── WifiPrintServer/            # Core Server Application (WPF + Kestrel)
│   │   ├── Program.cs              # ASP.NET Core & WPF bootstrap
│   │   ├── App.xaml(.cs)           # Application lifecycle & system tray
│   │   ├── MainWindow.xaml(.cs)    # Modern Dark/Light Desktop Dashboard
│   │   ├── Controllers/            # REST API & Web Print controllers
│   │   ├── Services/               # Printer, Queue, Tunnel, Security services
│   │   ├── Models/                 # Data contracts & storage configuration
│   │   └── Hubs/                   # SignalR WebSocket hubs
│   ├── WifiPrintServer.Tests/      # Automated test suite (xUnit)
│   └── WifiPrintInstaller/         # Standalone self-contained setup builder
│
├── WifiPrintApp/                   # Printora Android Mobile Client (Kotlin)
│   ├── app/src/main/
│   │   ├── AndroidManifest.xml     # Permissions, activities, file providers
│   │   └── java/com/wifiprint/app/
│   │       ├── data/               # Room database, API services, repositories
│   │       ├── discovery/          # mDNS NSD & UDP beacon discovery
│   │       ├── ui/                 # Jetpack Compose UI (Material 3)
│   │       └── workers/            # Background WorkManager upload jobs
│
└── release-package/                # Packaged installer and server setup scripts
```

---

## ✅ Quality & Verification

### Run Server Tests
```powershell
dotnet test WifiPrintServer/WifiPrintServer.sln
```
*Current test suite: 23 unit tests passing (covering queue management, privacy auto-cleanup, authentication, encryption, and Web Print protection).*

### Run Android Tests & Build
```powershell
cd WifiPrintApp
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

---

## 📄 License
Printora is distributed under the proprietary license of the Printora Team. All rights reserved.
