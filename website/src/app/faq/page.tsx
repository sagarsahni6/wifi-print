import { FaqSection } from "@/components/FaqSection";
import { FaqJsonLd, BreadcrumbJsonLd } from "@/components/JsonLd";
import { constructMetadata } from "@/lib/metadata";

export const metadata = constructMetadata({
  title: "Frequently Asked Questions (FAQ)",
  description: "Comprehensive answers to common questions about Printora wireless printing, QR Web Print, scanner capabilities, security, and hardware compatibility.",
  path: "/faq",
  keywords: [
    "printora faq",
    "wifi print troubleshooting",
    "frequently asked questions wireless printing",
    "printora server help",
  ],
});

export default function FaqPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "FAQ", path: "/faq" },
  ];

  return (
    <div className="py-12 sm:py-16">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd />
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-8 text-center">
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Frequently Asked Questions
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
          Find fast answers regarding network connectivity, supported file formats, security architecture, and system requirements.
        </p>
      </div>
      <FaqSection />
    </div>
  );
}
