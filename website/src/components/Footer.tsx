import Link from "next/link";
import { siteConfig } from "@/config/site";
import { Printer, ExternalLink } from "lucide-react";

function GithubIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" {...props}>
      <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22" />
    </svg>
  );
}

export function Footer() {
  return (
    <footer className="bg-slate-950 text-slate-400 border-t border-slate-900 pt-16 pb-12 text-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 md:grid-cols-6 gap-8 lg:gap-10 pb-12 border-b border-slate-900">
          {/* Brand Column (2 cols on md) */}
          <div className="col-span-2 space-y-4">
            <Link href="/" className="flex items-center gap-2.5 font-bold text-lg text-white group">
              <div className="w-8 h-8 rounded-xl bg-emerald-700 flex items-center justify-center text-white shadow-md shadow-emerald-700/20 group-hover:scale-105 transition-transform duration-200">
                <Printer className="w-4 h-4" />
              </div>
              <span className="font-extrabold text-base tracking-tight text-white">
                Printora
              </span>
            </Link>

            <p className="text-slate-400 text-xs leading-relaxed max-w-sm">
              Print from Android, iPhone, iPad, or any modern web browser to printers connected to your Windows computer. Privacy-first, local-first architecture.
            </p>

            <div className="pt-2 flex items-center gap-3">
              <a
                href={siteConfig.githubUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900 hover:bg-slate-800 text-slate-300 transition-colors border border-slate-800 text-xs"
              >
                <GithubIcon className="w-3.5 h-3.5" />
                <span>Open Source Repository</span>
                <ExternalLink className="w-3 h-3 text-slate-500" />
              </a>
            </div>
          </div>

          {/* Col 1: Product Features */}
          <div className="space-y-3">
            <p className="font-bold text-slate-200 uppercase tracking-wider text-[11px]">Product</p>
            <ul className="space-y-2">
              <li>
                <Link href="/features/wireless-printing" className="hover:text-white transition-colors">
                  Wireless Printing
                </Link>
              </li>
              <li>
                <Link href="/features/document-scanner" className="hover:text-white transition-colors">
                  Document Scanner
                </Link>
              </li>
              <li>
                <Link href="/features/id-card-scanner" className="hover:text-white transition-colors">
                  ID Card Scanner
                </Link>
              </li>
              <li>
                <Link href="/features/ocr" className="hover:text-white transition-colors">
                  OCR Text Extraction
                </Link>
              </li>
              <li>
                <Link href="/features/web-print" className="hover:text-white transition-colors">
                  QR Web Print
                </Link>
              </li>
              <li>
                <Link href="/features/remote-printing" className="hover:text-white transition-colors">
                  Remote Printing
                </Link>
              </li>
              <li>
                <Link href="/features/printer-management" className="hover:text-white transition-colors">
                  Printer Management
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 2: Solutions & Step-by-Step Guides */}
          <div className="space-y-3">
            <p className="font-bold text-slate-200 uppercase tracking-wider text-[11px]">Solutions</p>
            <ul className="space-y-2">
              <li>
                <Link href="/print-from-android-to-windows-printer" className="hover:text-white transition-colors">
                  Print from Android
                </Link>
              </li>
              <li>
                <Link href="/print-from-iphone-to-windows-printer" className="hover:text-white transition-colors">
                  Print from iPhone
                </Link>
              </li>
              <li>
                <Link href="/print-from-ipad-to-windows-printer" className="hover:text-white transition-colors">
                  Print from iPad
                </Link>
              </li>
              <li>
                <Link href="/print-from-phone-to-usb-printer" className="hover:text-white transition-colors">
                  Print to USB Printer
                </Link>
              </li>
              <li>
                <Link href="/remote-printing-from-phone" className="hover:text-white transition-colors">
                  Remote 4G/5G Print
                </Link>
              </li>
              <li>
                <Link href="/id-card-scanner-to-pdf" className="hover:text-white transition-colors">
                  ID Card to A4 PDF
                </Link>
              </li>
              <li>
                <Link href="/mobile-document-scanner-to-pdf" className="hover:text-white transition-colors">
                  Phone Scan to PDF
                </Link>
              </li>
              <li>
                <Link href="/qr-code-web-printing" className="hover:text-white transition-colors">
                  QR Web Print Architecture
                </Link>
              </li>
            </ul>
          </div>

          {/* Col 3: Resources */}
          <div className="space-y-3">
            <p className="font-bold text-slate-200 uppercase tracking-wider text-[11px]">Resources</p>
            <ul className="space-y-2">
              <li>
                <Link href="/how-it-works" className="hover:text-white transition-colors">
                  How It Works
                </Link>
              </li>
              <li>
                <Link href="/faq" className="hover:text-white transition-colors">
                  FAQ
                </Link>
              </li>
              <li>
                <Link href="/guides" className="hover:text-white transition-colors">
                  Guides &amp; Tutorials
                </Link>
              </li>
              <li>
                <Link href="/compatibility" className="hover:text-white transition-colors">
                  Compatibility Matrix
                </Link>
              </li>
              <li>
                <Link href="/support" className="hover:text-white transition-colors">
                  Support &amp; Help Desk
                </Link>
              </li>
              <li>
                <a 
                  href={siteConfig.githubUrl} 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  className="hover:text-white transition-colors inline-flex items-center gap-1"
                >
                  <span>Documentation</span>
                  <ExternalLink className="w-2.5 h-2.5" />
                </a>
              </li>
            </ul>
          </div>

          {/* Col 4: Trust & Downloads */}
          <div className="space-y-3">
            <p className="font-bold text-slate-200 uppercase tracking-wider text-[11px]">Trust &amp; Get</p>
            <ul className="space-y-2">
              <li>
                <Link href="/security" className="hover:text-white transition-colors">
                  Security Whitepaper
                </Link>
              </li>
              <li>
                <Link href="/privacy" className="hover:text-white transition-colors">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link href="/terms" className="hover:text-white transition-colors">
                  Terms of Service
                </Link>
              </li>
              <li>
                <Link href="/download/windows" className="hover:text-white transition-colors font-semibold text-blue-400">
                  Download Windows (.NET 8)
                </Link>
              </li>
              <li>
                <Link href="/download/android" className="hover:text-white transition-colors font-semibold text-emerald-400">
                  Get Android App (APK)
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom copyright & attribution */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-slate-400">
          <p>© 2026 Printora. All rights reserved. Local-first printing architecture.</p>
          <p className="flex items-center gap-1">
            <span>Windows PC is the print host • Zero permanent cloud document storage</span>
          </p>
        </div>
      </div>
    </footer>
  );
}
