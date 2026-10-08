import Link from "next/link";
import { Download, QrCode, ShieldCheck } from "lucide-react";

export function FinalCtaSection() {
  return (
    <section className="py-24 sm:py-32 bg-slate-950 text-white relative overflow-hidden border-t border-slate-800">
      {/* Background glow radial effects */}
      <div 
        className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[350px] bg-blue-600/15 rounded-full blur-3xl pointer-events-none" 
        aria-hidden="true" 
      />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
        <div className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-blue-900/40 border border-blue-700/60 text-blue-300 text-xs font-semibold mb-6">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Local-First • Privacy-First Architecture</span>
        </div>

        <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight text-white leading-[1.15] max-w-3xl mx-auto">
          Your printer is already connected.{" "}
          <span className="bg-gradient-to-r from-blue-400 via-indigo-400 to-cyan-300 bg-clip-text text-transparent">
            Make it available from your phone.
          </span>
        </h2>

        <p className="mt-6 text-base sm:text-lg text-slate-300 max-w-2xl mx-auto leading-relaxed">
          Print documents. Scan paperwork. Create PDFs. Manage your printer. Use Web Print. Print remotely when you need it.
        </p>

        {/* Buttons */}
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <Link
            href="/download/windows"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-7 py-4 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-500 shadow-xl shadow-blue-600/30 active:scale-[0.98] rounded-2xl transition-all duration-150"
          >
            <Download className="w-4 h-4" />
            <span>Download for Windows</span>
          </Link>

          <Link
            href="#web-print"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2.5 px-7 py-4 text-sm font-semibold text-slate-200 bg-slate-900 hover:bg-slate-800 border border-slate-800 active:scale-[0.98] rounded-2xl transition-all duration-150"
          >
            <QrCode className="w-4 h-4 text-indigo-400" />
            <span>Print from Your Phone</span>
          </Link>
        </div>

        <p className="mt-8 text-xs text-slate-400">
          Privacy-first printing built around your own devices.
        </p>
      </div>
    </section>
  );
}
