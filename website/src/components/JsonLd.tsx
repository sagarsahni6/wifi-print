import { siteConfig } from "@/config/site";
import { FAQ_ITEMS } from "@/config/faq";

export function JsonLd() {
  const softwareAppSchema = {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    "name": "Printora",
    "operatingSystem": "Windows 10, Windows 11, Android 8.0+",
    "applicationCategory": "UtilitiesApplication",
    "description": siteConfig.description,
    "softwareVersion": "1.0.0",
    "url": siteConfig.url,
    "downloadUrl": siteConfig.windowsDownloadUrl,
    "featureList": [
      "Wireless Android printing to Windows printers",
      "QR-based Web Print Studio for iPhone and browsers",
      "Optional secure remote printing via Cloudflare Tunnel",
      "Mobile document camera scanner with edge detection",
      "ID card dual-side combination to A4",
      "On-device OCR text extraction",
      "Windows printer queue management and telemetry",
      "Privacy-first local execution with auto-cleanup of temporary uploads"
    ],
    "offers": {
      "@type": "Offer",
      "price": "0",
      "priceCurrency": "USD"
    }
  };

  const organizationSchema = {
    "@context": "https://schema.org",
    "@type": "Organization",
    "name": "Printora",
    "url": siteConfig.url,
    "logo": `${siteConfig.url}/logo.png`,
    "sameAs": [
      siteConfig.githubUrl
    ]
  };

  const websiteSchema = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    "name": "Printora",
    "url": siteConfig.url,
    "description": siteConfig.description
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(softwareAppSchema) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationSchema) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(websiteSchema) }}
      />
    </>
  );
}

export function FaqJsonLd({
  items,
}: {
  items?: { q: string; a: string }[];
} = {}) {
  const faqList = items || FAQ_ITEMS.slice(0, 10);
  const faqSchema = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    "mainEntity": faqList.map((item) => ({
      "@type": "Question",
      "name": item.q,
      "acceptedAnswer": {
        "@type": "Answer",
        "text": item.a,
      },
    })),
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }}
    />
  );
}

export function BreadcrumbJsonLd({
  items,
}: {
  items: { name: string; path: string }[];
}) {
  const breadcrumbSchema = {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    "itemListElement": items.map((item, index) => ({
      "@type": "ListItem",
      "position": index + 1,
      "name": item.name,
      "item": `${siteConfig.url}${item.path.startsWith("/") ? item.path : `/${item.path}`}`,
    })),
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbSchema) }}
    />
  );
}

export interface HowToStepItem {
  name: string;
  text: string;
}

export function HowToJsonLd({
  name,
  description,
  totalTime,
  steps,
  tools,
  supplies,
}: {
  name: string;
  description: string;
  totalTime?: string;
  steps: HowToStepItem[];
  tools?: string[];
  supplies?: string[];
}) {
  const howToSchema = {
    "@context": "https://schema.org",
    "@type": "HowTo",
    "name": name,
    "description": description,
    ...(totalTime ? { totalTime } : {}),
    ...(tools && tools.length > 0
      ? { tool: tools.map((t) => ({ "@type": "HowToTool", name: t })) }
      : {}),
    ...(supplies && supplies.length > 0
      ? { supply: supplies.map((s) => ({ "@type": "HowToSupply", name: s })) }
      : {}),
    "step": steps.map((step, idx) => ({
      "@type": "HowToStep",
      "position": idx + 1,
      "name": step.name,
      "text": step.text,
    })),
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(howToSchema) }}
    />
  );
}
