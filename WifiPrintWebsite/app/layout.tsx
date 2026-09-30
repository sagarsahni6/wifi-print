import type { Metadata } from 'next';
import { Inter, Outfit } from 'next/font/google';
import './globals.css';
import ScrollObserver from './components/ScrollObserver';
import Navbar from './components/Navbar';

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' });
const outfit = Outfit({ subsets: ['latin'], variable: '--font-outfit' });

export const metadata: Metadata = {
  title: 'WiFi Print — Free Android to PC Wireless Printing & OCR Document Scanner',
  description: 'Free app to print from Android to any Windows PC printer over Wi-Fi. Features on-device ML Kit OCR scanner, batch scan, password-protected PDF printing, cross-network PIN pairing, and zero cloud tracking.',
  keywords: 'print from phone to PC, print from android to windows, wifi printing app, wireless printer android, print pdf from phone, mobile document scanner ocr, batch scan to pdf, print from android to usb printer, how to print from phone without wifi printer, android print to pc, print from phone to computer, mobile printing app free, nokoprint alternative, printershare alternative, id card scanner app, print password protected pdf android, print from phone to hp printer, print docx from android, local network printing, print without cloud, offline printer app, wifi direct print android, scan to print, free wireless printing app, how to connect phone to printer wifi, best printing app android 2026',
  robots: 'index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1',
  alternates: {
    canonical: 'https://wifiprint.calclabz.com/',
  },
  icons: {
    apple: '/assets/apple-touch-icon.png',
  },
  openGraph: {
    type: 'website',
    url: 'https://wifiprint.calclabz.com/',
    title: 'WiFi Print — Free Android to PC Wireless Printing & OCR Document Scanner',
    description: 'Print from Android to any Windows PC printer over Wi-Fi. Built-in on-device OCR scanner, batch scanning, password-protected PDF printing, and instant P2P pairing. Zero cloud, 100% private.',
    siteName: 'WiFi Print',
    images: [
      {
        url: 'https://wifiprint.calclabz.com/assets/og-image.jpg',
        width: 1200,
        height: 630,
        alt: 'WiFi Print — Free Wireless Printing from Android to Windows PC'
      }
    ],
    locale: 'en_US',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'WiFi Print — Free Android to PC Wireless Printing & OCR Document Scanner',
    description: 'Print from Android to any Windows PC printer over Wi-Fi. Built-in on-device OCR scanner, batch scanning, password-protected PDF printing, and zero cloud tracking.',
    images: ['https://wifiprint.calclabz.com/assets/og-image.jpg'],
  },
};

export const viewport = {
  themeColor: '#4f46e5',
  width: 'device-width',
  initialScale: 1,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={`${inter.variable} ${outfit.variable}`}>
      <head>
        <meta name="language" content="English" />
        <meta name="author" content="WiFi Print" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />

        {/* ── JSON-LD: WebSite ── */}
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "WebSite",
              "name": "WiFi Print",
              "url": "https://wifiprint.calclabz.com/",
              "description": "Free wireless printing app to print documents, PDFs, photos, and scans from Android to any Windows PC printer over Wi-Fi.",
              "publisher": {
                "@type": "Organization",
                "name": "WiFi Print",
                "url": "https://wifiprint.calclabz.com/"
              }
            })
          }}
        />

        {/* ── JSON-LD: SoftwareApplication ── */}
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "SoftwareApplication",
              "name": "WiFi Print",
              "operatingSystem": "Android 8.0+, Windows 10, Windows 11 (64-bit)",
              "applicationCategory": "UtilitiesApplication",
              "applicationSubCategory": "Printing & Document Scanner",
              "softwareVersion": "2.0.0",
              "offers": {
                "@type": "Offer",
                "price": "0",
                "priceCurrency": "USD",
                "availability": "https://schema.org/InStock"
              },
              "aggregateRating": {
                "@type": "AggregateRating",
                "ratingValue": "4.9",
                "reviewCount": "1480",
                "bestRating": "5",
                "worstRating": "1"
              },
              "description": "Print documents, photos, password-protected PDFs, and scans from Android directly to any Windows PC printer over Wi-Fi. Features built-in on-device ML Kit OCR scanner, batch scan mode, and cross-network PIN pairing without cloud tracking.",
              "author": {
                "@type": "Organization",
                "name": "WiFi Print",
                "url": "https://wifiprint.calclabz.com/"
              },
              "featureList": "Wi-Fi Printing, On-Device ML Kit OCR, Continuous Batch Document Scanner, Auto Edge Detection & Perspective Crop, ID Card Front & Back Scanner, Password-Protected PDF Support, Subnet Auto-Connect & 6-Digit Rotating PIN Pairing, Real-Time SignalR WebSockets Print Queue, Watermark & Multi-Size PDF Export (A4, Letter, Legal, A3), Zero Cloud Offline P2P Security",
              "downloadUrl": "https://wifiprint.calclabz.com/#download"
            })
          }}
        />

        {/* ── JSON-LD: FAQPage ── */}
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "FAQPage",
              "mainEntity": [
                {
                  "@type": "Question",
                  "name": "How do I print from my Android phone to my Windows PC printer wirelessly?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Run the WiFi Print Server on your Windows 10 or 11 PC, open the WiFi Print app on your Android phone, and connect. On the same Wi-Fi subnet, devices auto-connect instantly. Select any PDF, DOCX, image, or scanned document, customize print settings, and tap Print. Your document prints in seconds over your local Wi-Fi."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Can I print to a USB-only printer that has no Wi-Fi?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes! WiFi Print transforms any standard USB printer connected to your Windows PC into a wireless printer. Your phone sends the print job to your PC over Wi-Fi, and your PC outputs it to the USB printer via native Windows print drivers."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Does WiFi Print work without internet or cloud services?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes. WiFi Print operates 100% offline across your local area network (LAN). Your files never leave your home or office router, ensuring zero cloud latency and absolute data privacy."
                  }
                },
                {
                  "@type": "Question",
                  "name": "How does the built-in document scanner with on-device OCR work?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print includes a document scanner with ML Kit on-device optical character recognition (OCR). It automatically detects paper edges, straightens perspective, enhances contrast, and extracts text directly on your device without sending images to external servers."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Can I print password-protected PDFs from my phone?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes. WiFi Print natively supports encrypted, password-protected PDFs (like bank e-statements, payslips, and invoices). Simply enter the password in the app when prompted to unlock and print directly without unprotecting files on third-party sites."
                  }
                },
                {
                  "@type": "Question",
                  "name": "How do I connect if my phone and PC are on different Wi-Fi networks or subnets?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print includes Cross-Network Pairing. The Windows desktop server generates a permanent QR code and a rotating 6-digit PIN. Scan the QR code or enter the server IP and PIN to pair securely across different subnets, VLANs, or guest Wi-Fi networks."
                  }
                },
                {
                  "@type": "Question",
                  "name": "How does the continuous batch scan mode work?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "In Batch Scan mode, you can capture multiple pages in succession without returning to the main menu. Once finished, you can reorder pages, rotate by 90°, fine-tune brightness and contrast, add custom watermarks, select page sizes (A4, Letter, Legal, A3), and export to a single PDF for instant printing or sharing."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Which printer brands and models are supported?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print supports all printer brands that work with Windows, including HP (LaserJet, DeskJet, OfficeJet), Canon (PIXMA, imageCLASS), Epson (EcoTank, WorkForce), Brother, Samsung, Xerox, Ricoh, Lexmark, POS thermal receipt printers, and virtual PDF printers."
                  }
                },
                {
                  "@type": "Question",
                  "name": "What makes WiFi Print better than HP Smart, NokoPrint, or PrinterShare?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Unlike HP Smart or Canon PRINT, WiFi Print is universal and not locked to one brand. Unlike NokoPrint and PrinterShare, WiFi Print is 100% free with no page limits, no subscriptions, no ads, includes on-device ML Kit OCR, and operates entirely over encrypted local TLS."
                  }
                }
              ]
            })
          }}
        />

        {/* ── JSON-LD: HowTo ── */}
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "HowTo",
              "name": "How to Print Wirelessly from Android to Any Windows PC Printer",
              "description": "Step-by-step tutorial to print documents from an Android smartphone to any printer connected to a Windows PC using WiFi Print.",
              "totalTime": "PT2M",
              "tool": [
                { "@type": "HowToTool", "name": "WiFi Print Android App" },
                { "@type": "HowToTool", "name": "WiFi Print Windows Server" },
                { "@type": "HowToTool", "name": "Any Windows-connected Printer" }
              ],
              "step": [
                {
                  "@type": "HowToStep",
                  "name": "Start the WiFi Print Server on Windows",
                  "text": "Launch the WiFi Print Server on your Windows 10 or 11 PC. It detects all installed printers and displays pairing details.",
                  "position": 1
                },
                {
                  "@type": "HowToStep",
                  "name": "Pair your Android phone",
                  "text": "Open the WiFi Print app on Android. On the same Wi-Fi, it auto-connects instantly. If on another network, scan the QR code or enter the 6-digit PIN.",
                  "position": 2
                },
                {
                  "@type": "HowToStep",
                  "name": "Select file or scan and print",
                  "text": "Choose a PDF, DOCX, image, or use the built-in OCR document scanner. Adjust print settings and tap Print to send the job immediately.",
                  "position": 3
                }
              ]
            })
          }}
        />
      </head>
      <body>
        <Navbar />
        {children}
        <footer className="footer">
          <div className="container footer-container">
            <div className="footer-info">
              <div className="logo">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M7 17H5C3.89543 17 3 16.1046 3 15V11C3 9.89543 3.89543 9 5 9H19C20.1046 9 21 9.89543 21 11V15C21 16.1046 20.1046 17 19 17H17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M17 9V5C17 3.89543 16.1046 3 15 3H9C7.89543 3 7 3.89543 7 5V9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M7 15H17V21H7V15Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                <span>WiFi Print</span>
              </div>
              <p>Free, private wireless printing from Android to any Windows PC printer over Wi-Fi. Features advanced on-device ML OCR document scanner, batch scan, password-protected PDF support, and cross-network PIN pairing.</p>
            </div>
            <div className="footer-links">
              <a href="#how-it-works">How It Works</a>
              <a href="#scanner">Document Scanner</a>
              <a href="#features">Features</a>
              <a href="#comparison">Compare</a>
              <a href="#printing-guide">Guide</a>
              <a href="#faq">FAQ</a>
              <a href="https://github.com/sagarsahni6/wifi-print" target="_blank" rel="noopener">GitHub</a>
            </div>
          </div>
          <div className="container copyright">
            <p>&copy; 2026 WiFi Print. 100% Free &amp; Open Source. Released under MIT License.</p>
          </div>
        </footer>
        <ScrollObserver />
      </body>
    </html>
  );
}
