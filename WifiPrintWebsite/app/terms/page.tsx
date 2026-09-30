import Link from 'next/link';

export const metadata = {
  title: 'Terms of Service | WiFi Print — Local & Secure Wireless Printing',
  description: 'Review the Terms of Service for WiFi Print. Read about our freeware license, user guidelines, and network security protocols.',
};

export default function TermsPage() {
  return (
    <main>
      {/* Hero Header */}
      <header className="hero" style={{ paddingBottom: '3rem' }}>
        <div className="container" style={{ textAlign: 'center', zIndex: 10, position: 'relative' }}>
          <div className="badge">Terms &amp; Agreements</div>
          <h1 style={{ marginBottom: '1rem', fontSize: '3.5rem' }}>Terms of Service</h1>
          <p style={{ maxWidth: '650px', margin: '0 auto 1.5rem', fontSize: '1.2rem', color: 'var(--text-secondary)' }}>
            Please read these terms carefully before installing or using our local wireless printing utility on Android and Windows PC.
          </p>
          <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 500 }}>
            Last Updated: May 18, 2026
          </div>
        </div>
        <div className="glow glow-1"></div>
        <div className="glow glow-2"></div>
      </header>

      {/* Main Content Section */}
      <section className="section bg-darker" style={{ padding: '4rem 0 6rem' }}>
        <div className="container legal-grid">
          
          {/* Table of Contents Sticky Sidebar */}
          <aside className="legal-sidebar">
            <div className="glass-panel sticky-sidebar-card">
              <h3>Table of Contents</h3>
              <nav className="legal-nav">
                <a href="#acceptance">1. Acceptance of Terms</a>
                <a href="#license">2. Software License</a>
                <a href="#user-conduct">3. Permitted Usage</a>
                <a href="#network-security">4. Local Security &amp; PINs</a>
                <a href="#third-party-conversions">5. Document Conversion</a>
                <a href="#warranty-disclaimer">6. Disclaimer of Warranties</a>
                <a href="#liability-limitation">7. Limitation of Liability</a>
                <a href="#governing-law">8. Governing Law</a>
                <a href="#contact">9. Contact Us</a>
              </nav>
            </div>
          </aside>

          {/* Legal Content Document */}
          <article className="glass-panel legal-content-card">
            
            <section id="acceptance" className="legal-section">
              <h2>1. Acceptance of Terms</h2>
              <p>
                By downloading, installing, running, or otherwise utilizing the <strong>WiFi Print Android Application</strong> (the "App") and the <strong>WiFi Print Windows Server</strong> (the "Server"), collectively referred to as "WiFi Print" or the "Service," you agree to be bound by these Terms of Service ("Terms").
              </p>
              <p>
                If you do not agree to these Terms, please do not download, install, or use WiFi Print. You may uninstall the App and Server at any time to terminate this agreement.
              </p>
            </section>

            <section id="license" className="legal-section">
              <h2>2. Software License</h2>
              <p>
                WiFi Print is distributed as <strong>free utility software (freeware)</strong> for personal, commercial, academic, and business use.
              </p>
              <p>
                We grant you a non-exclusive, non-transferable, revocable license to:
              </p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li>Install and run the WiFi Print mobile application on your personal or company Android devices.</li>
                <li>Install and run the WiFi Print Server on your Windows computers.</li>
                <li>Utilize the Service strictly to scan, format, and transmit document print tasks across your local-area networks.</li>
              </ul>
              <p>
                You may not reverse-engineer, decompile, lease, rent, sell, or repackage WiFi Print to distribute it as a paid product without prior explicit consent.
              </p>
            </section>

            <section id="user-conduct" className="legal-section">
              <h2>3. Permitted Usage &amp; Local Network Conduct</h2>
              <p>
                Since WiFi Print functions exclusively over local-area networks (LAN), you are solely responsible for ensuring the safety and configuration of your Wi-Fi environment.
              </p>
              <p><strong>You agree to:</strong></p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li>Use WiFi Print only on secure, trusted networks. Utilizing WiFi Print on public unencrypted networks (such as public airport or café hotspots) is highly discouraged, as other users on the network might attempt unauthorized pairing scans.</li>
                <li>Maintain control over the Windows PC running the Server to prevent unauthorized access to the paired printers.</li>
                <li>Refrain from using the application to transmit malicious files or attempt to exploit the network port protocols.</li>
              </ul>
            </section>

            <section id="network-security" className="legal-section">
              <h2>4. Local Security &amp; Pairing PINs</h2>
              <p>
                WiFi Print implements automatic mDNS discovery, TLS encryption, and secure PIN pairing to protect your local transmissions.
              </p>
              <p>
                You acknowledge that the security of your documents relies on keeping the <strong>6-digit numeric pairing PIN</strong> private. Do not share this PIN with untrusted individuals. We are not liable for unauthorized print transmissions or scanner requests resulting from negligence in securing your computer's screen or sharing pairing PIN credentials.
              </p>
            </section>

            <section id="third-party-conversions" className="legal-section">
              <h2>5. Document Conversion Disclaimer (LibreOffice)</h2>
              <p>
                For direct printing of Microsoft Word documents (DOCX/DOC), the WiFi Print Server relies on integration with **LibreOffice** (a third-party, free open-source office suite installed on your computer).
              </p>
              <p>
                WiFi Print does not own LibreOffice, nor are we responsible for its conversion accuracy, layout adjustments, or processing performance. We recommend utilizing PDF format for absolute print layout consistency.
              </p>
            </section>

            <section id="warranty-disclaimer" className="legal-section">
              <h2>6. Disclaimer of Warranties</h2>
              <p>
                WIFI PRINT IS PROVIDED TO YOU ON AN "AS-IS" AND "AS-AVAILABLE" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED.
              </p>
              <p>
                To the maximum extent permitted by applicable law, we disclaim all warranties, including but not limited to:
              </p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li>Implied warranties of merchantability or fitness for a particular purpose.</li>
                <li>Warranties that the Service will operate completely error-free or uninterrupted across all router hardware models.</li>
                <li>Warranties regarding the specific printing margins, page scaling, color accuracy, or scan alignment of individual printer manufacturers (HP, Canon, Brother, Epson, etc.).</li>
              </ul>
            </section>

            <section id="liability-limitation" className="legal-section">
              <h2>7. Limitation of Liability</h2>
              <p>
                IN NO EVENT SHALL SAGAR SAHNI BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, INCLUDING BUT NOT LIMITED TO:
              </p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li>Loss of printer hardware functionality, paper wastage, or toner exhaustion.</li>
                <li>Loss of data, documents, or transmission failures due to local network packet drops, firewall blockages, or signal drops.</li>
                <li>Security breaches resulting from compromised local router configurations or open Wi-Fi setups.</li>
              </ul>
              <p>
                Because WiFi Print is 100% free and does not charge any usage fees, our total liability under this agreement is limited to $0.00.
              </p>
            </section>

            <section id="governing-law" className="legal-section">
              <h2>8. Governing Law</h2>
              <p>
                These Terms and your use of WiFi Print are governed by the laws of India, without regard to its conflict of law principles. Any dispute arising from these Terms shall be resolved in the competent courts of New Delhi, India.
              </p>
            </section>

            <section id="contact" className="legal-section">
              <h2>9. Contact Us</h2>
              <p>
                If you have questions, bug reports, feature suggestions, or general legal queries regarding these Terms of Service, please reach out to us:
              </p>
              <div className="both-required-badge" style={{ marginTop: '1rem', width: 'fit-content' }}>
                <span>📧 Support Email: <strong>support@calclabz.com</strong></span>
              </div>
              <p style={{ marginTop: '1.5rem' }}>
                You can also fill out the inquiry form on our <Link href="/contact" style={{ color: 'var(--primary-color)', textDecoration: 'none', fontWeight: 600 }}>Contact Page</Link>.
              </p>
            </section>

          </article>
        </div>
      </section>
    </main>
  );
}
