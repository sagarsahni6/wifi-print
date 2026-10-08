"use client";

import { useState } from "react";
import { 
  CheckCircle2, 
  AlertTriangle, 
  Gauge, 
  Layers, 
  FileSpreadsheet, 
  Info,
  Droplet
} from "lucide-react";

type PrinterState = {
  id: string;
  name: string;
  isDefault: boolean;
  status: "Ready" | "Busy" | "Offline" | "Toner Low" | "Out of Paper";
  colorSupport: boolean;
  duplexSupport: boolean;
  paperTray: "OK (85%)" | "Low (15%)" | "Empty";
  tonerBlack: number;
  tonerCyan: number;
  tonerMagenta: number;
  tonerYellow: number;
  totalPages: number;
  lastError: string;
};

const SAMPLE_PRINTERS: PrinterState[] = [
  {
    id: "hp-laserjet",
    name: "HP LaserJet Pro MFP M428fdw",
    isDefault: true,
    status: "Ready",
    colorSupport: true,
    duplexSupport: true,
    paperTray: "OK (85%)",
    tonerBlack: 73,
    tonerCyan: 88,
    tonerMagenta: 82,
    tonerYellow: 79,
    totalPages: 18421,
    lastError: "None",
  },
  {
    id: "brother-hl",
    name: "Brother HL-L2350DW (Monochrome)",
    isDefault: false,
    status: "Ready",
    colorSupport: false,
    duplexSupport: true,
    paperTray: "OK (85%)",
    tonerBlack: 42,
    tonerCyan: 0,
    tonerMagenta: 0,
    tonerYellow: 0,
    totalPages: 9340,
    lastError: "None",
  },
  {
    id: "canon-pixma",
    name: "Canon PIXMA G3010 Series",
    isDefault: false,
    status: "Toner Low",
    colorSupport: true,
    duplexSupport: false,
    paperTray: "Low (15%)",
    tonerBlack: 12,
    tonerCyan: 35,
    tonerMagenta: 40,
    tonerYellow: 38,
    totalPages: 5120,
    lastError: "Cartridge ink low warning",
  },
];

export function PrinterHealthDemo() {
  const [selectedPrinter, setSelectedPrinter] = useState<PrinterState>(SAMPLE_PRINTERS[0]);

  return (
    <div className="w-full rounded-3xl bg-slate-950 border border-slate-800 p-6 sm:p-8 text-white shadow-xl relative overflow-hidden">
      {/* Header & Printer Selector */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
            <h3 className="text-base font-bold text-slate-100">Windows Print Spooler Monitor</h3>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">Real-time status, supply metrics, and hardware capabilities</p>
        </div>

        {/* Printer dropdown switcher */}
        <div className="flex items-center gap-2">
          <label htmlFor="printer-select" className="text-xs text-slate-400">Select Printer:</label>
          <select
            id="printer-select"
            value={selectedPrinter.id}
            onChange={(e) => {
              const p = SAMPLE_PRINTERS.find((item) => item.id === e.target.value);
              if (p) setSelectedPrinter(p);
            }}
            className="bg-slate-900 border border-slate-700 text-slate-200 text-xs rounded-xl px-3 py-1.5 focus:outline-none focus:border-blue-500 font-medium cursor-pointer"
          >
            {SAMPLE_PRINTERS.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name} {p.isDefault ? "(Default)" : ""}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Main Grid: Status + Consumables */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 py-6">
        {/* Metric 1: Printer Status */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800">
          <span className="text-[11px] font-mono text-slate-400 uppercase tracking-wider block">Printer Status</span>
          <div className="mt-2 flex items-center gap-2">
            {selectedPrinter.status === "Ready" ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            ) : (
              <AlertTriangle className="w-5 h-5 text-amber-400" />
            )}
            <span className="text-lg font-bold text-slate-100">{selectedPrinter.status}</span>
          </div>
          <span className="text-[10px] text-slate-400 block mt-1">Windows Spooler Active</span>
        </div>

        {/* Metric 2: Paper Tray */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800">
          <span className="text-[11px] font-mono text-slate-400 uppercase tracking-wider block">Paper Tray State</span>
          <div className="mt-2 flex items-center gap-2">
            <Layers className="w-5 h-5 text-blue-400" />
            <span className="text-lg font-bold text-slate-100">{selectedPrinter.paperTray}</span>
          </div>
          <span className="text-[10px] text-slate-400 block mt-1">Standard Tray 1 (A4/Letter)</span>
        </div>

        {/* Metric 3: Duplex & Color */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800">
          <span className="text-[11px] font-mono text-slate-400 uppercase tracking-wider block">Hardware Features</span>
          <div className="mt-2 flex items-center gap-2">
            <FileSpreadsheet className="w-5 h-5 text-indigo-400" />
            <span className="text-base font-bold text-slate-100">
              {selectedPrinter.duplexSupport ? "Duplex: Yes" : "Duplex: Manual"}
            </span>
          </div>
          <span className="text-[10px] text-slate-400 block mt-1">
            {selectedPrinter.colorSupport ? "Color & Grayscale Engine" : "Monochrome Only"}
          </span>
        </div>

        {/* Metric 4: Total Pages Printed */}
        <div className="p-4 rounded-2xl bg-slate-900/90 border border-slate-800">
          <span className="text-[11px] font-mono text-slate-400 uppercase tracking-wider block">Total Pages Printed</span>
          <div className="mt-2 flex items-center gap-2">
            <Gauge className="w-5 h-5 text-cyan-400" />
            <span className="text-lg font-bold text-slate-100 font-mono">
              {selectedPrinter.totalPages.toLocaleString()}
            </span>
          </div>
          <span className="text-[10px] text-slate-400 block mt-1">Last Error: {selectedPrinter.lastError}</span>
        </div>
      </div>

      {/* Toner / Ink Consumable Levels */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Droplet className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
              Supply & Consumable Levels
            </span>
          </div>
          <span className="text-[11px] text-slate-400">
            {selectedPrinter.colorSupport ? "4-Cartridge CMYK System" : "High-Yield Black Cartridge"}
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Black */}
          <div className="space-y-1.5">
            <div className="flex justify-between text-xs">
              <span className="text-slate-300 font-medium">Black (K)</span>
              <span className="font-mono text-slate-200">{selectedPrinter.tonerBlack}%</span>
            </div>
            <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
              <div 
                className={`h-full rounded-full ${
                  selectedPrinter.tonerBlack < 20 ? "bg-amber-500" : "bg-slate-300"
                }`}
                style={{ width: `${selectedPrinter.tonerBlack}%` }}
              />
            </div>
          </div>

          {/* Cyan */}
          {selectedPrinter.colorSupport && (
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-cyan-400 font-medium">Cyan (C)</span>
                <span className="font-mono text-cyan-200">{selectedPrinter.tonerCyan}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                <div 
                  className="h-full rounded-full bg-cyan-400"
                  style={{ width: `${selectedPrinter.tonerCyan}%` }}
                />
              </div>
            </div>
          )}

          {/* Magenta */}
          {selectedPrinter.colorSupport && (
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-pink-400 font-medium">Magenta (M)</span>
                <span className="font-mono text-pink-200">{selectedPrinter.tonerMagenta}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                <div 
                  className="h-full rounded-full bg-pink-500"
                  style={{ width: `${selectedPrinter.tonerMagenta}%` }}
                />
              </div>
            </div>
          )}

          {/* Yellow */}
          {selectedPrinter.colorSupport && (
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-yellow-400 font-medium">Yellow (Y)</span>
                <span className="font-mono text-yellow-200">{selectedPrinter.tonerYellow}%</span>
              </div>
              <div className="h-2 rounded-full bg-slate-800 overflow-hidden">
                <div 
                  className="h-full rounded-full bg-yellow-400"
                  style={{ width: `${selectedPrinter.tonerYellow}%` }}
                />
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Technical Disclaimer Note required by Section 16 */}
      <div className="mt-4 flex items-center gap-2 text-[11px] text-slate-400">
        <Info className="w-3.5 h-3.5 text-blue-400 shrink-0" />
        <span>
          Note: Supply information and consumable levels are displayed when exposed by the installed printer driver and hardware manufacturer.
        </span>
      </div>
    </div>
  );
}
