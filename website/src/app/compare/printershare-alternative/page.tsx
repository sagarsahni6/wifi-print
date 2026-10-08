import Link from "next/link";
import { Check, X, Shield, Download, Smartphone, Zap, Sparkles } from "lucide-react";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Best Free PrinterShare Alternative (No Ads, No Account) – Printora",
  description: "Looking for a free alternative to PrinterShare? Printora offers zero subscriptions, zero ads, and instant wireless mobile printing to any Windows-connected printer with full in-memory privacy.",
  path: "/compare/printershare-alternative",
  keywords: [
    "printershare alternative",
    "free printershare alternative",
    "printershare vs printora",
    "print from phone to windows printer free",
    "wireless print server for pc",
    "ad free mobile printing app",
  ],
});

export default function PrinterShareAlternativePage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Compare", path: "/compare/printershare-alternative" },
    { name: "PrinterShare Alternative", path: "/compare/printershare-alternative" },
  ];

  const comparisonFeatures = [
    { feature: "Price & Licensing", printora: "100% Free Forever", printerShare: "$12.95 Premium / Subscriptions" },
    { feature: "Advertising", printora: "Zero Ads", printerShare: "Ads in free tier" },
    { feature: "Account Required", printora: "No Account Needed", printerShare: "Registration recommended" },
    { feature: "Cloud Data Storage", printora: "0 Bytes Retained (RAM only)", printerShare: "Third-party cloud storage options" },
    { feature: "iPhone Support Without App", printora: "Yes (QR Web Print in Safari)", printerShare: "No (Must install iOS app)" },
    { feature: "Windows USB Printer Sharing", printora: "Native 1-Click Spooler Bridge", printerShare: "Requires separate desktop agent" },
    { feature: "Built-in 2-in-1 ID Card Scanner", printora: "Included (Auto A4 format)", printerShare: "Not available" },
    { feature: "On-Device OCR Text Extraction", printora: "Included (Offline Tesseract)", printerShare: "Not available" },
    { feature: "Architecture", printora: "Modern .NET 8 + Kotlin + Compose", printerShare: "Legacy legacy codebase" },
    { feature: "Open Source Code Available", printora: "Yes (GitHub)", printerShare: "Closed Source Proprietary" },
  ];

  const faqs = [
    {
      q: "Why is Printora considered the best PrinterShare alternative?",
      a: "Printora removes all the friction of PrinterShare: no paid licenses, no page quotas, no account registration, and zero cloud retention. It turns any printer connected to your Windows computer into a mobile-ready wireless printer over local Wi-Fi or secure encrypted tunnel."
    },
    {
      q: "Can I print to a USB printer without AirPrint or Wi-Fi?",
      a: "Yes. If your printer is connected to your Windows 10 or 11 PC via USB and prints normally in Windows, Printora lets your Android phone, iPhone, iPad, or browser print to it instantly."
    },
    {
      q: "Do I need to install an app on iPhone to replace PrinterShare?",
      a: "No! Unlike PrinterShare which requires downloading their mobile app from the App Store, Printora features QR Web Print Studio. Simply point your iPhone camera at the Windows screen, and Safari opens a zero-install print studio."
    },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={faqs} />

      {/* Hero Header */}
      <div className="text-center max-w-3xl mx-auto pb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-[#0a7746] dark:text-emerald-300 text-xs font-semibold mb-4 border border-[#cbe5d4] dark:border-emerald-800">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Independent Software Comparison</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold text-[#0e171b] dark:text-white tracking-tight leading-[1.15]">
          The 100% Free, Ad-Free <br className="hidden sm:inline" />
          <span className="text-[#0a7746] dark:text-emerald-400">PrinterShare Alternative</span>
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-300 leading-relaxed">
          Tired of subscriptions, page limits, and outdated interfaces? Printora bridges your phone to any Windows printer with zero cloud accounts, zero ads, and complete in-memory airgap privacy.
        </p>

        {/* CTA Buttons */}
        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3.5">
          <Link
            href="/download/windows"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-[#0a7746] hover:bg-[#08633a] dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white text-sm font-semibold shadow-sm text-center inline-flex items-center justify-center gap-2"
          >
            <Download className="w-4 h-4" />
            <span>Download for Windows (Free)</span>
          </Link>
          <Link
            href="/download/android"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-800 dark:text-slate-200 text-sm font-semibold shadow-xs text-center inline-flex items-center justify-center gap-2"
          >
            <Smartphone className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>Get the Android App</span>
          </Link>
        </div>
      </div>

      {/* Feature Comparison Table */}
      <div className="mt-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-xl shadow-slate-900/5 overflow-hidden">
        <div className="px-6 py-4 bg-slate-50/70 dark:bg-slate-800/50 border-b border-slate-200/80 dark:border-slate-800 flex items-center justify-between">
          <span className="font-bold text-sm text-slate-900 dark:text-white">Feature Breakdown</span>
          <span className="text-xs font-mono text-slate-500">Updated: October 2026</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200/80 dark:border-slate-800">
                <th className="py-4 px-6 font-bold text-slate-900 dark:text-white w-2/5">Capability</th>
                <th className="py-4 px-6 font-bold text-[#0a7746] dark:text-emerald-400 bg-emerald-50/40 dark:bg-emerald-950/20 w-3/10">
                  Printora
                </th>
                <th className="py-4 px-6 font-bold text-slate-500 dark:text-slate-400 w-3/10">
                  PrinterShare
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800/80">
              {comparisonFeatures.map((row, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors">
                  <td className="py-3.5 px-6 font-medium text-slate-900 dark:text-slate-200">
                    {row.feature}
                  </td>
                  <td className="py-3.5 px-6 font-semibold text-[#0a7746] dark:text-emerald-400 bg-emerald-50/30 dark:bg-emerald-950/10 flex items-center gap-1.5">
                    <Check className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
                    <span>{row.printora}</span>
                  </td>
                  <td className="py-3.5 px-6 text-slate-600 dark:text-slate-400">
                    {row.printerShare}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Key Advantages Cards */}
      <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Zap className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Zero App Needed for iPhone</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            While PrinterShare requires installing an iOS app, Printora generates a QR code on your PC screen that opens Web Print Studio directly in native Apple Safari.
          </p>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Shield className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Ephemeral In-Memory Spooling</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            Your documents are streamed straight into RAM buffers and purged immediately once printing concludes. 0 bytes are stored on third-party cloud servers.
          </p>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Smartphone className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Built-in Mobile Scanner &amp; OCR</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            Scan documents with auto edge detection, capture front &amp; back of ID cards onto a single A4, and extract text offline with Tesseract OCR without extra utility apps.
          </p>
        </div>
      </div>

      {/* FAQ Section */}
      <div className="mt-16 border-t border-slate-200 dark:border-slate-800 pt-12">
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight mb-6">
          Frequently Asked Questions
        </h2>
        <div className="space-y-4">
          {faqs.map((faq, idx) => (
            <div key={idx} className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200/80 dark:border-slate-800">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">{faq.q}</h3>
              <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{faq.a}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
