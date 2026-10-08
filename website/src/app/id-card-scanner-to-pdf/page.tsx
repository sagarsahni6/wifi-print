import { CreditCard, Sparkles } from "lucide-react";
import Link from "next/link";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd, HowToJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "ID Card Scanner to PDF – Dual-Sided Scan to Single A4 Page",
  description: "Scan the front and back of any identity card, driver license, or employee badge and combine both sides cleanly onto a single A4 PDF page without paper flipping.",
  path: "/id-card-scanner-to-pdf",
  keywords: [
    "id card scanner to pdf",
    "scan both sides of id card on one page",
    "driver license scan to a4",
    "dual sided card scanner app",
    "id card photocopy to printer",
  ],
});

const ID_CARD_FAQS = [
  {
    q: "Does the combined A4 PDF maintain the real physical dimensions of the ID card?",
    a: "Yes. Printora's layout engine maps standard credit card and government ID dimensions (CR80 standard: 85.60 × 53.98 mm) onto the output canvas with 1:1 physical aspect ratio locking. When printed at 100% scale on A4 or US Letter paper, the printed cards match the exact physical size of the original plastic cards.",
  },
  {
    q: "How does Printora eliminate the need to manually flip paper in office copiers?",
    a: "Traditional copiers require scanning side one, manually feeding the paper back into the tray upside down, and praying the second scan prints in the right alignment. Printora handles the merging in digital memory: your phone camera captures Side A and Side B, the app merges both into a single digital PDF, and sends that one-page document to the printer.",
  },
  {
    q: "Can I save or email the combined ID card PDF without printing it?",
    a: "Absolutely. Once the dual-sided A4 PDF is generated, you can save it directly to your phone's storage, send it via WhatsApp, attach it to an email for visa or bank applications, or route it wirelessly to your Windows printer.",
  },
  {
    q: "Is sensitive personally identifiable information (PII) saved or uploaded to the cloud?",
    a: "No. ID cards contain highly sensitive numbers, addresses, and photos. Printora executes all perspective correction, filtering, and A4 canvas layout entirely on your phone's local CPU. Zero card data is uploaded to remote cloud servers or analytics platforms.",
  },
];

const HOW_TO_STEPS = [
  {
    name: "Place card on flat surface and capture Side A (Front)",
    text: "Position your driver's license, passport card, or identity badge on a table. Align it within the rectangular viewfinder and capture the front.",
  },
  {
    name: "Flip card and capture Side B (Back)",
    text: "Turn the card over. The app automatically prompts for Side B, detects the outer edges, and corrects any perspective distortion.",
  },
  {
    name: "Review automated dual-side A4 layout",
    text: "Printora instantly composes both card faces onto a standard A4 canvas: Side A centered on top and Side B centered on the bottom.",
  },
  {
    name: "Select enhancement filter",
    text: "Choose Magic B&W Photocopy to produce ultra-clean legal copies or Enhanced Color to retain holographic seals and colored card artwork.",
  },
  {
    name: "Print or export to PDF",
    text: "Tap Print to output the completed A4 sheet directly to your Windows printer, or tap Share to export a standardized PDF file.",
  },
];

export default function IdCardScannerToPdfPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Solutions", path: "/#solutions" },
    { name: "ID Card Scanner to PDF", path: "/id-card-scanner-to-pdf" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={ID_CARD_FAQS} />
      <HowToJsonLd
        name="How to Scan Both Sides of an ID Card onto a Single A4 Page"
        description="Comprehensive tutorial on scanning the front and back of plastic ID cards, aligning them on standard A4 paper, and printing without manual copier gymnastics."
        totalTime="PT1M"
        steps={HOW_TO_STEPS}
        tools={["Printora Android App", "Smartphone Camera"]}
        supplies={["ID Card / Driver License", "Standard A4 / Letter Paper"]}
      />

      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-8">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
          <CreditCard className="w-3.5 h-3.5" />
          <span>ID Card Workflow Solution</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          ID Card Scanner to Printable A4 PDF
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Cleanly capture both sides of any identity card, driver license, or employee badge and combine them onto a single standard A4 page. Perfect for legal paperwork, banking applications, and human resources.
        </p>
      </div>

      <div className="mt-10 space-y-10 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        {/* Problem & Solution */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Why Traditional ID Card Copying is Broken</h2>
          <p>
            When applying for visas, opening bank accounts, renting apartments, or completing employment onboarding, organizations routinely mandate: <em>&quot;Please provide a single-page photocopy showing both the front and back of your government identification card.&quot;</em>
          </p>
          <p>
            Using a traditional desktop printer or photocopier requires an awkward trial-and-error dance: placing the card on the glass, scanning side one, manually rotating the paper, feeding it back into the tray, and hoping the second pass doesn&apos;t overlap or print upside down. One misalignment wastes toner and paper.
          </p>
        </section>

        {/* The Printora Solution */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-blue-600" />
            <span>The Printora Digital Composition Advantage</span>
          </h2>
          <p>
            Printora transforms this entire ordeal into a two-tap mobile workflow:
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Guided Camera Viewfinder</strong>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                App guides you through Side A (Front), automatically locks boundary corners, and immediately prompts for Side B (Back).
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Automated A4 Canvas Layout</strong>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Positions Side A centered on the top half and Side B centered on the bottom half with symmetrical margins and 1:1 true scale.
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">High-Contrast Photocopy Filter</strong>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Removes glossy card reflections, table shadows, and background texture while sharpening barcode lines and microtext.
              </p>
            </div>
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <strong className="text-slate-900 dark:text-white font-bold block text-sm">Instant Spooler Injection</strong>
              <p className="text-slate-600 dark:text-slate-400 leading-relaxed">
                Sends the compiled single-page PDF directly to your Windows print spooler over local Wi-Fi in under 3 seconds.
              </p>
            </div>
          </div>
        </section>

        {/* Step by Step Guide */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">Step-by-Step Instructions</h2>
          <ol className="list-decimal pl-5 space-y-3 text-slate-600 dark:text-slate-400 text-xs sm:text-sm">
            {HOW_TO_STEPS.map((step, idx) => (
              <li key={idx}>
                <strong className="text-slate-900 dark:text-white">{step.name}:</strong> {step.text}
              </li>
            ))}
          </ol>
        </section>

        {/* Author Verification */}
        <AuthorBadge
          reviewedBy="Printora Imaging &amp; Layout Team"
          testedEnvironment="CR80 Standard Cards (Driver License &amp; National ID) • A4 &amp; Letter Format"
          lastUpdated="October 2026"
        />

        {/* Mini FAQ */}
        <MiniFaq
          items={ID_CARD_FAQS}
          title="ID Card Scanner FAQ"
          description="Common technical questions about card dimensions, layout scaling, and privacy guarantees."
        />

        {/* CTA Footer */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row justify-between items-center gap-4">
          <Link href="/features/id-card-scanner" className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline inline-flex items-center gap-1">
            &larr; View ID Card Scanner Feature
          </Link>
          <Link
            href="/download/android"
            className="w-full sm:w-auto px-6 py-3 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs inline-flex items-center justify-center gap-2 shadow-md shadow-blue-500/20"
          >
            <CreditCard className="w-4 h-4" />
            <span>Get ID Card Scanner App</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
