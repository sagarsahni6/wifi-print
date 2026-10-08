import { Shield, EyeOff, Camera, HardDrive } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Privacy Policy & Local-First Data Architecture",
  description: "Printora privacy policy and local-first data processing disclosure: zero permanent cloud storage, local device state, camera permissions, and automatic cleanup.",
  path: "/privacy",
  keywords: [
    "printora privacy policy",
    "zero cloud storage print software",
    "local-first printing privacy",
    "data protection wireless printing",
  ],
});

export default function PrivacyPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Privacy Policy", path: "/privacy" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-10">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 text-xs font-semibold mb-4">
          <Shield className="w-3.5 h-3.5" />
          <span>Privacy by Design</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Privacy Policy &amp; Data Disclosure
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          How Printora handles your documents, hardware telemetry, network sessions, and application permissions.
        </p>
      </div>

      {/* Main Privacy Breakdown */}
      <div className="mt-12 space-y-12 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        {/* Core Pledge */}
        <div className="p-6 rounded-3xl bg-blue-50/70 dark:bg-blue-950/30 border border-blue-200 dark:border-blue-900">
          <h2 className="text-lg font-bold text-blue-950 dark:text-blue-100 flex items-center gap-2">
            <EyeOff className="w-5 h-5 text-blue-600" />
            <span>The Printora Privacy Promise</span>
          </h2>
          <p className="mt-2 text-sm text-blue-900 dark:text-blue-200 leading-relaxed">
            Printora operates on a <strong>local-first architecture</strong>. Your printed documents, invoices, legal contracts, and scanned photos never touch a multi-tenant corporate cloud storage server. Your Windows PC is the sole host and custodian of your print jobs.
          </p>
        </div>

        {/* Section 1: Document Processing & Automatic Cleanup */}
        <section className="space-y-4">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            1. Document Data &amp; Automatic Cleanup
          </h3>
          <p>
            When you send a document to print, the file is received by the Printora server on your Windows host PC:
          </p>
          <ul className="list-disc pl-5 space-y-2 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <li>
              <strong>Temporary Staging:</strong> Uploaded files are temporarily staged in the local application directory to allow conversion, spooling, and rendering.
            </li>
            <li>
              <strong>Automatic Cleanup:</strong> When automatic cleanup is enabled in settings, temporary upload files and converted PDF artifacts are purged immediately after the Windows print spooler reports successful execution.
            </li>
            <li>
              <strong>No Cloud Archive:</strong> There is no cloud document archive, no indexing of personal contents, and no training of AI models on your scanned files.
            </li>
          </ul>
        </section>

        {/* Section 2: Mobile Application Permissions */}
        <section className="space-y-4">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            2. Mobile Permissions (Android &amp; iOS)
          </h3>
          <p>
            Our native Android application requests minimal permissions strictly necessary for core functionality:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <span className="font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                <Camera className="w-4 h-4 text-emerald-500" />
                Camera Permission
              </span>
              <p className="text-slate-500 dark:text-slate-400">
                Used solely for the interactive document scanner, ID card scanner, and scanning pairing QR codes. Photos taken are processed locally on-device.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <span className="font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                <HardDrive className="w-4 h-4 text-blue-500" />
                File Access / Photo Picker
              </span>
              <p className="text-slate-500 dark:text-slate-400">
                Used strictly when you explicitly select a PDF, image, or document to send to the printer.
              </p>
            </div>
          </div>
          <p className="text-xs text-slate-500">
            iPhone and iPad Web Print uses the standard HTML file input in Safari without requiring device permissions.
          </p>
        </section>

        {/* Section 3: OCR Processing */}
        <section className="space-y-4">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            3. OCR (Optical Character Recognition) Processing
          </h3>
          <p>
            Optical Character Recognition algorithms run entirely on your mobile phone or Windows host machine. Scanned document text is never transmitted to cloud vision APIs or remote transcription vendors.
          </p>
        </section>

        {/* Section 4: Remote Printing & Cloudflare Tunnel */}
        <section className="space-y-4">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            4. Remote Printing &amp; Third-Party Services
          </h3>
          <p>
            Remote printing is completely optional. If enabled:
          </p>
          <ul className="list-disc pl-5 space-y-2 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <li>
              Printora can connect via Cloudflare Tunnel infrastructure to relay encrypted HTTPS traffic to your Windows host.
            </li>
            <li>
              The tunnel relay acts solely as a real-time byte conduit and does not permanently store print payloads.
            </li>
            <li>
              If you prefer zero external infrastructure, remote printing can remain disabled and print strictly over local Wi-Fi.
            </li>
          </ul>
        </section>

        {/* Section 5: Telemetry & Analytics */}
        <section className="space-y-4">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            5. Analytics &amp; Advertising
          </h3>
          <p>
            Printora contains zero third-party advertising SDKs, zero cross-site tracking pixels, and zero data monetization brokers. Application state (such as default printer choice and approved devices) is stored exclusively in your local Windows database.
          </p>
        </section>

        {/* Footer links */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-wrap items-center justify-between gap-4">
          <Link
            href="/security"
            className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline"
          >
            ← View Security Architecture Whitepaper
          </Link>
          <span className="text-xs text-slate-400">
            Last Updated: October 2026
          </span>
        </div>
      </div>
    </div>
  );
}
