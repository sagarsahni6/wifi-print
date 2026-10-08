import { FileSearch, CheckCircle2, Cpu, Zap } from "lucide-react";
import Link from "next/link";
import { OcrSection } from "@/components/OcrSection";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "On-Device OCR Text Extraction Feature",
  description: "Extract selectable, editable text directly from paper documents, invoices, receipts, and photos with Printora on-device optical character recognition. 100% offline and private.",
  path: "/features/ocr",
  keywords: [
    "on device ocr text extraction",
    "offline optical character recognition",
    "extract text from receipt android",
    "scan paper to editable text",
    "privacy first ocr mobile app",
  ],
});

const OCR_FAQS = [
  {
    q: "Does OCR text recognition require an active internet connection or API key?",
    a: "No. Printora runs neural text recognition entirely on your smartphone's local neural engine using ML Kit on-device models. It operates with zero internet access, zero cloud API quotas, and zero per-page transcription fees.",
  },
  {
    q: "How accurate is the OCR engine on crumpled receipts or low-contrast paper?",
    a: "When paired with Printora's automated perspective correction and Magic B&W binarization filter, OCR achieves over 99% character accuracy on printed Latin text down to 6pt font. For thermal receipts with faint print, toggling the contrast enhancer dramatically improves recognition.",
  },
  {
    q: "Can I copy individual lines or must I export the entire scanned page?",
    a: "Printora features an interactive text block viewer. You can tap on individual words, highlight specific paragraphs, or tap 'Copy All' to place the entire transcribed text onto your clipboard for pasting into Word, Slack, or email.",
  },
  {
    q: "Are scanned financial records, passwords, or personal documents confidential?",
    a: "Completely. Because the OCR neural network processes pixel tensors strictly in volatile device RAM without transmitting data to remote servers, your sensitive corporate contracts, bank statements, and tax forms never leave your hardware.",
  },
];

export default function OcrPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Features", path: "/#features" },
    { name: "OCR Text Extraction", path: "/features/ocr" },
  ];

  return (
    <div className="py-12 sm:py-16">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={OCR_FAQS} />

      {/* Header */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mb-12">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-100 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 text-xs font-semibold mb-3">
          <FileSearch className="w-3.5 h-3.5" />
          <span>Deep-Dive Feature</span>
        </div>
        <h1 className="text-3xl sm:text-5xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Optical Character Recognition (OCR)
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          Transform physical paper records, invoices, receipts, and whiteboard diagrams into digital, selectable text. 100% on-device execution with zero cloud transcription costs.
        </p>
      </div>

      {/* Interactive OCR Demo Component */}
      <OcrSection />

      {/* Technical Deep Dive */}
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 mt-16 space-y-12 text-sm sm:text-base text-slate-700 dark:text-slate-300">
        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Cpu className="w-6 h-6 text-indigo-600" />
            <span>How Printora Executes On-Device Text Recognition</span>
          </h2>
          <p className="leading-relaxed">
            Most mobile scanning apps quietly route your photos to cloud servers running proprietary OCR backends, introducing privacy risks, latency delays, and subscription walls. Printora takes an air-gapped, local-first engineering approach:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-2">
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-indigo-50 dark:bg-indigo-950/80 text-indigo-600 flex items-center justify-center font-bold text-xs">
                01
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Local Neural Processing</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Utilizes hardware-accelerated on-device neural models to detect character strokes, words, and text lines in under 350 milliseconds.
              </p>
            </div>
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-blue-50 dark:bg-blue-950/80 text-blue-600 flex items-center justify-center font-bold text-xs">
                02
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Spatial Layout Analysis</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Analyzes hierarchical bounding boxes to reconstruct columns, paragraphs, and invoice line items in natural reading order.
              </p>
            </div>
            <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <div className="w-8 h-8 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-emerald-600 flex items-center justify-center font-bold text-xs">
                03
              </div>
              <h3 className="font-bold text-slate-900 dark:text-white text-sm">Instant Clipboard Integration</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">
                Outputs clean Unicode text with one-tap clipboard copy, direct TXT export, or inclusion into formatted digital documents.
              </p>
            </div>
          </div>
        </section>

        {/* Best Practices Checklist */}
        <section className="space-y-4">
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Zap className="w-6 h-6 text-amber-500" />
            <span>Best Practices for Maximum OCR Accuracy</span>
          </h2>
          <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-3 text-xs sm:text-sm">
            <div className="flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <div>
                <strong className="text-slate-900 dark:text-white">Avoid Specular Glare:</strong> Angle your camera slightly to prevent direct light bulb or flash reflections on glossy paper, magazines, or laminated cards.
              </div>
            </div>
            <div className="flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <div>
                <strong className="text-slate-900 dark:text-white">Enable Magic B&amp;W Filter:</strong> For thermal receipts or faded dot-matrix printouts, applying Printora&apos;s binarization filter cleans up background speckles.
              </div>
            </div>
            <div className="flex items-start gap-3">
              <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
              <div>
                <strong className="text-slate-900 dark:text-white">Ensure Sharp Focus:</strong> Tap your phone screen to lock optical focus before capturing fine print (such as serial numbers or legal terms).
              </div>
            </div>
          </div>
        </section>

        {/* Author Verification */}
        <AuthorBadge
          reviewedBy="Printora Machine Learning Team"
          testedEnvironment="On-Device Neural Engine • Tested with Latin &amp; Numeric Scripts"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ */}
        <MiniFaq
          items={OCR_FAQS}
          title="OCR Text Recognition FAQ"
          description="Technical insights on on-device ML models, character error rates, and data privacy."
        />

        {/* CTA Footer */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row justify-between items-center gap-4">
          <Link href="/" className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline">
            &larr; Return to Home
          </Link>
          <div className="flex items-center gap-3">
            <Link
              href="/features/document-scanner"
              className="px-4 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 text-xs font-semibold hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              Document Scanner
            </Link>
            <Link
              href="/download/android"
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm inline-flex items-center gap-2"
            >
              <FileSearch className="w-3.5 h-3.5" />
              <span>Get OCR Scanning App</span>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
