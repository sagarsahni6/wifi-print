import { Download, QrCode, FileText, Printer } from "lucide-react";

export function HowItWorksSection() {
  const steps = [
    {
      num: "01",
      title: "Install Printora Server",
      desc: "Download and run the lightweight .NET 8 application on your Windows 10 or 11 PC. It automatically enumerates your connected printers.",
      icon: Download,
    },
    {
      num: "02",
      title: "Connect Your Phone",
      desc: "Android discovers the PC automatically over Wi-Fi. iPhone and browser users simply scan the QR code displayed on the Windows host.",
      icon: QrCode,
    },
    {
      num: "03",
      title: "Pick or Scan a Document",
      desc: "Choose an existing PDF or photo, or use the built-in mobile camera scanner to capture paperwork, receipts, or both sides of an ID card.",
      icon: FileText,
    },
    {
      num: "04",
      title: "Print & Control Spooler",
      desc: "Set copies, color, duplex, and page range. Track live spooling progress on your phone and the Windows host in real time.",
      icon: Printer,
    },
  ];

  return (
    <section id="how-it-works" className="py-20 sm:py-28 bg-white dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300 text-xs font-semibold mb-3">
            <span>Setup & Workflow</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            How Printora Works
          </h2>
          <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
            From download to your first printed page in less than two minutes. Zero cloud accounts required.
          </p>
        </div>

        {/* 4 Step Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {steps.map((step) => {
            const Icon = step.icon;
            return (
              <div 
                key={step.num}
                className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-900/60 border border-slate-200/80 dark:border-slate-800 flex flex-col justify-between hover:border-emerald-500/50 transition-colors group"
              >
                <div>
                  <div className="flex items-center justify-between mb-6">
                    <span aria-hidden="true" className="font-mono text-2xl font-black text-emerald-700 dark:text-emerald-400 group-hover:text-emerald-600 transition-colors">
                      {step.num}
                    </span>
                    <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-400 flex items-center justify-center">
                      <Icon className="w-5 h-5" />
                    </div>
                  </div>
                  <h3 className="text-base font-bold text-slate-900 dark:text-white">{step.title}</h3>
                  <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{step.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
