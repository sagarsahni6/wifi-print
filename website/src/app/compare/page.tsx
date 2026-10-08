import Link from "next/link";
import { ArrowRight, Check, Sparkles, Scale, Shield, Zap, DollarSign } from "lucide-react";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Compare Mobile & Windows Printing Solutions – Printora Alternatives",
  description: "See how Printora compares to PrinterShare, Google Cloud Print, and NokoPrint. 100% free, zero ads, zero accounts, and in-memory privacy for your Windows PC printers.",
  path: "/compare",
  keywords: [
    "mobile printing comparison",
    "printershare alternative",
    "google cloud print alternative",
    "nokoprint alternative",
    "best free mobile printing app",
    "print from phone to pc printer",
  ],
});

export default function CompareHubPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Compare", path: "/compare" },
  ];

  const comparisons = [
    {
      title: "Printora vs PrinterShare",
      slug: "/compare/printershare-alternative",
      tagline: "Stop paying $12.95 subscriptions for basic wireless printing.",
      highlight: "Save $12.95 + Zero Subscriptions",
      badge: "Most Popular",
      points: [
        "100% Free forever with no page limits",
        "Zero ads and zero tracking telemetry",
        "QR Web Print Studio works on iPhone without app installation",
        "Includes 2-in-1 ID card scanning and offline OCR",
      ],
    },
    {
      title: "Printora vs Google Cloud Print",
      slug: "/compare/google-cloud-print-alternative",
      tagline: "The modern, private replacement for Google's discontinued service.",
      highlight: "Offline Local Wi-Fi + 4G/5G Remote",
      badge: "Cloud Replacement",
      points: [
        "Replaces deprecated Google Cloud Print with zero Google account",
        "Operates fully offline on local Wi-Fi when internet is down",
        "Optional secure Cloudflare Tunnel for remote printing anywhere",
        "Ultra-lightweight Windows service (<15 MB RAM vs Chrome's 300MB)",
      ],
    },
    {
      title: "Printora vs NokoPrint",
      slug: "/compare/nokoprint-alternative",
      tagline: "Say goodbye to intrusive video ads and complex Windows SMB setups.",
      highlight: "100% Ad-Free + Zero IP Config",
      badge: "Clean & Fast",
      points: [
        "Zero video ads, banners, or commercial interruptions",
        "Automatic mDNS auto-discovery (no manual \\\\192.168.1.XX IPs)",
        "No Windows user passwords required to connect",
        "Cross-platform support for both Android and iOS Safari",
      ],
    },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />

      {/* Hero Header */}
      <div className="text-center max-w-3xl mx-auto pb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-[#0a7746] dark:text-emerald-300 text-xs font-semibold mb-4 border border-[#cbe5d4] dark:border-emerald-800">
          <Scale className="w-3.5 h-3.5" />
          <span>Product Comparisons &amp; Migration Guides</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold text-[#0e171b] dark:text-white tracking-tight leading-[1.15]">
          Find the Right Mobile <br className="hidden sm:inline" />
          <span className="text-[#0a7746] dark:text-emerald-400">Print Solution for You</span>
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-300 leading-relaxed">
          Compare Printora head-to-head with other mobile-to-PC printing tools. Discover why thousands choose our 100% free, privacy-first, zero-ad architecture.
        </p>
      </div>

      {/* Comparison Cards Grid */}
      <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
        {comparisons.map((item, idx) => (
          <div
            key={idx}
            className="flex flex-col justify-between p-7 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-md shadow-slate-900/5 hover:border-emerald-500/50 hover:shadow-lg transition-all"
          >
            <div>
              <div className="flex items-center justify-between gap-2 mb-3">
                <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-100 text-[#0a7746] dark:bg-emerald-950 dark:text-emerald-300">
                  {item.badge}
                </span>
                <span className="text-[11px] font-semibold text-slate-500">{item.highlight}</span>
              </div>
              <h2 className="text-xl font-bold text-slate-900 dark:text-white mb-2">{item.title}</h2>
              <p className="text-xs text-slate-600 dark:text-slate-400 mb-6 leading-relaxed">{item.tagline}</p>

              <div className="space-y-2.5 border-t border-slate-100 dark:border-slate-800/80 pt-4 mb-6">
                {item.points.map((pt, pIdx) => (
                  <div key={pIdx} className="flex items-start gap-2 text-xs text-slate-700 dark:text-slate-300">
                    <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
                    <span>{pt}</span>
                  </div>
                ))}
              </div>
            </div>

            <Link
              href={item.slug}
              className="mt-2 w-full py-2.5 px-4 rounded-xl bg-slate-50 dark:bg-slate-800/70 hover:bg-[#0a7746] hover:text-white dark:hover:bg-emerald-600 text-slate-800 dark:text-slate-200 text-xs font-semibold text-center inline-flex items-center justify-center gap-1.5 transition-colors group"
            >
              <span>View Full Comparison</span>
              <ArrowRight className="w-3.5 h-3.5 transition-transform group-hover:translate-x-0.5" />
            </Link>
          </div>
        ))}
      </div>

      {/* Summary Trust Banner */}
      <div className="mt-16 p-8 rounded-3xl bg-emerald-50/60 dark:bg-emerald-950/20 border border-[#cbe5d4] dark:border-emerald-900/50 flex flex-col sm:flex-row items-center justify-between gap-6">
        <div>
          <h3 className="text-lg font-bold text-slate-900 dark:text-white">
            Ready to experience effortless wireless printing?
          </h3>
          <p className="mt-1 text-xs text-slate-600 dark:text-slate-300">
            Download Printora for Windows in seconds. No credit card, no registration, no spyware.
          </p>
        </div>
        <Link
          href="/download/windows"
          className="shrink-0 px-6 py-3 rounded-xl bg-[#0a7746] hover:bg-[#08633a] dark:bg-emerald-600 dark:hover:bg-emerald-500 text-white text-xs font-semibold shadow-sm text-center inline-flex items-center gap-2"
        >
          <span>Get Printora for Free</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </div>
    </div>
  );
}
