# Schema & Structured Data Audit: Printora (printora.app)

**Category Score**: 58 / 100  
**Weight**: 10%  
**Status**: Critical Fix Required (1 Critical Finding, 2 Medium Findings)

---

## 1. Executive Summary

The structured data implementation uses standard JSON-LD syntax without parsing errors. However, there is a **critical schema implementation error**: `RootLayout` unconditionally renders `<JsonLd />`, resulting in **10 FAQ Q&A pairs being injected sitewide into all 26 pages** (including `/terms`, `/privacy`, `/download/*`, and all procedural guides). 

This is an explicit violation of Google's Search Central Structured Data Guidelines and risks algorithmic penalty or disqualification from rich search results.

---

## 2. Key Findings

### 2.1 Sitewide FAQPage Schema Pollution (Critical)
- **Severity**: Critical (P0)
- **Guidelines Violated**: Google Search Central FAQ Guidelines:
  > *"Only use FAQPage if your page has a list of questions and answers. If the page doesn't have an FAQ, don't use FAQPage."*  
  > *"Every question must be visible to the user on the page itself."*
- **Evidence**:
  In `src/app/layout.tsx`:
  ```tsx
  <body className="...">
    <JsonLd /> {/* Injected globally onto all pages! */}
    <Navbar />
    <main>{children}</main>
    <Footer />
  </body>
  ```
  In `src/components/JsonLd.tsx`:
  ```tsx
  const faqSchema = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    "mainEntity": FAQ_ITEMS.slice(0, 10).map(...)
  };
  ```
  Result:
  - Visiting `https://printora.app/terms` injects 10 FAQ questions about Wi-Fi printing into Google's parser, even though `/terms` only contains legal text!
  - Visiting `https://printora.app/privacy` injects 10 FAQ questions.
  - Visiting `https://printora.app/download/windows` injects 10 FAQ questions.
- **Risk**: Google flags mismatched structured data as deceptive or spammy markup. In addition to ignoring the FAQ rich snippet, Google may revoke rich results for other schemas on the domain.
- **Remediation**:
  1. Remove `<script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }} />` from `JsonLd.tsx`.
  2. Create a dedicated `<FaqJsonLd />` component and place it exclusively in:
     - `src/app/faq/page.tsx`
     - `src/app/page.tsx` (where the FAQ accordion is visually rendered)

---

### 2.2 Missing BreadcrumbList Schema on Nested Routes
- **Severity**: Medium (P2)
- **Impact**: Google cannot render breadcrumb trails in mobile and desktop SERPs (e.g., `printora.app > Features > Wireless Printing`).
- **Remediation**:
  Add `BreadcrumbList` schema to all subpages. Example for `/features/wireless-printing`:
  ```json
  {
    "@context": "https://schema.org",
    "@type": "BreadcrumbList",
    "itemListElement": [
      {
        "@type": "ListItem",
        "position": 1,
        "name": "Home",
        "item": "https://printora.app"
      },
      {
        "@type": "ListItem",
        "position": 2,
        "name": "Features",
        "item": "https://printora.app/#features"
      },
      {
        "@type": "ListItem",
        "position": 3,
        "name": "Wireless Printing",
        "item": "https://printora.app/features/wireless-printing"
      }
    ]
  }
  ```

---

### 2.3 Missing HowTo Schema on Procedural Guides
- **Severity**: Medium (P2)
- **Impact**: The site has 8 high-quality procedural guides (e.g., `/print-from-android-to-windows-printer`, `/print-from-iphone-to-windows-printer`, `/id-card-scanner-to-pdf`) but none implement `HowTo` schema.
- **Remediation**:
  Add `HowTo` structured data specifying `step` elements, `totalTime`, and `supply`/`tool` prerequisites (e.g., Windows PC, USB Printer, Android phone).
