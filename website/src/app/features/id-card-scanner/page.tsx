import { CreditCard } from "lucide-react";
import Link from "next/link";
import { IdCardSection } from "@/components/IdCardSection";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "ID Card Scanner – Dual-Side Combination to A4",
  description: "Scan both sides of an ID card or license and automatically combine them onto a single printable A4 page without promotional clutter.",
  path: "/features/id-card-scanner",
  keywords: [
    "id card dual side scanner",
    "combine id card front back a4",
    "driver license photocopy app",
    "id card scan to print",
  ],
});

export default function IdCardScannerPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "ID Card Scanner", path: "/features/id-card-scanner" },
  ];

  return (
    <div className="py-12">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
          <CreditCard className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          ID Card Dual-Side Scanner
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Cleanly combine front and back scans of driver licenses, employee badges, and ID cards onto one A4 page.
        </p>
      </div>

      <IdCardSection />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 pt-8 border-t border-slate-200 dark:border-slate-800 flex justify-between items-center">
        <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
          &larr; Return to Home
        </Link>
        <Link
          href="/download/android"
          className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
        >
          <CreditCard className="w-3.5 h-3.5" />
          <span>Get ID Card Scanner App</span>
        </Link>
      </div>
    </div>
  );
}
