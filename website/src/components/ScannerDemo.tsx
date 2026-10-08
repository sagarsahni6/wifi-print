"use client";

import { UserScanVectorAnimation } from "./UserScanVectorAnimation";
import { Camera, Crop, Sparkles, Printer } from "lucide-react";

export function ScannerDemo() {
  return (
    <section id="scanner" className="py-20 sm:py-28 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      {/* Section Header */}
      <div className="text-center max-w-3xl mx-auto mb-16">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-100 dark:bg-cyan-950/60 text-cyan-700 dark:text-cyan-300 text-xs font-semibold mb-3">
          <Camera className="w-3.5 h-3.5" />
          <span>Mobile Phone Camera Scanner</span>
        </div>
        <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Turn your phone into a document scanner.
        </h2>
        <p className="mt-3 text-lg text-slate-600 dark:text-slate-400">
          Scan. Clean. Create a PDF. Print.
        </p>
        <p className="mt-1 text-sm text-slate-500 max-w-xl mx-auto">
          Built-in camera scanner with automatic quad edge detection, perspective correction, filter enhancements, and direct scan-to-print. Supports up to 20 pages per document.
        </p>
      </div>

      {/* Vector Graphic Animation Component */}
      <UserScanVectorAnimation />

      {/* Additional Scanner Highlights */}
      <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-2xl bg-cyan-600/10 text-cyan-600 dark:text-cyan-400 flex items-center justify-center mb-4">
              <Crop className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">Auto Quad Edge Boundary Lock</h3>
            <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              Real-time OpenCV contour detection isolates documents on desks, crops background clutter, and corrects keystoned perspective angles.
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-[11px] font-mono font-semibold text-cyan-700 dark:text-cyan-400">
            Geometric Rectification
          </div>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-2xl bg-blue-600/10 text-blue-600 dark:text-blue-400 flex items-center justify-center mb-4">
              <Sparkles className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">Adaptive Text Enhancement</h3>
            <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              Remove cast shadows, boost character contrast, and flatten crumpled page lighting using on-device binarization and sharpening filters.
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-[11px] font-mono font-semibold text-blue-700 dark:text-blue-400">
            Auto Enhance • B&amp;W • Grayscale
          </div>
        </div>

        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between">
          <div>
            <div className="w-10 h-10 rounded-2xl bg-emerald-600/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mb-4">
              <Printer className="w-5 h-5" />
            </div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">Direct Scan-to-Print Pipeline</h3>
            <p className="mt-2 text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
              Skip intermediate file exports. Stream captured vector pages directly to the Windows print host for immediate physical output.
            </p>
          </div>
          <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-[11px] font-mono font-semibold text-emerald-700 dark:text-emerald-400">
            Zero-Export Fast Spooling
          </div>
        </div>
      </div>
    </section>
  );
}
