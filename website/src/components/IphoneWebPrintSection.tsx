"use client";

import { useState } from "react";
import { QrCode, Upload, Printer, CheckCircle2, ShieldCheck, ArrowRight } from "lucide-react";
import Link from "next/link";

export function IphoneWebPrintSection() {
  const [fileUploaded, setFileUploaded] = useState(false);
  const [isPrinting, setIsPrinting] = useState(false);

  const handlePrint = () => {
    setIsPrinting(true);
    setTimeout(() => {
      setIsPrinting(false);
      setFileUploaded(true);
    }, 1200);
  };

  return (
    <section id="web-print" className="py-20 sm:py-28 bg-white dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left: Copy & Explanation (6 cols) */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 text-xs font-semibold">
              <QrCode className="w-3.5 h-3.5" />
              <span>Zero-Install Browser Printing</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight leading-tight">
              iPhone? Just scan the QR code.
            </h2>

            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-300 leading-relaxed font-normal">
              No native iOS app required for Web Print.
            </p>

            <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
              Android gets the dedicated native Kotlin app. iPhone, iPad, and desktop browser users print seamlessly through QR-powered Web Print. Just point the camera, tap the link, upload your document, and print.
            </p>

            {/* 4 Step Flow */}
            <div className="space-y-3 pt-2">
              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-950 text-blue-600 dark:text-blue-400 font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  1
                </div>
                <div className="text-xs text-slate-600 dark:text-slate-400">
                  <strong className="text-slate-900 dark:text-slate-200">Open Printora on Windows:</strong> Display the secure Web Print QR code or connection PIN on your computer screen.
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-950 text-blue-600 dark:text-blue-400 font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  2
                </div>
                <div className="text-xs text-slate-600 dark:text-slate-400">
                  <strong className="text-slate-900 dark:text-slate-200">Scan with iPhone Camera:</strong> Safari or Chrome opens the Web Print Studio immediately. No App Store download required.
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-950 text-blue-600 dark:text-blue-400 font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  3
                </div>
                <div className="text-xs text-slate-600 dark:text-slate-400">
                  <strong className="text-slate-900 dark:text-slate-200">Select File & Preferences:</strong> Choose PDFs or images from your Files app or Photo Library. Configure copies, color, and duplex.
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-blue-100 dark:bg-blue-950 text-blue-600 dark:text-blue-400 font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  4
                </div>
                <div className="text-xs text-slate-600 dark:text-slate-400">
                  <strong className="text-slate-900 dark:text-slate-200">Tap Print:</strong> Your Windows printer spools the job instantly and cleans the temporary upload file once printed.
                </div>
              </div>
            </div>

            <div className="pt-2">
              <Link
                href="/features/web-print"
                className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700"
              >
                <span>Read Web Print Studio guide & setup</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>

          {/* Right: Realistic iPhone Browser Mockup (6 cols) */}
          <div className="lg:col-span-6 flex justify-center">
            <div className="w-full max-w-[300px] sm:max-w-[320px] rounded-[44px] bg-slate-950 p-4 border-[6px] border-slate-800 shadow-2xl relative text-slate-900">
              {/* Dynamic Island / Notch */}
              <div className="w-24 h-4 rounded-full bg-black mx-auto mb-3" />

              {/* iPhone Screen Content */}
              <div className="rounded-[32px] bg-slate-50 overflow-hidden flex flex-col justify-between aspect-[9/19.5] p-4 text-xs">
                {/* Safari URL Bar */}
                <div className="bg-white rounded-xl p-2 border border-slate-200/80 shadow-xs flex items-center justify-between text-[11px] font-mono text-slate-600">
                  <span className="flex items-center gap-1">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
                    printora.local/web-print
                  </span>
                  <span className="text-[9px] text-slate-600 font-medium">100%</span>
                </div>

                {/* Web Print Studio Body */}
                <div className="my-auto space-y-3">
                  <div className="text-center">
                    <p className="font-extrabold text-sm text-slate-900">Web Print Studio</p>
                    <p className="text-[10px] text-slate-600">Connected to Host: HP LaserJet Pro</p>
                  </div>

                  {/* Upload Box */}
                  <div className="border-2 border-dashed border-blue-400/80 rounded-2xl bg-blue-50/50 p-4 text-center">
                    <Upload className="w-6 h-6 text-blue-600 mx-auto mb-1" />
                    <div className="font-semibold text-[11px] text-slate-800">
                      {fileUploaded ? "Resume_2026.pdf (1.2 MB)" : "Choose Document or Photo"}
                    </div>
                    <div className="text-[10px] text-slate-600 font-medium">PDF, JPG, PNG, TXT supported</div>
                  </div>

                  {/* Print settings inside mobile web studio */}
                  <div className="bg-white rounded-xl p-2.5 border border-slate-200 space-y-1.5 text-[10px]">
                    <div className="flex justify-between items-center text-slate-600">
                      <span>Copies:</span>
                      <span className="font-bold text-slate-800">1 Copy</span>
                    </div>
                    <div className="flex justify-between items-center text-slate-600">
                      <span>Color:</span>
                      <span className="font-bold text-slate-800">Grayscale (B&W)</span>
                    </div>
                    <div className="flex justify-between items-center text-slate-600">
                      <span>Paper Size:</span>
                      <span className="font-bold text-slate-800">A4 Standard</span>
                    </div>
                  </div>

                  {/* Action Print Button */}
                  <button
                    type="button"
                    onClick={handlePrint}
                    className="w-full py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs flex items-center justify-center gap-2 shadow-sm transition-all cursor-pointer"
                  >
                    <Printer className="w-3.5 h-3.5" />
                    <span>{isPrinting ? "Spooling..." : fileUploaded ? "Print Document Again" : "Print Document"}</span>
                  </button>

                  {fileUploaded && (
                    <div className="text-center text-[10px] text-emerald-700 font-medium flex items-center justify-center gap-1">
                      <CheckCircle2 className="w-3 h-3" /> Job sent to Windows host
                    </div>
                  )}
                </div>

                {/* Bottom Home Indicator Bar */}
                <div className="w-32 h-1 rounded-full bg-slate-300 mx-auto mt-2" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
