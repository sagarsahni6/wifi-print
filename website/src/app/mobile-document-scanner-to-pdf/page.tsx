import { Camera, Smartphone, Layers, Sparkles, Sliders } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd, HowToJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "Mobile Document Scanner to PDF – Multi-Page Phone Scanning",
  description: "Transform your smartphone into a high-precision document scanner with automatic edge detection, perspective warping, contrast filters, and direct 300 DPI PDF creation.",
  path: "/mobile-document-scanner-to-pdf",
  keywords: [
    "mobile document scanner to pdf",
    "phone scan to pdf",
    "multi page document scanner",
    "camera scanner perspective correction",
    "free pdf document scanner app",
  ],
});

const SCANNER_FAQS = [
  {
    q: "How does the scanner handle curved, tilted, or wrinkled paper?",
    a: "Printora's scanning engine executes on-device computer vision to locate the four outer corners of the paper. It then applies a 4-point perspective warp transform that mathematically flattens the surface into an undistorted, rectangular A4 or Letter page as if scanned on a flatbed scanner.",
  },
  {
    q: "What resolution and file size are generated for multi-page PDFs?",
    a: "Scanned pages are processed at up to 300 DPI print quality. Built-in smart compression (JPEG for color images and CCITT Group 4 / ZIP for black-and-white documents) ensures multi-page PDFs remain compact (typically 400KB to 1.5MB for 10 pages) without sacrificing crisp text readability.",
  },
  {
    q: "Are my camera photos or scanned documents uploaded to the cloud?",
    a: "Never. All edge detection algorithms, perspective corrections, image filter enhancements, and PDF compilation run entirely on your phone's local processor. No image data is transmitted to external cloud servers.",
  },
  {
    q: "Can I extract text or directly print the scanned document?",
    a: "Yes. In one tap, you can pass the scanned pages into Printora's on-device OCR engine to copy selectable text, or send the compiled PDF directly to your Windows print spooler over local Wi-Fi without saving it to temporary storage first.",
  },
];

const HOW_TO_STEPS = [
  {
    name: "Place the document on a contrasting surface",
    text: "Position your paper contract, receipt, or invoice on a solid desk or surface with good lighting and clear edge contrast.",
  },
  {
    name: "Capture with automatic quad-corner detection",
    text: "Open the Printora scanner. The camera viewfinder automatically highlights the four corners of the page in real time and snaps the photo.",
  },
  {
    name: "Apply enhancement filters",
    text: "Choose between Original Color, Magic Auto-Enhance, or Black & White High Contrast to eliminate shadows and background desk grain.",
  },
  {
    name: "Add additional pages or export to PDF",
    text: "Tap 'Add Page' to scan up to 20 sheets in a single document session, reorder pages if needed, and export directly as a standardized PDF.",
  },
  {
    name: "Print or share immediately",
    text: "Tap Print to route the PDF directly to your Windows printer over Wi-Fi, or share the PDF via email, WhatsApp, or cloud drive.",
  },
];

export default function MobileDocumentScannerPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Solutions", path: "/#solutions" },
    { name: "Mobile Document Scanner to PDF", path: "/mobile-document-scanner-to-pdf" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={SCANNER_FAQS} />
      <HowToJsonLd
        name="How to Scan Documents to PDF with Your Phone"
        description="Step-by-step instructions for capturing multi-page paper documents, correcting perspective distortion, and generating print-ready PDFs."
        totalTime="PT2M"
        steps={HOW_TO_STEPS}
        tools={["Printora Android App", "Smartphone Camera"]}
        supplies={["Paper Document", "Well-lit Flat Surface"]}
      />

      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-8">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 text-xs font-semibold mb-3">
          <Camera className="w-3.5 h-3.5" />
          <span>Mobile PDF Scanner</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Mobile Document Scanner to PDF
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Capture paper records, contracts, receipts, and classroom whiteboards directly into clean, standardized multi-page PDF documents.
        </p>
      </div>

      <div className="mt-10 space-y-10 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        {/* Core Technology Deep Dive */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-indigo-600" />
            <span>Built-in Quad Edge Detection &amp; Perspective Correction</span>
          </h2>
          <p>
            You do not have to hold your smartphone perfectly parallel to the table. Printora employs real-time edge detection algorithms to analyze luminance differentials and identify the precise boundaries of paper documents against any tabletop, desk, or countertop.
          </p>
          <p>
            Once boundaries are identified, a hardware-accelerated homography matrix performs geometric rectification. Trapeze-shaped photos captured from steep angles are mathematically warped into perfectly rectangular, flat pages, eliminating keystoning and lens distortion.
          </p>
        </section>

        {/* Enhancement Filters */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Sliders className="w-5 h-5 text-blue-600" />
            <span>Adaptive Image Enhancement Filters</span>
          </h2>
          <p>
            Ambient indoor lighting frequently casts phone shadows or yellow tints across paper. Printora incorporates four specialized post-processing filters tailored for physical documents:
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Magic B&amp;W Photocopy</strong>
              <p className="text-slate-600 dark:text-slate-400">
                Binarizes text and removes uneven desk shadows, creating clean, high-contrast black text on pure white backgrounds optimized for laser printers.
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Enhanced Color Document</strong>
              <p className="text-slate-600 dark:text-slate-400">
                Boosts saturation and sharpness on stamps, signatures, colored logos, and diagrams while leveling background brightness.
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Grayscale Preservation</strong>
              <p className="text-slate-600 dark:text-slate-400">
                Retains subtle pencil sketches, shaded tables, and grayscale artwork without harsh threshold clipping.
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-1.5">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Original Color Tone</strong>
              <p className="text-slate-600 dark:text-slate-400">
                Retains natural photography color balances without automated contrast adjustments.
              </p>
            </div>
          </div>
        </section>

        {/* Multi-Page & Batch Sessions */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Layers className="w-5 h-5 text-emerald-600" />
            <span>Multi-Page Batch Sessions (Up to 20 Pages)</span>
          </h2>
          <p>
            Unlike basic camera apps that create dozens of loose image files in your photo gallery, Printora manages a unified document session. You can capture multiple successive sheets—such as a 12-page legal lease or an 8-page homework assignment—in seconds.
          </p>
          <p>
            The session manager lets you reorder pages with drag-and-drop, delete accidental duplicates, rotate individual orientations, and combine all pages into a single, standardized 300 DPI PDF ready for printing or cloud archiving.
          </p>
        </section>

        {/* Step by Step Guide */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Step-by-Step Scanning Walkthrough</h2>
          <ol className="list-decimal pl-5 space-y-3 text-slate-600 dark:text-slate-400 text-xs sm:text-sm">
            {HOW_TO_STEPS.map((step, idx) => (
              <li key={idx}>
                <strong className="text-slate-900 dark:text-white">{step.name}:</strong> {step.text}
              </li>
            ))}
          </ol>
        </section>

        {/* Security & Verification */}
        <AuthorBadge
          reviewedBy="Printora Computer Vision Team"
          testedEnvironment="Android 14 (CameraX API) • On-Device Neural Processing"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ Accordion */}
        <MiniFaq
          items={SCANNER_FAQS}
          title="Document Scanner FAQ"
          description="Common questions about perspective correction, compression, and image privacy."
        />

        {/* Navigation & CTA */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row justify-between items-center gap-4">
          <Link href="/features/document-scanner" className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline inline-flex items-center gap-1">
            &larr; View Interactive Scanner Demo
          </Link>
          <Link
            href="/download/android"
            className="w-full sm:w-auto px-6 py-3 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs inline-flex items-center justify-center gap-2 shadow-md shadow-emerald-500/20"
          >
            <Smartphone className="w-4 h-4" />
            <span>Download Android Scanner App</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
