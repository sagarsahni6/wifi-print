"use client";

import { useState } from "react";
import { 
  Play, 
  Pause, 
  ArrowUp, 
  Trash2, 
  ShieldCheck, 
  Copy, 
  Check, 
  Search, 
  CheckCircle2, 
  Printer, 
  Lock, 
  FileCheck, 
  Layers,
  Sparkles
} from "lucide-react";

interface QueueJob {
  id: string;
  name: string;
  pages: number;
  status: "Printing" | "Queued" | "Paused";
  priority: "High" | "Normal";
}

const PRINTER_MODELS = [
  { name: "HP LaserJet Pro MFP 4101", brand: "HP", type: "Laser MFP", status: "Supported" },
  { name: "Brother HL-L2350DW Compact", brand: "Brother", type: "Monochrome", status: "Supported" },
  { name: "Epson EcoTank ET-2850", brand: "Epson", type: "Ink Tank", status: "Supported" },
  { name: "Canon imageCLASS MF445dw", brand: "Canon", type: "Laser MFP", status: "Supported" },
  { name: "Zebra ZD421 Direct Thermal", brand: "Zebra", type: "Barcode Label", status: "Supported" },
  { name: "Kyocera ECOSYS M2540dw", brand: "Kyocera", type: "Heavy Duty", status: "Supported" },
  { name: "Ricoh IM C2000 Color MFP", brand: "Ricoh", type: "Workgroup", status: "Supported" },
  { name: "Generic Windows USB Printer", brand: "All", type: "Any USB", status: "Supported" },
] as const;

export function ModernBentoGrid() {
  // Card 1 State: Queue controller
  const [queueJobs, setQueueJobs] = useState<QueueJob[]>([
    { id: "job-1", name: "Contract_Signed_2026.pdf", pages: 4, status: "Printing", priority: "High" },
    { id: "job-2", name: "Architectural_Plan_A3.pdf", pages: 12, status: "Queued", priority: "Normal" },
    { id: "job-3", name: "Tax_Receipt_April.png", pages: 1, status: "Queued", priority: "Normal" },
  ]);
  const [isSpoolerPaused, setIsSpoolerPaused] = useState(false);

  // Card 3 State: OCR text copy
  const [copiedOcr, setCopiedOcr] = useState(false);
  const sampleOcrText = "INVOICE #94021\nTOTAL: $142.50\nDATE: OCT 08, 2026\nSTATUS: PAID VIA ACH\nVENDOR: PRINTORA SYSTEMS";

  const handleCopyOcr = () => {
    navigator.clipboard?.writeText(sampleOcrText);
    setCopiedOcr(true);
    setTimeout(() => setCopiedOcr(false), 2000);
  };

  // Card 4 State: Printer search
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedBrand, setSelectedBrand] = useState("All");

  const query = searchQuery.toLowerCase();
  const filteredPrinters = PRINTER_MODELS.filter((p) => {
    const matchesBrand = selectedBrand === "All" || p.brand === selectedBrand;
    return matchesBrand && (
      p.name.toLowerCase().includes(query) || 
      p.brand.toLowerCase().includes(query) ||
      p.type.toLowerCase().includes(query)
    );
  });

  const boostPriority = (id: string) => {
    setQueueJobs((prev) =>
      prev.map((job) =>
        job.id === id ? { ...job, priority: "High" } : job
      )
    );
  };

  const removeJob = (id: string) => {
    setQueueJobs((prev) => prev.filter((job) => job.id !== id));
  };

  return (
    <section id="features" className="py-20 sm:py-28 bg-slate-50/60 dark:bg-slate-950 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Powerhouse Capabilities</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Engineered for Precision & Complete Privacy
          </h2>
          <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
            Everything your mobile phone needs to command Windows print spoolers with zero latency.
          </p>
        </div>

        {/* 4-Card Asymmetrical Bento Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

          {/* BENTO CARD 1: Interactive Print Queue Controller (7 Cols) */}
          <div className="lg:col-span-7 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-sm flex flex-col justify-between hover:border-blue-500/40 transition-all">
            <div>
              <div className="flex items-center justify-between gap-4 mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/70 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
                    <Layers className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                      Live Windows Spooler Queue
                    </h3>
                    <p className="text-xs text-slate-500">
                      Direct Win32 GDI job control • Pause, reprioritize, or cancel on the fly
                    </p>
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => setIsSpoolerPaused(!isSpoolerPaused)}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                    isSpoolerPaused
                      ? "bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300"
                      : "bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200"
                  }`}
                >
                  {isSpoolerPaused ? <Play className="w-3.5 h-3.5 fill-current" /> : <Pause className="w-3.5 h-3.5 fill-current" />}
                  <span>{isSpoolerPaused ? "Resume Spooler" : "Pause Spooler"}</span>
                </button>
              </div>

              {/* Interactive Jobs Table */}
              <div className="space-y-2.5 mt-6">
                {queueJobs.map((job) => (
                  <div
                    key={job.id}
                    className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-950/80 border border-slate-200/70 dark:border-slate-800/80 flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className={`w-2 h-2 rounded-full shrink-0 ${
                        job.status === "Printing" && !isSpoolerPaused
                          ? "bg-emerald-500 animate-pulse"
                          : isSpoolerPaused
                          ? "bg-amber-400"
                          : "bg-blue-400"
                      }`} />
                      <div className="min-w-0">
                        <div className="font-semibold text-slate-900 dark:text-white truncate">
                          {job.name}
                        </div>
                        <div className="text-[11px] text-slate-500 font-mono">
                          {job.pages} pages • {isSpoolerPaused ? "Paused by Admin" : job.status}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0">
                      {job.priority === "High" ? (
                        <span className="px-2 py-0.5 rounded-md bg-amber-100 dark:bg-amber-950 text-amber-700 dark:text-amber-300 font-bold text-[10px]">
                          TOP PRIORITY
                        </span>
                      ) : (
                        <button
                          type="button"
                          onClick={() => boostPriority(job.id)}
                          title="Boost job to front of queue"
                          className="p-1.5 rounded-lg text-slate-500 hover:text-blue-600 hover:bg-white dark:hover:bg-slate-800 transition-colors cursor-pointer"
                        >
                          <ArrowUp className="w-3.5 h-3.5" />
                        </button>
                      )}

                      <button
                        type="button"
                        onClick={() => removeJob(job.id)}
                        title="Cancel print job"
                        className="p-1.5 rounded-lg text-slate-400 hover:text-red-500 hover:bg-white dark:hover:bg-slate-800 transition-colors cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500 font-mono">
              <span>Windows Spooler Service: HEALTHY</span>
              <span>Memory Footprint: 38.4 MB</span>
            </div>
          </div>

          {/* BENTO CARD 2: Zero-Cloud Privacy Vault (5 Cols) */}
          <div className="lg:col-span-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-sm flex flex-col justify-between hover:border-emerald-500/40 transition-all">
            <div>
              <div className="w-10 h-10 rounded-xl bg-emerald-50 dark:bg-emerald-950/70 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                Zero-Cloud Privacy Vault
              </h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 leading-relaxed">
                Your medical files, contracts, and banking sheets never pass through a cloud server.
              </p>

              {/* Privacy Architecture Flow Indicator */}
              <div className="mt-6 space-y-3">
                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200/70 dark:border-slate-800 flex items-center gap-3">
                  <Lock className="w-4 h-4 text-emerald-500 shrink-0" />
                  <div className="text-xs">
                    <span className="font-bold text-slate-900 dark:text-white">In-Memory Spooling:</span>{" "}
                    <span className="text-slate-500">Payload streamed straight into RAM buffers.</span>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200/70 dark:border-slate-800 flex items-center gap-3">
                  <Trash2 className="w-4 h-4 text-emerald-500 shrink-0" />
                  <div className="text-xs">
                    <span className="font-bold text-slate-900 dark:text-white">Auto-Shred Temp Files:</span>{" "}
                    <span className="text-slate-500">Zero persistent copies left on host disk.</span>
                  </div>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200/70 dark:border-slate-800 flex items-center gap-3">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                  <div className="text-xs">
                    <span className="font-bold text-slate-900 dark:text-white">Zero Cloud Telemetry:</span>{" "}
                    <span className="text-slate-500">No account required, no file analytics logged.</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 text-[11px] font-mono text-emerald-600 dark:text-emerald-400 font-semibold">
              ✓ Compliant with strict enterprise airgap privacy
            </div>
          </div>

          {/* BENTO CARD 3: On-Device OCR Scanner (5 Cols) */}
          <div className="lg:col-span-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-sm flex flex-col justify-between hover:border-indigo-500/40 transition-all">
            <div>
              <div className="w-10 h-10 rounded-xl bg-indigo-50 dark:bg-indigo-950/70 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0 mb-4">
                <FileCheck className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                On-Device OCR & Text Extraction
              </h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 mt-1 leading-relaxed">
                Scan receipts, bills, and contracts. Convert scanned images to selectable text locally.
              </p>

              {/* Interactive OCR Demo Box */}
              <div className="mt-6 p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-950 border border-slate-200/80 dark:border-slate-800 relative">
                <div className="flex items-center justify-between pb-2 border-b border-slate-200/60 dark:border-slate-800/80 mb-2">
                  <span className="text-[10px] font-mono font-bold text-indigo-600 dark:text-indigo-400 uppercase">
                    Extracted Text (Tesseract Engine)
                  </span>
                  <button
                    type="button"
                    onClick={handleCopyOcr}
                    className="inline-flex items-center gap-1 text-[11px] font-bold text-slate-700 dark:text-slate-300 hover:text-indigo-600 transition-colors cursor-pointer"
                  >
                    {copiedOcr ? <Check className="w-3.5 h-3.5 text-emerald-500" /> : <Copy className="w-3.5 h-3.5" />}
                    <span>{copiedOcr ? "Copied!" : "Copy"}</span>
                  </button>
                </div>
                <pre className="font-mono text-xs text-slate-700 dark:text-slate-300 whitespace-pre-wrap leading-relaxed">
                  {sampleOcrText}
                </pre>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 text-[11px] font-mono text-slate-500">
              Confidence Score: 99.4% • 100% Offline Processing
            </div>
          </div>

          {/* BENTO CARD 4: Interactive Hardware Compatibility Search (7 Cols) */}
          <div className="lg:col-span-7 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/90 dark:border-slate-800 p-6 sm:p-8 shadow-sm flex flex-col justify-between hover:border-blue-500/40 transition-all">
            <div>
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/70 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
                    <Printer className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                      Hardware Compatibility Explorer
                    </h3>
                    <p className="text-xs text-slate-500">
                      Search 4,000+ printers supported through the native Windows Spooler
                    </p>
                  </div>
                </div>
              </div>

              {/* Search Bar & Brand Chips */}
              <div className="mt-4 space-y-3">
                <div className="relative">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search brand or model (e.g., HP LaserJet, EcoTank, Zebra)..."
                    className="w-full pl-10 pr-4 py-2 text-xs rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200 dark:border-slate-800 text-slate-900 dark:text-white focus:outline-hidden focus:border-blue-600"
                  />
                </div>

                <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
                  {["All", "HP", "Brother", "Canon", "Epson", "Zebra", "Kyocera"].map((brand) => (
                    <button
                      key={brand}
                      type="button"
                      onClick={() => setSelectedBrand(brand)}
                      className={`px-2.5 py-1 rounded-lg text-xs font-semibold cursor-pointer transition-all shrink-0 ${
                        selectedBrand === brand
                          ? "bg-blue-600 text-white"
                          : "bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white"
                      }`}
                    >
                      {brand}
                    </button>
                  ))}
                </div>

                {/* Filtered Printer Results List */}
                <div className="max-h-[160px] overflow-y-auto space-y-1.5 pr-1">
                  {filteredPrinters.length > 0 ? (
                    filteredPrinters.map((item, idx) => (
                      <div
                        key={`${item.name}-${idx}`}
                        className="px-3 py-2 rounded-xl bg-slate-50 dark:bg-slate-950 border border-slate-200/60 dark:border-slate-800/80 flex items-center justify-between text-xs"
                      >
                        <div className="flex items-center gap-2">
                          <Printer className="w-3.5 h-3.5 text-slate-400" />
                          <span className="font-semibold text-slate-900 dark:text-white">{item.name}</span>
                          <span className="text-[10px] text-slate-500 font-mono">({item.type})</span>
                        </div>
                        <span className="text-[10px] font-bold text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                          <CheckCircle2 className="w-3 h-3" /> Fully Compatible
                        </span>
                      </div>
                    ))
                  ) : (
                    <div className="p-4 text-center text-xs text-slate-500">
                      No specific model matched, but if Windows prints to it, Printora prints to it!
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between text-[11px] text-slate-500">
              <span>Supports USB, Wi-Fi, Ethernet, Bluetooth & Network Shares</span>
              <span className="font-mono text-blue-600">Rule: If Windows can print, Printora can print.</span>
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}
