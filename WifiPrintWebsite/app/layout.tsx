import type { Metadata } from 'next';
import { Inter, Outfit } from 'next/font/google';
import './globals.css';
import ThemeToggle from './components/ThemeToggle';
import ScrollObserver from './components/ScrollObserver';

const inter = Inter({ subsets: ['latin'], variable: '--font-inter' });
const outfit = Outfit({ subsets: ['latin'], variable: '--font-outfit' });

import Navbar from './components/Navbar';

export const metadata: Metadata = {
  title: 'Print from Android to PC over Wi-Fi | WiFi Print — Free App & Scanner',
  description: 'Free app to print from your Android phone to any Windows PC printer over Wi-Fi. No cloud, no cables. Built-in document scanner, ID card scan, passport photos. Supports PDF, DOCX, images. Download now.',
  keywords: 'print from phone to PC, print from android to windows, wifi printing app, wireless printer android, print pdf from phone, print documents from mobile, how to print from phone without wifi printer, android print to pc, print from phone to computer, mobile printing app free, scan and print app, id card scanner app, passport photo print app, print from phone to hp printer, print from samsung phone, print docx from android, local network printing, print without cloud, offline printer app, wifi direct print android, document scanner app android, scan to print, photo print from phone, free wireless printing app, print from phone to laptop, how to connect phone to printer wifi, best printing app android 2026, print from android to printer, wifi print app download',
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
    title: 'Print from Android to PC over Wi-Fi — Free Wireless Printing App',
    description: 'Free app to print from your Android phone to any Windows PC printer. Built-in document scanner, ID card scan & passport photos. No cloud needed. Download now.',
    siteName: 'WiFi Print',
    images: [{ url: 'https://wifiprint.calclabz.com/assets/og-image.jpg' }],
    locale: 'en_US',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Print from Android to PC over Wi-Fi — Free Wireless Printing App',
    description: 'Free app to print from your Android phone to any Windows PC printer. Built-in document scanner, ID card scan & passport photos. No cloud needed.',
    images: ['https://wifiprint.calclabz.com/assets/og-image.jpg'],
  },
};

export const viewport = {
  themeColor: '#6366f1',
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
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "WebSite",
              "name": "WiFi Print",
              "url": "https://wifiprint.calclabz.com/",
              "description": "Free wireless printing app to print from Android phone to any Windows PC printer over Wi-Fi.",
              "publisher": {
                "@type": "Organization",
                "name": "WiFi Print",
                "url": "https://wifiprint.calclabz.com/"
              }
            })
          }}
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "SoftwareApplication",
              "name": "WiFi Print",
              "operatingSystem": "Android 8.0+, Windows 10, Windows 11",
              "applicationCategory": "UtilitiesApplication",
              "applicationSubCategory": "Printing",
              "offers": {
                "@type": "Offer",
                "price": "0",
                "priceCurrency": "USD"
              },
              "description": "Print documents, photos, and scans from your Android phone directly to any Windows PC printer over Wi-Fi. No cloud, no cables — fast, secure, and free.",
              "author": {
                "@type": "Organization",
                "name": "WiFi Print"
              },
              "featureList": "Wi-Fi Printing, Document Scanner, ID Card Scanner, Passport Photo Templates, PDF/DOCX/Image Support, P2P Encryption, Auto-Discovery via mDNS",
              "softwareVersion": "1.2.0",
              "downloadUrl": "https://wifiprint.calclabz.com/#download"
            })
          }}
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "FAQPage",
              "mainEntity": [
                {
                  "@type": "Question",
                  "name": "How do I print from my Android phone to my Windows PC?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Run the WiFi Print Server on your Windows PC, open the WiFi Print app on your Android phone (both on the same Wi-Fi network), enter the pairing PIN, select your document (PDF, DOCX, or Image), and tap print. Your file is sent instantly over your local network."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Do I need an internet connection or cloud service to print?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "No. WiFi Print operates entirely on your local Wi-Fi network (LAN). Your files never leave your home or office network. No Google Cloud Print, no third-party servers required."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Which file formats are supported for wireless printing?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print supports PDF files, standard images (JPEG, PNG, WebP), and plain text files natively. With LibreOffice installed on your PC, it also converts and prints Microsoft Word documents (DOCX, DOC)."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Can I scan documents and print them directly from the app?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes. WiFi Print includes an AI-powered document scanner with edge detection, perspective correction, and smart cropping. You can also scan both sides of an ID card and the app will combine them onto a single printable page."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Is the connection between my phone and PC secure?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes. The connection uses HTTPS with self-signed certificates. After PIN pairing, all communication is authenticated using JSON Web Tokens (JWT). Documents never leave your local network."
                  }
                },
                {
                  "@type": "Question",
                  "name": "How do I print from my Android phone to an HP printer?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "If your HP printer is installed on your Windows PC, simply run WiFi Print Server. The app discovers all installed printers including HP LaserJet, DeskJet, and OfficeJet models. Select the HP printer from the list on your phone and print."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Can I print from my phone without a wireless printer?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes! WiFi Print works with ANY printer — including USB-only printers — as long as it is connected to your Windows PC. Your phone sends the file to the PC over Wi-Fi, and the PC handles the actual printing."
                  }
                },
                {
                  "@type": "Question",
                  "name": "How to print passport photos from my phone?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print includes built-in passport photo templates (35x45mm), visa photos (2x2 inch), and wallet-sized photos. Take a photo, select the template, and the app auto-arranges multiple copies on a single A4 sheet for economical printing."
                  }
                },
                {
                  "@type": "Question",
                  "name": "Does WiFi Print work without internet?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "Yes. WiFi Print requires only a local Wi-Fi connection between your phone and PC — no internet access is needed. This makes it ideal for secure environments, offline offices, and areas with poor connectivity."
                  }
                },
                {
                  "@type": "Question",
                  "name": "What is the best free printing app for Android in 2026?",
                  "acceptedAnswer": {
                    "@type": "Answer",
                    "text": "WiFi Print is a top-rated free printing app for Android in 2026. It supports PDF, DOCX, and images, includes a built-in document scanner and ID card scanner, and works with any printer connected to a Windows PC — all without cloud services or subscriptions."
                  }
                }
              ]
            })
          }}
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{
            __html: JSON.stringify({
              "@context": "https://schema.org",
              "@type": "HowTo",
              "name": "How to Print from Android Phone to Windows PC Printer over Wi-Fi",
              "description": "A step-by-step guide to printing documents from your Android phone to any printer connected to your Windows PC using WiFi Print.",
              "totalTime": "PT2M",
              "tool": [
                { "@type": "HowToTool", "name": "WiFi Print Android App" },
                { "@type": "HowToTool", "name": "WiFi Print Windows Server" }
              ],
              "step": [
                {
                  "@type": "HowToStep",
                  "name": "Install and run the WiFi Print Server",
                  "text": "Download and run the WiFi Print Server application on your Windows 10 or Windows 11 PC. It will automatically detect all installed printers and display a pairing PIN.",
                  "position": 1
                },
                {
                  "@type": "HowToStep",
                  "name": "Open the WiFi Print app on Android and pair",
                  "text": "Install and open the WiFi Print app on your Android device. Ensure both devices are on the same Wi-Fi network. The app will automatically discover your PC. Enter the 6-digit pairing PIN to connect.",
                  "position": 2
                },
                {
                  "@type": "HowToStep",
                  "name": "Select a file and print",
                  "text": "Choose a PDF, DOCX, image, or scanned document from your phone. Select the target printer, adjust print settings (copies, orientation, color), and tap Print. The document is sent over your local Wi-Fi and printed instantly.",
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
              <p>Free wireless printing from Android to Windows PC. Print PDFs, documents, images, and scanned files to any printer over Wi-Fi.</p>
            </div>
            <div className="footer-links">
              <a href="#">Privacy Policy</a>
              <a href="#">Terms of Service</a>
            </div>
          </div>
          <div className="container copyright">
            <p>&copy; 2026 WiFi Print. All rights reserved.</p>
          </div>
        </footer>
        <ScrollObserver />
      </body>
    </html>
  );
}
