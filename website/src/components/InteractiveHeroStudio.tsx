"use client";

import { useState } from "react";
import { 
  Wifi, 
  Scan, 
  CreditCard, 
  QrCode, 
  Globe, 
  Smartphone, 
  Monitor, 
  Printer, 
  FileText, 
  Lock, 
  RefreshCw,
  Sparkles,
  ShieldCheck
} from "lucide-react";

const TABS = [
  { id: "wifi", label: "Wi-Fi Fast Path", icon: Wifi },
  { id: "scanner", label: "Smart Scanner", icon: Scan },
  { id: "idcard", label: "ID 2-in-1", icon: CreditCard },
  { id: "webprint", label: "Safari Web Print", icon: QrCode },
  { id: "tunnel", label: "Encrypted Tunnel", icon: Globe },
] as const;

type StudioTab = (typeof TABS)[number]["id"];

export function InteractiveHeroStudio() {
  const [activeTab, setActiveTab] = useState<StudioTab>("wifi");
  const [printProgress, setPrintProgress] = useState<number>(100);
  const [isSimulating, setIsSimulating] = useState<boolean>(false);
  const [scannerFilter, setScannerFilter] = useState<"magic" | "bw" | "color">("magic");
  const [cardSide, setCardSide] = useState<"front" | "back">("front");

  // Trigger simulated print cycle
  const runPrintSimulation = () => {
    setIsSimulating(true);
    setPrintProgress(0);
    const interval = setInterval(() => {
      setPrintProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setIsSimulating(false);
          return 100;
        }
        return prev + 20;
      });
    }, 250);
  };

  return (
    <div className="w-full max-w-5xl mx-auto rounded-3xl glass-panel p-3 sm:p-5 shadow-2xl shadow-blue-950/10 dark:shadow-blue-950/40 border border-slate-200/90 dark:border-slate-800/80 transition-all duration-300">
      {/* Studio Header & Tab Switcher */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pb-4 border-b border-slate-200/70 dark:border-slate-800">
        <div className="flex items-center gap-2.5 px-2">
          <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-xs font-mono font-semibold tracking-wider uppercase text-slate-500 dark:text-slate-400">
            Interactive Product Studio
          </span>
          <span className="text-[11px] px-2 py-0.5 rounded-md bg-blue-100 dark:bg-blue-950/80 text-blue-700 dark:text-blue-300 font-medium">
            Live Preview
          </span>
        </div>

        {/* Tab Buttons */}
        <div className="flex items-center gap-1 overflow-x-auto p-1 bg-slate-100/90 dark:bg-slate-900/90 rounded-2xl border border-slate-200/60 dark:border-slate-800/70 scrollbar-none">
          {TABS.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              type="button"
              onClick={() => setActiveTab(id)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer whitespace-nowrap ${
                activeTab === id
                  ? "bg-white dark:bg-slate-800 text-blue-600 dark:text-blue-400 shadow-sm"
                  : "text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white"
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Studio Display Body */}
      <div className="mt-4 p-4 sm:p-6 rounded-2xl bg-slate-50/70 dark:bg-slate-950/60 border border-slate-200/60 dark:border-slate-800/60 min-h-[360px] flex flex-col justify-center">
        
        {/* TAB 1: LOCAL WI-FI FAST PATH */}
        {activeTab === "wifi" && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-mono font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">
                  Zero-Latency Direct Spooler
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-0.5">
                  Local Network Auto-Discovery (mDNS)
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1">
                  Mobile sends raw print jobs straight to the Windows spooler over Wi-Fi. No data leaves your room.
                </p>
              </div>

              <button
                type="button"
                onClick={runPrintSimulation}
                disabled={isSimulating}
                className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 active:scale-95 transition-all shadow-md shadow-blue-500/20 disabled:opacity-60 cursor-pointer shrink-0"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${isSimulating ? "animate-spin" : ""}`} />
                <span>{isSimulating ? "Spooling Job..." : "Simulate Print Job"}</span>
              </button>
            </div>

            {/* Visual Node Diagram */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
              {/* Phone Node */}
              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm relative overflow-hidden">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-blue-50 dark:bg-blue-950/80 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
                    <Smartphone className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-slate-900 dark:text-white">Android / iOS</div>
                    <div className="text-[11px] text-slate-500 font-mono">192.168.1.42</div>
                  </div>
                </div>
                <div className="mt-3 text-xs bg-slate-50 dark:bg-slate-950 p-2 rounded-lg font-mono text-slate-600 dark:text-slate-400">
                  📄 Q3_Financials.pdf (2.4 MB)
                </div>
              </div>

              {/* Wire & Progress Node */}
              <div className="flex flex-col items-center justify-center p-2 text-center">
                <div className="w-full flex items-center justify-center gap-2 mb-2">
                  <span className="text-[11px] font-mono text-emerald-600 dark:text-emerald-400 font-semibold flex items-center gap-1">
                    <Wifi className="w-3.5 h-3.5" /> 5 GHz Wi-Fi • 4ms
                  </span>
                </div>
                {/* Visual Pipeline Bar */}
                <div className="w-full bg-slate-200 dark:bg-slate-800 h-2 rounded-full overflow-hidden relative">
                  <div 
                    className="bg-gradient-to-r from-blue-500 to-indigo-500 h-full transition-all duration-300 rounded-full"
                    style={{ width: `${printProgress}%` }}
                  />
                </div>
                <div className="mt-2 text-[11px] font-mono text-slate-500">
                  {printProgress === 100 ? "Ready in spooler (100%)" : `Transmitting... ${printProgress}%`}
                </div>
              </div>

              {/* Windows Host Node */}
              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm relative overflow-hidden">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-indigo-50 dark:bg-indigo-950/80 text-indigo-600 dark:text-indigo-400 flex items-center justify-center shrink-0">
                    <Monitor className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-xs font-bold text-slate-900 dark:text-white">Windows 11 Host</div>
                    <div className="text-[11px] text-emerald-600 dark:text-emerald-400 font-mono font-medium flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" /> Printora Server
                    </div>
                  </div>
                </div>
                <div className="mt-3 flex items-center justify-between text-xs bg-slate-50 dark:bg-slate-950 p-2 rounded-lg">
                  <span className="text-slate-600 dark:text-slate-400 flex items-center gap-1 font-mono">
                    <Printer className="w-3.5 h-3.5" /> HP LaserJet Pro
                  </span>
                  <span className="text-emerald-600 font-semibold text-[11px]">Online</span>
                </div>
              </div>
            </div>

            {/* Performance Metric Footer */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-2">
              <div className="p-3 rounded-xl bg-white/80 dark:bg-slate-900/80 border border-slate-200/80 dark:border-slate-800">
                <div className="text-[11px] text-slate-500 font-medium">Protocol</div>
                <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">Local mDNS + HTTP/2</div>
              </div>
              <div className="p-3 rounded-xl bg-white/80 dark:bg-slate-900/80 border border-slate-200/80 dark:border-slate-800">
                <div className="text-[11px] text-slate-500 font-medium">Print Latency</div>
                <div className="text-sm font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">&lt; 1.2 seconds</div>
              </div>
              <div className="p-3 rounded-xl bg-white/80 dark:bg-slate-900/80 border border-slate-200/80 dark:border-slate-800">
                <div className="text-[11px] text-slate-500 font-medium">Telemetry</div>
                <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">0 bytes sent out</div>
              </div>
              <div className="p-3 rounded-xl bg-white/80 dark:bg-slate-900/80 border border-slate-200/80 dark:border-slate-800">
                <div className="text-[11px] text-slate-500 font-medium">Spool Driver</div>
                <div className="text-sm font-bold text-blue-600 dark:text-blue-400 mt-0.5">Native Win32 GDI/XPS</div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: SMART SCANNER */}
        {activeTab === "scanner" && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-mono font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">
                  Edge Detection & Perspective Warp
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-0.5">
                  Mobile Document Scanner & Magic Filter
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1">
                  Replaces flatbed scanners with high-resolution quad-corner homography on your smartphone camera.
                </p>
              </div>

              {/* Filter controls */}
              <div className="flex items-center gap-1.5 p-1 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
                <button
                  type="button"
                  onClick={() => setScannerFilter("magic")}
                  className={`px-3 py-1 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                    scannerFilter === "magic"
                      ? "bg-blue-600 text-white shadow-xs"
                      : "text-slate-600 dark:text-slate-400"
                  }`}
                >
                  Magic Color
                </button>
                <button
                  type="button"
                  onClick={() => setScannerFilter("bw")}
                  className={`px-3 py-1 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                    scannerFilter === "bw"
                      ? "bg-blue-600 text-white shadow-xs"
                      : "text-slate-600 dark:text-slate-400"
                  }`}
                >
                  Sharp B&W
                </button>
                <button
                  type="button"
                  onClick={() => setScannerFilter("color")}
                  className={`px-3 py-1 rounded-lg text-xs font-semibold cursor-pointer transition-all ${
                    scannerFilter === "color"
                      ? "bg-blue-600 text-white shadow-xs"
                      : "text-slate-600 dark:text-slate-400"
                  }`}
                >
                  True Photo
                </button>
              </div>
            </div>

            {/* Viewfinder simulation */}
            <div className="relative rounded-2xl bg-slate-900 border border-slate-800 overflow-hidden p-6 text-white min-h-[200px] flex items-center justify-center">
              {/* Corner boundary marks */}
              <div className="absolute top-4 left-4 w-6 h-6 border-t-2 border-l-2 border-emerald-400" />
              <div className="absolute top-4 right-4 w-6 h-6 border-t-2 border-r-2 border-emerald-400" />
              <div className="absolute bottom-4 left-4 w-6 h-4 border-b-2 border-l-2 border-emerald-400" />
              <div className="absolute bottom-4 right-4 w-6 h-4 border-b-2 border-r-2 border-emerald-400" />

              <div className="relative max-w-sm w-full p-4 rounded-xl bg-white/10 backdrop-blur-md border border-white/20 text-center">
                <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 text-[11px] font-mono font-medium mb-3">
                  <Sparkles className="w-3 h-3" /> Auto Edge Lock: 4 Corners Detected
                </div>
                <div className="text-sm font-semibold text-white">Invoice_Contract_2026.pdf</div>
                <div className="text-xs text-slate-300 mt-1">
                  Filter Active: <span className="font-bold text-emerald-400 capitalize">{scannerFilter}</span> • 300 DPI Flat Vector
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1">
              <span>Quad Homography Matrix: 99.8% precision</span>
              <span>On-device Tesseract OCR ready</span>
            </div>
          </div>
        )}

        {/* TAB 3: ID CARD 2-IN-1 */}
        {activeTab === "idcard" && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-mono font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">
                  Automated Two-Side Layout
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-0.5">
                  ID Card & License 2-in-1 Composite
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1">
                  Snap front and back in seconds. Printora automatically centers both sides onto a single standard A4 sheet.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setCardSide(cardSide === "front" ? "back" : "front")}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-800 dark:text-slate-200 hover:border-blue-500 cursor-pointer shadow-xs transition-all"
                >
                  <RefreshCw className="w-3.5 h-3.5 text-blue-500" />
                  <span>Flip: Currently {cardSide === "front" ? "Front Side" : "Back Side"}</span>
                </button>
              </div>
            </div>

            {/* Simulated A4 Paper with Centered ID Badges */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 items-center">
              {/* Virtual A4 Canvas */}
              <div className="w-full max-w-[260px] mx-auto aspect-[1/1.414] bg-white dark:bg-slate-900 border-2 border-dashed border-slate-300 dark:border-slate-700 rounded-xl p-4 flex flex-col justify-between shadow-lg relative">
                <div className="text-[10px] font-mono text-slate-400 text-center uppercase tracking-widest">
                  Standard A4 Sheet (210 x 297 mm)
                </div>

                {/* Top Badge: Front */}
                <div className={`p-2.5 rounded-lg border transition-all duration-300 ${
                  cardSide === "front" 
                    ? "bg-blue-50 dark:bg-blue-950/60 border-blue-500 ring-2 ring-blue-500/30" 
                    : "bg-slate-100 dark:bg-slate-800 border-slate-300 dark:border-slate-700"
                }`}>
                  <div className="flex items-center justify-between text-[10px] font-bold text-slate-800 dark:text-slate-200">
                    <span>DRIVER LICENSE (FRONT)</span>
                    <span className="text-blue-600 text-[9px]">54 x 85 mm</span>
                  </div>
                  <div className="flex items-center gap-2 mt-2">
                    <div className="w-6 h-7 bg-blue-300 dark:bg-blue-800 rounded-xs" />
                    <div className="space-y-1 flex-1">
                      <div className="h-1.5 bg-slate-300 dark:bg-slate-600 rounded-xs w-3/4" />
                      <div className="h-1.5 bg-slate-300 dark:bg-slate-600 rounded-xs w-1/2" />
                    </div>
                  </div>
                </div>

                {/* Bottom Badge: Back */}
                <div className={`p-2.5 rounded-lg border transition-all duration-300 ${
                  cardSide === "back" 
                    ? "bg-blue-50 dark:bg-blue-950/60 border-blue-500 ring-2 ring-blue-500/30" 
                    : "bg-slate-100 dark:bg-slate-800 border-slate-300 dark:border-slate-700"
                }`}>
                  <div className="flex items-center justify-between text-[10px] font-bold text-slate-800 dark:text-slate-200">
                    <span>MAGNETIC STRIP (BACK)</span>
                    <span className="text-indigo-600 text-[9px]">54 x 85 mm</span>
                  </div>
                  <div className="mt-2 space-y-1">
                    <div className="h-2 bg-slate-800 dark:bg-slate-400 rounded-xs w-full" />
                    <div className="h-1 bg-slate-300 dark:bg-slate-600 rounded-xs w-2/3 mt-1" />
                  </div>
                </div>

                <div className="text-[10px] text-emerald-600 dark:text-emerald-400 font-mono text-center font-bold">
                  ✓ Auto-Aligned • 100% Scale 1:1
                </div>
              </div>

              {/* Explainer Specs */}
              <div className="space-y-3">
                <div className="p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Zero Cropping Hassle</div>
                  <div className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
                    No Photoshop or photo collage apps needed. Printora calculates millimeter-accurate bounding boxes.
                  </div>
                </div>
                <div className="p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Official Government Sizing</div>
                  <div className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">
                    Complies with ISO/IEC 7810 ID-1 standard dimensions for national ID, passports, and driver licenses.
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: SAFARI QR WEB PRINT */}
        {activeTab === "webprint" && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-mono font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">
                  No App Store Download Required
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-0.5">
                  Instant iPhone & Guest Safari Web Print
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1">
                  Point iPhone camera at the Windows screen QR code. A local, sandboxed Web app launches in Mobile Safari.
                </p>
              </div>

              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-300 text-xs font-semibold">
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>Zero App Install</span>
              </div>
            </div>

            {/* Simulated iPhone Safari Bar */}
            <div className="max-w-md mx-auto w-full rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-md overflow-hidden">
              <div className="bg-slate-100 dark:bg-slate-950 p-2.5 flex items-center justify-between border-b border-slate-200 dark:border-slate-800">
                <div className="flex items-center gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-slate-300 dark:bg-slate-700" />
                  <div className="w-2.5 h-2.5 rounded-full bg-slate-300 dark:bg-slate-700" />
                </div>
                <div className="px-3 py-1 rounded-lg bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-[11px] font-mono text-slate-600 dark:text-slate-300 flex items-center gap-1.5">
                  <Lock className="w-3 h-3 text-emerald-500" />
                  <span>http://192.168.1.5:8080/print</span>
                </div>
                <div className="text-[11px] font-semibold text-blue-600">Safari</div>
              </div>

              <div className="p-4 space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-semibold text-slate-900 dark:text-white">Selected Printer:</span>
                  <span className="font-mono text-blue-600 dark:text-blue-400">Brother HL-L2350DW</span>
                </div>
                <div className="p-3 rounded-xl border border-dashed border-blue-400 bg-blue-50/50 dark:bg-blue-950/20 text-center">
                  <FileText className="w-6 h-6 text-blue-600 mx-auto mb-1" />
                  <div className="text-xs font-bold text-slate-900 dark:text-white">Tax_Return_2026.pdf</div>
                  <div className="text-[10px] text-slate-500">2 Pages • Color • Duplex On</div>
                </div>
                <button
                  type="button"
                  className="w-full py-2.5 rounded-xl text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-500/20 active:scale-98 transition-all cursor-pointer"
                >
                  Send Print Request to Windows Host
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 5: ENCRYPTED REMOTE TUNNEL */}
        {activeTab === "tunnel" && (
          <div className="space-y-6">
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs font-mono font-semibold text-blue-600 dark:text-blue-400 uppercase tracking-wider">
                  Cloudflare Quick Tunnel Integration
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-0.5">
                  Zero-Port Remote Printing from Anywhere
                </h3>
                <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-400 mt-1">
                  At a coffee shop on cellular 5G? Print back home securely with an end-to-end encrypted outbound tunnel.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs font-mono px-2.5 py-1 rounded-lg bg-emerald-100 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-300 font-bold">
                  🔒 PIN Guard Active
                </span>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-center">
                <div className="text-xs font-bold text-slate-900 dark:text-white">External Phone (5G)</div>
                <div className="text-[11px] text-slate-500 mt-0.5 font-mono">Anywhere on Earth</div>
                <div className="mt-3 p-2 bg-slate-50 dark:bg-slate-950 rounded-lg text-xs font-mono text-blue-600">
                  PIN: 849-210
                </div>
              </div>

              <div className="flex flex-col items-center justify-center p-2 text-center">
                <div className="text-[11px] font-mono font-bold text-indigo-600 dark:text-indigo-400 mb-1 flex items-center gap-1">
                  <ShieldCheck className="w-3.5 h-3.5" /> Outbound Tunnel
                </div>
                <div className="w-full h-1 bg-gradient-to-r from-blue-500 via-indigo-500 to-purple-500 rounded-full" />
                <div className="text-[10px] text-slate-500 mt-1 font-mono">No router ports opened (0 port forwarding)</div>
              </div>

              <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-center">
                <div className="text-xs font-bold text-slate-900 dark:text-white">Home Windows PC</div>
                <div className="text-[11px] text-emerald-600 mt-0.5 font-semibold">Firewall Intact</div>
                <div className="mt-3 p-2 bg-slate-50 dark:bg-slate-950 rounded-lg text-xs font-mono text-emerald-600">
                  Paper prints instantly
                </div>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
