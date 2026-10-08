import { Check, X } from "lucide-react";

export function ComparisonSection() {
  const comparisons = [
    {
      task: "Sending a file to print",
      traditional: "Emailing files to yourself, uploading to USB drives, or logging into PC",
      printora: "Send directly from Android app or scan a QR code from any browser",
    },
    {
      task: "Scanning documents",
      traditional: "Purchasing a bulky flatbed hardware scanner or separate scanner app",
      printora: "Built-in phone camera scanner with auto edge detection, OCR, and filters",
    },
    {
      task: "Scanning identity cards",
      traditional: "Manual photo editing, resizing, or printing front & back on separate sheets",
      printora: "Automatic dual-side layout combined onto a single standard A4 page",
    },
    {
      task: "Checking printer status",
      traditional: "Walking across the room to check paper trays and blink codes",
      printora: "Live desktop telemetry for toner levels, paper status, and errors",
    },
    {
      task: "Print queue management",
      traditional: "Jobs get stuck in Windows spooler with no priority or pause controls",
      printora: "Interactive queue with pause, resume, priority boost, cancel, and reorder",
    },
    {
      task: "Remote printing",
      traditional: "Complex router port forwarding, static IP costs, or cloud print subscriptions",
      printora: "Zero-port Cloudflare Tunnel integration protected with PIN & rate limits",
    },
    {
      task: "Document privacy",
      traditional: "Third-party cloud print servers store and log your confidential files",
      printora: "Local-first host. Zero permanent cloud copies. Auto-cleanup of temp uploads",
    },
  ];

  return (
    <section className="py-20 sm:py-28 bg-white dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300 text-xs font-semibold mb-3">
            <span>Workflow Evolution</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Traditional Workflow vs. Printora
          </h2>
          <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
            See how Printora replaces slow, multi-step workarounds with a clean, unified local-first architecture.
          </p>
        </div>

        {/* Comparison Table */}
        <div className="max-w-5xl mx-auto overflow-hidden rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm bg-white dark:bg-slate-900">
          <div className="grid grid-cols-12 bg-slate-50 dark:bg-slate-950/80 p-4 border-b border-slate-200 dark:border-slate-800 text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
            <div className="col-span-3 sm:col-span-3">Task</div>
            <div className="col-span-4 sm:col-span-4 text-rose-700 dark:text-rose-400">Traditional Methods</div>
            <div className="col-span-5 sm:col-span-5 text-emerald-700 dark:text-emerald-400">With Printora</div>
          </div>

          <div className="divide-y divide-slate-100 dark:divide-slate-800/80">
            {comparisons.map((item) => (
              <div 
                key={item.task}
                className="grid grid-cols-12 p-4 sm:p-5 text-xs sm:text-sm items-center hover:bg-slate-50/50 dark:hover:bg-slate-800/40 transition-colors"
              >
                <div className="col-span-3 font-semibold text-slate-900 dark:text-white pr-2">
                  {item.task}
                </div>
                <div className="col-span-4 text-slate-600 dark:text-slate-300 flex items-start gap-2 pr-3">
                  <X className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                  <span className="leading-snug text-xs">{item.traditional}</span>
                </div>
                <div className="col-span-5 text-slate-800 dark:text-slate-200 font-medium flex items-start gap-2">
                  <Check className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
                  <span className="leading-snug text-xs">{item.printora}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
