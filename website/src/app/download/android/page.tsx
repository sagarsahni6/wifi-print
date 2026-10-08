import { Download, Smartphone, CheckCircle2, ShieldCheck, QrCode } from "lucide-react";
import { siteConfig } from "@/config/site";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "Download Printora for Android (Native Kotlin APK)",
  description: "Download the native Printora Android mobile app for phones and tablets. Automatic mDNS discovery, camera document scanner, ID card A4 mode, and direct wireless printing.",
  path: "/download/android",
  keywords: [
    "download printora android apk",
    "android wireless print app",
    "phone document scanner apk",
    "print from android to windows printer",
    "jetpack compose printer app",
  ],
});

const ANDROID_FAQS = [
  {
    q: "Can I print directly from other apps using the Android Share Sheet?",
    a: "Yes! Printora integrates directly with the native Android intent filter system. Whenever you view a PDF, photo, or document in Chrome, Google Drive, WhatsApp, or your gallery, simply tap 'Share' and select 'Print with Printora' to send the file immediately to your selected printer.",
  },
  {
    q: "Does the Android app require an internet connection to print?",
    a: "No. Printora operates entirely over your local Wi-Fi network. Your smartphone communicates directly with your Windows PC host over local sockets. No data ever leaves your router, making it fully functional even during ISP outages.",
  },
  {
    q: "Why does the app request camera and storage permissions?",
    a: "Camera permission is used solely when capturing physical paperwork in the Document Scanner or ID Card Scanner modes, and when scanning the host PC pairing QR code. Storage permission is requested only to read user-selected files for printing. No background tracking or telemetry SDKs exist in the app.",
  },
  {
    q: "What if automatic mDNS server discovery fails on my Wi-Fi network?",
    a: "Some enterprise or budget routers block multicast DNS packets. Printora includes an automatic UDP beacon broadcast fallback and a direct manual IP / QR code pairing option. Simply point your camera at the Windows dashboard QR code to establish an immediate connection.",
  },
];

export default function DownloadAndroidPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Download", path: "/#download" },
    { name: "Android App", path: "/download/android" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={ANDROID_FAQS} />

      {/* Header */}
      <div className="text-center max-w-2xl mx-auto">
        <div className="w-16 h-16 rounded-3xl bg-emerald-600/10 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto mb-6">
          <Smartphone className="w-8 h-8" />
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Download Printora for Android
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Native Kotlin and Jetpack Compose mobile application with high-speed mDNS discovery and integrated document scanning.
        </p>
      </div>

      {/* Main Download Card */}
      <div className="mt-12 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-8 shadow-xl">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 pb-6 border-b border-slate-100 dark:border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">Printora Android APK v1.0.0</h2>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 border border-emerald-500/30">
                Release Build
              </span>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Supports Android 8.0 through Android 15 • Universal ARM64 / ARMv7 APK
            </p>
          </div>

          <a
            href={`${siteConfig.githubUrl}/releases`}
            target="_blank"
            rel="noopener noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm shadow-md shadow-emerald-500/20 transition-all cursor-pointer"
          >
            <Download className="w-4 h-4" />
            <span>Download APK Package</span>
          </a>
        </div>

        {/* Feature Highlights */}
        <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6 text-xs text-slate-600 dark:text-slate-400">
          <div className="space-y-3">
            <h3 className="font-bold text-slate-900 dark:text-white uppercase tracking-wider text-[11px]">Included Capabilities</h3>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Automatic mDNS discovery with fallback UDP broadcast</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Camera document scanner with auto quad-edge detection</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>ID Card mode: combine front &amp; back onto a single A4 PDF</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>On-device OCR text recognition with instant clipboard copy</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Native Android Share Sheet receiver for one-tap printing</span>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h3 className="font-bold text-slate-900 dark:text-white uppercase tracking-wider text-[11px]">Privacy &amp; Security Architecture</h3>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>SHA-256 TLS certificate fingerprint pinning prevents MITM</span>
              </div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>Zero third-party analytics, telemetry, or advertising SDKs</span>
              </div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>All image cropping &amp; OCR processing runs 100% on-device</span>
              </div>
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>Ephemeral cache automatically cleared after print completion</span>
              </div>
            </div>
          </div>
        </div>

        {/* Installation Instructions for APK */}
        <div className="mt-8 pt-6 border-t border-slate-100 dark:border-slate-800">
          <h3 className="font-bold text-slate-900 dark:text-white text-xs uppercase tracking-wider mb-3">
            How to Install the APK on Android
          </h3>
          <ol className="list-decimal pl-5 space-y-2 text-xs text-slate-600 dark:text-slate-400">
            <li>
              <strong>Download the APK:</strong> Tap the download button above on your Android phone to save `printora-app-v1.0.0.apk`.
            </li>
            <li>
              <strong>Allow Installation:</strong> If prompted by Android or Chrome with &quot;File might be harmful&quot;, select <em>&quot;Download anyway&quot;</em>. When opening the file, enable <em>&quot;Allow from this source&quot;</em> in your device settings.
            </li>
            <li>
              <strong>Launch &amp; Connect:</strong> Open Printora. Ensure your phone is connected to the same Wi-Fi network as your Windows PC. The app will auto-discover your Windows print server within 2 to 3 seconds.
            </li>
            <li>
              <strong>Print or Scan:</strong> Choose a file from your device, capture a document with your camera, or share an item from any other app directly to Printora.
            </li>
          </ol>
        </div>

        {/* Note on iPhone Users */}
        <div className="mt-8 p-4 rounded-2xl bg-blue-50/70 dark:bg-blue-950/40 border border-blue-200 dark:border-blue-900 text-xs text-blue-900 dark:text-blue-200 flex items-start gap-3">
          <QrCode className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
          <div>
            <strong>Need to print from an iPhone or iPad?</strong> iOS devices do not need an app. Simply scan the QR code displayed on your Windows host screen with the native Apple Camera to open the zero-install Web Print Studio in Safari.
          </div>
        </div>
      </div>

      {/* Author and Trust Badge */}
      <div className="mt-10">
        <AuthorBadge
          reviewedBy="Printora Android Core Team"
          testedEnvironment="Android 14 (Pixel 8) • Android 12 (Samsung Galaxy S21) • Android 10"
          lastUpdated="October 2026"
        />
      </div>

      {/* Mini FAQ Accordion */}
      <div className="mt-10">
        <MiniFaq
          items={ANDROID_FAQS}
          title="Android App Frequently Asked Questions"
          description="Everything you need to know about APK installation, network permissions, and scanner features."
        />
      </div>
    </div>
  );
}
