"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { 
  Printer, 
  Menu, 
  X 
} from "lucide-react";

export function Navbar() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        setMobileMenuOpen(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  return (
    <header className="sticky top-0 z-50 w-full transition-all duration-300 px-3 sm:px-6 lg:px-8 pt-3 pb-1">
      <div 
        className={`max-w-7xl mx-auto rounded-2xl transition-all duration-300 ${
          isScrolled 
            ? "glass-panel shadow-md shadow-emerald-950/5 dark:shadow-emerald-950/20 py-2.5 px-4 sm:px-6 border border-slate-200/90 dark:border-emerald-900/50" 
            : "bg-white/85 dark:bg-slate-950/80 backdrop-blur-md py-3 px-4 sm:px-6 border border-slate-200/60 dark:border-emerald-900/30 shadow-xs"
        } flex items-center justify-between text-slate-900 dark:text-white`}
      >
        {/* Brand Logo */}
        <Link 
          href="/" 
          className="flex items-center gap-2.5 font-bold text-xl tracking-tight text-slate-900 dark:text-white group focus-visible:outline-2 focus-visible:outline-emerald-600 rounded-lg p-1"
        >
          <div className="w-8 h-8 rounded-lg bg-slate-900 dark:bg-emerald-950/90 border border-slate-800 dark:border-emerald-800/60 flex items-center justify-center text-emerald-400 shadow-sm group-hover:scale-105 transition-transform duration-200">
            <Printer className="w-4 h-4" aria-hidden="true" />
          </div>
          <span className="font-extrabold text-lg tracking-tight text-slate-900 dark:text-white">
            Printora
          </span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center gap-7 lg:gap-9" aria-label="Main Navigation">
          <Link
            href="/how-it-works"
            className="text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors"
          >
            How it works
          </Link>
          <Link
            href="/privacy"
            className="text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors"
          >
            Privacy
          </Link>
          <Link
            href="/qr-code-web-printing"
            className="text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors"
          >
            Web Print
          </Link>
          <Link
            href="/compare"
            className="text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors"
          >
            Compare
          </Link>
          <Link
            href="/faq"
            className="text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-emerald-700 dark:hover:text-emerald-400 transition-colors"
          >
            FAQ
          </Link>
        </nav>

        {/* Action CTAs */}
        <div className="flex items-center gap-3">
          <Link
            href="/download/windows"
            className="inline-flex items-center justify-center px-5 py-2.5 text-xs font-semibold text-white bg-[#0e171b] hover:bg-slate-800 rounded-full transition-all duration-150 shadow-sm"
          >
            Download free
          </Link>

          {/* Mobile menu trigger */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-xl text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 focus-visible:outline-2 focus-visible:outline-emerald-600 cursor-pointer"
            aria-label={mobileMenuOpen ? "Close menu" : "Open menu"}
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>

      {/* Mobile Menu Backdrop */}
      {mobileMenuOpen && (
        <div 
          className="fixed inset-0 bg-slate-950/50 backdrop-blur-xs z-40 md:hidden"
          onClick={() => setMobileMenuOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Mobile Menu Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden relative z-50 mt-2 mx-auto max-w-7xl rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 p-4 space-y-3 shadow-2xl animate-in fade-in slide-in-from-top-2 duration-150">
          <div className="grid gap-1 py-1">
            <Link
              href="/how-it-works"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              How it works
            </Link>
            <Link
              href="/privacy"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Privacy
            </Link>
            <Link
              href="/qr-code-web-printing"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Web Print
            </Link>
            <Link
              href="/compare"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Compare
            </Link>
            <Link
              href="/faq"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              FAQ
            </Link>
          </div>

          <div className="pt-3 border-t border-slate-100 dark:border-slate-800">
            <Link
              href="/download/windows"
              onClick={() => setMobileMenuOpen(false)}
              className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-white bg-slate-900 hover:bg-slate-800 dark:bg-emerald-600 shadow-md"
            >
              Download free
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
