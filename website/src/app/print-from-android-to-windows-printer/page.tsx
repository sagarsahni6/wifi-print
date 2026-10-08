import { Smartphone, Download, CheckCircle2 } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd, HowToJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "How to Print from Android to a Windows Printer (Step-by-Step)",
  description: "Complete guide on printing from Android phones and tablets to any Windows-connected USB or network printer using Printora. Local Wi-Fi discovery with zero cloud dependencies.",
  path: "/print-from-android-to-windows-printer",
  keywords: [
    "print from android to windows printer",
    "android wifi printing tutorial",
    "android mdns printer discovery",
    "print from samsung phone to windows pc",
    "local network wireless printing android",
  ],
});

const ANDROID_GUIDE_FAQS = [
  {
    q: "Why use your Windows PC as a print server instead of cloud print services?",
    a: "Google Cloud Print was deprecated years ago, and third-party cloud print services introduce subscription fees and privacy risks. By using Printora on Windows, your PC acts as an in-house hardware bridge that processes print drivers locally with zero cloud hops.",
  },
  {
    q: "What if automatic mDNS server discovery does not find my Windows PC?",
    a: "Check if your Wi-Fi router has 'AP Isolation' or 'Client Isolation' enabled. If so, devices on the same Wi-Fi cannot see each other. Alternatively, you can scan the pairing QR code on the Printora Windows screen or type your PC's local IP address (e.g., 192.168.1.50) into the app.",
  },
  {
    q: "Can I print photos and documents directly from WhatsApp or Google Drive?",
    a: "Yes. In any app, tap 'Share' and choose 'Print with Printora' from the Android system share sheet. The file loads directly into the print configuration screen with your Windows printers ready.",
  },
];

const HOW_TO_STEPS = [
  {
    name: "Start Printora Server on your Windows PC",
    text: "Launch Printora on your PC. The desktop dashboard will display your active printers and a connection status of 'Ready'.",
  },
  {
    name: "Open Printora on your Android device",
    text: "Launch the native Printora app on your Android phone or tablet. The app initiates automatic mDNS discovery to locate your PC.",
  },
  {
    name: "Select your document or capture a scan",
    text: "Choose a file from your device storage, share a document from another app via the system Share menu, or tap the Camera Scanner to capture paperwork.",
  },
  {
    name: "Configure print preferences",
    text: "Adjust copies, color mode (Color vs B&W), duplex (two-sided), page range, and quality mode (Draft, Normal, High).",
  },
  {
    name: "Send job to Windows print spooler",
    text: "Tap Print. The job is encrypted via TLS with SHA-256 fingerprint verification and spooled immediately to your Windows printer.",
  },
];

export default function AndroidToWindowsPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Guides", path: "/guides" },
    { name: "Print from Android to Windows", path: "/print-from-android-to-windows-printer" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={ANDROID_GUIDE_FAQS} />
      <HowToJsonLd
        name="How to Print from Android to Any Windows Printer"
        description="Step-by-step technical guide for connecting Android phones to Windows print spoolers over local Wi-Fi."
        totalTime="PT3M"
        steps={HOW_TO_STEPS}
        tools={["Printora Android App", "Printora Windows Server", "Windows-Connected Printer"]}
        supplies={["Local Wi-Fi Network"]}
      />

      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-8">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 text-xs font-semibold mb-3">
          <Smartphone className="w-3.5 h-3.5" />
          <span>Android Guide</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          How to Print from Android to Any Windows Printer
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Learn how to connect your Android phone or tablet over local Wi-Fi to send PDFs, photos, and documents directly to your Windows print spooler.
        </p>
      </div>

      <div className="mt-10 space-y-10 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Why Use Windows as the Print Bridge?</h2>
          <p>
            Most traditional consumer printers lack native mobile driver support or require complicated cloud registration services. By running Printora Server on your Windows PC, your computer acts as an intelligent translator between the native Android app and the installed Windows hardware driver.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Prerequisites</h2>
          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2 text-xs sm:text-sm">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
              <span>A PC running Windows 10 or 11 with the printer configured and verified working.</span>
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
              <span>An Android device running Android 8.0 (API 26) or higher.</span>
            </div>
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
              <span>Both devices connected to the same local Wi-Fi router (for local-first printing).</span>
            </div>
          </div>
        </section>

        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Step-by-Step Instructions</h2>
          <ol className="list-decimal pl-5 space-y-3 text-slate-600 dark:text-slate-400 text-xs sm:text-sm">
            {HOW_TO_STEPS.map((step, idx) => (
              <li key={idx}>
                <strong className="text-slate-900 dark:text-white">{step.name}:</strong> {step.text}
              </li>
            ))}
          </ol>
        </section>

        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Troubleshooting Connection Issues</h2>
          <ul className="list-disc pl-5 space-y-2 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <li>
              <strong>Windows Defender Firewall:</strong> Ensure &quot;Printora Server&quot; is allowed on Private Networks in Windows Defender Firewall settings.
            </li>
            <li>
              <strong>Guest Wi-Fi Isolation:</strong> If your Wi-Fi router has AP Isolation enabled, phones cannot communicate with computers. Connect both devices to the primary Wi-Fi network.
            </li>
            <li>
              <strong>Spooler Offline Status:</strong> If your printer shows offline, verify USB cabling or restart the Windows spooler via PowerShell: <code>net stop spooler &amp;&amp; net start spooler</code>.
            </li>
          </ul>
        </section>

        {/* Author Verification */}
        <AuthorBadge
          reviewedBy="Printora Systems Engineering Team"
          testedEnvironment="Android 14 • Windows 11 (24H2) • Local Wi-Fi"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ */}
        <MiniFaq
          items={ANDROID_GUIDE_FAQS}
          title="Android Printing FAQ"
          description="Common technical questions regarding Android printing, discovery, and file formats."
        />

        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-wrap gap-4 items-center justify-between">
          <Link
            href="/download/android"
            className="px-6 py-3 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs inline-flex items-center gap-2"
          >
            <Smartphone className="w-4 h-4" />
            <span>Download Printora Android APK</span>
          </Link>
          <Link
            href="/download/windows"
            className="px-6 py-3 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs inline-flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            <span>Download Windows Host</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
