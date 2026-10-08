import { QrCode, Download } from "lucide-react";
import Link from "next/link";
import { IphoneWebPrintSection } from "@/components/IphoneWebPrintSection";
import { WebPrintSecuritySection } from "@/components/WebPrintSecuritySection";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "QR Web Print Studio for iPhone, iPad & Modern Browsers",
  description: "Zero-install printing for iOS and browser users. Scan the QR code, upload your document, configure print options, and spool directly to Windows.",
  path: "/features/web-print",
  keywords: [
    "qr web print studio",
    "zero install iphone printing",
    "browser printing windows printer",
    "qr code guest print station",
  ],
});

export default function WebPrintPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "QR Web Print", path: "/features/web-print" },
  ];

  return (
    <div className="py-12">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 text-xs font-semibold mb-3">
          <QrCode className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          QR-Powered Web Print Studio
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Instant browser-based printing designed for iPhones, iPads, office visitors, and walk-in customers.
        </p>
      </div>

      <IphoneWebPrintSection />
      <WebPrintSecuritySection />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 pt-8 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center">
        <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
          &larr; Return to Home
        </Link>
        <Link
          href="/download/windows"
          className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
        >
          <Download className="w-3.5 h-3.5" />
          <span>Launch Web Print on Windows</span>
        </Link>
      </div>
    </div>
  );
}
