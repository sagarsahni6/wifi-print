import { ShieldCheck, KeyRound, Server, CheckCircle2, ExternalLink } from "lucide-react";
import Link from "next/link";
import { siteConfig } from "@/config/site";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Security Architecture & Cryptographic Model",
  description: "Comprehensive security whitepaper for Printora: TLS certificate fingerprint pinning, PIN verification, IP rate limiting, HMAC signed tokens, and threat model.",
  path: "/security",
  keywords: [
    "printora security whitepaper",
    "tls certificate fingerprint pinning",
    "zero cloud storage encryption",
    "print spooler security model",
  ],
});

export default function SecurityPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Security", path: "/security" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="border-b border-slate-200 dark:border-slate-800 pb-10">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-4">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Security Whitepaper</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Printora Security Architecture
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400 max-w-3xl leading-relaxed">
          A technical breakdown of our transport encryption, certificate fingerprint verification, device authentication, sliding-window rate limiting, and temporary file sanitization.
        </p>
      </div>

      {/* Content Sections */}
      <div className="mt-12 space-y-12 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        {/* Section 1: Security Philosophy */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            1. Security Philosophy: Local-First Isolation
          </h2>
          <p>
            Unlike traditional cloud print services that require routing confidential documents through multi-tenant cloud storage, Printora treats the local Windows computer as the authoritative print host.
          </p>
          <div className="p-4 rounded-2xl bg-slate-100 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-xs sm:text-sm font-mono space-y-1">
            <div className="text-emerald-600 dark:text-emerald-400 font-bold">✓ Local-First Fast Path:</div>
            <div>Phone (Client) ──[ Direct TLS / Pinning ]──&gt; Windows Host ──&gt; Local Windows Spooler</div>
          </div>
          <p className="text-xs sm:text-sm text-slate-500">
            At no point in the local printing path do unencrypted bytes leave your local private network.
          </p>
        </section>

        {/* Section 2: Transport Security & Certificate Pinning */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            2. TLS &amp; SHA-256 Certificate Fingerprint Pinning
          </h2>
          <p>
            Local network discovery services (mDNS and UDP beacons) broadcast the server&apos;s connection endpoint. Because local-area network hostnames typically lack public CA signed certificates, Printora generates a self-signed X.509 certificate on the Windows host and exports its SHA-256 fingerprint into the pairing QR code and discovery payload.
          </p>
          <ul className="list-disc pl-5 space-y-2 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <li>
              <strong>Android App:</strong> Enforces certificate fingerprint verification on every TLS handshake via custom TrustManager implementations, preventing man-in-the-middle (MITM) attacks on shared Wi-Fi networks.
            </li>
            <li>
              <strong>Zero Plaintext HTTP:</strong> All local-network REST and WebSocket API endpoints require TLS.
            </li>
          </ul>
        </section>

        {/* Section 3: Device Authentication & Pairing */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            3. Device Authentication, Pairing &amp; Host Approvals
          </h2>
          <p>
            The Windows desktop server maintains an internal device registry. When an unknown client requests a print connection:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <span className="font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                <KeyRound className="w-4 h-4 text-blue-600" />
                Rotating 6-Digit PIN
              </span>
              <p className="text-slate-500 dark:text-slate-400">
                A rotating connection PIN is displayed on the Windows host UI. Submitting an incorrect PIN triggers an anti-brute-force lockout.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-2">
              <span className="font-bold text-slate-900 dark:text-white flex items-center gap-1.5">
                <Server className="w-4 h-4 text-indigo-600" />
                Administrator Approval Toast
              </span>
              <p className="text-slate-500 dark:text-slate-400">
                The desktop host displays a native Windows approval notification for newly discovered devices, allowing the operator to approve or block the client permanently.
              </p>
            </div>
          </div>
        </section>

        {/* Section 4: Web Print Anti-Abuse Safeguards */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            4. Web Print Anti-Abuse Defenses
          </h2>
          <p>
            To prevent paper and toner exhaustion when exposing the Web Print Studio to browsers or guest devices, the server enforces multi-layer sliding-window protection:
          </p>
          <div className="p-5 rounded-2xl bg-slate-900 text-white font-mono text-xs space-y-2 border border-slate-800">
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Rate Limit Per IP:</span>
              <span className="text-blue-400 font-bold">2 print jobs / minute, 15 / hour</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Brute-Force Lockout:</span>
              <span className="text-amber-400 font-bold">5 failed attempts = 5-minute lockout</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Default Web Copies Limit:</span>
              <span className="text-slate-200 font-bold">Max 3 copies per submission</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-800">
              <span className="text-slate-400">Default Page Ceiling:</span>
              <span className="text-slate-200 font-bold">Max 30 PDF pages per job</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-slate-400">Session Token Lifetime:</span>
              <span className="text-emerald-400 font-bold">2-hour HMAC-SHA256 signed cookie/header</span>
            </div>
          </div>
        </section>

        {/* Section 5: Automatic Temporary File Sanitization */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            5. Automatic Temporary File Sanitization
          </h2>
          <p>
            When a mobile client uploads a document, the server streams the file to an isolated staging directory under the local user profile. If format conversion is required (such as rendering DOCX to PDF or preparing high-DPI raster layers for the spooler), temporary working files are tracked in the job execution context.
          </p>
          <div className="p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 text-xs sm:text-sm text-emerald-900 dark:text-emerald-200 flex items-start gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
            <div>
              <strong>Sanitization Lifecycle:</strong> Once the Windows print spooler signals job completion, all staged uploaded files and intermediate converted artifacts are automatically deleted from the disk. Original personal documents outside the staging folder remain unaffected.
            </div>
          </div>
        </section>

        {/* Section 6: Threat Model & Boundaries */}
        <section className="space-y-4">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">
            6. Realistic Threat Model &amp; System Limitations
          </h2>
          <p>
            We do not make unverified claims such as &quot;100% unbreakable&quot; or &quot;zero data exists anywhere.&quot; Understanding boundaries is the cornerstone of professional security:
          </p>
          <ul className="list-disc pl-5 space-y-2 text-xs sm:text-sm text-slate-600 dark:text-slate-400">
            <li>
              <strong>Physical Host Security:</strong> Security guarantees depend on the Windows host operating system being properly updated and secured. An attacker with administrative control over the host PC can observe spooler memory.
            </li>
            <li>
              <strong>Hardware Spooler Logging:</strong> Certain enterprise printer hardware features internal hard drives that log print job accounting metadata independently of Printora.
            </li>
            <li>
              <strong>Remote Path Relays:</strong> While Cloudflare Tunnel provides transport encryption without open inbound ports, tunnel infrastructure operates under Cloudflare&apos;s published edge security policies.
            </li>
          </ul>
        </section>

        {/* CTA */}
        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 flex flex-wrap items-center justify-between gap-4">
          <Link
            href="/privacy"
            className="text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline"
          >
            ← View Privacy Architecture &amp; Policy
          </Link>
          <a
            href={siteConfig.githubUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs text-slate-500 hover:text-slate-900 dark:hover:text-white inline-flex items-center gap-1"
          >
            <span>Review Source Code on GitHub</span>
            <ExternalLink className="w-3 h-3" />
          </a>
        </div>
      </div>
    </div>
  );
}
