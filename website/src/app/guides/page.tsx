import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";
import { 
  BookOpen, 
  Smartphone, 
  Printer, 
  QrCode, 
  ShieldCheck, 
  Camera, 
  IdCard, 
  Wifi, 
  ArrowRight,
  Clock
} from "lucide-react";

export const metadata = constructMetadata({
  title: "Printing & Scanning Guides Hub",
  description: "Comprehensive step-by-step technical guides for wireless mobile printing, QR Web Print, camera scanning, ID card PDF creation, and Windows host management.",
  path: "/guides",
  keywords: [
    "printing guides tutorials",
    "how to print from phone to windows",
    "qr web print tutorial",
    "mobile scanning instructions",
  ],
});

export default function GuidesHubPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Guides", path: "/guides" },
  ];
  const guides = [
    {
      title: "How to Print from Android to a Windows Printer",
      slug: "/print-from-android-to-windows-printer",
      category: "Android",
      readTime: "3 min read",
      desc: "Complete walkthrough using the native Printora Kotlin app, mDNS server auto-discovery, and Windows spooler transmission.",
      icon: Smartphone,
      accent: "text-emerald-500 bg-emerald-500/10",
    },
    {
      title: "How to Print from iPhone to a Windows Printer",
      slug: "/print-from-iphone-to-windows-printer",
      category: "iOS & iPadOS",
      readTime: "2 min read",
      desc: "Zero app installation needed. Scan the Windows host QR code to open Web Print Studio in mobile Safari.",
      icon: QrCode,
      accent: "text-blue-500 bg-blue-500/10",
    },
    {
      title: "How to Print from iPad to a Windows Printer",
      slug: "/print-from-ipad-to-windows-printer",
      category: "iOS & iPadOS",
      readTime: "2 min read",
      desc: "Full tablet browser workflow for multitasking, split-screen PDF preview, and direct wireless printing.",
      icon: QrCode,
      accent: "text-blue-500 bg-blue-500/10",
    },
    {
      title: "How to Print from Phone to a USB-Only Printer",
      slug: "/print-from-phone-to-usb-printer",
      category: "Hardware",
      readTime: "4 min read",
      desc: "Transform legacy non-networked USB printers into modern wireless mobile endpoints using your PC as the print server.",
      icon: Printer,
      accent: "text-indigo-500 bg-indigo-500/10",
    },
    {
      title: "How to Scan Documents to PDF with Your Phone",
      slug: "/mobile-document-scanner-to-pdf",
      category: "Document Scanning",
      readTime: "3 min read",
      desc: "Automatic edge detection, perspective correction, filter enhancement, and multi-page compilation up to 20 pages.",
      icon: Camera,
      accent: "text-amber-500 bg-amber-500/10",
    },
    {
      title: "How to Scan Both Sides of an ID Card onto a Single A4 Page",
      slug: "/id-card-scanner-to-pdf",
      category: "Identity Scanning",
      readTime: "2 min read",
      desc: "Step-by-step guide to dual-capture driver licenses or IDs and generate standard single-sheet printable layouts.",
      icon: IdCard,
      accent: "text-cyan-500 bg-cyan-500/10",
    },
    {
      title: "How to Print from Your Phone Over 4G/5G Remotely",
      slug: "/remote-printing-from-phone",
      category: "Remote Access",
      readTime: "4 min read",
      desc: "Secure remote printing using Cloudflare Zero Trust Tunnels, PIN authentication, and egress encryption.",
      icon: Wifi,
      accent: "text-purple-500 bg-purple-500/10",
    },
    {
      title: "How QR Code Web Printing Works Architecture",
      slug: "/qr-code-web-printing",
      category: "Architecture",
      readTime: "3 min read",
      desc: "Technical deep dive into one-time pairing tokens, local HTTP server sockets, and ephemeral browser upload sessions.",
      icon: ShieldCheck,
      accent: "text-rose-500 bg-rose-500/10",
    },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="text-center max-w-3xl mx-auto">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-4">
          <BookOpen className="w-3.5 h-3.5" />
          <span>Guides &amp; Setup Tutorials</span>
        </div>
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Printora Guides &amp; Tutorials
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-400">
          Learn how to get the most out of Printora on Android, iPhone, iPad, and your Windows PC.
        </p>
      </div>

      {/* Guide Cards Grid */}
      <div className="mt-16 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {guides.map((guide) => {
          const Icon = guide.icon;
          return (
            <Link
              key={guide.slug}
              href={guide.slug}
              className="group p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-blue-500/50 hover:shadow-lg transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className={`w-10 h-10 rounded-2xl flex items-center justify-center ${guide.accent}`}>
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400">
                      {guide.category}
                    </span>
                  </div>
                </div>

                <h2 className="text-base font-bold text-slate-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors line-clamp-2">
                  {guide.title}
                </h2>
                <p className="mt-2 text-xs text-slate-500 dark:text-slate-400 leading-relaxed line-clamp-3">
                  {guide.desc}
                </p>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between text-xs">
                <span className="text-slate-400 flex items-center gap-1 text-[11px]">
                  <Clock className="w-3 h-3" /> {guide.readTime}
                </span>
                <span className="text-blue-600 dark:text-blue-400 font-semibold group-hover:translate-x-1 transition-transform inline-flex items-center gap-1 text-[11px]">
                  Read Guide <ArrowRight className="w-3.5 h-3.5" />
                </span>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
