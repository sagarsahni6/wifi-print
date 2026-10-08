import Image from "next/image";
import { Download, Smartphone, Layers } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "How It Works – Step-by-Step Guide",
  description: "Learn how Printora bridges mobile devices to your Windows-connected printers in four simple steps: host setup, pairing, document capture, and printing.",
  path: "/how-it-works",
  keywords: [
    "how printora works",
    "wifi print architecture guide",
    "mobile print workflow steps",
    "windows print host setup",
  ],
});

export default function HowItWorksPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "How It Works", path: "/how-it-works" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-10">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300 text-xs font-semibold mb-4">
          <Layers className="w-3.5 h-3.5" />
          <span>Complete Workflow Walkthrough</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          How Printora Works: Step by Step
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          From downloading the Windows print host to printing your first document from your phone in under two minutes.
        </p>
      </div>

      {/* Live Animated Workflow Diagram */}
      <div className="my-10 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-2 sm:p-4 shadow-xl shadow-slate-900/5 overflow-hidden">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 px-3 py-2 border-b border-slate-100 dark:border-slate-800/80 mb-2">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-mono text-xs font-bold uppercase tracking-wider text-[#0a7746] dark:text-emerald-400">
              Interactive Data Path • Phone to PC Spooler
            </span>
          </div>
          <span className="text-[11px] font-mono text-slate-500 dark:text-slate-400">
            Encrypted TLS Transit • Zero Cloud Retention
          </span>
        </div>
        <div className="w-full overflow-hidden rounded-2xl bg-[#fbfcf9] dark:bg-slate-950">
          <Image
            src="/print-workflow.svg"
            alt="Printora Live Workflow: Phone to Encrypted Network to Windows PC to Printer"
            width={1200}
            height={560}
            className="w-full h-auto block select-none"
            priority
            unoptimized
          />
        </div>
      </div>

      <div className="space-y-16">
        {/* Step 1 */}
        <div className="flex flex-col md:flex-row gap-6 md:gap-10">
          <div className="w-12 h-12 rounded-2xl bg-[#0a7746] text-white font-mono font-bold text-xl flex items-center justify-center shrink-0 shadow-lg shadow-emerald-950/20">
            01
          </div>
          <div className="space-y-3 flex-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
              Install Printora Server on Windows
            </h2>
            <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400 leading-relaxed">
              Download the Printora Server installer or standalone portable package on your Windows 10 or 11 computer (.NET 8). Launch the application. Printora will automatically enumerate all installed USB, Wi-Fi, and network printers registered in Windows.
            </p>
            <div className="p-4 rounded-2xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs font-mono text-slate-700 dark:text-slate-300">
              <div>✓ Windows Firewall prompt: Allow local network communication</div>
              <div>✓ Default printer selected automatically</div>
            </div>
          </div>
        </div>

        {/* Step 2 */}
        <div className="flex flex-col md:flex-row gap-6 md:gap-10">
          <div className="w-12 h-12 rounded-2xl bg-[#0a7746] text-white font-mono font-bold text-xl flex items-center justify-center shrink-0 shadow-lg shadow-emerald-950/20">
            02
          </div>
          <div className="space-y-3 flex-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
              Connect Your Phone (Android or iPhone)
            </h2>
            <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400 leading-relaxed">
              Choose your preferred path based on your device:
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1">
                <span className="font-bold text-slate-900 dark:text-white">Android Native App:</span>
                <p className="text-slate-500 dark:text-slate-400">
                  Open the app. It automatically detects the Windows server via mDNS and UDP beacon broadcast. Tap Connect or scan the pairing QR code.
                </p>
              </div>
              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1">
                <span className="font-bold text-slate-900 dark:text-white">iPhone / iPad / Guest:</span>
                <p className="text-slate-500 dark:text-slate-400">
                  Point the camera at the Web Print QR code on the Windows screen. Safari immediately opens the Web Print Studio. Zero app installation.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Step 3 */}
        <div className="flex flex-col md:flex-row gap-6 md:gap-10">
          <div className="w-12 h-12 rounded-2xl bg-[#0a7746] text-white font-mono font-bold text-xl flex items-center justify-center shrink-0 shadow-lg shadow-emerald-950/20">
            03
          </div>
          <div className="space-y-3 flex-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
              Select or Scan a Document
            </h2>
            <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400 leading-relaxed">
              Pick existing PDFs, pictures, or notes from your phone, or open the built-in mobile scanner:
            </p>
            <ul className="list-disc pl-5 space-y-1.5 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
              <li><strong>Document Mode:</strong> Multi-page scanning (up to 20 pages) with auto quad edge boundary detection and straightening.</li>
              <li><strong>ID Card Mode:</strong> Scan front and back sides; Printora combines both faces onto a single clean A4 sheet.</li>
              <li><strong>OCR &amp; Filters:</strong> Enhance text with B&amp;W or Auto-contrast, extract text, and preview before spooling.</li>
            </ul>
          </div>
        </div>

        {/* Step 4 */}
        <div className="flex flex-col md:flex-row gap-6 md:gap-10">
          <div className="w-12 h-12 rounded-2xl bg-[#0a7746] text-white font-mono font-bold text-xl flex items-center justify-center shrink-0 shadow-lg shadow-emerald-950/20">
            04
          </div>
          <div className="space-y-3 flex-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">
              Print &amp; Track Spooler Status
            </h2>
            <p className="text-sm sm:text-base text-slate-600 dark:text-slate-400 leading-relaxed">
              Configure copies, color, duplex, paper size, and quality mode. Tap Print. The job is spooled directly to the Windows printer. Both the mobile device and desktop show live progress percentage. Once completed, temporary upload files are cleaned up automatically.
            </p>
            <div className="pt-4 flex flex-wrap gap-4">
              <Link
                href="/download/windows"
                className="px-5 py-2.5 rounded-xl bg-[#0a7746] hover:bg-[#08633a] dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Get Windows Server</span>
              </Link>
              <Link
                href="/download/android"
                className="px-5 py-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs font-semibold inline-flex items-center gap-2 shadow-xs"
              >
                <Smartphone className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
                <span>Get Android App</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
