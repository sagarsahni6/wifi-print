# On-Page SEO Audit: Printora (printora.app)

**Category Score**: 70 / 100  
**Weight**: 20%  
**Status**: Action Required (2 High Findings, 1 Low Finding)

---

## 1. Executive Summary

All 26 pages on `printora.app` feature unique H1 tags and customized meta descriptions. However, the audit detected a **brand template collision** causing duplicate `| Printora | Printora` suffixes on three key routes, a **sitewide OpenGraph / Twitter metadata inheritance bug** where every subpage clones the homepage's social tags and points `og:url` to `https://printora.app`, and a skipped heading level on `/guides`.

---

## 2. Key Findings

### 2.1 Duplicate Brand Suffix in Page Titles
- **Severity**: High (P1)
- **Impact**: In SERPs, title tags with duplicated brand names appear sloppy, waste character budget, and dilute click-through rate.
- **Evidence**:
  Next.js `layout.tsx` defines:
  ```typescript
  title: {
    default: "Printora – Print From Phone to Any Windows Printer",
    template: "%s | Printora",
  }
  ```
  However, three pages append `| Printora` directly in their page `metadata`:
  - `src/app/guides/page.tsx`:
    `title: "Printing & Scanning Guides Hub | Printora"`  
    **Rendered output**: `"Printing & Scanning Guides Hub | Printora | Printora"` (57 chars)
  - `src/app/support/page.tsx`:
    `title: "Support & Troubleshooting | Printora"`  
    **Rendered output**: `"Support & Troubleshooting | Printora | Printora"` (53 chars)
  - `src/app/terms/page.tsx`:
    `title: "Terms of Service | Printora"`  
    **Rendered output**: `"Terms of Service | Printora | Printora"` (40 chars)
- **Remediation**:
  Remove `| Printora` from `title` in those 3 files:
  - `guides`: `title: "Printing & Scanning Guides Hub"`
  - `support`: `title: "Support & Troubleshooting"`
  - `terms`: `title: "Terms of Service"`

---

### 2.2 Sitewide OpenGraph and Twitter Metadata Cloning
- **Severity**: High (P1)
- **Impact**: When users share links on LinkedIn, Twitter, Slack, WhatsApp, iMessage, or Facebook to specific features or guides (e.g., `/features/wireless-printing` or `/print-from-android-to-windows-printer`), the social cards do not display page-specific information. Instead, they all display:
  - `og:title`: `"Printora – Print From Phone to Any Windows Printer"`
  - `og:description`: `"Turn Any Windows Printer Into a Printer You Can Use From Anywhere..."`
  - `og:url`: `"https://printora.app"`
  - `twitter:title`: `"Printora – Print From Phone to Any Windows Printer"`
- **Root Cause**: `src/app/layout.tsx` defines `openGraph` and `twitter` with hardcoded homepage values. Subpages define only `title` and `description` without overriding `openGraph` or `twitter`.
- **Remediation**:
  Export page-specific `openGraph` and `twitter` objects on subpages, or create a metadata helper function that derives `openGraph.title`, `openGraph.description`, and `openGraph.url` from page metadata automatically.

---

### 2.3 Heading Hierarchy Skip on `/guides` (H1 to H3)
- **Severity**: Low (P3)
- **Impact**: Heading level skipping impacts screen reader accessibility and structural semantic parsing by search spiders.
- **Evidence**:
  In `src/app/guides/page.tsx`:
  - Line 105: `<h1 ...>Printora Guides & Tutorials</h1>`
  - Line 135: `<h3 ...>{guide.title}</h3>`
  - There is no `<h2>` element present anywhere between the `<h1>` and `<h3>`.
- **Remediation**:
  Change the card titles from `<h3>` to `<h2>` or wrap the guides grid with `<h2 className="sr-only">Step-by-Step Technical Guides</h2>`.

---

## 3. Title & Meta Description Audit Sample

| URL | Title Tag | Length | Meta Description Length |
|---|---|---|---|
| `/` | `Printora – Print From Phone to Any Windows Printer` | 51 chars | 206 chars (Slightly long) |
| `/features/wireless-printing` | `Wireless Printing from Phone to Windows Printer | Printora` | 60 chars | 155 chars (Optimal) |
| `/download/windows` | `Download Printora Server for Windows | Printora` | 48 chars | 156 chars (Optimal) |
| `/download/android` | `Download Printora App for Android | Printora` | 44 chars | 148 chars (Optimal) |
| `/guides` | `Printing & Scanning Guides Hub | Printora | Printora` | **57 chars (Duplicated)** | 158 chars (Optimal) |
| `/security` | `Security Architecture & Cryptographic Model | Printora` | 56 chars | 152 chars (Optimal) |
