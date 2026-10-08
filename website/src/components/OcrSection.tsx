"use client";

import { useState } from "react";
import { FileSearch, Copy, Check, Sparkles, ArrowRight } from "lucide-react";
import Link from "next/link";

export function OcrSection() {
  const [copied, setCopied] = useState(false);
  const sampleOcrText = `INVOICE #INV-2026-904
Date: October 07, 2026
Client: North Star Logistics
Services: Print Spooler Integration
Amount Due: $450.00 USD
Status: Paid via Direct Transfer`;

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(sampleOcrText);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback if clipboard permissions are restricted
    }
  };

  return (
    <section className="py-20 sm:py-28 bg-white dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left: Copy & Explanations */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 text-xs font-semibold">
              <FileSearch className="w-3.5 h-3.5" />
              <span>Optical Character Recognition (OCR)</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight leading-tight">
              Scan text. Extract text.
            </h2>

            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-300 leading-relaxed font-normal">
              Convert physical receipts, invoices, agreements, and textbook pages into selectable, editable text. Recognized text can be copied to your clipboard or exported instantly into a digital document.
            </p>

            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              <strong className="text-slate-900 dark:text-slate-200 block mb-1">
                Local On-Device Processing
              </strong>
              Text recognition runs right on your mobile device without uploading your confidential paperwork to third-party cloud transcription databases.
            </div>

            <div className="pt-2">
              <Link
                href="/features/ocr"
                className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:text-blue-700"
              >
                <span>Read OCR technical specifications & capabilities</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>

          {/* Right: Interactive OCR Extraction Sandbox */}
          <div className="lg:col-span-6">
            <div className="rounded-3xl bg-slate-950 border border-slate-800 p-6 sm:p-7 shadow-xl text-white">
              <div className="flex items-center justify-between pb-4 border-b border-slate-800 text-xs font-mono">
                <span className="text-slate-400">OCR Recognition Output</span>
                <span className="text-emerald-400 flex items-center gap-1">
                  <Sparkles className="w-3.5 h-3.5" />
                  Ready to Copy
                </span>
              </div>

              {/* Code / Text Block */}
              <div className="my-5 p-4 rounded-2xl bg-slate-900 border border-slate-800 font-mono text-xs text-slate-200 leading-relaxed relative">
                <pre className="whitespace-pre-wrap">{sampleOcrText}</pre>

                <button
                  type="button"
                  onClick={copyToClipboard}
                  className="absolute top-3 right-3 p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 transition-colors flex items-center gap-1.5 text-xs font-sans cursor-pointer"
                  title="Copy extracted text"
                >
                  <span aria-live="polite" className="flex items-center gap-1.5">
                    {copied ? (
                      <>
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                        <span className="text-emerald-400 font-semibold">Copied!</span>
                      </>
                    ) : (
                      <>
                        <Copy className="w-3.5 h-3.5 text-slate-400" />
                        <span>Copy Text</span>
                      </>
                    )}
                  </span>
                </button>
              </div>

              <div className="flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-slate-800/80">
                <span>Supports Latin alphanumeric charsets</span>
                <span>One-tap clipboard integration</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
