'use client';

import { useState } from 'react';
import ThemeToggle from './ThemeToggle';

export default function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <nav className="navbar" aria-label="Main navigation">
      <div className="container nav-container">
        <a href="#" className="logo" aria-label="WiFi Print Home">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M7 17H5C3.89543 17 3 16.1046 3 15V11C3 9.89543 3.89543 9 5 9H19C20.1046 9 21 9.89543 21 11V15C21 16.1046 20.1046 17 19 17H17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M17 9V5C17 3.89543 16.1046 3 15 3H9C7.89543 3 7 3.89543 7 5V9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            <path d="M7 15H17V21H7V15Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          <span>WiFi Print</span>
        </a>

        {/* Mobile menu toggle */}
        <button 
          className={`mobile-menu-btn ${isMenuOpen ? 'open' : ''}`} 
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          aria-label={isMenuOpen ? 'Close menu' : 'Open menu'}
          aria-expanded={isMenuOpen}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div className={`nav-links ${isMenuOpen ? 'active' : ''}`}>
          <a href="#how-it-works" onClick={() => setIsMenuOpen(false)}>How it Works</a>
          <a href="#scanner" onClick={() => setIsMenuOpen(false)}>Scanner</a>
          <a href="#features" onClick={() => setIsMenuOpen(false)}>Features</a>
          <a href="#printing-guide" onClick={() => setIsMenuOpen(false)}>Guide</a>
          <a href="#faq" onClick={() => setIsMenuOpen(false)}>FAQ</a>
          <ThemeToggle />
          <a href="#download" className="btn btn-primary btn-small" onClick={() => setIsMenuOpen(false)}>Download</a>
        </div>
      </div>
    </nav>
  );
}
