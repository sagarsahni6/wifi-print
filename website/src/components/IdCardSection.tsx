"use client";

import { useState } from "react";
import { CreditCard, CheckCircle2, ArrowRight } from "lucide-react";
import Link from "next/link";

export function IdCardSection() {
  const [mode, setMode] = useState<"dual" | "single">("dual");

  return (
    <section className="py-20 sm:py-28 bg-slate-50/60 dark:bg-slate-900/40 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left: Copy & Explanations (6 cols) */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold">
              <CreditCard className="w-3.5 h-3.5" />
              <span>Smart ID Card Layout Engine</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight leading-tight">
              Scan both sides of an ID card into a clean printable page.
            </h2>

            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-300 leading-relaxed font-normal">
              Capture the front and back of any identity document, badge, or license. Printora automatically arranges both sides centered onto a single standard A4 sheet without watermarks or intrusive promotional labels.
            </p>

            {/* Mode selection buttons */}
            <div className="flex items-center gap-2 pt-2">
              <button
                type="button"
                aria-pressed={mode === "dual"}
                onClick={() => setMode("dual")}
                className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                  mode === "dual"
                    ? "bg-blue-600 text-white shadow-sm"
                    : "bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-700"
                }`}
              >
                Front + Back Dual Layout
              </button>
              <button
                type="button"
                aria-pressed={mode === "single"}
                onClick={() => setMode("single")}
                className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                  mode === "single"
                    ? "bg-blue-600 text-white shadow-sm"
                    : "bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-700"
                }`}
              >
                Front-Only Layout
              </button>
            </div>

            {/* Feature points */}
            <div className="space-y-3 pt-2 text-sm text-slate-600 dark:text-slate-400">
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Automatic perspective correction on each card face</span>
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Standardized 1:1 scale reproduction for legal and office paperwork</span>
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                <span>Zero intrusive application branding printed onto final card output</span>
              </div>
            </div>

            <div className="pt-2">
              <Link
                href="/features/id-card-scanner"
                className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700"
              >
                <span>Read ID card scanning workflow details</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>

          {/* Right: Printable A4 Sheet Visual Mockup (6 cols) */}
          <div className="lg:col-span-6 flex justify-center">
            <div className="w-full max-w-sm aspect-[1/1.414] bg-white rounded-2xl shadow-2xl border border-slate-300 p-6 flex flex-col justify-between text-slate-900 relative">
              {/* Top A4 header indicator */}
              <div className="flex justify-between items-center text-[10px] font-mono text-slate-600 border-b border-slate-200 pb-2">
                <span>STANDARD A4 SHEET (210 × 297 mm)</span>
                <span>300 DPI</span>
              </div>

              {/* Card Previews Arranged Centered on Sheet */}
              <div className="space-y-6 my-auto">
                {/* Front Side */}
                <div className="w-full h-28 rounded-xl border border-slate-300 bg-slate-50 p-3 shadow-xs relative flex items-center gap-3">
                  <div className="w-14 h-18 bg-slate-200 rounded-md flex items-center justify-center text-[9px] text-slate-700 font-mono font-bold">
                    PHOTO
                  </div>
                  <div className="space-y-1 text-xs">
                    <div className="text-[10px] uppercase font-bold text-blue-700 font-mono">IDENTIFICATION CARD • FRONT</div>
                    <div className="font-bold text-slate-900">EMILY S. JOHNSON</div>
                    <div className="text-[10px] text-slate-600 font-mono">ID NO: 4920-8819-012</div>
                    <div className="text-[10px] text-slate-600">EXPIRES: 12 / 2030</div>
                  </div>
                  <span className="absolute top-2 right-2 text-[9px] font-mono px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 font-semibold">
                    Side A
                  </span>
                </div>

                {/* Back Side (if dual mode) */}
                {mode === "dual" && (
                  <div className="w-full h-28 rounded-xl border border-slate-300 bg-slate-50 p-3 shadow-xs relative flex items-center justify-between">
                    <div className="space-y-1.5 text-xs w-full">
                      <div className="text-[10px] uppercase font-bold text-slate-700 font-mono">IDENTIFICATION CARD • BACK</div>
                      <div className="h-4 w-full bg-slate-200 rounded font-mono text-[9px] flex items-center px-2 text-slate-700">
                        |||||||||||||||||||||||||||||||||||||||||||||||||||||||
                      </div>
                      <div className="text-[10px] text-slate-600">RESIDENCE ADDRESS: 104 PARK AVENUE</div>
                      <div className="text-[9px] text-slate-600">ISSUING AUTHORITY: REGIONAL REGISTRY</div>
                    </div>
                    <span className="absolute top-2 right-2 text-[9px] font-mono px-1.5 py-0.5 rounded bg-slate-200 text-slate-800 font-semibold">
                      Side B
                    </span>
                  </div>
                )}
              </div>

              {/* Bottom Sheet Footer */}
              <div className="text-center text-[10px] text-slate-600 font-mono font-semibold border-t border-slate-200 pt-2">
                READY FOR INSTANT WINDOWS SPOOLER PRINTING
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
