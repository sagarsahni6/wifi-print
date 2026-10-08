import { Wifi, Download } from "lucide-react";
import Link from "next/link";
import { WirelessPrintingSection } from "@/components/WirelessPrintingSection";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Wireless Printing from Phone to Windows Printer",
  description: "Print directly from Android or modern browsers to any printer connected to your Windows computer. Uses local Wi-Fi, mDNS discovery, and full spooler controls.",
  path: "/features/wireless-printing",
  keywords: [
    "wireless printing phone to windows",
    "mdns local printer discovery",
    "android wifi printing engine",
    "print from phone to pc printer",
  ],
});

export default function WirelessPrintingPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "Wireless Printing", path: "/features/wireless-printing" },
  ];

  return (
    <div className="py-12">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
          <Wifi className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Wireless Printing Engine
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Transform legacy USB and network printers into fast wireless devices accessible from your phone.
        </p>
      </div>

      <WirelessPrintingSection />

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
