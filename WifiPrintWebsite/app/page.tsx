export default function Home() {
  return (
    <main>

    <header className="hero">
        <div className="container hero-container">
            <div className="hero-content fade-in">
                <div className="badge">Free Wireless Printing App for Android &amp; Windows</div>
                <h1>Print from Your Android Phone to Any PC Printer over Wi-Fi</h1>
                <p>Connect your Android phone to your Windows 10 or Windows 11 PC over Wi-Fi and <strong>print documents directly</strong> to any installed printer. No cloud services, no cables — fast, secure, and completely free. Supports PDF, DOCX, images, and includes a built-in document scanner.</p>
                <div className="hero-cta">
                    <a href="#download" className="btn btn-primary" aria-label="Download Android App">
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M22.64 7.53L12 2.22 1.36 7.53a1 1 0 0 0-.58.83v7.28a1 1 0 0 0 .58.83L12 21.78l10.64-5.31a1 1 0 0 0 .58-.83V8.36a1 1 0 0 0-.58-.83z"></path><polyline points="12 22 12 12"></polyline><polyline points="23 8 12 12 1 8"></polyline></svg>
                        Get Android App
                    </a>
                    <a href="#download" className="btn btn-secondary" aria-label="Download Desktop Server">
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
                        Desktop Server
                    </a>
                </div>

            </div>
            <div className="hero-image fade-in-delay">
                <div className="glass-panel main-panel">
                    <div className="app-mockup">
                        <div className="mockup-header">
                            <span className="dot red"></span>
                            <span className="dot yellow"></span>
                            <span className="dot green"></span>
                        </div>
                        <div className="mockup-body">
                            <div className="printer-status">
                                <div className="printer-icon">🖨️</div>
                                <div>
                                    <div className="mockup-printer-name">HP LaserJet Pro</div>
                                    <p>Ready • 192.168.1.5</p>
                                </div>
                            </div>
                            <div className="print-job">
                                <div className="job-icon">📄</div>
                                <div className="job-info">
                                    <div className="mockup-file-name">Annual_Report_2026.pdf</div>
                                    <div className="progress-bar"><div className="progress" style={{ width: "75%" }}></div></div>
                                </div>
                                <div className="job-status">Printing...</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div className="glow glow-1"></div>
        <div className="glow glow-2"></div>
    </header>

    <section id="about" className="section bg-darker">
        <div className="container">
            <div className="section-header fade-in">
                <h2>Why Choose WiFi Print for Your Wireless Printing Needs?</h2>
                <p>A reliable, local-network printing solution designed for privacy, speed, and ease of use.</p>
            </div>
            <div className="about-content fade-in">
                <p>In today's fast-paced environment, the ability to <strong>print directly from your Android phone to a Windows PC</strong> shouldn't require complex cloud setups, subscription services, or sending your sensitive documents to third-party servers. WiFi Print bridges the gap between your mobile device and your existing desktop printers over your secure local Wi-Fi network.</p>
                <p>Whether you need to quickly print a PDF document, a Microsoft Word DOCX file, or a high-resolution image, our cross-platform system handles it seamlessly. The Android application acts as a powerful print client, discovering your Windows 10 or Windows 11 PC automatically using mDNS technology. Once paired securely via a PIN, your devices communicate via an encrypted HTTPS connection.</p>
                <p>By eliminating the middleman, WiFi Print offers unparalleled speed and privacy. It's the perfect solution for home offices, small businesses, and students who want an offline, local network printing solution without the hassle of configuring traditional network printers.</p>
            </div>
        </div>
    </section>

    <section id="how-it-works" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <h2>How It Works</h2>
                <p>A simple, three-step connection from your phone to your printer.</p>
            </div>
            <div className="steps-grid">
                <div className="step-card fade-in">
                    <div className="step-number">1</div>
                    <div className="step-icon">
                        <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
                    </div>
                    <h3>Run the Server</h3>
                    <p>Start the lightweight .NET 8 desktop server on your Windows PC. It automatically discovers your installed printers.</p>
                </div>
                <div className="step-card fade-in">
                    <div className="step-number">2</div>
                    <div className="step-icon">
                        <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>
                    </div>
                    <h3>Pair Your Device</h3>
                    <p>Open the Android app. It will automatically find the PC on your Wi-Fi network. Pair securely using a 6-digit PIN.</p>
                </div>
                <div className="step-card fade-in">
                    <div className="step-number">3</div>
                    <div className="step-icon">
                        <svg viewBox="0 0 24 24" width="48" height="48" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 6 2 18 2 18 9"></polyline><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path><rect x="6" y="14" width="12" height="8"></rect></svg>
                    </div>
                    <h3>Print Instantly</h3>
                    <p>Select a PDF, DOCX, or image, choose your printer settings, and send it directly to your PC for instant printing.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="scanner" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <div className="badge">Advanced Android Scanning</div>
                <h2>The Ultimate Mobile Scan &amp; Print App</h2>
                <p>Turn your smartphone into a professional-grade scanning station with built-in smart correction and templates.</p>
            </div>
            
            
            <div className="scanner-showcase fade-in">
                
                
                <div className="showcase-item">
                    <div className="phone-frame">
                        <div className="phone-notch"></div>
                        <div className="phone-screen phone-screen--doc">
                            <div className="scan-line"></div>
                            <div className="doc-viewfinder">
                                <div className="doc-paper">
                                    <div className="doc-line" style={{ width: "80%" }}></div>
                                    <div className="doc-line" style={{ width: "60%" }}></div>
                                    <div className="doc-line" style={{ width: "90%" }}></div>
                                    <div className="doc-line" style={{ width: "45%" }}></div>
                                    <div className="doc-line" style={{ width: "75%" }}></div>
                                    <div className="doc-line" style={{ width: "55%" }}></div>
                                </div>
                                <div className="detect-corner corner-tl"></div>
                                <div className="detect-corner corner-tr"></div>
                                <div className="detect-corner corner-bl"></div>
                                <div className="detect-corner corner-br"></div>
                            </div>
                            <div className="phone-hud">
                                <div className="hud-pill hud-pill--active">Auto</div>
                                <div className="hud-capture-btn"><div className="hud-capture-inner"></div></div>
                                <div className="hud-pill">Manual</div>
                            </div>
                        </div>
                    </div>
                    <div className="showcase-label">
                        <div className="showcase-icon">
                            <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                        </div>
                        <h3>Document Scanner</h3>
                        <p>AI edge detection &amp; auto-correction. Scan up to 20 pages into a single PDF.</p>
                    </div>
                </div>

                
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
                                        <div className="id-line id-line--short"></div>
                                    </div>
                                    <span className="id-label">FRONT</span>
                                </div>
                                <div className="id-merge-arrow">
                                    <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                                </div>
                                <div className="id-card id-card--back">
                                    <div className="id-stripe"></div>
                                    <div className="id-barcode">
                                        <div className="barcode-line"></div><div className="barcode-line"></div><div className="barcode-line"></div>
                                        <div className="barcode-line"></div><div className="barcode-line"></div><div className="barcode-line"></div><div className="barcode-line"></div>
                                    </div>
                                    <span className="id-label">BACK</span>
                                </div>
                            </div>
                            <div className="phone-hud">
                                <div className="id-step-indicator">
                                    <span className="step-dot step-dot--done"></span>
                                    <span className="step-connector"></span>
                                    <span className="step-dot step-dot--active"></span>
                                </div>
                                <div className="hud-status">Combining sides...</div>
                            </div>
                        </div>
                    </div>
                    <div className="showcase-label">
                        <div className="showcase-icon">
                            <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none"><rect x="3" y="4" width="18" height="16" rx="2"></rect><line x1="7" y1="8" x2="17" y2="8"></line><line x1="7" y1="12" x2="17" y2="12"></line><line x1="7" y1="16" x2="13" y2="16"></line></svg>
                        </div>
                        <h3>ID Card Scanner</h3>
                        <p>Scan front &amp; back, auto-merge onto a single printable page.</p>
                    </div>
                </div>

                
                <div className="showcase-item">
                    <div className="phone-frame">
                        <div className="phone-notch"></div>
                        <div className="phone-screen phone-screen--photo">
                            <div className="photo-grid-preview">
                                <div className="photo-sheet">
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                    <div className="photo-cell"><div className="photo-avatar"></div></div>
                                </div>
                            </div>
                            <div className="phone-hud">
                                <div className="template-chips">
                                    <span className="template-chip">Passport</span>
                                    <span className="template-chip template-chip--active">Visa 2x2</span>
                                    <span className="template-chip">Wallet</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="showcase-label">
                        <div className="showcase-icon">
                            <svg viewBox="0 0 24 24" width="22" height="22" stroke="currentColor" strokeWidth="2" fill="none"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                        </div>
                        <h3>Photo Templates</h3>
                        <p>Passport, Visa &amp; Wallet photos - auto-grid on A4 paper.</p>
                    </div>
                </div>

            </div>
        </div>
    </section>


    <section id="use-cases" className="section bg-darker">
        <div className="container">
            <div className="section-header fade-in">
                <h2>Ideal For Every Printing Scenario</h2>
                <p>Discover how WiFi Print simplifies document management across different environments.</p>
            </div>
            <div className="use-cases-grid">
                <div className="use-case-card fade-in">
                    <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="var(--primary-color)" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path><rect x="9" y="22" width="6" height="10"></rect></svg>
                        <h3 style={{ margin: "0" }}>For Home Offices</h3>
                    </div>
                    <p>Stop emailing documents to yourself just to print them. Use WiFi Print to send PDFs and receipts directly from your phone to your home printer while you're still on the couch. No cables, no hassle.</p>
                </div>
                <div className="use-case-card fade-in">
                    <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="var(--primary-color)" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                        <h3 style={{ margin: "0" }}>For Small Businesses</h3>
                    </div>
                    <p>Enable employees to print reports and invoices from their Android tablets without granting them full network access to the server. Secure JWT authentication ensures only authorized staff can use the office printer.</p>
                </div>
                <div className="use-case-card fade-in">
                    <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
                        <svg viewBox="0 0 24 24" width="24" height="24" stroke="var(--primary-color)" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M22 10v6M2 10l10-5 10 5-10 5z"></path><path d="M6 12v5c3 3 9 3 12 0v-5"></path></svg>
                        <h3 style={{ margin: "0" }}>For Students</h3>
                    </div>
                    <p>Quickly print lecture notes, assignments, and study guides from your phone. With support for DOCX and PDF, you can finalize your project on your mobile and have the hard copy waiting at your desk.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="features" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <h2>Powerful Features</h2>
                <p>Everything you need for a seamless printing experience.</p>
            </div>
            <div className="features-grid">
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                    </div>
                    <h3>Document Scanner</h3>
                    <p>Live edge detection and perspective correction for perfectly straightened document scans.</p>
                </div>
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="16" rx="2"></rect><line x1="7" y1="8" x2="17" y2="8"></line><line x1="7" y1="12" x2="17" y2="12"></line><line x1="7" y1="16" x2="13" y2="16"></line></svg>
                    </div>
                    <h3>ID Card Support</h3>
                    <p>Scan both sides of ID cards and automatically combine them onto one printable page.</p>
                </div>
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                    </div>
                    <h3>Photo Templates</h3>
                    <p>Built-in templates for Passport, Visa, and Wallet-sized photos with automatic grid alignment.</p>
                </div>
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"></path></svg>
                    </div>
                    <h3>Zero Configuration</h3>
                    <p>The Android app automatically finds the Windows server via mDNS. No manual IP entry required.</p>
                </div>
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                    </div>
                    <h3>P2P Security</h3>
                    <p>Encrypted peer-to-peer connection ensures your documents never leave your local network.</p>
                </div>
                <div className="feature-card fade-in">
                    <div className="feature-icon">
                        <svg viewBox="0 0 24 24" width="32" height="32" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12.55a11 11 0 0 1 14.08 0"></path><path d="M1.42 9a16 16 0 0 1 21.16 0"></path><path d="M8.53 16.11a6 6 0 0 1 6.95 0"></path><line x1="12" y1="20" x2="12.01" y2="20"></line></svg>
                    </div>
                    <h3>SignalR Updates</h3>
                    <p>Real-time status updates via WebSockets show exactly when your printer starts and finishes.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="technical-benefits" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <h2>Built for Performance & Privacy</h2>
                <p>Why our local network architecture is superior to cloud-based solutions.</p>
            </div>
            <div className="about-content fade-in">
                <p><strong>Zero Latency, Maximum Speed:</strong> By utilizing your local 5GHz or 2.4GHz Wi-Fi network, WiFi Print avoids the delays inherent in cloud-based printing. Large PDF files and high-resolution images are transferred at the maximum speed of your router, ensuring your printer starts working almost the moment you tap 'Print'.</p>
                <p><strong>Enhanced Privacy Control:</strong> In an era of data harvesting, WiFi Print stands out by keeping your data local. Documents are never uploaded to an external server or processed by a third-party AI. The connection is direct (Peer-to-Peer), encrypted with TLS, and fully under your control.</p>
                <p><strong>Broad Compatibility:</strong> Our .NET 8 server is designed to work with virtually any printer that is already installed and working on your Windows 10 or 11 machine. From legacy inkjet printers to the latest office laser jets, if your PC can see it, your phone can print to it.</p>
            </div>
        </div>
        <div className="glow glow-3"></div>
    </section>

    
    <section id="printing-guide" className="section bg-darker">
        <div className="container">
            <article className="fade-in">
                <div className="section-header">
                    <div className="badge">Complete Guide</div>
                    <h2>How to Print from Your Android Phone to Any PC Printer</h2>
                    <p>A step-by-step tutorial for wireless printing from Android to Windows.</p>
                </div>
                <div className="about-content">
                    <p>Printing from your <strong>Android phone to a Windows PC printer</strong> has never been easier thanks to WiFi Print. Whether you own an HP LaserJet, Canon PIXMA, Epson EcoTank, Brother MFC, or any other USB or network printer connected to your computer, this guide will walk you through the entire process in under two minutes.</p>

                    <h3>Step 1: Set Up the WiFi Print Server on Your Windows PC</h3>
                    <p>Download the WiFi Print Server application from the <a href="#download">download section</a> below. The server runs on <strong>Windows 10 and Windows 11</strong> and requires .NET 8 Runtime. Once installed, launch the application — it will automatically detect all printers installed on your system, including USB printers, network printers, and virtual PDF printers. A 6-digit pairing PIN will be displayed on screen.</p>

                    <h3>Step 2: Install the WiFi Print Android App &amp; Pair Your Device</h3>
                    <p>Install the WiFi Print app from the Google Play Store on your <strong>Android phone or tablet</strong> (requires Android 8.0 Oreo or newer). Make sure both your phone and PC are connected to the <strong>same Wi-Fi network</strong> — they don't need internet access, just a local router connection. Open the app, and it will automatically discover your PC using <strong>mDNS (Bonjour)</strong> technology. Tap to connect and enter the 6-digit PIN shown on your PC screen. The devices are now securely paired.</p>

                    <h3>Step 3: Select Your Document and Print</h3>
                    <p>You can print files in multiple ways:</p>
                    <ul style={{ margin: "1rem 0 1rem 1.5rem", lineHeight: "2" }}>
                        <li><strong>Browse files:</strong> Open PDFs, images (JPEG, PNG, WebP), or DOCX files directly from your phone's storage.</li>
                        <li><strong>Scan &amp; print:</strong> Use the built-in document scanner to capture physical documents, apply auto-correction, and send them straight to your printer.</li>
                        <li><strong>ID card scan:</strong> Scan the front and back of any ID card — the app automatically merges both sides onto a single page for printing.</li>
                        <li><strong>Share to print:</strong> From any app (WhatsApp, Gmail, Google Drive), tap "Share" and select WiFi Print to send the file directly to your printer.</li>
                        <li><strong>Passport photos:</strong> Take a photo, select a passport or visa template (35×45mm, 2×2 inch), and the app auto-arranges multiple copies on an A4 sheet.</li>
                    </ul>
                    <p>Choose your target printer from the list, adjust settings like <strong>number of copies, color vs grayscale, paper size (A4, Letter, Legal), and page orientation</strong> (portrait or landscape). Tap "Print" and your document is transferred over your local Wi-Fi at full speed — no internet required.</p>

                    <h3>Supported Printer Brands</h3>
                    <p>WiFi Print works with <strong>every printer brand</strong> as long as the printer is installed on your Windows PC. This includes: <strong>HP</strong> (LaserJet, DeskJet, OfficeJet, Envy), <strong>Canon</strong> (PIXMA, imageCLASS, MAXIFY), <strong>Epson</strong> (EcoTank, WorkForce, Expression), <strong>Brother</strong> (MFC, HL, DCP), <strong>Samsung</strong>, <strong>Lexmark</strong>, <strong>Xerox</strong>, <strong>Ricoh</strong>, and <strong>Kyocera</strong>. If your PC can print to it, your phone can too.</p>

                    <h3>Troubleshooting Tips</h3>
                    <p>If your phone can't find your PC, ensure both devices are on the same Wi-Fi network (not a guest network). Disable any VPN on your phone. Make sure Windows Firewall allows the WiFi Print Server through. If printing DOCX files, install <a href="https://www.libreoffice.org/" target="_blank" rel="noopener">LibreOffice</a> on your PC for automatic document conversion.</p>
                </div>
            </article>
        </div>
    </section>

    
    <section id="comparison" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <h2>WiFi Print vs Cloud Printing — Why Local Is Better</h2>
                <p>See how WiFi Print compares to cloud-based alternatives like Google Cloud Print and manufacturer apps.</p>
            </div>
            <div className="about-content fade-in">
                <p>After Google discontinued <strong>Google Cloud Print</strong> in December 2020, millions of users were left without a simple way to print from their phones. Most alternatives — like HP Smart, Canon PRINT, and Epson iPrint — require you to own a specific brand of Wi-Fi-enabled printer, install bloated manufacturer apps, create accounts, and rely on external cloud servers to process your documents.</p>

                <p><strong>WiFi Print takes a fundamentally different approach.</strong> Instead of routing your files through the cloud, it creates a direct, encrypted connection between your Android phone and your Windows PC over your existing Wi-Fi network. Here's why this matters:</p>

                <p><strong>Privacy First:</strong> Your documents — whether they're tax returns, medical records, legal contracts, or personal photos — never leave your local network. There's no third-party server reading, caching, or storing your files. In an age of increasing data breaches, this is the safest way to print.</p>

                <p><strong>Works with Any Printer:</strong> Unlike brand-specific apps that only work with Wi-Fi-enabled printers, WiFi Print works with <strong>any printer connected to your PC</strong>, including old USB-only printers, office network printers, and even virtual PDF printers. If Windows can see it, your phone can print to it.</p>

                <p><strong>No Internet Required:</strong> WiFi Print works on a completely <strong>offline, air-gapped network</strong>. This makes it ideal for government offices, medical facilities, law firms, schools, and any environment where internet access is restricted or unavailable.</p>

                <p><strong>No Accounts, No Subscriptions:</strong> WiFi Print is <strong>100% free</strong>. No sign-ups, no premium tiers, no ads. Just download, pair, and print.</p>

                <p><strong>Faster Transfers:</strong> Cloud printing requires uploading your file to a server and then downloading it back to your printer. WiFi Print transfers files directly at your <strong>local network speed</strong> (typically 50–300 Mbps on 5GHz Wi-Fi), making it significantly faster, especially for large PDF files and high-resolution images.</p>
            </div>
        </div>
    </section>

    <section id="faq" className="section">
        <div className="container">
            <div className="section-header fade-in">
                <h2>Frequently Asked Questions</h2>
                <p>Everything you need to know about our cross-platform printing system.</p>
            </div>
            <div className="faq-grid">
                <details className="faq-item fade-in">
                    <summary>How do I print from my Android phone to my Windows PC?</summary>
                    <p>It's incredibly simple! First, run the WiFi Print Server application on your Windows 10 or Windows 11 computer. Next, open the WiFi Print app on your Android device (ensure both are connected to the same Wi-Fi network). The app will automatically discover your PC. Enter the pairing PIN, select your document (PDF, DOCX, or Image), and tap print. Your file is sent instantly over your local network to the selected printer.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Do I need an active internet connection or cloud service?</summary>
                    <p>No, absolutely not. Unlike Google Cloud Print or manufacturer-specific remote printing apps, WiFi Print operates entirely on your local Wi-Fi network (LAN). Your files never leave your home or office network, making it a highly secure, private, and offline-capable printing solution.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Which file formats are supported for wireless printing?</summary>
                    <p>The Android printing app supports a wide variety of formats. You can natively print PDF files, standard images (JPEG, PNG, WebP), and plain text files. Furthermore, if you install LibreOffice on your Windows PC, the server can automatically convert and print Microsoft Word documents (DOCX, DOC) sent from your phone.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Can I scan documents directly with the app?</summary>
                    <p>Yes! The WiFi Print Android app includes a full-featured document scanner. It uses AI to detect document edges, correct perspective, and crop out backgrounds. You can scan multi-page documents or use the special ID Card mode to print both sides of an ID on a single page.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Is the connection between my phone and PC secure?</summary>
                    <p>Yes. The connection between the Android client and the Windows server is secured using HTTPS with automatically generated self-signed certificates. After the initial PIN pairing process, all subsequent communication and file transfers are authenticated using JSON Web Tokens (JWT), ensuring that only authorized devices can send print jobs to your computer.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>How do I print from my Android phone to an HP printer?</summary>
                    <p>If your HP printer (LaserJet, DeskJet, OfficeJet, or Envy) is installed on your Windows PC, simply run the WiFi Print Server. The Android app discovers all installed printers on your PC, including HP models. Select your HP printer from the list, choose your document, and print — no HP Smart app required.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Can I print from my phone without a wireless printer?</summary>
                    <p>Yes! This is one of WiFi Print's biggest advantages. Your printer doesn't need to be a "wireless printer" at all. Even a basic USB-only printer connected to your Windows PC works perfectly. WiFi Print sends the file from your phone to your PC over Wi-Fi, and your PC handles the actual printing to the connected printer. This means you can wirelessly print to any printer, including old or budget models.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>How do I print a PDF from my phone to my computer?</summary>
                    <p>Open the WiFi Print app, browse or share the PDF file from any app, select your PC's printer, adjust settings (copies, color, paper size), and tap Print. The PDF is transferred at full speed over your local Wi-Fi and printed instantly. WiFi Print handles standard PDF files natively without any conversion.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>What is the best free printing app for Android in 2026?</summary>
                    <p>WiFi Print is one of the top-rated free printing apps for Android in 2026. Unlike manufacturer apps (HP Smart, Canon PRINT) that only work with specific printers, WiFi Print works with any printer connected to a Windows PC. It's completely free, open source, and includes a built-in document scanner, ID card scanner, and passport photo templates — all without ads or subscriptions.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>How to connect my Android phone to my Windows printer?</summary>
                    <p>Ensure both your phone and PC are on the same Wi-Fi network. Run the WiFi Print Server on your PC (it shows a PIN). Open WiFi Print on your phone — it will auto-discover your PC using mDNS technology. Tap to connect, enter the 6-digit PIN, and you're paired. The entire process takes less than 30 seconds.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>How do I scan documents and print them from my phone?</summary>
                    <p>WiFi Print includes a full-featured document scanner powered by AI edge detection. Open the app, select "Scan", point your camera at the document, and it will automatically detect edges, correct perspective, and enhance quality. You can scan multiple pages into a single PDF. Then simply select your printer and tap Print — the scanned document goes directly from your phone's camera to paper.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Is WiFi Print safe for printing confidential documents?</summary>
                    <p>Absolutely. WiFi Print is designed with security as a priority. All communication between your phone and PC is encrypted using HTTPS (TLS). Authentication is handled via JSON Web Tokens (JWT) after a secure PIN pairing. Most importantly, your documents never leave your local network — no cloud server, no third-party processing. This makes it suitable for printing financial records, medical documents, legal papers, and other sensitive information.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>What printers are compatible with WiFi Print?</summary>
                    <p>WiFi Print is compatible with every printer that is installed on your Windows PC. This includes HP, Canon, Epson, Brother, Samsung, Lexmark, Xerox, Ricoh, Kyocera, and any other brand. It supports USB printers, network printers, and virtual printers. The app communicates with your PC, which then handles the printing using the standard Windows print system.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>How to print passport photos from my phone?</summary>
                    <p>WiFi Print includes built-in photo templates for passport photos (35×45mm), visa photos (2×2 inch), and wallet-sized photos. Take a photo with your phone's camera, select the desired template, and the app automatically arranges multiple copies in a grid layout on a single A4 sheet. This saves paper and ensures standard compliance for official documents.</p>
                </details>
                <details className="faq-item fade-in">
                    <summary>Does WiFi Print work without internet?</summary>
                    <p>Yes. WiFi Print requires only a local Wi-Fi connection (a router connecting your phone and PC). No internet access is needed at any point. This makes it perfect for use in areas with poor connectivity, restricted networks (schools, offices, government buildings), or when you simply want to keep your printing completely offline and private.</p>
                </details>
            </div>
        </div>
    </section>

    <section id="download" className="section cta-section bg-darker">
        <div className="container">
            <div className="cta-box fade-in">
                <h2>Ready to start printing?</h2>
                <p>Download the Android app and the Desktop Server to get started.</p>

                
                <div className="both-required-badge">
                    <svg viewBox="0 0 24 24" width="18" height="18" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                    <span><strong>Both components required</strong> — You need the Android App <em>and</em> the PC Server to print wirelessly.</span>
                </div>

                
                <div className="setup-visual">
                    <div className="setup-component">
                        <div className="setup-icon">📱</div>
                        <span>Android App</span>
                    </div>
                    <div className="setup-connector">
                        <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                        <span className="setup-wifi-label">Wi-Fi</span>
                        <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
                    </div>
                    <div className="setup-component">
                        <div className="setup-icon">🖥️</div>
                        <span>PC Server</span>
                    </div>
                    <div className="setup-connector setup-connector--result">
                        <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2.5" fill="none"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                    </div>
                    <div className="setup-component">
                        <div className="setup-icon">🖨️</div>
                        <span>Printer</span>
                    </div>
                </div>

                <div className="download-grid">
                    <div className="download-card">
                        <h3 style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "0.5rem" }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>
                            Android App
                        </h3>
                        <p>Requires Android 8.0+</p>
                        <a href="#" className="btn btn-primary w-full" aria-label="Download Android App APK">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                            Download APK
                        </a>
                        <p className="download-cross-ref">👉 You'll also need the <a href="#download-server-card"><strong>PC Server</strong></a> — see below</p>
                    </div>
                    <div className="download-card" id="download-server-card">
                        <h3 style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "0.5rem" }}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
                            Windows Server
                        </h3>
                        <p>Requires Windows 10/11 & .NET 8</p>
                        <a href="#" className="btn btn-secondary w-full" aria-label="Download Windows Server">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                            Download Server
                        </a>
                        <p className="download-cross-ref">👉 You'll also need the <a href="#download"><strong>Android App</strong></a> — see above</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    </main>
  );
}