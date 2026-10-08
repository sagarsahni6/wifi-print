import Link from "next/link";
import { Lock } from "lucide-react";

export function Hero() {
  return (
    <section className="relative pt-12 pb-16 sm:pt-16 sm:pb-24 overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
          
          {/* Left Column: Copy & Actions */}
          <div className="lg:col-span-6 text-left">
            {/* Top pill badge */}
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#eaf6ed] dark:bg-emerald-950/60 border border-[#cbe5d4] dark:border-emerald-800/80 text-[#0a7746] dark:text-emerald-300 font-mono text-xs font-semibold tracking-wider mb-6">
              <span className="w-2 h-2 rounded-full bg-[#0a7746] dark:bg-emerald-400" />
              <span>FREE • NO ACCOUNT • ZERO CLOUD STORAGE</span>
            </div>

            {/* Main Headline */}
            <h1 className="text-4xl sm:text-5xl lg:text-[56px] font-extrabold tracking-tight text-[#0e171b] dark:text-white leading-[1.12]">
              Print from your phone.
              <br />
              <span className="text-[#0a7746] dark:text-emerald-400">
                Leave no trace.
              </span>
            </h1>

            {/* Subtitle */}
            <p className="mt-5 text-base sm:text-lg text-slate-600 dark:text-slate-300 leading-relaxed max-w-xl">
              Print from any phone or browser to the printer on your Windows PC, at home or away. Files are encrypted in transit and deleted the moment they print.
            </p>

            {/* Action Buttons */}
            <div className="mt-8 flex flex-col sm:flex-row items-stretch sm:items-center gap-3.5">
              <Link
                href="/download/windows"
                className="inline-flex items-center justify-center px-6 py-3.5 text-sm font-semibold text-white bg-[#0a7746] hover:bg-[#08633a] dark:bg-emerald-600 dark:hover:bg-emerald-500 rounded-xl transition-all duration-150 shadow-sm text-center"
              >
                Download for Windows
              </Link>

              <Link
                href="/download/android"
                className="inline-flex items-center justify-center px-6 py-3.5 text-sm font-semibold text-slate-800 dark:text-slate-200 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-800 rounded-xl transition-all duration-150 shadow-xs text-center"
              >
                Get the Android app
              </Link>
            </div>

            {/* Sub-CTA Link */}
            <div className="mt-5">
              <Link
                href="/qr-code-web-printing"
                className="inline-flex items-center gap-1.5 text-sm font-medium text-[#0a7746] dark:text-emerald-400 hover:underline transition-colors group"
              >
                <span>Or try Web Print, no install needed</span>
                <span className="transition-transform group-hover:translate-x-1">→</span>
              </Link>
            </div>
          </div>

          {/* Right Column: Encrypted Live Job Card Diagram */}
          <div className="lg:col-span-6 flex justify-center lg:justify-end">
            <div className="w-full max-w-[500px] rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-xl shadow-slate-900/5 relative overflow-hidden">
              
              {/* Card Header */}
              <div className="flex items-center justify-between font-mono text-xs pb-6 border-b border-slate-100 dark:border-slate-800/80">
                <span className="text-slate-400 uppercase tracking-wider">
                  LIVE JOB • <span className="lowercase text-slate-600 dark:text-slate-400">invoice_0412.pdf</span>
                </span>
                <span className="text-[#0a7746] dark:text-emerald-400 font-bold tracking-wider">
                  TLS + AES-256
                </span>
              </div>

              {/* Diagram Stage */}
              <div className="py-10 flex items-center justify-between px-2 sm:px-6">
                
                {/* Device: Phone */}
                <div className="flex flex-col items-center">
                  <div className="w-16 h-28 rounded-2xl border-2 border-slate-800 dark:border-slate-400 bg-white dark:bg-slate-950 p-2 flex flex-col justify-between shadow-sm relative">
                    {/* Screen content bars */}
                    <div className="space-y-1.5 pt-2">
                      <div className="h-1 bg-emerald-500 rounded-full w-full" />
                      <div className="h-1 bg-emerald-400 rounded-full w-4/5" />
                      <div className="h-1 bg-emerald-300 rounded-full w-3/5" />
                    </div>
                    {/* Bottom home bar */}
                    <div className="h-0.5 bg-slate-800 dark:bg-slate-400 rounded-full w-1/3 mx-auto" />
                    
                    {/* Connection port tag */}
                    <div className="absolute -right-3 top-1/2 -translate-y-1/2 w-4 h-4 bg-[#0a7746] rounded-xs flex items-center justify-center text-white text-[8px] font-bold">
                      =
                    </div>
                  </div>
                  <span className="font-mono text-[10px] tracking-widest text-slate-400 uppercase mt-3">
                    PHONE
                  </span>
                </div>

                {/* Encrypted Transit Bridge */}
                <div className="flex-1 px-4 flex flex-col items-center justify-center relative">
                  {/* Dashed connector line */}
                  <div className="w-full border-t-2 border-dashed border-slate-300 dark:border-slate-700" />
                  
                  {/* Encrypted Lock Node */}
                  <div className="absolute -top-3.5 w-7 h-7 rounded-full bg-[#0e171b] border border-slate-700 text-white flex items-center justify-center shadow-md">
                    <Lock className="w-3.5 h-3.5 text-white" />
                  </div>

                  <span className="font-mono text-[9px] tracking-widest text-slate-400 uppercase mt-4">
                    ENCRYPTED
                  </span>
                </div>

                {/* Device: Your PC & Printer */}
                <div className="flex flex-col items-center">
                  <div className="w-24 h-24 rounded-2xl bg-[#0e171b] border border-slate-800 p-2.5 flex flex-col justify-between relative shadow-md">
                    {/* Paper in slot */}
                    <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 w-14 h-6 bg-white dark:bg-slate-200 border border-slate-300 dark:border-slate-600 rounded-t-xs p-1">
                      <div className="h-0.5 bg-slate-400 rounded-full w-full mb-1" />
                      <div className="h-0.5 bg-slate-300 rounded-full w-2/3" />
                    </div>

                    {/* Printer body details */}
                    <div className="flex items-center justify-end pt-3">
                      <div className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                    </div>

                    {/* Green printing slit */}
                    <div className="h-1.5 bg-emerald-400 rounded-xs w-full shadow-xs shadow-emerald-400/50" />
                  </div>
                  <span className="font-mono text-[10px] tracking-widest text-slate-400 uppercase mt-3">
                    YOUR PC
                  </span>
                </div>

              </div>

              {/* Bottom Badge inside card */}
              <div className="flex justify-center pt-2">
                <div className="inline-flex items-center gap-2 px-5 py-2 rounded-full bg-[#0e171b] text-emerald-400 font-mono text-[11px] font-semibold border border-slate-800 shadow-sm">
                  <span>FILE SHREDDED • 0 BYTES STORED</span>
                </div>
              </div>

            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
