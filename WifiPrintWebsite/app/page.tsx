export default function Home() {
  return (
    <main>
      {/* ══════════════════ Hero Section ══════════════════ */}
      <header className="hero">
        <div className="container hero-container">
          <div className="hero-content fade-in">
            <div className="announcement-badge">
              <span className="pulse-dot"></span>
              <span>v2.0 Released: On-Device OCR Scanner + Batch Mode + Cross-Network PIN</span>
            </div>
            <h1>Print from Your Android Phone to Any PC Printer over Wi-Fi</h1>
            <p>
              Transform your Windows 10 or 11 PC into an instant wireless print bridge. Print documents,
              password-protected PDFs, Office files, and photos directly from your phone — <strong>completely offline,
              zero cloud, no cables, and 100% free</strong>. Now featuring an advanced on-device ML Kit OCR scanner
              with continuous batch scanning and post-scan editing.
            </p>
            <div className="hero-cta">
              <a href="#download" className="btn btn-primary" aria-label="Download Android App APK">
                <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
                Download Android App
              </a>
              <a href="#download" className="btn btn-secondary" aria-label="Download Windows Server">
                <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                  <line x1="8" y1="21" x2="16" y2="21"></line>
                  <line x1="12" y1="17" x2="12" y2="21"></line>
                </svg>
                Download PC Server
              </a>
              <a href="https://github.com/sagarsahni6/wifi-print" target="_blank" rel="noopener" className="btn btn-secondary" aria-label="GitHub Repository">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor" aria-hidden="true">
                  <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"></path>
                </svg>
                GitHub
              </a>
            </div>
          </div>

          {/* ── Hero Mockup Preview ── */}
          <div className="hero-image fade-in-delay">
            <div className="glass-panel main-panel">
              <div className="app-mockup">
                <div className="mockup-header">
                  <span className="dot red"></span>
                  <span className="dot yellow"></span>
                  <span className="dot green"></span>
                  <span style={{ fontSize: "11px", fontWeight: "600", color: "var(--text-secondary)", marginLeft: "8px" }}>
                    WiFi Print Server v2.0 — Live Status
                  </span>
                </div>
                <div className="mockup-body">
                  <div className="printer-status">
                    <div className="printer-icon">🖨️</div>
                    <div>
                      <div className="mockup-printer-name">HP LaserJet Pro 400</div>
                      <p>Ready • 192.168.1.56:5000 • PIN: 558 127</p>
                    </div>
                  </div>
                  <div className="print-job">
                    <div className="job-icon">📄</div>
                    <div className="job-info">
                      <div className="mockup-file-name">Contract_Scan_OCR_Page1-6.pdf</div>
                      <div className="progress-bar"><div className="progress" style={{ width: "88%" }}></div></div>
                    </div>
                    <div className="job-status">Printing (88%)...</div>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: "1rem", paddingTop: "0.75rem", borderTop: "1px solid var(--border-color)", fontSize: "0.75rem", color: "var(--text-secondary)" }}>
                    <span>⚡ Subnet Auto-Connect: Active</span>
                    <span style={{ color: "#10b981", fontWeight: "600" }}>✓ AES TLS Encrypted</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="glow glow-1"></div>
        <div className="glow glow-2"></div>
      </header>

      {/* ══════════════════ Trust & Metrics Bar ══════════════════ */}
      <section className="container" style={{ padding: "0" }}>
        <div className="trust-bar fade-in">
          <div className="trust-item">
            <div className="trust-icon">🛡️</div>
            <div>
              <h4>100% Local &amp; Private</h4>
              <p>Zero cloud servers. Your files never leave your LAN.</p>
            </div>
          </div>
          <div className="trust-item">
            <div className="trust-icon">⚡</div>
            <div>
              <h4>Zero Latency Speed</h4>
              <p>Direct peer-to-peer Wi-Fi transfer at router line-speed.</p>
            </div>
          </div>
          <div className="trust-item">
            <div className="trust-icon">🖨️</div>
            <div>
              <h4>Universal Hardware</h4>
              <p>Works with USB, network, and thermal printers on Windows.</p>
            </div>
          </div>
          <div className="trust-item">
            <div className="trust-icon">🔍</div>
            <div>
              <h4>On-Device ML OCR</h4>
              <p>Instant text recognition &amp; batch document scanning.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════ Why Choose WiFi Print ══════════════════ */}
      <section id="about" className="section bg-darker">
        <div className="container">
          <div className="section-header fade-in">
            <h2>Why Choose WiFi Print for Your Wireless Printing Needs?</h2>
            <p>A fast, private, and universal alternative to abandoned cloud services and bloated manufacturer apps.</p>
          </div>
          <div className="about-content fade-in">
            <p>
              In December 2020, Google discontinued <strong>Google Cloud Print</strong>, leaving millions of users
              unable to easily print from their Android phones to standard PC printers. Manufacturer apps like HP Smart,
              Canon PRINT, and Epson iPrint only work with their specific brand of expensive wireless printers, require
              mandatory accounts, and push your private documents through external cloud servers.
            </p>
            <p>
              <strong>WiFi Print solves this permanently.</strong> It turns any Windows 10 or Windows 11 PC into an
              intelligent, secure wireless print hub. If a printer is installed on your computer — even an old
              budget USB-only printer — your Android phone can print to it wirelessly across your local Wi-Fi network.
            </p>
            <p>
              With <strong>v2.0</strong>, WiFi Print now includes a full-fledged mobile document workstation:
              continuous batch scanning, Google ML Kit on-device OCR, password-protected PDF support, and cross-network
              PIN pairing. All with zero subscriptions, zero ads, and zero cloud tracking.
            </p>
          </div>
        </div>
      </section>

      {/* ══════════════════ How It Works ══════════════════ */}
      <section id="how-it-works" className="section">
        <div className="container">
          <div className="section-header fade-in">
            <h2>How It Works in 3 Simple Steps</h2>
            <p>Connect your phone and printer in under 30 seconds.</p>
          </div>
          <div className="steps-grid">
            <div className="step-card fade-in">
              <div className="step-number">1</div>
              <div className="step-icon">
                <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                  <line x1="8" y1="21" x2="16" y2="21"></line>
                  <line x1="12" y1="17" x2="12" y2="21"></line>
                </svg>
              </div>
              <h3>Launch the Server</h3>
              <p>Run the lightweight .NET 8 desktop server on your Windows PC. It auto-detects all installed printers (USB, network, or virtual PDF) and displays your connection PIN.</p>
            </div>
            <div className="step-card fade-in">
              <div className="step-number">2</div>
              <div className="step-icon">
                <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                  <line x1="12" y1="18" x2="12.01" y2="18"></line>
                </svg>
              </div>
              <h3>Pair Instantly</h3>
              <p>Open the Android app. On the same Wi-Fi, it discovers the server automatically via mDNS. If on another network, scan the QR code or enter the 6-digit PIN.</p>
            </div>
            <div className="step-card fade-in">
              <div className="step-number">3</div>
              <div className="step-icon">
                <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="6 9 6 2 18 2 18 9"></polyline>
                  <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                  <rect x="6" y="14" width="12" height="8"></rect>
                </svg>
              </div>
              <h3>Select, Scan &amp; Print</h3>
              <p>Pick a PDF, Office doc, image, or use the built-in OCR document scanner. Set copies, color, and paper size, then print directly at line-speed.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════ Scanner & OCR Showcase ══════════════════ */}
      <section id="scanner" className="section bg-darker">
        <div className="container">
          <div className="section-header fade-in">
            <div className="announcement-badge">
              <span>Major Upgrade in v2.0</span>
            </div>
            <h2>Advanced On-Device Document Scanner &amp; ML Kit OCR</h2>
            <p>Turn your phone into a professional portable document scanner with machine learning text extraction and post-scan studio.</p>
          </div>

          <div className="scanner-showcase fade-in">
            {/* Showcase 1: ML Kit OCR */}
            <div className="showcase-item">
              <div className="phone-frame">
                <div className="phone-notch"></div>
                <div className="phone-screen phone-screen--ocr">
                  <div>
                    <span className="ocr-badge">⚡ ML Kit On-Device OCR</span>
                    <div className="ocr-box">
                      <div className="ocr-line" style={{ width: "95%" }}></div>
                      <div className="ocr-line" style={{ width: "80%" }}></div>
                      <div className="ocr-line" style={{ width: "88%" }}></div>
                      <div className="ocr-line" style={{ width: "65%" }}></div>
                    </div>
                  </div>
                  <div className="ocr-extracted-preview">
                    <strong>Recognized Text:</strong><br />
                    INVOICE #9402<br />
                    Total Due: $1,450.00<br />
                    Status: Approved
                  </div>
                  <div className="phone-hud">
                    <div className="hud-pill hud-pill--active">Copy All Text</div>
                    <div className="hud-pill">Search Text</div>
                  </div>
                </div>
              </div>
              <div className="showcase-label">
                <div className="showcase-icon">
                  <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none">
                    <polyline points="4 7 4 4 20 4 20 7"></polyline>
                    <line x1="9" y1="20" x2="15" y2="20"></line>
                    <line x1="12" y1="4" x2="12" y2="20"></line>
                  </svg>
                </div>
                <h3>On-Device ML Kit OCR</h3>
                <p>Extract text instantly from scanned receipts, invoices, and documents. Copy to clipboard or search text completely offline.</p>
              </div>
            </div>

            {/* Showcase 2: Continuous Batch Scan */}
            <div className="showcase-item">
              <div className="phone-frame">
                <div className="phone-notch"></div>
                <div className="phone-screen phone-screen--batch">
                  <span className="batch-counter">Batch: 5 Pages Captured</span>
                  <div className="batch-pages-stack">
                    <div className="batch-page-layer"></div>
                    <div className="batch-page-layer"></div>
                    <div className="batch-page-layer"></div>
                  </div>
                  <div className="phone-hud">
                    <div className="hud-pill">Scan Next</div>
                    <div className="hud-pill hud-pill--active">Finish &amp; Review</div>
                  </div>
                </div>
              </div>
              <div className="showcase-label">
                <div className="showcase-icon">
                  <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none">
                    <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
                    <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
                  </svg>
                </div>
                <h3>Continuous Batch Mode</h3>
                <p>Scan multi-page contracts, book chapters, and documents one after another. Reorder pages and compile into a single PDF.</p>
              </div>
            </div>

            {/* Showcase 3: ID Card Auto-Merge */}
            <div className="showcase-item">
              <div className="phone-frame">
                <div className="phone-notch"></div>
                <div className="phone-screen phone-screen--id">
                  <div className="id-viewfinder">
                    <div className="id-card id-card--front">
                      <div className="id-photo-placeholder"></div>
                      <div className="id-details">
                        <div className="id-line id-line--name"></div>
                        <div className="id-line id-line--short"></div>
                      </div>
                      <span className="id-label">FRONT</span>
                    </div>
                    <div className="id-merge-arrow">
                      <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                    </div>
                    <div className="id-card id-card--back">
                      <div className="id-stripe"></div>
                      <span className="id-label">BACK</span>
                    </div>
                  </div>
                  <div className="phone-hud">
                    <div className="hud-status">Combined on 1 Page</div>
                  </div>
                </div>
              </div>
              <div className="showcase-label">
                <div className="showcase-icon">
                  <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none">
                    <rect x="3" y="4" width="18" height="16" rx="2"></rect>
                    <line x1="7" y1="8" x2="17" y2="8"></line>
                    <line x1="7" y1="12" x2="17" y2="12"></line>
                  </svg>
                </div>
                <h3>ID Card Auto-Merge</h3>
                <p>Scan front and back of driver's licenses or badges. The app aligns both sides onto a single printable page automatically.</p>
              </div>
            </div>

            {/* Showcase 4: Post-Scan Editing Studio */}
            <div className="showcase-item">
              <div className="phone-frame">
                <div className="phone-notch"></div>
                <div className="phone-screen phone-screen--doc">
                  <div className="doc-viewfinder">
                    <div className="doc-paper" style={{ transform: "rotate(0deg)" }}>
                      <div className="doc-line" style={{ width: "85%" }}></div>
                      <div className="doc-line" style={{ width: "70%" }}></div>
                      <div className="doc-line" style={{ width: "90%" }}></div>
                      <div className="doc-line" style={{ width: "60%" }}></div>
                    </div>
                    <div className="detect-corner corner-tl"></div>
                    <div className="detect-corner corner-tr"></div>
                    <div className="detect-corner corner-bl"></div>
                    <div className="detect-corner corner-br"></div>
                  </div>
                  <div className="phone-hud">
                    <div className="hud-pill">Crop</div>
                    <div className="hud-pill">90° Rotate</div>
                    <div className="hud-pill hud-pill--active">Watermark</div>
                  </div>
                </div>
              </div>
              <div className="showcase-label">
                <div className="showcase-icon">
                  <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none">
                    <circle cx="12" cy="12" r="3"></circle>
                    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
                  </svg>
                </div>
                <h3>Post-Scan Studio</h3>
                <p>Rotate by 90°, interactive crop with rule-of-thirds grid, brightness &amp; contrast sliders, custom watermark, and page size selection (A4/Letter/Legal/A3).</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════ Cross-Network & Dual Connectivity ══════════════════ */}
      <section className="section">
        <div className="container">
          <div className="section-header fade-in">
            <h2>Seamless Connectivity: Same Wi-Fi or Cross-Network</h2>
            <p>Whether you're on your home Wi-Fi, office VLANs, or a guest network, WiFi Print connects seamlessly.</p>
          </div>
          <div className="network-dual-card fade-in">
            <div className="network-mode-box">
              <h4>⚡ Same Wi-Fi Subnet</h4>
              <p>When phone and PC share the same Wi-Fi router, zero setup is needed. The app auto-discovers your PC via mDNS and connects immediately with no manual IP configuration.</p>
              <div className="check-yes">✓ Zero Configuration · Instant Auto-Connect</div>
            </div>
            <div className="network-mode-box">
              <h4>🔒 Cross-Network / Guest Wi-Fi / VLANs</h4>
              <p>On separate networks or office subnets? Simply scan the permanent QR code on the desktop dashboard or enter the IP with the 6-digit rotating PIN.</p>
              <div className="pin-badge-display">
                <span>PAIRING PIN:</span>
                <span>558 127</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════ Password-Protected PDF Support ══════════════════ */}
      <section className="section bg-darker">
        <div className="container">
          <div className="section-header fade-in">
            <h2>Native Support for Password-Protected PDFs</h2>
            <p>Print bank statements, tax forms, payslips, and invoices without unprotecting them on third-party websites.</p>
          </div>
          <div className="about-content fade-in" style={{ textAlign: "center", maxWidth: "800px", margin: "0 auto" }}>
            <p>
              Many sensitive documents like monthly bank e-statements, salary slips, and medical records arrive with
              password encryption. Uploading them to random online PDF unlocker websites creates severe identity theft risks.
            </p>
            <p>
              With WiFi Print, your encrypted PDFs remain <strong>100% private and protected</strong>. The app prompts you
              for the password, verifies it on your device, and decrypts the document directly for local printing via
              encrypted peer-to-peer streaming to your Windows PC.
            </p>
          </div>
        </div>
      </section>

      {/* ══════════════════ Comparison Table (SEO Powerhouse) ══════════════════ */}
      <section id="comparison" className="section">
        <div className="container">
          <div className="section-header fade-in">
            <h2>WiFi Print vs Cloud Print &amp; Competitor Apps</h2>
            <p>See why thousands of users and businesses choose WiFi Print over manufacturer cloud tools.</p>
          </div>

          <div className="comparison-table-wrapper fade-in">
            <table className="comparison-table">
              <thead>
                <tr>
                  <th>Feature</th>
                  <th className="highlight-col">WiFi Print v2.0</th>
                  <th>Google Cloud Print (Legacy)</th>
                  <th>HP Smart / Canon PRINT</th>
                  <th>NokoPrint / PrinterShare</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><strong>Pricing &amp; Ads</strong></td>
                  <td className="highlight-col"><span className="check-yes">100% Free &amp; Ad-Free</span></td>
                  <td>Discontinued (2020)</td>
                  <td>Free (Encourages Subscriptions)</td>
                  <td>Paid Upgrades / Ads</td>
                </tr>
                <tr>
                  <td><strong>Zero Cloud / 100% Local LAN</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ Local P2P TLS</span></td>
                  <td><span className="check-no">✗ Cloud Dependent</span></td>
                  <td><span className="check-no">✗ Cloud Dependent</span></td>
                  <td>Partial (SMB Sharing)</td>
                </tr>
                <tr>
                  <td><strong>Works with USB-Only Printers</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ Any Windows Printer</span></td>
                  <td><span className="check-yes">✓ Via Chrome</span></td>
                  <td><span className="check-no">✗ Requires Brand Wi-Fi</span></td>
                  <td>Complex SMB Setup</td>
                </tr>
                <tr>
                  <td><strong>Offline Printing (No Internet)</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ 100% Offline Capable</span></td>
                  <td><span className="check-no">✗ Required Internet</span></td>
                  <td><span className="check-no">✗ Requires Internet</span></td>
                  <td><span className="check-yes">✓ Local Network</span></td>
                </tr>
                <tr>
                  <td><strong>Built-in On-Device ML OCR Scanner</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ Included (ML Kit)</span></td>
                  <td><span className="check-no">✗ Not Available</span></td>
                  <td>Cloud-Processed Scan</td>
                  <td><span className="check-no">✗ Not Available</span></td>
                </tr>
                <tr>
                  <td><strong>Continuous Batch Multi-Page Scan</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ Included</span></td>
                  <td><span className="check-no">✗ Not Available</span></td>
                  <td>Limited</td>
                  <td><span className="check-no">✗ Not Available</span></td>
                </tr>
                <tr>
                  <td><strong>Password-Protected PDF Support</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ In-App Decryption</span></td>
                  <td><span className="check-no">✗ Failed on Password</span></td>
                  <td>Inconsistent</td>
                  <td>Paid Feature</td>
                </tr>
                <tr>
                  <td><strong>Cross-Network &amp; Subnet PIN Pairing</strong></td>
                  <td className="highlight-col"><span className="check-yes">✓ QR + Rotating PIN</span></td>
                  <td>Google Account Login</td>
                  <td>Same SSID Only</td>
                  <td>Manual IP Required</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* ══════════════════ Features Grid ══════════════════ */}
      <section id="features" className="section bg-darker">
        <div className="container">
          <div className="section-header fade-in">
            <h2>Everything You Need for Seamless Wireless Printing</h2>
            <p>Engineered for reliability, privacy, and speed in any printing environment.</p>
          </div>
          <div className="features-grid">
            <div className="feature-card fade-in">
              <div className="feature-icon">🔍</div>
              <h3>ML Kit Document Scanner</h3>
              <p>On-device optical character recognition extracts text, straightens perspective, and auto-detects edges.</p>
            </div>
            <div className="feature-card fade-in">
              <div className="feature-icon">📑</div>
              <h3>Continuous Batch Scan</h3>
              <p>Capture multi-page contracts and reports without interruptions. Reorder, rotate, and export to a single PDF.</p>
            </div>
            <div className="feature-card fade-in">
              <div className="feature-icon">🔒</div>
              <h3>Protected PDF Support</h3>
              <p>Direct decryption and printing of password-locked bank statements, invoices, and confidential files.</p>
            </div>
            <div className="feature-card fade-in">
              <div className="feature-icon">🌐</div>
              <h3>Cross-Network Pairing</h3>
              <p>Connect seamlessly across separate Wi-Fi subnets, office VLANs, or guest networks using permanent QR and PIN.</p>
            </div>
            <div className="feature-card fade-in">
              <div className="feature-icon">🪪</div>
              <h3>ID Card Auto-Merge</h3>
              <p>Scan both sides of licenses and ID badges; auto-collates both images onto a single printable page.</p>
            </div>
            <div className="feature-card fade-in">
              <div className="feature-icon">⚡</div>
              <h3>SignalR Real-Time Queue</h3>
              <p>Live WebSockets connection provides real-time desktop print job progress and completion alerts.</p>
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════════ Step-by-Step Guide (SEO Rich) ══════════════════ */}
      <section id="printing-guide" className="section">
        <div className="container">
          <article className="fade-in">
            <div className="section-header">
              <div className="announcement-badge">
                <span>Tutorial &amp; Setup</span>
              </div>
              <h2>How to Print from Android to Any Windows PC Printer: Full Guide</h2>
              <p>A comprehensive walkthrough for home offices, businesses, and schools.</p>
            </div>
            <div className="about-content">
              <p>
                Printing from an <strong>Android smartphone or tablet to a printer connected to a Windows PC</strong>
                used to require complex SMB file-sharing permissions or reliance on defunct cloud services. WiFi Print
                eliminates all complexity.
              </p>

              <h3>Step 1: Set Up the WiFi Print Server on Your Windows PC</h3>
              <p>
                Download and install the WiFi Print Server on your <strong>Windows 10 or Windows 11</strong> computer.
                The server runs silently in your system tray using the high-performance .NET 8 runtime. It immediately
                queries your Windows print spooler and lists all active printers — including <strong>HP LaserJet, Canon PIXMA,
                Epson EcoTank, Brother MFC</strong>, receipt printers, and USB-only models.
              </p>

              <h3>Step 2: Connect Your Android Device</h3>
              <p>
                Open the WiFi Print app on your Android device (requires Android 8.0 or newer). If your phone and PC
                are connected to the <strong>same Wi-Fi network</strong>, the app discovers the server in under a second
                via mDNS. If your devices are on different subnets or an office guest Wi-Fi, tap "Scan QR" and point your
                camera at the desktop dashboard, or enter the rotating 6-digit PIN.
              </p>

              <h3>Step 3: Choose Your Document &amp; Print Instantly</h3>
              <p>
                You can print documents in three convenient ways:
              </p>
              <ul style={{ margin: "1rem 0 1rem 1.5rem", lineHeight: "2" }}>
                <li><strong>Browse files:</strong> Open PDFs, password-protected e-statements, images (JPEG, PNG, WebP), or Word DOCX files.</li>
                <li><strong>Scan with OCR:</strong> Use the camera to capture documents with edge detection, extract text with on-device OCR, and print directly.</li>
                <li><strong>Share to print:</strong> From WhatsApp, Gmail, Chrome, or Google Drive, tap "Share" and select WiFi Print.</li>
              </ul>
              <p>
                Select your printer, adjust copies, color vs. monochrome, orientation, and paper size (A4, Letter, Legal),
                and tap Print. Your document transfers over your local network and prints immediately.
              </p>
            </div>
          </article>
        </div>
      </section>

      {/* ══════════════════ FAQ Section ══════════════════ */}
      <section id="faq" className="section bg-darker">
        <div className="container">
          <div className="section-header fade-in">
            <h2>Frequently Asked Questions</h2>
            <p>Everything you need to know about wireless printing with WiFi Print.</p>
          </div>
          <div className="faq-grid">
            <details className="faq-item fade-in">
              <summary>How do I print from my Android phone to my Windows PC printer wirelessly?</summary>
              <p>
                Install the free WiFi Print Server on your Windows PC and the WiFi Print app on your Android phone.
                On the same Wi-Fi, the app discovers your PC automatically. Select any document (PDF, Word, or image)
                or scan paper with the built-in scanner, tap Print, and the file outputs directly to your PC's printer.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Can I print to a USB-only printer that has no built-in Wi-Fi?</summary>
              <p>
                Yes! This is one of WiFi Print's biggest benefits. As long as your USB printer is plugged into your
                Windows PC and working, WiFi Print acts as a wireless bridge. Your phone talks to the PC over Wi-Fi,
                and the PC sends the job to your USB printer.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Does WiFi Print work without internet or cloud services?</summary>
              <p>
                Yes. WiFi Print operates 100% offline across your local Wi-Fi router (LAN). Your files never leave your
                premises, ensuring maximum privacy, zero cloud upload delays, and reliability in air-gapped or offline environments.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>How does the built-in document scanner with on-device OCR work?</summary>
              <p>
                The scanner uses Google ML Kit running entirely on your phone's processor. It straightens skewed pages,
                crops backgrounds, enhances contrast, and recognizes text so you can copy, search, or export clean PDFs
                ready for printing.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Can I print password-protected PDFs from my phone?</summary>
              <p>
                Yes. When selecting an encrypted PDF (such as a bank statement or salary slip), WiFi Print prompts you
                for the password and unlocks it on your device for direct printing, without needing to upload the file to third-party unlock sites.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>How do I connect if my phone and PC are on different Wi-Fi networks or subnets?</summary>
              <p>
                The Windows desktop dashboard includes Cross-Network Pairing. Simply scan the permanent QR code shown on
                your PC screen or enter the PC's IP and 6-digit rotating PIN in the Android app.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>How does the continuous batch scan mode work?</summary>
              <p>
                In Batch Scan mode, you can capture multiple pages in succession without returning to the main menu.
                Once captured, you can reorder pages, rotate by 90°, adjust brightness and contrast, apply watermarks,
                and export a single multi-page PDF.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Which printer brands and models are supported?</summary>
              <p>
                Every printer that works with Windows is supported. This includes HP (LaserJet, DeskJet, OfficeJet),
                Canon (PIXMA, imageCLASS), Epson (EcoTank), Brother, Samsung, Xerox, Ricoh, Lexmark, POS thermal receipt
                printers, and virtual PDF printers.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Is WiFi Print safe for confidential and business documents?</summary>
              <p>
                Yes. All communication between client and server is encrypted using TLS (HTTPS). Device pairing is
                protected via JSON Web Tokens (JWT) and an approval banner on your desktop. Your files are processed
                locally and never uploaded to any remote server.
              </p>
            </details>
            <details className="faq-item fade-in">
              <summary>Is WiFi Print really 100% free and open source?</summary>
              <p>
                Yes. WiFi Print is free and open-source under the MIT License. There are no subscriptions, no print
                quotas, and no ads.
              </p>
            </details>
          </div>
        </div>
      </section>

      {/* ══════════════════ Download Section ══════════════════ */}
      <section id="download" className="section cta-section bg-darker">
        <div className="container">
          <div className="cta-box fade-in">
            <h2>Ready to start printing?</h2>
            <p>Download the Android app and the Windows Desktop Server v2.0 to begin.</p>

            <div className="both-required-badge">
              <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
              </svg>
              <span><strong>Both components required</strong> — You need the Android App on your phone and the Server on your Windows PC.</span>
            </div>

            <div className="setup-visual">
              <div className="setup-component">
                <div className="setup-icon">📱</div>
                <span>Android App</span>
              </div>
              <div className="setup-connector">
                <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                <span className="setup-wifi-label">Local Wi-Fi</span>
                <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
              </div>
              <div className="setup-component">
                <div className="setup-icon">🖥️</div>
                <span>Windows Server</span>
              </div>
              <div className="setup-connector setup-connector--result">
                <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
              </div>
              <div className="setup-component">
                <div className="setup-icon">🖨️</div>
                <span>Your Printer</span>
              </div>
            </div>

            <div className="download-grid">
              {/* Android Card */}
              <div className="download-card">
                <h3 style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "0.5rem" }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                    <line x1="12" y1="18" x2="12.01" y2="18"></line>
                  </svg>
                  Android App v2.0
                </h3>
                <p>Requires Android 8.0 (Oreo) or newer</p>
                <a href="https://github.com/sagarsahni6/wifi-print/releases" className="btn btn-primary w-full" aria-label="Download Android App APK">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  Download APK
                </a>
                <p className="download-cross-ref">👉 You'll also need the <a href="#download-server-card"><strong>Windows Server</strong></a> on your PC</p>
              </div>

              {/* Windows Card */}
              <div className="download-card" id="download-server-card">
                <h3 style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "0.5rem" }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                    <line x1="8" y1="21" x2="16" y2="21"></line>
                    <line x1="12" y1="17" x2="12" y2="21"></line>
                  </svg>
                  Windows Server v2.0
                </h3>
                <p>Requires Windows 10/11 (64-bit) &amp; .NET 8</p>
                <a href="https://github.com/sagarsahni6/wifi-print/releases" className="btn btn-secondary w-full" aria-label="Download Windows Server Installer">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="7 10 12 15 17 10"></polyline>
                    <line x1="12" y1="15" x2="12" y2="3"></line>
                  </svg>
                  Download Server Setup
                </a>
                <p className="download-cross-ref">👉 You'll also need the <a href="#download"><strong>Android App</strong></a> on your phone</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}