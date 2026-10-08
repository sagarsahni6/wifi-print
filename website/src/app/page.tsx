import { Hero } from "@/components/Hero";
import { EcosystemMarquee } from "@/components/EcosystemMarquee";
import { ModernBentoGrid } from "@/components/ModernBentoGrid";
import { HowItWorksSection } from "@/components/HowItWorksSection";
import { ComparisonSection } from "@/components/ComparisonSection";
import { FaqSection } from "@/components/FaqSection";
import { FinalCtaSection } from "@/components/FinalCtaSection";
import { FaqJsonLd } from "@/components/JsonLd";

export default function HomePage() {
  return (
    <>
      {/* Rich SEO Schema Markup */}
      <FaqJsonLd />

      {/* 1. Hero 2.0 with Interactive Product Studio */}
      <Hero />

      {/* 2. Universal Ecosystem Marquee & Telemetry Bar */}
      <EcosystemMarquee />

      {/* 3. Modern Bento Grid (Queue, Privacy, OCR, Hardware Compatibility) */}
      <ModernBentoGrid />

      {/* 4. Interactive Setup Timeline */}
      <HowItWorksSection />

      {/* 5. Workflow Evolution Matrix (Traditional vs Printora) */}
      <ComparisonSection />

      {/* 6. Curated Categorized FAQ */}
      <FaqSection />

      {/* 7. High-Converting Glass CTA */}
      <FinalCtaSection />
    </>
  );
}
