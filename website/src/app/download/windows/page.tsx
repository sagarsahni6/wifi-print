import { Download, Monitor, CheckCircle2, ShieldCheck, Terminal, ExternalLink, HardDrive, AlertTriangle, Wifi } from "lucide-react";
import { siteConfig } from "@/config/site";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd, FaqJsonLd } from "@/components/JsonLd";
import { AuthorBadge } from "@/components/AuthorBadge";
import { MiniFaq } from "@/components/MiniFaq";

export const metadata = constructMetadata({
  title: "Download Printora Server for Windows (64-Bit .NET 8)",
  description: "Download the official Printora Windows Server for Windows 10 and Windows 11. Turn any PC into a secure local wireless print bridge for Android, iPhone, and web browsers.",
  path: "/download/windows",
  keywords: [
    "download printora windows",
    "windows print host server",
    "wifi print server download",
    "windows 11 mobile print bridge",
    "local print spooler",
  ],
});

const WINDOWS_FAQS = [
  {
    q: "Does the Windows computer need to remain awake to receive mobile print jobs?",
    a: "Yes. Because Printora functions as a local print spool bridge directly connected to your printer hardware, your Windows PC must be powered on and connected to the local network or cellular tunnel. If the computer enters sleep or hibernation mode, the local print spooler will pause until the PC wakes.",
  },
  {
    q: "How does Printora handle Windows Defender Firewall permissions?",
    a: "During initial launch, Windows Defender Firewall prompts you to permit local network communication for Printora. Printora listens on HTTP port 5000 for mobile print payloads and UDP port 5353 for local mDNS service broadcasts. Both rules are configured for private local networks without exposing ports to the public internet.",
  },
  {
    q: "Can I connect and share multiple printers simultaneously?",
    a: "Yes. Printora automatically enumerates all print queues installed on your Windows machine, including USB desktop printers, networked Ethernet printers, receipt printers, and virtual PDF printers. The mobile app and Web Print interface let users select their destination printer on the fly.",
  },
  {
    q: "Are printed documents or camera scans stored permanently on the PC?",
    a: "Never. Printora follows a strict local-first, zero-retention architecture. Incoming PDF and image payloads are placed into an ephemeral working cache solely for driver translation and Windows spooler injection. Immediately after the print spooler accepts the job, temporary files are permanently purged.",
  },
];

export default function DownloadWindowsPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Download", path: "/#download" },
    { name: "Windows Server", path: "/download/windows" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <FaqJsonLd items={WINDOWS_FAQS} />

      {/* Header */}
      <div className="text-center max-w-2xl mx-auto">
        <div className="w-16 h-16 rounded-3xl bg-blue-600/10 text-blue-600 dark:text-blue-400 border border-blue-500/20 flex items-center justify-center mx-auto mb-6">
          <Monitor className="w-8 h-8" />
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Download Printora Server for Windows
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          The high-speed print host that bridges all your connected printers to Android, iPhone, iPad, and modern web browsers.
        </p>
      </div>

      {/* Main Download Card */}
      <div className="mt-12 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-8 shadow-xl">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 pb-6 border-b border-slate-100 dark:border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white">Printora Server v1.0.0</h2>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300 border border-emerald-500/30">
                Stable Release
              </span>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Supports Windows 10 &amp; Windows 11 (64-bit) • Microsoft .NET 8 Desktop Runtime
            </p>
          </div>

          <a
            href={`${siteConfig.githubUrl}/releases`}
            target="_blank"
            rel="noopener noreferrer"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-md shadow-blue-500/20 transition-all cursor-pointer"
          >
            <Download className="w-4 h-4" />
            <span>Download Installer (.exe)</span>
          </a>
        </div>

        {/* System Requirements & Verification Details */}
        <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6 text-xs text-slate-600 dark:text-slate-400">
          <div className="space-y-3">
            <h3 className="font-bold text-slate-900 dark:text-white uppercase tracking-wider text-[11px]">System Requirements</h3>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Windows 10 (Build 19041+) or Windows 11 64-bit</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Microsoft .NET 8.0 Desktop Runtime (x64)</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>Wi-Fi or Ethernet LAN adapter with multicast support</span>
              </div>
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                <span>LibreOffice installed for automatic DOC/DOCX conversion (optional)</span>
              </div>
            </div>
          </div>

          <div className="space-y-3">
            <h3 className="font-bold text-slate-900 dark:text-white uppercase tracking-wider text-[11px]">Security &amp; Package Verification</h3>
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>100% open-source codebase with public commit logs</span>
              </div>
              <div className="flex items-center gap-2">
                <Terminal className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>Official GitHub release includes verified SHA-256 checksum</span>
              </div>
              <div className="flex items-center gap-2">
                <HardDrive className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>Automated temp-file cleanup immediately after spooling</span>
              </div>
              <div className="flex items-center gap-2">
                <Wifi className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                <span>Bound strictly to local network sockets unless tunnel is enabled</span>
              </div>
            </div>
          </div>
        </div>

        {/* Step-by-Step Setup Guide */}
        <div className="mt-8 pt-6 border-t border-slate-100 dark:border-slate-800">
          <h3 className="font-bold text-slate-900 dark:text-white text-xs uppercase tracking-wider mb-3">Step-by-Step Installation Guide</h3>
          <ol className="list-decimal pl-5 space-y-2 text-xs text-slate-600 dark:text-slate-400">
            <li>
              <strong>Download the Installer:</strong> Click the download button above or visit our GitHub Releases page to retrieve `PrintoraServer-Setup-v1.0.0.exe`.
            </li>
            <li>
              <strong>Windows SmartScreen Notice:</strong> If Microsoft Defender SmartScreen displays a warning for newly released binaries, click <em>&quot;More info&quot;</em> followed by <em>&quot;Run anyway&quot;</em>.
            </li>
            <li>
              <strong>Grant Local Network Permissions:</strong> When prompted by Windows Defender Firewall, check <em>&quot;Private networks&quot;</em> and click <em>&quot;Allow access&quot;</em>. This allows your mobile devices to discover the host over local Wi-Fi.
            </li>
            <li>
              <strong>Verify Installed Printers:</strong> The desktop dashboard will display all detected Windows printers with live status indicators (Ready, Printing, Paused, or Offline).
            </li>
            <li>
              <strong>Launch Mobile Printing:</strong> Open the Printora app on Android or scan the QR code displayed on your desktop screen with any iPhone or iPad camera to start printing immediately.
            </li>
          </ol>
        </div>

        {/* Firewall & Troubleshooting Callout */}
        <div className="mt-8 p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-900/50 text-xs text-amber-900 dark:text-amber-200 space-y-2">
          <div className="flex items-center gap-2 font-bold">
            <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0" />
            <span>Troubleshooting Network &amp; Spooler Detection</span>
          </div>
          <p className="leading-relaxed">
            If your mobile app does not detect the PC automatically, verify that both devices are on the same Wi-Fi subnet (e.g. 192.168.1.x) and that your router does not enforce &quot;AP Isolation&quot; or &quot;Client Isolation&quot;. If a printer shows &quot;Offline&quot;, ensure the USB or network cable is firmly seated and run <code>net stop spooler &amp;&amp; net start spooler</code> in an Administrator PowerShell window.
          </p>
        </div>
      </div>

      {/* Author and Trust Badge */}
      <div className="mt-10">
        <AuthorBadge
          reviewedBy="Printora Systems Engineering Team"
          testedEnvironment="Windows 11 (24H2) x64 • Windows 10 (22H2) x64"
          lastUpdated="October 2026"
        />
      </div>

      {/* Mini FAQ Accordion */}
      <div className="mt-10">
        <MiniFaq
          items={WINDOWS_FAQS}
          title="Windows Server Setup FAQ"
          description="Common technical questions regarding ports, spooler access, and runtime dependencies."
        />
      </div>

      {/* Alternative download methods */}
      <div className="mt-10 text-center text-xs text-slate-500">
        Prefer inspecting or compiling from source code? View the project repository on{" "}
        <a 
          href={siteConfig.githubUrl} 
          target="_blank" 
          rel="noopener noreferrer" 
          className="text-blue-600 dark:text-blue-400 font-semibold hover:underline inline-flex items-center gap-1"
        >
          <span>GitHub</span>
          <ExternalLink className="w-3 h-3" />
        </a>
      </div>
    </div>
  );
}
