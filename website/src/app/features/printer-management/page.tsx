import { Sliders, Download } from "lucide-react";
import Link from "next/link";
import { PrinterManagementSection } from "@/components/PrinterManagementSection";
import { PrintQueueDemo } from "@/components/PrintQueueDemo";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Windows Printer Spooler & Hardware Management",
  description: "Monitor printer health, toner levels, paper trays, and manage print queue jobs with live pause, resume, priority boost, and cancellation controls.",
  path: "/features/printer-management",
  keywords: [
    "windows printer queue management",
    "print spooler monitor tool",
    "printer toner level status",
    "cancel pause resume print jobs windows",
  ],
});

export default function PrinterManagementPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "Printer Management", path: "/features/printer-management" },
  ];

  return (
    <div className="py-12">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
          <Sliders className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Printer &amp; Queue Management
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Take total control over your print spooler, supply levels, hardware telemetry, and active document queues.
        </p>
      </div>

      <PrinterManagementSection />
      <PrintQueueDemo />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 pt-8 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center">
        <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
          &larr; Return to Home
        </Link>
        <Link
          href="/download/windows"
          className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
        >
          <Download className="w-3.5 h-3.5" />
          <span>Get Printora for Windows</span>
        </Link>
      </div>
    </div>
  );
}
