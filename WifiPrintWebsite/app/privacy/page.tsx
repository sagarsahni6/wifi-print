import Link from 'next/link';

export const metadata = {
  title: 'Privacy Policy | WiFi Print — Local & Secure Wireless Printing',
  description: 'Learn how WiFi Print secures your wireless printing. Our local-first, peer-to-peer connection ensures your documents and scans never leave your local Wi-Fi network.',
};

export default function PrivacyPage() {
  return (
    <main>
      {/* Hero Header */}
      <header className="hero" style={{ paddingBottom: '3rem' }}>
        <div className="container" style={{ textAlign: 'center', zIndex: 10, position: 'relative' }}>
          <div className="badge">Trust &amp; Privacy First</div>
          <h1 style={{ marginBottom: '1rem', fontSize: '3.5rem' }}>Privacy Policy</h1>
          <p style={{ maxWidth: '650px', margin: '0 auto 1.5rem', fontSize: '1.2rem', color: 'var(--text-secondary)' }}>
            We believe your documents should stay yours. WiFi Print operates entirely on your local network, keeping your personal files secure and private.
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
                <a href="#introduction">Introduction</a>
                <a href="#local-processing">1. Local Processing Model</a>
                <a href="#data-collection">2. Data We Do Not Collect</a>
                <a href="#permissions">3. Device Permissions</a>
                <a href="#security">4. Local Network Security</a>
                <a href="#third-party">5. Third-Party Libraries</a>
                <a href="#children">6. Children's Privacy</a>
                <a href="#changes">7. Policy Changes</a>
                <a href="#contact">8. Support &amp; Contact</a>
              </nav>
            </div>
          </aside>

          {/* Legal Content Document */}
          <article className="glass-panel legal-content-card">
            
            <section id="introduction" className="legal-section">
              <h2>Introduction</h2>
              <p>
                At <strong>WiFi Print</strong>, we respect your privacy and are committed to protecting it. This Privacy Policy describes how the WiFi Print Android Application and the WiFi Print Windows Server handle your information.
              </p>
              <p>
                Unlike traditional remote printing apps and deprecated cloud print services, WiFi Print is designed from the ground up as a <strong>local-first utility</strong>. Your files are processed, compiled, and sent directly between your devices over your private home or office local network. No external servers are utilized, meaning your documents remain under your absolute control at all times.
              </p>
            </section>

            <section id="local-processing" className="legal-section">
              <h2>1. Local Processing Model</h2>
              <p>
                WiFi Print’s primary design philosophy is that <strong>your data never leaves your local area network (LAN)</strong>.
              </p>
              <div className="about-content" style={{ margin: '1.5rem 0' }}>
                <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8' }}>
                  <li><strong>Local Transmissions:</strong> Documents, photos, ID card images, passport photo arrangements, and scans are transferred directly from your Android phone to your Windows PC over local Wi-Fi.</li>
                  <li><strong>Offline Functionality:</strong> Both components operate entirely without an internet connection. No file contents are uploaded, stored, cached, or transferred to any cloud servers or third parties.</li>
                  <li><strong>Temporary Storage:</strong> During printing, files are held in local temporary directories on your PC as standard print jobs managed strictly by the Windows Print Spooler. Once printed, these temporary spool files are deleted by Windows.</li>
                </ul>
              </div>
            </section>

            <section id="data-collection" className="legal-section">
              <h2>2. Data We Do Not Collect</h2>
              <p>
                Because WiFi Print is a local network utility, we do not maintain a server database or store user profiles.
              </p>
              <p><strong>Specifically:</strong></p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li><strong>No Document Harvesting:</strong> We do not collect, view, index, or parse the contents of the files you print (PDFs, DOCX files, photos, or documents).</li>
                <li><strong>No Scanning Logs:</strong> The camera scans (including ID Cards and Passport Photos) are processed in-memory on your mobile device. We do not store or transmit these images to external storage.</li>
                <li><strong>No Personal Identifiable Information (PII):</strong> We do not ask you to register, create accounts, input emails, or provide any personal details to use the software.</li>
                <li><strong>No Location Tracking:</strong> We do not track your physical location. The local network discovery process runs in-memory and does not share coordinates or network names globally.</li>
              </ul>
            </section>

            <section id="permissions" className="legal-section">
              <h2>3. Device Permissions</h2>
              <p>
                To provide wireless printing and document scanning, the WiFi Print Android application requests specific system permissions. Here is what we use them for:
              </p>
              
              <div className="faq-grid" style={{ gap: '0.75rem', marginTop: '1.5rem' }}>
                <div className="faq-item" style={{ padding: '1rem 1.25rem' }}>
                  <strong style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>📷 Camera Access (CAMERA)</strong>
                  <p style={{ margin: 0, fontSize: '0.95rem' }}>Used exclusively by the built-in Document Scanner, ID Card Scanner, and Passport Photo features to capture the document or photo. No imagery is uploaded or saved outside your local gallery.</p>
                </div>
                <div className="faq-item" style={{ padding: '1rem 1.25rem' }}>
                  <strong style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>📁 Storage / Media Access (READ_EXTERNAL_STORAGE)</strong>
                  <p style={{ margin: 0, fontSize: '0.95rem' }}>Allows you to browse, select, and print existing PDF documents, Microsoft Word files (DOCX), and images stored on your mobile device.</p>
                </div>
                <div className="faq-item" style={{ padding: '1rem 1.25rem' }}>
                  <strong style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>🌐 Local Network Discovery (ACCESS_FINE_LOCATION / NEARBY_DEVICES)</strong>
                  <p style={{ margin: 0, fontSize: '0.95rem' }}>Used to scan the local Wi-Fi subnet for the active WiFi Print Desktop Server using Multicast DNS (mDNS) auto-discovery. Android groups local Wi-Fi scans under location permissions, but no location data is read or stored.</p>
                </div>
              </div>
            </section>

            <section id="security" className="legal-section">
              <h2>4. Local Network Security</h2>
              <p>
                Even though files are sent solely over your private Wi-Fi network, WiFi Print implements robust, industry-standard cryptographic practices to prevent local interception:
              </p>
              <ul style={{ paddingLeft: '1.5rem', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                <li><strong>Local HTTPS Encryption:</strong> Data packets traveling from your phone to your PC are encrypted in transit using localized HTTPS with self-signed cryptographic certificates generated on setup by the Desktop Server.</li>
                <li><strong>PIN Verification:</strong> Initial pairing requires entering a unique, 6-digit numeric PIN displayed on the PC. This prevents unauthorized devices on the same network from submitting print jobs.</li>
                <li><strong>JSON Web Tokens (JWT):</strong> After successful PIN pairing, future connections are authenticated using a cryptographically signed local token stored securely on the phone.</li>
              </ul>
            </section>

            <section id="third-party" className="legal-section">
              <h2>5. Third-Party Libraries</h2>
              <p>
                WiFi Print uses standard, open-source framework libraries (such as ASP.NET Core, Microsoft SignalR, and OpenCV) to compile the applications.
              </p>
              <p>
                These libraries do not inject tracking scripts, nor do they transmit telemetry back to us. The desktop server uses SignalR locally to push printing progress notifications (like "Printing...", "75% Done") to the paired Android app in real time via WebSockets.
              </p>
            </section>

            <section id="children" className="legal-section">
              <h2>6. Children's Privacy</h2>
              <p>
                Our services do not collect personal data from anyone, including children under the age of 13. Since we do not transmit, harvest, or request any data whatsoever, WiFi Print is completely safe for use by students, minors, and educational institutions.
              </p>
            </section>

            <section id="changes" className="legal-section">
              <h2>7. Changes to This Policy</h2>
              <p>
                We may update our Privacy Policy from time to time. However, because we are committed to the local network processing model, any updates will never change our core rule: <strong>your documents will always remain local, secure, and offline from third-party clouds</strong>. Any changes will be posted here with an updated "Last Updated" date.
              </p>
            </section>

            <section id="contact" className="legal-section">
              <h2>8. Support &amp; Contact</h2>
              <p>
                If you have any questions, concerns, or technical feedback regarding our security model, please contact our support team on our <Link href="/contact" style={{ color: 'var(--primary-color)', textDecoration: 'none', fontWeight: 600 }}>Contact Page</Link> or reach out directly via email:
              </p>
              <div className="both-required-badge" style={{ marginTop: '1rem', width: 'fit-content' }}>
                <span>📧 Support Email: <strong>support@calclabz.com</strong></span>
              </div>
            </section>

          </article>
        </div>
      </section>
    </main>
  );
}
