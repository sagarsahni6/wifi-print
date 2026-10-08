import type { Metadata } from "next";
import { siteConfig } from "@/config/site";

export interface MetadataProps {
  title: string;
  description: string;
  path: string;
  keywords?: string[];
  image?: string;
}

export function constructMetadata({
  title,
  description,
  path,
  keywords,
  image = `${siteConfig.url}/opengraph-image`,
}: MetadataProps): Metadata {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const fullUrl = `${siteConfig.url}${normalizedPath}`;

  return {
    title,
    description,
    alternates: {
      canonical: fullUrl,
    },
    keywords: keywords || [
      "wireless print",
      "print from android to windows printer",
      "print from iphone to windows printer",
      "qr web print",
      "windows printer host",
      "phone document scanner",
      "id card scan to pdf",
      "remote printing",
      "printora",
      "privacy-first wireless printing",
    ],
    openGraph: {
      title: `${title} | Printora`,
      description,
      url: fullUrl,
      siteName: "Printora",
      locale: "en_US",
      type: "website",
      images: [
        {
          url: image,
          width: 1200,
          height: 630,
          alt: `${title} | Printora`,
        },
      ],
    },
    twitter: {
      card: "summary_large_image",
      title: `${title} | Printora`,
      description,
      images: [image],
    },
  };
}
