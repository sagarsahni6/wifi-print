import Link from "next/link";
import { siteConfig } from "@/config/site";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";
import { 
  LifeBuoy, 
  Terminal, 
  Bug, 
  Lightbulb, 
  CheckCircle2, 
  ExternalLink, 
  ArrowRight
} from "lucide-react";

export const metadata = constructMetadata({
  title: "Support & Troubleshooting",
  description: "Get assistance, report issues, and follow step-by-step troubleshooting guides for the Printora Windows Server and mobile print apps.",
  path: "/support",
  keywords: [
    "printora support",
    "wifi printing troubleshooting",
    "print server help desk",
    "printora bug report github",
  ],
});

export default function SupportPage() {
  const troubleshootingGuides = [
    {
      title: "Firewall & Local Discovery (mDNS)",
      desc: "Ensure Windows Defender or third-party firewalls permit mDNS (port 5353) and UDP beacon broadcast fallback across your private Wi-Fi network.",
      tag: "Network"
    },
    {
      title: "DOC & DOCX File Conversion Setup",
      desc: "Printora converts Word documents server-side via headless LibreOffice. Learn how to verify your LibreOffice path and enable document processing.",
      tag: "Server Config"
    },
    {
      title: "Remote Cloudflare Tunnel Troubleshooting",
      desc: "Diagnose tunnel connectivity, check token authentication, verify egress rules, and confirm rate limiting parameters for remote printing.",
      tag: "Remote Printing"
    },
    {
      title: "Printer Spooler & Offline State Recovery",
      desc: "How to resolve Windows Print Spooler locks, clear stuck print jobs in the system queue, and refresh driver telemetry.",
      tag: "Hardware"
    }
  ];

  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Support", path: "/support" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      {/* Header */}
      <div className="text-center max-w-3xl mx-auto">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-4">
          <LifeBuoy className="w-3.5 h-3.5" />
          <span>Technical Support &amp; Help Desk</span>
        </div>
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          How can we help you?
        </h1>
        <p className="mt-4 text-base sm:text-lg text-slate-600 dark:text-slate-400">
          Printora is open-source, local-first software. Find answers, diagnose connectivity issues, or connect directly with the maintainers on GitHub.
        </p>
      </div>

      {/* Primary Support Channels */}
      <div className="mt-12">
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-6">Contact &amp; Community Support</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-8 shadow-sm flex flex-col justify-between">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-blue-600/10 text-blue-600 dark:text-blue-400 flex items-center justify-center mb-6">
                <Bug className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white">Report an Issue or Bug</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
                Found a bug or encountering an unexpected crash? File a detailed issue report on GitHub including your Windows OS build, printer model, and application logs.
              </p>
            </div>
            <div className="mt-6 pt-6 border-t border-slate-100 dark:border-slate-800">
              <a
                href={`${siteConfig.githubUrl}/issues/new`}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 text-sm font-semibold text-blue-600 dark:text-blue-400 hover:underline"
              >
                <span>Open GitHub Issue</span>
                <ExternalLink className="w-4 h-4" />
              </a>
            </div>
          </div>

          <div className="rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 p-8 shadow-sm flex flex-col justify-between">
            <div>
              <div className="w-12 h-12 rounded-2xl bg-emerald-600/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mb-6">
                <Lightbulb className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white">Feature Requests &amp; Ideas</h3>
              <p className="mt-2 text-sm text-slate-600 dark:text-slate-400 leading-relaxed">
                Have an idea for a new scanning filter, spooler queue capability, or mobile convenience feature? Start a community discussion with the development team.
              </p>
            </div>
            <div className="mt-6 pt-6 border-t border-slate-100 dark:border-slate-800">
              <a
                href={`${siteConfig.githubUrl}/discussions`}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 text-sm font-semibold text-emerald-600 dark:text-emerald-400 hover:underline"
              >
                <span>Community Discussions</span>
                <ExternalLink className="w-4 h-4" />
              </a>
            </div>
          </div>
        </div>
      </div>

      {/* Step-by-Step Troubleshooting Guides */}
      <div className="mt-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Self-Service Guides</h2>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              Common solutions for typical home and office network configurations.
            </p>
          </div>
          <Link
            href="/faq"
            className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline inline-flex items-center gap-1"
          >
            <span>View Full FAQ</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {troubleshootingGuides.map((guide) => (
            <div
              key={guide.title}
              className="p-6 rounded-2xl bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 transition-colors"
            >
              <span className="text-[10px] font-mono uppercase px-2 py-0.5 rounded bg-blue-100 dark:bg-blue-950 text-blue-700 dark:text-blue-300 font-bold">
                {guide.tag}
              </span>
              <h3 className="text-base font-bold text-slate-900 dark:text-white mt-3">
                {guide.title}
              </h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 mt-2 leading-relaxed">
                {guide.desc}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Diagnostic Checklist */}
      <div className="mt-16 p-8 rounded-3xl bg-slate-900 text-white border border-slate-800">
        <div className="flex items-center gap-3 mb-4">
          <Terminal className="w-5 h-5 text-blue-400" />
          <h2 className="text-lg font-bold">Diagnostics Checklist Before Filing a Report</h2>
        </div>
        <p className="text-xs text-slate-300 leading-relaxed max-w-2xl">
          To help us resolve your issue quickly, please verify the following items on your Windows print host machine:
        </p>

        <ul className="mt-6 space-y-3 text-xs text-slate-300">
          <li className="flex items-start gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <span><strong>Verify .NET 8:</strong> Run <code className="px-1.5 py-0.5 rounded bg-slate-800 font-mono text-emerald-300">dotnet --list-runtimes</code> in PowerShell to confirm Microsoft.NETCore.App 8.0+ is installed.</span>
          </li>
          <li className="flex items-start gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <span><strong>Windows Print Spooler Service:</strong> Ensure the service <code className="px-1.5 py-0.5 rounded bg-slate-800 font-mono text-emerald-300">Spooler</code> is set to Running in Windows Services.</span>
          </li>
          <li className="flex items-start gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <span><strong>Same Subnet Wi-Fi Check:</strong> Ensure your mobile device and Windows PC are connected to the same Wi-Fi router (avoid Guest Wi-Fi which isolates client devices).</span>
          </li>
          <li className="flex items-start gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
            <span><strong>Server Console Output:</strong> Check the terminal logs or system tray notifications for any TLS handshake or driver exceptions.</span>
          </li>
        </ul>
      </div>
    </div>
  );
}
