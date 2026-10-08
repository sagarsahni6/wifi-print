import { Globe, Smartphone, Server, Printer, CheckCircle2, ArrowRight } from "lucide-react";
import Link from "next/link";

export function RemotePrintingSection() {
  return (
    <section className="py-20 sm:py-28 bg-slate-900 text-white border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Left: Copy & Explanation (6 cols) */}
          <div className="lg:col-span-6 space-y-6">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-950 border border-cyan-800/80 text-cyan-300 text-xs font-semibold">
              <Globe className="w-3.5 h-3.5" />
              <span>Optional Remote Printing Engine</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight">
              Print from anywhere.
            </h2>

            <p className="text-base sm:text-lg text-slate-300 leading-relaxed font-normal">
              Keep the printer at home or in the office while your phone is somewhere else.
            </p>

            <p className="text-sm text-slate-400 leading-relaxed">
              Printora can optionally establish an encrypted Cloudflare Tunnel or connect via a custom public URL. Print over cellular 4G/5G connections without opening router ports or exposing your home IP to the public web.
            </p>

            <div className="space-y-3 pt-2 text-sm text-slate-300">
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Zero router port-forwarding or dynamic DNS setup required</span>
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Protected by 6-digit PIN verification and 2-hour signed session tokens</span>
              </div>
              <div className="flex items-center gap-2.5">
                <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Tunnel relays never permanently store your document contents</span>
              </div>
            </div>

            <div className="pt-2">
              <Link
                href="/features/remote-printing"
                className="inline-flex items-center gap-2 text-sm font-semibold text-cyan-400 hover:text-cyan-300"
              >
                <span>Learn how optional remote printing works</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          </div>

          {/* Right: Architecture Diagram Mockup (6 cols) */}
          <div className="lg:col-span-6">
            <div className="rounded-3xl bg-slate-950 border border-slate-800 p-6 sm:p-8 shadow-2xl relative overflow-hidden">
              <div className="text-xs font-mono text-slate-400 pb-4 mb-6 border-b border-slate-800 flex justify-between items-center">
                <span>Secure Tunnel Relay Topology</span>
                <span className="text-cyan-400">Zero Open Ports</span>
              </div>

              {/* 4 Node Diagram */}
              <div className="space-y-4">
                {/* 1. Phone outside on 4G/5G */}
                <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-blue-600/20 text-blue-400 flex items-center justify-center">
                      <Smartphone className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-100">Phone on 4G / 5G / Remote Wi-Fi</div>
                      <div className="text-[11px] text-slate-400">Android App or Web Print Browser Session</div>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-blue-950 text-blue-300 border border-blue-800">
                    Client
                  </span>
                </div>

                <div className="flex justify-center -my-2">
                  <div className="h-6 w-0.5 bg-gradient-to-b from-blue-500 to-cyan-500" />
                </div>

                {/* 2. Cloudflare Tunnel Relay */}
                <div className="p-4 rounded-2xl bg-slate-900 border border-cyan-500/30 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-cyan-600/20 text-cyan-400 flex items-center justify-center">
                      <Globe className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-100">Cloudflare Tunnel Edge</div>
                      <div className="text-[11px] text-slate-400">Encrypted WebSocket Ingress • Temporary relay only</div>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-cyan-950 text-cyan-300 border border-cyan-800">
                    HTTPS TLS
                  </span>
                </div>

                <div className="flex justify-center -my-2">
                  <div className="h-6 w-0.5 bg-gradient-to-b from-cyan-500 to-indigo-500" />
                </div>

                {/* 3. Windows PC Host */}
                <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center">
                      <Server className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-100">Windows PC Host (Your Office / Home)</div>
                      <div className="text-[11px] text-slate-400">Verifies PIN, issues session token, cleans temp files</div>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-800">
                    Host Spooler
                  </span>
                </div>

                <div className="flex justify-center -my-2">
                  <div className="h-6 w-0.5 bg-gradient-to-b from-indigo-500 to-emerald-500" />
                </div>

                {/* 4. Physical Printer */}
                <div className="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-emerald-600/20 text-emerald-400 flex items-center justify-center">
                      <Printer className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-100">Physical Printer</div>
                      <div className="text-[11px] text-slate-400">Prints document immediately</div>
                    </div>
                  </div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-950 text-emerald-300 border border-emerald-800">
                    Printed
                  </span>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-800 text-xs text-slate-400 flex justify-between items-center">
                <span>Remote printing is completely optional</span>
                <span className="text-cyan-400 font-mono">Rate Limited: 2 jobs/min</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
