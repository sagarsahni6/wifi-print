import Link from "next/link";
import { Check, X, Shield, Download, Smartphone, Zap, Sparkles, Globe, Server } from "lucide-react";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Best Google Cloud Print Alternative for Windows (Free, 2026) – Printora",
  description: "Still missing Google Cloud Print? Printora is the lightweight, 100% free Windows & mobile alternative. Print from phone to any printer over Wi-Fi or 4G/5G with zero port forwarding.",
  path: "/compare/google-cloud-print-alternative",
  keywords: [
    "google cloud print alternative",
    "google cloud print replacement windows",
    "cloud print alternative for home",
    "print from phone to windows printer without google cloud print",
    "free cloud printing software",
    "print to pc printer from anywhere",
  ],
});

export default function GoogleCloudPrintAlternativePage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Compare", path: "/compare/google-cloud-print-alternative" },
    { name: "Google Cloud Print Alternative", path: "/compare/google-cloud-print-alternative" },
  ];

  const comparisonFeatures = [
    { feature: "Active Status", printora: "Actively Maintained (2026+)", gcp: "Discontinued (Dec 2020)" },
    { feature: "Pricing Model", printora: "100% Free Forever", gcp: "Free (when it existed)" },
    { feature: "Google Account Required", printora: "None (Zero Account)", gcp: "Mandatory Google Account" },
    { feature: "Remote Anywhere Printing", printora: "Cloudflare Tunnel (Encrypted)", gcp: "Google Cloud Relay" },
    { feature: "Port Forwarding Needed", printora: "No (Outbound TLS only)", gcp: "No" },
    { feature: "Local Wi-Fi Offline Printing", printora: "Yes (Direct mDNS LAN)", gcp: "No (Required Internet)" },
    { feature: "Document Privacy", printora: "100% RAM Only (0 Cloud Storage)", gcp: "Uploaded to Google Servers" },
    { feature: "iOS / Safari Support", printora: "Instant QR Web Print Studio", gcp: "Third-party iOS apps required" },
    { feature: "Host PC Resource Footprint", printora: "Ultra-Light (<15 MB RAM)", gcp: "Chrome Background Process (>300 MB)" },
    { feature: "Built-in Document Scanner", printora: "Included (2-in-1 ID & PDF)", gcp: "None" },
  ];

  const faqs = [
    {
      q: "How does Printora replace Google Cloud Print in 2026?",
      a: "Google Cloud Print routed documents through Google's cloud servers to a desktop Chrome connector. Printora provides a modern, privacy-first replacement: a native Windows host app that shares any attached printer over local Wi-Fi, plus an optional encrypted Cloudflare Tunnel that lets you print from cellular 4G/5G anywhere in the world without opening router ports."
    },
    {
      q: "Do I need to keep Chrome open like old Google Cloud Print?",
      a: "No! Unlike the old Google Cloud Print connector which required running Google Chrome in the background, Printora runs as a dedicated lightweight Windows utility (.NET 8) that consumes under 15 MB of RAM and boots cleanly with Windows."
    },
    {
      q: "Can I print when my home internet is down?",
      a: "Yes. Google Cloud Print failed whenever internet access went down because print jobs had to travel to Google's servers and back. Printora operates local-first over your Wi-Fi router or hotspot using mDNS and UDP discovery, functioning completely offline without internet."
    },
    {
      q: "Is Printora really free compared to PaperCut or Mobility Print?",
      a: "Yes, Printora is 100% free and open source. Enterprise alternatives like PaperCut Mobility Print are built for corporate networks with LDAP and licensing costs. Printora is built specifically for individuals, home offices, and small business workshops."
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
          <span>Modern Cloud Print Replacement</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold text-[#0e171b] dark:text-white tracking-tight leading-[1.15]">
          The Privacy-First <br className="hidden sm:inline" />
          <span className="text-[#0a7746] dark:text-emerald-400">Google Cloud Print Alternative</span>
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-300 leading-relaxed">
          Google Cloud Print was shut down, leaving millions of legacy USB and standard Windows printers stranded. Printora brings wireless local and remote printing back — without Google accounts, telemetry, or server fees.
        </p>

        {/* CTA Buttons */}
        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3.5">
          <Link
            href="/download/windows"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-[#0a7746] hover:bg-[#08633a] dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white text-sm font-semibold shadow-sm text-center inline-flex items-center justify-center gap-2"
          >
            <Download className="w-4 h-4" />
            <span>Download Windows Bridge (Free)</span>
          </Link>
          <Link
            href="/how-it-works"
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-800 dark:text-slate-200 text-sm font-semibold shadow-xs text-center inline-flex items-center justify-center gap-2"
          >
            <Server className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>How the Architecture Works</span>
          </Link>
        </div>
      </div>

      {/* Feature Comparison Table */}
      <div className="mt-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-xl shadow-slate-900/5 overflow-hidden">
        <div className="px-6 py-4 bg-slate-50/70 dark:bg-slate-800/50 border-b border-slate-200/80 dark:border-slate-800 flex items-center justify-between">
          <span className="font-bold text-sm text-slate-900 dark:text-white">Printora vs Legacy Google Cloud Print</span>
          <span className="text-xs font-mono text-slate-500">Feature Matrix</span>
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
                  Google Cloud Print (Legacy)
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
                    {row.gcp}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 3 Pillar Value Cards */}
      <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Zap className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Local-First Speed &amp; Offline Freedom</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            Print directly across your home Wi-Fi or office subnet. Jobs transfer in milliseconds without traveling to external cloud servers, even when your ISP is offline.
          </p>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Globe className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Safe Remote Printing Anywhere</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            Need to print from cellular data or while traveling? Turn on Printora&apos;s Cloudflare Tunnel integration for end-to-end TLS print routing with zero router port forwarding.
          </p>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-[#0a7746] dark:text-emerald-400 flex items-center justify-center mb-4">
            <Shield className="w-5 h-5" />
          </div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">Zero Account &amp; Airgap Privacy</h3>
          <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
            No Google account, no passwords to sync, and no document indexing. Everything stays on your local hardware with RAM-only processing and automatic purge.
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
