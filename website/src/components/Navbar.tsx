"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { siteConfig } from "@/config/site";
import { 
  Printer, 
  Menu, 
  X, 
  Download, 
  Smartphone, 
  ChevronDown,
  Sun,
  Moon
} from "lucide-react";

export function Navbar() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [featuresDropdownOpen, setFeaturesDropdownOpen] = useState(false);
  const [mobileFeaturesOpen, setMobileFeaturesOpen] = useState(false);

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
        setFeaturesDropdownOpen(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  const toggleTheme = () => {
    const isDark = document.documentElement.classList.toggle("dark");
    localStorage.setItem("theme", isDark ? "dark" : "light");
  };

  return (
    <header className="sticky top-0 z-50 w-full transition-all duration-300 px-3 sm:px-6 lg:px-8 pt-2.5 pb-1">
      <div 
        className={`max-w-7xl mx-auto rounded-2xl transition-all duration-300 ${
          isScrolled 
            ? "glass-panel shadow-lg shadow-blue-950/5 dark:shadow-blue-950/30 py-2.5 px-4 sm:px-6 border border-slate-200/90 dark:border-slate-800" 
            : "bg-white/80 dark:bg-slate-950/80 backdrop-blur-md py-3 px-4 sm:px-6 border border-slate-200/60 dark:border-slate-800/60 shadow-xs"
        } flex items-center justify-between text-slate-900 dark:text-white`}
      >
        {/* Brand Logo */}
        <Link 
          href="/" 
          className="flex items-center gap-2.5 font-bold text-xl tracking-tight text-slate-900 dark:text-white group focus-visible:outline-2 focus-visible:outline-blue-600 rounded-lg p-1"
        >
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 via-indigo-600 to-cyan-500 flex items-center justify-center text-white shadow-md shadow-blue-500/20 group-hover:scale-105 transition-transform duration-200">
            <Printer className="w-5 h-5" aria-hidden="true" />
          </div>
          <span className="font-extrabold text-lg tracking-tight text-slate-900 dark:text-white">
            Printora
          </span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden md:flex items-center gap-1 lg:gap-2" aria-label="Main Navigation">
          {/* Features Dropdown */}
          <div className="relative">
            <button
              onClick={() => setFeaturesDropdownOpen(!featuresDropdownOpen)}
              onMouseEnter={() => setFeaturesDropdownOpen(true)}
              className="flex items-center gap-1.5 px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600 cursor-pointer"
              aria-expanded={featuresDropdownOpen}
            >
              <span>Features</span>
              <ChevronDown className={`w-4 h-4 transition-transform duration-200 ${featuresDropdownOpen ? "rotate-180" : ""}`} />
            </button>

            {featuresDropdownOpen && (
              <div 
                onMouseLeave={() => setFeaturesDropdownOpen(false)}
                className="absolute top-full left-0 mt-1.5 w-72 rounded-2xl bg-white dark:bg-slate-900 shadow-2xl border border-slate-200/90 dark:border-slate-800 p-2 grid gap-1 animate-in fade-in slide-in-from-top-2 duration-150 z-50"
              >
                {siteConfig.featureSubnav.map((item) => (
                  <Link
                    key={item.title}
                    href={item.href}
                    onClick={() => setFeaturesDropdownOpen(false)}
                    className="p-2.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/70 transition-colors block text-left group"
                  >
                    <div className="text-sm font-semibold text-slate-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400">
                      {item.title}
                    </div>
                    <div className="text-xs text-slate-500 dark:text-slate-400 line-clamp-1 mt-0.5">
                      {item.desc}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>

          <Link
            href="/how-it-works"
            className="px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600"
          >
            How It Works
          </Link>
          <Link
            href="/features/document-scanner"
            className="px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600"
          >
            Scanner
          </Link>
          <Link
            href="/security"
            className="px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600"
          >
            Security
          </Link>
          <Link
            href="/compatibility"
            className="px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600"
          >
            Compatibility
          </Link>
          <Link
            href="/faq"
            className="px-3 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 rounded-lg transition-colors focus-visible:outline-2 focus-visible:outline-blue-600"
          >
            FAQ
          </Link>
        </nav>

        {/* Action CTAs & Theme Toggle */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Theme switcher button */}
          <button
            type="button"
            onClick={toggleTheme}
            className="p-2 rounded-xl text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors focus-visible:outline-2 focus-visible:outline-blue-600 cursor-pointer"
            aria-label="Toggle light or dark theme"
            title="Toggle theme"
          >
            <Sun className="w-4 h-4 text-amber-400 hidden dark:block" aria-hidden="true" />
            <Moon className="w-4 h-4 text-slate-600 block dark:hidden" aria-hidden="true" />
          </button>

          <div className="hidden sm:flex items-center gap-2.5">
            <Link
              href="/download/android"
              className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-slate-700 dark:text-slate-200 bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 rounded-xl transition-all duration-150 focus-visible:outline-2 focus-visible:outline-blue-600"
            >
              <Smartphone className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" aria-hidden="true" />
              <span>Android App</span>
            </Link>
            <Link
              href="/download/windows"
              className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-sm shadow-blue-500/25 active:scale-[0.98] rounded-xl transition-all duration-150 focus-visible:outline-2 focus-visible:outline-blue-600"
            >
              <Download className="w-3.5 h-3.5" aria-hidden="true" />
              <span>Download for Windows</span>
            </Link>
          </div>

          {/* Mobile menu trigger */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 rounded-xl text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 focus-visible:outline-2 focus-visible:outline-blue-600 cursor-pointer"
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
            <div>
              <button
                onClick={() => setMobileFeaturesOpen(!mobileFeaturesOpen)}
                className="w-full flex items-center justify-between px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
                aria-expanded={mobileFeaturesOpen}
              >
                <span>Features</span>
                <ChevronDown className={`w-4 h-4 transition-transform duration-200 ${mobileFeaturesOpen ? "rotate-180" : ""}`} />
              </button>
              {mobileFeaturesOpen && (
                <div className="pl-4 pr-2 py-1 space-y-1 bg-slate-50/70 dark:bg-slate-900/50 rounded-xl mt-1">
                  {siteConfig.featureSubnav.map((item) => (
                    <Link
                      key={item.title}
                      href={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className="block px-3 py-2 text-sm text-slate-600 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400"
                    >
                      {item.title}
                    </Link>
                  ))}
                </div>
              )}
            </div>

            <Link
              href="/how-it-works"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              How It Works
            </Link>
            <Link
              href="/features/document-scanner"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Scanner
            </Link>
            <Link
              href="/security"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Security
            </Link>
            <Link
              href="/compatibility"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              Compatibility
            </Link>
            <Link
              href="/faq"
              onClick={() => setMobileMenuOpen(false)}
              className="px-3 py-2 rounded-lg text-base font-medium text-slate-800 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-900"
            >
              FAQ
            </Link>
          </div>

          <div className="pt-3 border-t border-slate-100 dark:border-slate-800 grid gap-2">
            <Link
              href="/download/windows"
              onClick={() => setMobileMenuOpen(false)}
              className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-500/20"
            >
              <Download className="w-4 h-4" />
              <span>Download for Windows</span>
            </Link>
            <Link
              href="/download/android"
              onClick={() => setMobileMenuOpen(false)}
              className="w-full flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-sm font-semibold text-slate-800 dark:text-slate-200 bg-slate-100 dark:bg-slate-800"
            >
              <Smartphone className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
              <span>Download Android App</span>
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
