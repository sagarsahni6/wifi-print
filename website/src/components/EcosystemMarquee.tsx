"use client";

import { Printer, ShieldCheck, Zap, Laptop, CheckCircle } from "lucide-react";

const BRANDS = [
  { name: "HP LaserJet & DeskJet", category: "Printer OEM" },
  { name: "Brother HL / MFC Series", category: "Laser & Ink" },
  { name: "Canon imageCLASS & PIXMA", category: "Printer OEM" },
  { name: "Epson EcoTank & WorkForce", category: "Ink Tank" },
  { name: "Zebra ZD / GK Thermal", category: "Barcode / POS" },
  { name: "Ricoh & Kyocera Office", category: "Enterprise MFP" },
  { name: "Xerox VersaLink", category: "Office Workgroup" },
  { name: "Windows 11 & Windows 10", category: "Host OS" },
  { name: "Android 15 / 14 / 13", category: "Mobile App" },
  { name: "iOS 18 / iPadOS Safari", category: "QR Web Print" },
  { name: "Pantum & Lexmark Pro", category: "Desktop Laser" },
] as const;

const MARQUEE_ITEMS = [...BRANDS, ...BRANDS];

export function EcosystemMarquee() {
  return (
    <section className="py-12 border-y border-slate-200/80 dark:border-slate-800 bg-white/50 dark:bg-slate-950/50 backdrop-blur-xs overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mb-6">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              Universal Hardware Compatibility
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 text-center sm:text-right">
            Works with any printer recognized in your Windows Settings & Control Panel.
          </p>
        </div>
      </div>

      {/* Infinite Horizontal Marquee */}
      <div className="relative w-full overflow-hidden mask-linear-fade">
        {/* Gradient edge masks */}
        <div className="absolute left-0 top-0 bottom-0 w-24 bg-gradient-to-r from-slate-50 dark:from-slate-950 to-transparent z-10 pointer-events-none" />
        <div className="absolute right-0 top-0 bottom-0 w-24 bg-gradient-to-l from-slate-50 dark:from-slate-950 to-transparent z-10 pointer-events-none" />

        <div className="animate-marquee flex items-center gap-6 py-2">
          {/* Double array for seamless endless looping */}
          {MARQUEE_ITEMS.map((item, idx) => (
            <div
              key={`${item.name}-${idx}`}
              className="flex items-center gap-3 px-4 py-2.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800/80 shadow-xs hover:border-blue-500/50 transition-all shrink-0 group cursor-default"
            >
              <div className="w-7 h-7 rounded-lg bg-blue-50 dark:bg-blue-950/70 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
                <Printer className="w-3.5 h-3.5" />
              </div>
              <div className="text-left">
                <div className="text-xs font-bold text-slate-800 dark:text-slate-200 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                  {item.name}
                </div>
                <div className="text-[10px] text-slate-400 dark:text-slate-500 font-mono">
                  {item.category}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Trust & Telemetry Bar below Marquee */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="p-4 rounded-2xl bg-slate-50/80 dark:bg-slate-900/60 border border-slate-200/70 dark:border-slate-800/80 flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white">100% Local-First</div>
              <div className="text-[11px] text-slate-500">Documents never leave your network</div>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-50/80 dark:bg-slate-900/60 border border-slate-200/70 dark:border-slate-800/80 flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
              <Zap className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white">&lt; 1.2s Fast Path</div>
              <div className="text-[11px] text-slate-500">Zero-latency peer-to-peer Wi-Fi</div>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-50/80 dark:bg-slate-900/60 border border-slate-200/70 dark:border-slate-800/80 flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
              <Laptop className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white">Windows 10 & 11</div>
              <div className="text-[11px] text-slate-500">Lightweight .NET 8 native host</div>
            </div>
          </div>

          <div className="p-4 rounded-2xl bg-slate-50/80 dark:bg-slate-900/60 border border-slate-200/70 dark:border-slate-800/80 flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-50 dark:bg-amber-950/60 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
              <CheckCircle className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-bold text-slate-900 dark:text-white">No Subscription</div>
              <div className="text-[11px] text-slate-500">Free, unthrottled personal printing</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
