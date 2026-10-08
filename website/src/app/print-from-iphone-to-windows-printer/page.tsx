import { QrCode, Download } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd, HowToJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "How to Print from iPhone to a Windows Printer (No App Required)",
  description: "Learn how to print from your iPhone to any printer connected to a Windows PC using QR Web Print. No native iOS app installation or AirPrint adapter required.",
  path: "/print-from-iphone-to-windows-printer",
  keywords: [
    "print from iphone to windows printer",
    "iphone qr web print",
    "print to non airprint printer iphone",
    "print from safari to windows printer",
    "iphone wireless printing guide",
  ],
});

const IPHONE_GUIDE_FAQS = [
  {
    q: "Why don't I need to install an iOS app from the App Store?",
    a: "Printora takes advantage of modern web capabilities in Mobile Safari. When you scan the QR code, your Windows PC serves a local Web Print Studio web app directly to Safari. This gives you native file picker, camera, and print controls without installing third-party apps or device profiles.",
  },
  {
    q: "Can I print photos from Apple Photos and files from iCloud Drive?",
    a: "Yes. Web Print Studio supports standard iOS file selection. You can choose photos directly from your Camera Roll or select PDFs, documents, and spreadsheets from iCloud Drive, Google Drive, or the local Files app.",
  },
  {
    q: "What if the QR code does not open in Safari when scanned?",
    a: "Verify that both your iPhone and your Windows PC are connected to the same Wi-Fi router. Also ensure that your Windows Defender Firewall allows incoming connections on port 5000 for Printora Server.",
  },
];

const HOW_TO_STEPS = [
  {
    name: "Open Printora on your Windows PC",
    text: "Launch Printora Server. The desktop dashboard will display your connected printers and a unique Web Print QR code.",
  },
  {
    name: "Scan QR code with your iPhone Camera",
    text: "Point your iPhone camera at the screen and tap the yellow Safari notification banner to open Web Print Studio.",
  },
  {
    name: "Select documents or photos",
    text: "Tap 'Select File' to choose documents from the iOS Files app or photos from your Camera Roll.",
  },
  {
    name: "Configure print preferences",
    text: "Choose your destination printer, set copies, select Color or Monochrome mode, and choose paper orientation.",
  },
  {
    name: "Send print job to Windows spooler",
    text: "Tap 'Print'. The document streams to your Windows spooler and prints immediately, with temporary files cleaned up automatically.",
  },
];

export default function IphoneToWindowsPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Guides", path: "/guides" },
    { name: "Print from iPhone to Windows", path: "/print-from-iphone-to-windows-printer" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={IPHONE_GUIDE_FAQS} />
      <HowToJsonLd
        name="How to Print from iPhone to a Windows Printer"
        description="Step-by-step instructions for printing from iPhone Safari to Windows printers without App Store downloads or AirPrint hardware."
        totalTime="PT2M"
        steps={HOW_TO_STEPS}
        tools={["iPhone with Camera & Safari", "Printora Windows Server", "Windows-Connected Printer"]}
        supplies={["Local Wi-Fi Network"]}
      />

      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-8">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
          <QrCode className="w-3.5 h-3.5" />
          <span>iPhone &amp; iOS Guide</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          How to Print from iPhone to a Windows Printer
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Print directly from your iPhone without installing a third-party iOS app. Use Printora&apos;s QR-powered Web Print Studio in Safari.
        </p>
      </div>

      <div className="mt-10 space-y-10 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            The Zero-Install iOS Solution: Web Print Studio
          </h2>
          <p>
            Unlike Android which has a dedicated native app, Apple iOS does not require you to download anything from the App Store. Printora utilizes Safari and the modern web platform to turn your Windows PC into a local QR-powered print station.
          </p>
        </section>

        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">How It Works Step-by-Step</h2>
          <ol className="list-decimal pl-5 space-y-3 text-slate-600 dark:text-slate-400 text-xs sm:text-sm">
            {HOW_TO_STEPS.map((step, idx) => (
              <li key={idx}>
                <strong className="text-slate-900 dark:text-white">{step.name}:</strong> {step.text}
              </li>
            ))}
          </ol>
        </section>

        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Built-in iOS Safeguards</h2>
          <p>
            Web Print Studio enforces rate limiting (default 2 jobs/minute per IP) and document quotas (max 3 copies and 30 pages) to prevent paper and toner exhaustion.
          </p>
        </section>

        {/* Mobile Browsing Callout */}
        <div className="p-4 rounded-2xl bg-blue-50/70 dark:bg-blue-950/40 border border-blue-200 dark:border-blue-900 text-xs text-blue-900 dark:text-blue-200">
          <strong className="block font-semibold mb-1">Browsing on your iPhone right now?</strong>
          <span>
            You need to install Printora Server on your Windows computer first to generate your printer&apos;s QR code. Bookmark this guide or download the server when you are back at your PC.
          </span>
        </div>

        {/* Author Verification */}
        <AuthorBadge
          reviewedBy="Printora Apple Platform Team"
          testedEnvironment="iOS 17 &amp; iOS 16 (Safari) • Windows 11 (24H2)"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ */}
        <MiniFaq
          items={IPHONE_GUIDE_FAQS}
          title="iPhone Web Print FAQ"
          description="Common technical questions about iOS Safari printing, file selection, and connectivity."
        />

        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-wrap gap-4 items-center justify-between">
          <Link
            href="/download/windows"
            className="px-6 py-3 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs inline-flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            <span>Download Windows Printora Server</span>
          </Link>
          <Link
            href="/features/web-print"
            className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline"
          >
            Read more about QR Web Print &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
}
