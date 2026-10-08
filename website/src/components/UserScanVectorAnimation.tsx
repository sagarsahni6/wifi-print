"use client";

export function UserScanVectorAnimation() {
  return (
    <div className="w-full rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 shadow-xl shadow-blue-950/5 overflow-hidden ring-1 ring-slate-900/5">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img
        src="/printora-scan-and-print.svg"
        alt="Scan a document on your phone and print it – animated workflow showing edge detection, straightening, batching, and printing"
        className="w-full h-auto"
        loading="eager"
      />
    </div>
  );
}
