import { 
  Smartphone, 
  Monitor, 
  Printer, 
  CheckCircle2, 
  ArrowRight
} from "lucide-react";
import Link from "next/link";

export function WirelessPrintingSection() {
  const capabilities = [
    { title: "Printer Discovery", desc: "Fast mDNS discovery with UDP beacon fallback across Wi-Fi" },
    { title: "Document Formats", desc: "PDF, JPG, PNG, BMP, GIF, TXT, plus DOC/DOCX via LibreOffice" },
    { title: "Page Layouts", desc: "A4, Letter, Legal, A3, portrait or landscape orientation" },
    { title: "Color & Quality", desc: "Color or B&W modes; Draft, Normal, or High DPI quality tiers" },
    { title: "Duplex Printing", desc: "Two-sided printing supported when exposed by printer hardware" },
    { title: "Live Spool Progress", desc: "Real-time queue tracking, percentage progress & error reporting" },
  ];

  return (
    <section className="py-20 bg-slate-50/60 dark:bg-slate-900/40 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left Column: Copy & Benefits (6 cols) */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold">
              <span>Wireless Printing Engine</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight leading-tight">
              Print from the device already in your hand.
            </h2>

            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-300 leading-relaxed font-normal">
              Use your phone to send PDFs, photos, notes, and documents to printers connected to your Windows computer.
            </p>

            <div className="p-4 rounded-2xl bg-blue-50/80 dark:bg-blue-950/40 border border-blue-200/80 dark:border-blue-900/60 text-blue-900 dark:text-blue-200 text-sm leading-relaxed">
              <strong className="font-semibold text-blue-950 dark:text-blue-100 block mb-1">
                Your printer does not have to be a Wi-Fi printer.
              </strong>
              Whether your printer is connected via USB cable, ethernet LAN, or an existing Windows printer share, the Windows PC acts as the reliable bridge between your phone and your printer.
            </div>

            {/* Feature Checklist */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2">
              {capabilities.map((cap) => (
                <div key={cap.title} className="flex items-start gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
                  <div>
                    <h3 className="text-xs font-bold text-slate-900 dark:text-white">{cap.title}</h3>
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{cap.desc}</p>
                  </div>
                </div>
              ))}
            </div>

            <div className="pt-2">
              <Link
                href="/features/wireless-printing"
                className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700"
              >
                <span>Read detailed wireless printing specifications</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>

          {/* Right Column: Visual Diagram / Mockup (6 cols) */}
          <div className="lg:col-span-6">
            <div className="rounded-3xl bg-slate-900 border border-slate-800 p-6 sm:p-8 text-white shadow-xl relative overflow-hidden">
              <div className="text-xs font-mono text-slate-400 pb-4 mb-4 border-b border-slate-800 flex justify-between items-center">
                <span>Direct Wi-Fi Network Protocol</span>
                <span className="text-emerald-400">mDNS: _wifiprint._tcp</span>
              </div>

              {/* 3 Step Path */}
              <div className="space-y-4">
                <div className="flex items-center gap-4 p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 flex items-center justify-center shrink-0">
                    <Smartphone className="w-5 h-5" />
                  </div>
                  <div className="flex-1">
                    <div className="text-xs font-bold text-slate-100">Step 1: Select on Phone</div>
                    <div className="text-[11px] text-slate-400">Android App or Web Print Studio: choose copies, color, duplex</div>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 font-mono">Client</span>
                </div>

                <div className="flex items-center gap-4 p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <div className="w-10 h-10 rounded-xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center shrink-0">
                    <Monitor className="w-5 h-5" />
                  </div>
                  <div className="flex-1">
                    <div className="text-xs font-bold text-slate-100">Step 2: Windows Printora Host</div>
                    <div className="text-[11px] text-slate-400">Authenticates device, verifies TLS certificate, queues spooler job</div>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 font-mono">Host .NET 8</span>
                </div>

                <div className="flex items-center gap-4 p-3.5 rounded-2xl bg-slate-950 border border-slate-800">
                  <div className="w-10 h-10 rounded-xl bg-emerald-600/20 text-emerald-400 flex items-center justify-center shrink-0">
                    <Printer className="w-5 h-5" />
                  </div>
                  <div className="flex-1">
                    <div className="text-xs font-bold text-slate-100">Step 3: Any Windows Printer</div>
                    <div className="text-[11px] text-slate-400">Prints via standard Windows driver; temp upload cleaned on finish</div>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 font-mono">Spooler</span>
                </div>
              </div>

              {/* Status footer inside visual */}
              <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
                <span>Parallel processing across printers</span>
                <span className="text-emerald-400 font-mono font-medium">✓ Spooler Ready</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
