import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { siteConfig } from "@/config/site";
import { Navbar } from "@/components/Navbar";
import { Footer } from "@/components/Footer";
import { JsonLd } from "@/components/JsonLd";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
  display: "swap",
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  metadataBase: new URL(siteConfig.url),
  alternates: {
    canonical: "./",
  },
  title: {
    default: "Printora – Print From Phone to Any Windows Printer",
    template: "%s | Printora",
  },
  description: siteConfig.description,
  keywords: [
    "wireless print",
    "print from android to windows printer",
    "print from iphone to windows printer",
    "qr web print",
    "windows printer host",
    "phone document scanner",
    "id card scan to pdf",
    "remote printing",
    "cloudflare tunnel printing",
    "local first print spooler",
    "privacy-first wireless printing",
  ],
  authors: [{ name: "Printora Team" }],
  creator: "Printora",
  openGraph: {
    type: "website",
    locale: "en_US",
    url: siteConfig.url,
    title: "Printora – Print From Phone to Any Windows Printer",
    description: siteConfig.description,
    siteName: "Printora",
  },
  twitter: {
    card: "summary_large_image",
    title: "Printora – Print From Phone to Any Windows Printer",
    description: siteConfig.description,
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning className={`${geistSans.variable} ${geistMono.variable} scroll-smooth`}>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `
              try {
                const stored = localStorage.getItem('theme');
                const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                if (stored === 'dark' || (!stored && prefersDark)) {
                  document.documentElement.classList.add('dark');
                } else {
                  document.documentElement.classList.remove('dark');
                }
              } catch (e) {}
            `,
          }}
        />
      </head>
      <body className="min-h-screen flex flex-col font-sans bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 antialiased selection:bg-blue-500/20 selection:text-blue-600">
        <JsonLd />
        <Navbar />
        <main className="flex-1">{children}</main>
        <Footer />
      </body>
    </html>
  );
}
