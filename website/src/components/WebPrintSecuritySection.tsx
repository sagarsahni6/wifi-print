import { ShieldCheck, Lock, Sliders, AlertOctagon, KeyRound, Clock } from "lucide-react";

export function WebPrintSecuritySection() {
  const securityGuards = [
    {
      title: "Rate Limiting by Client IP",
      value: "2 jobs / min • 15 / hr",
      desc: "Sliding-window IP throttles prevent print flooding and network spam attacks.",
      icon: Clock,
    },
    {
      title: "Consumable Quota Limits",
      value: "Max 3 copies • 30 pages",
      desc: "Configurable ceilings safeguard your printer against accidental or malicious paper and toner exhaustion.",
      icon: Sliders,
    },
    {
      title: "Brute-Force PIN Lockout",
      value: "5 attempts / 5 mins",
      desc: "Automatic lockout triggers after five invalid PIN submissions to prevent credential guessing.",
      icon: AlertOctagon,
    },
    {
      title: "HMAC Signed Sessions",
      value: "2-hour token expiry",
      desc: "Web clients receive temporary cryptographic session tokens tied to their client IP address.",
      icon: KeyRound,
    },
    {
      title: "Tunnel Ingress Protection",
      value: "CF-Connecting-IP & Ray",
      desc: "Validates incoming reverse proxy headers to ensure remote requests conform to security policies.",
      icon: ShieldCheck,
    },
    {
      title: "Automatic File Sanitization",
      value: "Zero residual files",
      desc: "Uploaded documents and converted temporary files are purged from the host disk upon completion.",
      icon: Lock,
    },
  ];

  return (
    <section className="py-20 sm:py-28 bg-slate-900 text-white border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-950 border border-blue-800 text-blue-300 text-xs font-semibold mb-3">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>Multi-Layer Anti-Abuse Defense</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Designed to protect your paper and toner.
          </h2>
          <p className="mt-3 text-base text-slate-300">
            When you expose a printer to mobile devices or guest browsers, security isn&apos;t just about encryption — it&apos;s about abuse prevention, quotas, and host control.
          </p>
        </div>

        {/* Security Shield Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {securityGuards.map((item) => {
            const Icon = item.icon;
            return (
              <div 
                key={item.title}
                className="p-6 rounded-3xl bg-slate-950 border border-slate-800 flex flex-col justify-between hover:border-slate-700 transition-colors"
              >
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 flex items-center justify-center">
                      <Icon className="w-5 h-5" />
                    </div>
                    <span className="text-[11px] font-mono px-2.5 py-1 rounded-full bg-slate-900 text-blue-300 border border-slate-800">
                      {item.value}
                    </span>
                  </div>
                  <h3 className="text-base font-bold text-slate-100">{item.title}</h3>
                  <p className="mt-2 text-xs text-slate-400 leading-relaxed">{item.desc}</p>
                </div>
              </div>
            );
          })}
        </div>

        {/* Note on Configurability */}
        <div className="mt-12 text-center text-xs text-slate-400 max-w-2xl mx-auto">
          Values shown reflect built-in default settings verified from the Printora server implementation. All quota thresholds and approval policies are fully configurable by the administrator in the Windows desktop settings.
        </div>
      </div>
    </section>
  );
}
