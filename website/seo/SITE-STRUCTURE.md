# Printora Site Structure & Information Architecture

> **Architecture Style:** Topic Cluster & Hub-and-Spoke Hierarchy  
> **Total Indexed Pages Target:** 35–45 High-Quality Static Prerendered Pages

---

## 1. Information Architecture Map

```
/ (Homepage: Core Value Proposition, Diagram, Bento Grid, Hardware Search)
│
├── /download
│   ├── /windows (Primary Conversion Target: .NET 8 Server Package)
│   └── /android (Secondary Conversion Target: Native Kotlin APK)
│
├── /how-it-works (Animated Architecture, 4-Step Onboarding Pipeline)
│
├── /features (Product Capabilities)
│   ├── /wireless-printing (Direct Wi-Fi / LAN fast path)
│   ├── /web-print (QR Safari browser printing without app)
│   ├── /document-scanner (Multi-page camera scanner with auto-cropping)
│   ├── /id-card-scanner (2-in-1 front & back A4 compositor)
│   ├── /ocr (On-device Tesseract text extraction)
│   ├── /remote-printing (Private Cloudflare Tunnel relay)
│   └── /printer-management (Windows Spooler queue & health telemetry)
│
├── /compare (High-Converting Competitor Migration Hubs)
│   ├── /printershare-alternative (Top Competitor Alternative)
│   ├── /google-cloud-print-alternative (Legacy Migration)
│   └── /nokoprint-alternative (Ad-Free Alternative)
│
├── /guides (Device-Specific & Problem-Solving Solutions)
│   ├── /print-from-android-to-windows-printer
│   ├── /print-from-iphone-to-windows-printer
│   ├── /print-from-ipad-to-windows-printer
│   ├── /print-from-phone-to-usb-printer
│   ├── /qr-code-web-printing
│   ├── /mobile-document-scanner-to-pdf
│   ├── /id-card-scanner-to-pdf
│   └── /remote-printing-from-phone
│
├── /compatibility (Searchable 4,000+ Hardware Compatibility Matrix)
│
├── /security (Airgap Architecture, In-Memory Spooling, TLS Pinning)
│
├── /faq (Curated Categorized Knowledge Base)
│
└── /legal
    ├── /privacy (Zero Data Collection Policy)
    ├── /terms (Open Source License & Terms)
    └── /support (GitHub Issues & Community Help)
```

---

## 2. Internal Linking Rules

1. **Every Solution Page links to its corresponding Feature Page & Download Page:**
   - E.g., `/print-from-iphone-to-windows-printer` $\rightarrow$ links to `/features/web-print` and `/download/windows`.
2. **Every Comparison Page has a primary CTA to `/download/windows`:**
   - Direct, friction-free download links above the fold and below the comparison table.
3. **Breadcrumbs on All Subpages:**
   - `Home > How It Works` or `Home > Guides > Print from iPhone`.
   - Accompanied by machine-readable `BreadcrumbList` JSON-LD schema.
4. **Contextual Hardware Tags in Footer & Navigation:**
   - Cross-link related guides dynamically to keep crawl depth $\le 2$ clicks from the homepage.
