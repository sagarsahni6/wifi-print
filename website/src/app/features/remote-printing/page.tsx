import { Globe, Download } from "lucide-react";
import Link from "next/link";
import { RemotePrintingSection } from "@/components/RemotePrintingSection";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Optional Remote Printing via Cloudflare Tunnel",
  description: "Print from anywhere over 4G/5G connections using secure Cloudflare Tunnel integration. Zero port forwarding, PIN authentication, and session controls.",
  path: "/features/remote-printing",
  keywords: [
    "secure remote printing cloudflare tunnel",
    "print over cellular 4g 5g",
    "zero port forwarding wireless printing",
    "remote print server feature",
  ],
});

export default function RemotePrintingPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "Remote Printing", path: "/features/remote-printing" },
  ];

  return (
    <div className="py-12">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-100 dark:bg-cyan-950/60 text-cyan-700 dark:text-cyan-300 text-xs font-semibold mb-3">
          <Globe className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Secure Remote Printing
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Print documents while away from your desk over cellular data without exposing your home network.
        </p>
      </div>

      <RemotePrintingSection />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 pt-8 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center">
        <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
          &larr; Return to Home
        </Link>
        <Link
          href="/download/windows"
          className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
        >
          <Download className="w-3.5 h-3.5" />
          <span>Download Windows Server</span>
        </Link>
      </div>
    </div>
  );
}
