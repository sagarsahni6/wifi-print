import Link from "next/link";
import { Download, QrCode, CheckCircle2 } from "lucide-react";
import { InteractiveHeroStudio } from "./InteractiveHeroStudio";

export function Hero() {
  return (
    <section className="relative pt-10 pb-20 sm:pt-16 sm:pb-28 overflow-hidden">
      {/* Background ambient lighting */}
      <div 
        className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-96 bg-gradient-to-b from-blue-50/70 via-indigo-50/30 to-transparent dark:from-blue-950/30 dark:via-indigo-950/15 dark:to-transparent -z-10 pointer-events-none" 
        aria-hidden="true" 
      />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Top announcement pill */}
        <div className="flex justify-center mb-6">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-50/80 dark:bg-blue-950/60 border border-blue-200/80 dark:border-blue-800/80 text-blue-700 dark:text-blue-300 text-xs font-semibold shadow-xs backdrop-blur-xs">
            <span className="w-2 h-2 rounded-full bg-blue-600 dark:bg-blue-400 animate-pulse" />
            <span>Local-First Windows Print Host • QR Web Print Enabled</span>
          </div>
        </div>

        {/* Headlines */}
        <div className="text-center max-w-4xl mx-auto mb-10">
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 dark:text-white leading-[1.12]">
            Turn Any Windows Printer Into a Printer{" "}
            <span className="bg-gradient-to-r from-blue-600 via-indigo-600 to-cyan-500 bg-clip-text text-transparent">
              You Can Use From Anywhere.
            </span>
          </h1>

          <p className="mt-5 text-lg sm:text-xl text-slate-700 dark:text-slate-300 leading-relaxed font-normal max-w-3xl mx-auto">
            Print from your phone to any printer connected to your Windows PC — over Wi-Fi, 4G, 5G, or any internet connection.
          </p>

          <p className="mt-3 text-sm sm:text-base text-slate-600 dark:text-slate-400 font-medium max-w-2xl mx-auto">
            Your Windows PC becomes a secure print bridge, powered by local mDNS and Cloudflare Quick Tunnel.
          </p>

          {/* Action CTAs */}
          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3 sm:gap-4">
            <Link
              href="/download/windows"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-6 py-3.5 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-500/25 active:scale-[0.98] rounded-2xl transition-all duration-150 focus-visible:outline-2 focus-visible:outline-blue-600 cursor-pointer"
            >
              <Download className="w-4 h-4" aria-hidden="true" />
              <span>Download for Windows</span>
            </Link>

            <Link
              href="#web-print"
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-6 py-3.5 text-sm font-semibold text-slate-800 dark:text-slate-200 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-800 shadow-xs active:scale-[0.98] rounded-2xl transition-all duration-150 focus-visible:outline-2 focus-visible:outline-blue-600 cursor-pointer"
            >
              <QrCode className="w-4 h-4 text-indigo-600 dark:text-indigo-400" aria-hidden="true" />
              <span>Print from Your Phone</span>
            </Link>
          </div>

          {/* Trust microcopy */}
          <div className="mt-6 flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-xs text-slate-500 dark:text-slate-400">
            <span className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Android Native App
            </span>
            <span className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              iPhone & iPad via QR Web Print
            </span>
            <span className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Local Wi-Fi Fast Path (&lt; 1.2s)
            </span>
            <span className="flex items-center gap-1.5">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Optional Remote Cloudflare Access
            </span>
          </div>
        </div>

        {/* Interactive Studio Demo Showcase */}
        <div id="web-print" className="mt-8 scroll-mt-24">
          <InteractiveHeroStudio />
        </div>
      </div>
    </section>
  );
}
