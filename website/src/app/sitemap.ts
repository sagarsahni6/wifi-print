import { MetadataRoute } from "next";
import { siteConfig } from "@/config/site";

export default function sitemap(): MetadataRoute.Sitemap {
  const routes = [
    "",
    "/security",
    "/privacy",
    "/terms",
    "/support",
    "/guides",
    "/compatibility",
    "/how-it-works",
    "/faq",
    "/download/windows",
    "/download/android",
    "/features/wireless-printing",
    "/features/document-scanner",
    "/features/id-card-scanner",
    "/features/ocr",
    "/features/remote-printing",
    "/features/web-print",
    "/features/printer-management",
    "/print-from-android-to-windows-printer",
    "/print-from-iphone-to-windows-printer",
    "/print-from-ipad-to-windows-printer",
    "/print-from-phone-to-usb-printer",
    "/qr-code-web-printing",
    "/mobile-document-scanner-to-pdf",
    "/id-card-scanner-to-pdf",
    "/remote-printing-from-phone",
  ];

  return routes.map((route) => ({
    url: `${siteConfig.url}${route}`,
    lastModified: "2026-10-07",
    changeFrequency: route === "" ? ("weekly" as const) : ("monthly" as const),
    priority: route === "" ? 1.0 : route.startsWith("/download") || route.startsWith("/features") ? 0.8 : 0.7,
  }));
}
