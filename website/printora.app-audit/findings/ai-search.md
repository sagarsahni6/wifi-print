# AI Search Readiness & Agentic Audit: Printora (printora.app)

**Category Score**: 65 / 100  
**Weight**: 10%  
**Status**: Action Required (1 High Finding, 1 Medium Finding)

---

## 1. Executive Summary

As search behavior shifts towards generative engines (Perplexity, ChatGPT Search, Claude Search, Google AI Overviews), websites must optimize for crawler access, high-density structured facts, and agent discovery files.

Printora scores **100/100 on Lighthouse Agentic Browsing** and explicitly grants crawl access to `GPTBot`, `ClaudeBot`, `PerplexityBot`, and `ChatGPT-User` in `robots.txt`. However, it completely lacks **`llms.txt`**, **`llms-full.txt`**, and machine-readable agent catalogs (`.well-known/ai-catalog.json`), leaving AI search agents to parse complex HTML without direct semantic text feeds.

---

## 2. Crawler Access Matrix

| Crawler Token | Organization | Purpose | Robots.txt Status |
|---|---|---|---|
| `GPTBot` | OpenAI | Model Training & RAG | **ALLOWED** |
| `OAI-SearchBot` | OpenAI | ChatGPT Search Indexing | **ALLOWED** |
| `ChatGPT-User` | OpenAI | Real-Time User Browsing | **ALLOWED** |
| `ClaudeBot` | Anthropic | Training & Data | **ALLOWED** |
| `Claude-SearchBot` | Anthropic | Claude Search Indexing | **ALLOWED** |
| `PerplexityBot` | Perplexity | Answer Engine Crawling | **ALLOWED** |
| `Google-Extended` | Google | AI Training (Gemini) | **ALLOWED** |
| `Applebot-Extended` | Apple | Apple Intelligence Training | **ALLOWED** |

---

## 3. Key Findings

### 3.1 Missing `/llms.txt` and `/llms-full.txt`
- **Severity**: High (P1)
- **Status**: HTTP 404 (Not Found)
- **Impact**: Generative engines and agent workflows look for `/llms.txt` as a standard root-level markdown file describing the product, core links, APIs, and documentation.
- **Remediation**:
  Create `public/llms.txt`:
  ```markdown
  # Printora
  > Turn Any Windows Printer Into a Printer You Can Use From Anywhere.

  Printora transforms any standard Windows printer into a wireless mobile printing endpoint. It enables printing from Android (via native app), iPhone/iPad (via zero-install QR Web Print Studio), and remote 4G/5G connections (via Cloudflare Tunnel).

  ## Key Documentation & Guides
  - [How It Works](https://printora.app/how-it-works): Architecture and local-first protocol.
  - [Android Printing](https://printora.app/print-from-android-to-windows-printer): Native mDNS printing guide.
  - [iPhone QR Web Print](https://printora.app/print-from-iphone-to-windows-printer): Safari-based browser printing guide.
  - [Security Architecture](https://printora.app/security): TLS pinning, local network boundary, zero cloud retention.
  ```

---

### 3.2 Missing `/.well-known/ai-catalog.json`
- **Severity**: Medium (P2)
- **Status**: HTTP 404
- **Impact**: Autonomous agents looking for machine-readable service descriptions and capabilities cannot discover Printora's local web print studio or pairing endpoints.
- **Remediation**: Provide a standard catalog entry in `public/.well-known/ai-catalog.json`.
