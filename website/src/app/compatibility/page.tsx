import { Monitor, Smartphone, Printer, CheckCircle2, AlertCircle } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Compatibility Matrix & Supported Hardware",
  description: "Check system requirements, operating systems, mobile devices, network protocols, and printer drivers supported by Printora.",
  path: "/compatibility",
  keywords: [
    "printer compatibility matrix",
    "windows printer hardware requirements",
    "supported mobile devices printora",
    "dot matrix thermal inkjet laser compatibility",
  ],
});

export default function CompatibilityPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Compatibility", path: "/compatibility" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-10">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-4">
          <Printer className="w-3.5 h-3.5" />
          <span>Hardware &amp; System Matrix</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Works with the printer you already have.
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          The printer itself does not need native mobile wireless capabilities. As long as your printer is recognized by Windows, Printora makes it accessible from your phone.
        </p>
      </div>

      <div className="mt-12 space-y-12 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        {/* Category 1: Print Host Requirements */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Monitor className="w-5 h-5 text-blue-600" />
            <span>Windows Print Host Requirements</span>
          </h2>
          <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
            <div className="divide-y divide-slate-100 dark:divide-slate-800 text-xs sm:text-sm">
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Operating System</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">Windows 10 (64-bit) or Windows 11 (64-bit)</span>
              </div>
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Runtime Framework</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">Microsoft .NET 8 Desktop Runtime (x64)</span>
              </div>
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Office Conversion (Optional)</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">LibreOffice (x64) required on host for DOC/DOCX conversion to PDF</span>
              </div>
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Network Interface</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">Local Wi-Fi connection, Ethernet LAN, or internet access for remote tunnel</span>
              </div>
            </div>
          </div>
        </section>

        {/* Category 2: Client Device Support */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Smartphone className="w-5 h-5 text-emerald-600" />
            <span>Client Device Compatibility</span>
          </h2>
          <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
            <div className="divide-y divide-slate-100 dark:divide-slate-800 text-xs sm:text-sm">
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Android Phones &amp; Tablets</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">
                  Android 8.0 (Oreo) and above. Full native app with Jetpack Compose, camera scanner, and mDNS discovery.
                </span>
              </div>
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">iPhone &amp; iPad</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">
                  iOS 14+ / iPadOS 14+ via Safari or Chrome. Uses QR-based Web Print Studio. Zero app installation.
                </span>
              </div>
              <div className="p-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
                <span className="font-bold text-slate-900 dark:text-white">Desktop &amp; Laptop Browsers</span>
                <span className="sm:col-span-2 text-slate-600 dark:text-slate-300">
                  Google Chrome, Microsoft Edge, Mozilla Firefox, Apple Safari.
                </span>
              </div>
            </div>
          </div>
        </section>

        {/* Category 3: Printer Hardware Compatibility */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Printer className="w-5 h-5 text-indigo-600" />
            <span>Printer Hardware &amp; Connection Types</span>
          </h2>
          <div className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-4 text-xs sm:text-sm">
            <p>
              Printora communicates with your printers through the standard Windows Print Spooler subsystem. Any printer that works from Windows can be used:
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>USB Cable-Connected Printers</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Ethernet / LAN Network Printers</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Existing Wi-Fi Printers</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Windows Shared Network Printers</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Laser, Inkjet, Thermal, &amp; Dot-Matrix</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Multifunction Copiers &amp; MFPs</span>
              </div>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-900 text-xs text-amber-900 dark:text-amber-200 flex items-start gap-3">
            <AlertCircle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <strong>Hardware Feature Note:</strong> Duplex, color selection, and supply telemetry (toner and ink levels) depend on whether the printer manufacturer and installed Windows driver expose these capabilities to Windows.
            </div>
          </div>
        </section>

        {/* Footer Link */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800">
          <Link
            href="/download/windows"
            className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline"
          >
            <span>Proceed to Windows Server Download &rarr;</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
