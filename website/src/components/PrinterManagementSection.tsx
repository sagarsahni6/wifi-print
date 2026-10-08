import { PrinterHealthDemo } from "./PrinterHealthDemo";
import { Check } from "lucide-react";

export function PrinterManagementSection() {
  const features = [
    { title: "Multiple Printer Enumeration", desc: "Instantly detect all local and networked Windows printers" },
    { title: "One-Click Default Assignment", desc: "Select which printer receives automatic mobile print jobs" },
    { title: "Real-time Online/Offline State", desc: "Clear spooler communication status before sending jobs" },
    { title: "Duplex & Paper Tray Telemetry", desc: "Automatic detection of two-sided units and tray capacity" },
    { title: "Parallel Processing", desc: "Print different files concurrently across different printers" },
    { title: "Detailed Consumable Tracking", desc: "Black, Cyan, Magenta, and Yellow toner/ink levels where exposed" },
  ];

  return (
    <section className="py-20 sm:py-28 bg-white dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
            <span>Hardware Spooler Control</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            More than sending a print job.
          </h2>
          <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
            Gain full visibility into your Windows print environment. Monitor supplies, inspect spooler status, and manage printer capabilities directly from the host application.
          </p>
        </div>

        {/* Embedded Interactive Health & Spooler Dashboard */}
        <PrinterHealthDemo />

        {/* Feature Grid below Dashboard */}
        <div className="mt-16 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((item) => (
            <div 
              key={item.title}
              className="p-5 rounded-2xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200/70 dark:border-slate-800"
            >
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <Check className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>{item.title}</span>
              </h3>
              <p className="mt-1.5 text-xs text-slate-500 dark:text-slate-400 leading-relaxed pl-6">
                {item.desc}
              </p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
