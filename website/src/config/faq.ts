export const FAQ_ITEMS = [
  {
    q: "Can I print from Android to a Windows printer?",
    a: "Yes. The native Printora Android app (built with Kotlin and Jetpack Compose for Android 8.0+) connects over local Wi-Fi to the Printora server on your Windows 10/11 PC. It supports printer discovery via mDNS and UDP beacons, full print settings (copies, color/B&W, paper size, duplex, quality), and document preview.",
  },
  {
    q: "Can I print from iPhone or iPad?",
    a: "Yes. iPhone and iPad users can print through Printora's browser-based QR Web Print Studio. Simply point your camera at the QR code displayed on the Windows host screen, select your document in Safari or Chrome, configure print settings, and print directly. No native iOS app installation is required.",
  },
  {
    q: "Does my printer need to have built-in Wi-Fi?",
    a: "No. Your printer does not need Wi-Fi or wireless networking. Whether your printer is connected to your Windows computer using a standard USB cable, an Ethernet cord, or a local network share, Printora uses the Windows host PC as the print bridge.",
  },
  {
    q: "Can I print from any web browser without installing an app?",
    a: "Yes. Any modern desktop or mobile browser (Chrome, Safari, Edge, Firefox) can access Web Print Studio over the local network or via the optional secure tunnel, allowing guests, clients, and family members to print with zero downloads.",
  },
  {
    q: "How does QR Web Print work?",
    a: "The Windows host generates a temporary or permanent QR pairing code and 6-digit PIN. Scanning the QR code opens Web Print Studio in the phone's browser, authenticates the connection, and allows direct file uploads to the Windows spooler.",
  },
  {
    q: "Can I print remotely when I am away from home or the office?",
    a: "Yes. Printora includes optional Cloudflare Tunnel integration. When enabled, you can print securely over cellular 4G/5G connections without opening router ports or exposing your IP address. Remote sessions are protected with 6-digit PIN verification and 2-hour HMAC-signed tokens.",
  },
  {
    q: "Can I scan documents with Printora?",
    a: "Yes. The Android app includes a built-in camera document scanner with automatic quad-corner edge detection, perspective correction, crop & straighten, contrast enhancement, and multiple filters (Auto Enhance, B&W, Grayscale, Sharp, High Contrast). It supports document sessions of up to 20 pages.",
  },
  {
    q: "Can I scan both sides of an ID card onto a single page?",
    a: "Yes. In ID Card scanner mode, you capture the front side and the back side. Printora automatically cleans and combines both faces onto a single standardized printable A4 page without watermarks or clutter.",
  },
  {
    q: "Does Printora support OCR (Optical Character Recognition)?",
    a: "Yes. Scanned documents can be processed with on-device OCR to recognize and extract printed text, which can then be copied to your clipboard or exported.",
  },
  {
    q: "Can Printora handle password-protected PDFs?",
    a: "Yes. The print workflow can accept and verify passwords for protected PDF documents directly during preview before rendering the file for the print spooler.",
  },
  {
    q: "Does printer health and supply monitoring work with every printer?",
    a: "Printora monitors status, toner/ink levels, paper tray capacity, page counts, and errors whenever the printer hardware and installed Windows driver expose that telemetry to the Windows spooler API.",
  },
  {
    q: "Does Printora permanently store my documents in the cloud?",
    a: "No. Printora has a local-first architecture. Print jobs travel directly between your phone and your Windows host PC. There is no permanent cloud document library or storage database.",
  },
  {
    q: "What happens to uploaded print files after printing?",
    a: "When automatic cleanup is enabled, uploaded temporary print files and intermediate converted files are automatically deleted from the host PC once the print job successfully completes.",
  },
];
