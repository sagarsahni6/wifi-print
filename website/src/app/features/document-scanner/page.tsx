import { Camera, FileCheck, Cpu } from "lucide-react";
import Link from "next/link";
import { ScannerDemo } from "@/components/ScannerDemo";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "Mobile Phone Camera Document Scanner Feature",
  description: "Capture crisp, perspective-corrected PDFs with automatic quad edge detection, adaptive contrast filters, and direct scan-to-print. Supports up to 20 pages per batch.",
  path: "/features/document-scanner",
  keywords: [
    "mobile document scanner feature",
    "camera scanner quad edge detection",
    "scan to pdf android",
    "high resolution document capture",
    "on device pdf scanner",
  ],
});

const FEATURE_SCANNER_FAQS = [
  {
    q: "How does the quad edge detection algorithm isolate paper against complex surfaces?",
    a: "Printora employs a multi-stage computer vision pipeline built on top of Android CameraX. An adaptive Canny edge filter detects high-contrast boundary gradients, followed by a convex polygon contour finder that identifies the four largest quadrangular vertices corresponding to the paper edges.",
  },
  {
    q: "What occurs if the automatic boundary detection misidentifies a corner?",
    a: "Users can switch to manual crop mode at any moment. Interactive circular handles appear on each corner with a real-time magnified loupe viewer, allowing sub-pixel precision alignment along paper margins even on textured wooden tables.",
  },
  {
    q: "Can I adjust individual page orientation and ordering in a batch scan?",
    a: "Yes. The batch scanning carousel lets you rotate any sheet in 90-degree increments, reorder pages via drag-and-drop, delete accidental duplicate frames, or re-take individual pages without discarding the rest of the document.",
  },
  {
    q: "How are multi-page documents compiled into standard PDF files?",
    a: "Printora generates ISO 32000-1 compliant PDF documents directly on the Android device. Each page is encoded at up to 300 DPI print quality, ensuring sharp text reproduction on any commercial laser or inkjet printer.",
  },
];

export default function DocumentScannerPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "Document Scanner", path: "/features/document-scanner" },
  ];

  return (
    <div className="py-12 sm:py-16">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={FEATURE_SCANNER_FAQS} />

      {/* Header */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 text-xs font-semibold mb-3">
          <Camera className="w-3.5 h-3.5" />
          <span>Core Feature Architecture</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Mobile Document Camera Scanner
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          Capture physical paperwork, receipts, invoices, contracts, and study notes with hardware-accelerated computer vision. Zero bulky flatbed hardware required.
        </p>
      </div>

      {/* Interactive Interactive Scanner Component */}
      <ScannerDemo />

      {/* Detailed Technical Capabilities */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 space-y-12 text-sm sm:text-base text-slate-700 dark:text-slate-300">
        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Cpu className="w-6 h-6 text-indigo-600" />
            <span>On-Device Computer Vision Pipeline</span>
          </h2>
          <p className="leading-relaxed">
            Standard smartphone photo captures suffer from keystoning (trapezoidal distortion), uneven ambient shadows, and background surface clutter. Printora addresses these artifacts by executing an entirely local image processing pipeline:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-2">
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-indigo-50 dark:bg-indigo-950/80 text-indigo-600 flex items-center justify-center font-bold text-xs">
                01
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Real-Time Edge Detection</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Evaluates camera viewfinder frames to identify closed polygon contours, locking onto document borders instantly when paper is detected.
              </p>
            </div>
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-blue-50 dark:bg-blue-950/80 text-blue-600 flex items-center justify-center font-bold text-xs">
                02
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Perspective Rectification</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Applies a 4-point homography transform, converting tilted handheld shots into perfectly orthogonal flatbed scans.
              </p>
            </div>
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-emerald-600 flex items-center justify-center font-bold text-xs">
                03
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Adaptive Photocopy Filter</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Suppresses paper bleed-through, removes finger shadows, and normalizes background paper color to crisp printable white.
              </p>
            </div>
          </div>
        </section>

        {/* Feature Specifications Table */}
        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <FileCheck className="w-6 h-6 text-emerald-600" />
            <span>Document Scanner Technical Specifications</span>
          </h2>
          <div className="overflow-x-auto rounded-2xl border border-slate-200 dark:border-slate-800">
            <table className="w-full text-left text-xs text-slate-600 dark:text-slate-400">
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800 bg-white dark:bg-slate-900">
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white w-1/3">Maximum Pages per Document</td>
                  <td className="py-3 px-4">Up to 20 pages per unified session</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">Output Resolution</td>
                  <td className="py-3 px-4">Up to 300 DPI print quality (standard A4 / Letter)</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">Processing Engine</td>
                  <td className="py-3 px-4">100% on-device Android CameraX &amp; OpenCV-accelerated pipeline</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">Image Filters</td>
                  <td className="py-3 px-4">Magic B&amp;W Photocopy, Enhanced Color, Grayscale, Original Photo</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">Direct Print Integration</td>
                  <td className="py-3 px-4">One-tap wireless transmission to Windows print spooler</td>
                </tr>
                <tr>
                  <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">Privacy Guarantee</td>
                  <td className="py-3 px-4">Zero remote cloud transmission; temporary buffers purged on completion</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        {/* Author Verification */}
        <AuthorBadge
          reviewedBy="Printora Mobile Engineering Team"
          testedEnvironment="Android CameraX Pipeline • Verified on Android 14 & 13"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ */}
        <MiniFaq
          items={FEATURE_SCANNER_FAQS}
          title="Document Scanner Feature FAQ"
          description="Detailed answers on computer vision models, manual crop nudging, and batch PDF generation."
        />

        {/* CTA Footer */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row justify-between items-center gap-4">
          <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
            &larr; Return to Home
          </Link>
          <div className="flex items-center gap-3">
            <Link
              href="/mobile-document-scanner-to-pdf"
              className="px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              Step-by-Step Guide
            </Link>
            <Link
              href="/download/android"
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
            >
              <Camera className="w-3.5 h-3.5" />
              <span>Get Android Scanner App</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
