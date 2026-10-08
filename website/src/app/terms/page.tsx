import Link from "next/link";
import { siteConfig } from "@/config/site";
import { Scale, ArrowLeft } from "lucide-react";
import { constructMetadata } from "@/lib/metadata";
import { BreadcrumbJsonLd } from "@/components/JsonLd";

export const metadata = constructMetadata({
  title: "Terms of Service",
  description: "Terms and conditions for utilizing the Printora open-source print server and mobile printing software.",
  path: "/terms",
  keywords: [
    "printora terms of service",
    "open source license terms",
    "printora software disclaimer",
  ],
});

export default function TermsPage() {
  const breadcrumbs = [
    { name: "Home", path: "/" },
    { name: "Terms of Service", path: "/terms" },
  ];

  return (
    <div className="py-16 sm:py-24 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <BreadcrumbJsonLd items={breadcrumbs} />
      <div className="mb-8">
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-blue-600 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Back to Home</span>
        </Link>
      </div>

      <div className="border-b border-slate-200 dark:border-slate-800 pb-8 mb-10">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 text-xs font-semibold mb-4">
          <Scale className="w-3.5 h-3.5" />
          <span>Legal &amp; Open Source Terms</span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
          Terms of Service
        </h1>
        <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
          Last revised: October 2026. Please read these terms carefully before deploying or utilizing Printora.
        </p>
      </div>

      <div className="space-y-8 text-sm sm:text-base text-slate-700 dark:text-slate-300 leading-relaxed">
        <section className="space-y-3">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">1. Software License &amp; Permitted Use</h2>
          <p>
            Printora is free, open-source software distributed under standard permissive licensing terms. You are free to run the software on your personal or corporate computers, distribute it across private local networks, and modify the source code in compliance with the repository license.
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">2. Local Hardware &amp; Network Responsibility</h2>
          <p>
            Printora operates entirely on your local Windows PC, Android devices, and local area network (or through user-configured tunnels). You are solely responsible for ensuring that the computers and printers connected to your network comply with your organization&apos;s security and access control policies.
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">3. Cloudflare Tunnel &amp; Remote Access Disclaimer</h2>
          <p>
            Optional remote printing capabilities utilize Cloudflare Quick Tunnels. If enabled, traffic is routed through Cloudflare edge nodes in accordance with Cloudflare&apos;s Terms of Service. Printora maintainers do not operate, inspect, or manage third-party edge infrastructure.
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">4. Disclaimer of Warranty (&quot;AS IS&quot;)</h2>
          <p>
            The software is provided &quot;AS IS&quot;, without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose, or noninfringement. In no event shall the authors or copyright holders be liable for any claim, damages, or printer hardware malfunction arising from the use of the software.
          </p>
        </section>

        <section className="space-y-3">
          <h2 className="text-xl font-bold text-slate-900 dark:text-white">5. Telemetry &amp; Document Privacy</h2>
          <p>
            Printora does not collect, retain, store, or sell document contents, user credentials, camera captures, or personal data. All processing occurs locally on your host and mobile endpoints.
          </p>
        </section>

        <div className="pt-8 border-t border-slate-200 dark:border-slate-800 text-xs text-slate-500">
          For legal inquiries, open source licensing clarifications, or commercial enterprise questions, please visit our{" "}
          <a
            href={`${siteConfig.githubUrl}`}
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue-600 dark:text-blue-400 font-semibold hover:underline"
          >
            GitHub repository
          </a>.
        </div>
      </div>
    </div>
  );
}
