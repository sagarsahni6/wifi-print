'use client';

import { useState } from 'react';
import Link from 'next/link';

export default function ContactPage() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    category: 'General Inquiry',
    subject: '',
    message: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    if (errorMessage) setErrorMessage('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Simple validation
    if (!formData.name.trim() || !formData.email.trim() || !formData.subject.trim() || !formData.message.trim()) {
      setErrorMessage('Please fill out all fields before sending.');
      return;
    }

    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      setErrorMessage('Please enter a valid email address.');
      return;
    }

    setIsSubmitting(true);

    const accessKey = process.env.NEXT_PUBLIC_WEB3FORMS_ACCESS_KEY || "f0ee81fe-1120-4b75-afe8-eb185729bfda";

    if (!accessKey) {
      // Fallback demo submission when no API key is configured
      setTimeout(() => {
        setIsSubmitting(false);
        setIsSubmitted(true);
        setFormData({
          name: '',
          email: '',
          category: 'General Inquiry',
          subject: '',
          message: '',
        });
      }, 1200);
      return;
    }

    try {
      const response = await fetch('https://api.web3forms.com/submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({
          access_key: accessKey,
          name: formData.name,
          email: formData.email,
          subject: `[WiFi Print Support] ${formData.subject} (${formData.category})`,
          message: formData.message,
          from_name: 'WiFi Print Support',
        }),
      });

      const data = await response.json();

      if (data.success) {
        setIsSubmitted(true);
        setFormData({
          name: '',
          email: '',
          category: 'General Inquiry',
          subject: '',
          message: '',
        });
      } else {
        setErrorMessage(data.message || 'Failed to send message. Please try again.');
      }
    } catch (error) {
      setErrorMessage('Network error: Could not reach the email server. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main>
      {/* Hero Header */}
      <header className="hero" style={{ paddingBottom: '3rem' }}>
        <div className="container" style={{ textAlign: 'center', zIndex: 10, position: 'relative' }}>
          <div className="badge">Get in Touch</div>
          <h1 style={{ marginBottom: '1rem', fontSize: '3.5rem' }}>Support &amp; Feedback</h1>
          <p style={{ maxWidth: '650px', margin: '0 auto 1.5rem', fontSize: '1.2rem', color: 'var(--text-secondary)' }}>
            Have a question, connection issue, or a feature suggestion? Let us know! Our local-printing utility team is here to help.
          </p>
        </div>
        <div className="glow glow-1"></div>
        <div className="glow glow-2"></div>
      </header>

      {/* Main Content Section */}
      <section className="section bg-darker" style={{ padding: '4rem 0 6rem' }}>
        <div className="container contact-grid">
          
          {/* Left Column: Contact details & Troubleshooting checklist */}
          <aside className="contact-sidebar">
            
            {/* Status Widget */}
            <div className="glass-panel contact-sidebar-card" style={{ marginBottom: '2rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.25rem' }}>
                <span className="pulsing-dot"></span>
                <span style={{ fontSize: '0.85rem', fontWeight: 700, letterSpacing: '0.05em', textTransform: 'uppercase', color: '#10b981' }}>
                  Local Pairing Services
                </span>
              </div>
              <h3 style={{ fontSize: '1.4rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>100% Operational</h3>
              <p style={{ fontSize: '0.95rem', margin: 0, color: 'var(--text-secondary)' }}>
                Direct peer-to-peer PIN discovery and HTTPS secure printing channels are fully functional on local networks.
              </p>
            </div>

            {/* Support Details */}
            <div className="glass-panel contact-sidebar-card" style={{ marginBottom: '2rem' }}>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>Direct Channels</h3>
              <p style={{ fontSize: '1rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                For developer collaboration, API access requests, or business inquiries, feel free to contact us at:
              </p>
              <div style={{ background: 'var(--bg-darker)', border: '1px solid var(--border-color)', borderRadius: '10px', padding: '0.75rem 1rem', fontSize: '0.95rem', color: 'var(--text-primary)', fontWeight: 600 }}>
                📧 support@calclabz.com
              </div>
            </div>

            {/* Troubleshooting Checklist */}
            <div className="glass-panel contact-sidebar-card">
              <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>Common Connection Tips</h3>
              <p style={{ fontSize: '0.95rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                Having trouble pairing your Android phone with your PC? Try these steps before writing:
              </p>
              <ul className="troubleshoot-list" style={{ paddingLeft: '1.2rem', lineHeight: '1.7', fontSize: '0.925rem', color: 'var(--text-secondary)' }}>
                <li><strong>Same Router:</strong> Confirm both phone and PC are connected to the same Wi-Fi SSID (not a Guest network).</li>
                <li><strong>No VPNs:</strong> Disable active VPNs or secure shields on your phone as they isolate network routing.</li>
                <li><strong>Windows Firewall:</strong> Allow the <code>WiFiPrintServer.exe</code> executable through Windows Firewall.</li>
                <li><strong>Subnet Check:</strong> Ensure your router does not block local peer-to-peer AP isolation.</li>
              </ul>
            </div>

          </aside>

          {/* Right Column: Contact Form */}
          <div className="glass-panel contact-form-card" style={{ transform: 'none' }}>
            
            {!isSubmitted ? (
              <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                
                <div>
                  <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>Send a Message</h2>
                  <p style={{ fontSize: '0.95rem', color: 'var(--text-secondary)', margin: 0 }}>
                    We usually respond to bug reports and feature requests within 24-48 hours.
                  </p>
                </div>

                {!(process.env.NEXT_PUBLIC_WEB3FORMS_ACCESS_KEY || "f0ee81fe-1120-4b75-afe8-eb185729bfda") && (
                  <div style={{ background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)', color: '#f59e0b', padding: '0.75rem 1rem', borderRadius: '8px', fontSize: '0.875rem', lineHeight: '1.5' }}>
                    ⚠️ <strong>Demo Mode:</strong> The email server is not connected. To receive real submissions in your inbox, generate a free access key at <a href="https://web3forms.com" target="_blank" rel="noopener noreferrer" style={{ color: '#818cf8', textDecoration: 'underline', fontWeight: 600 }}>Web3Forms</a> and add it as <code>NEXT_PUBLIC_WEB3FORMS_ACCESS_KEY</code> in your environment variables.
                  </div>
                )}

                {errorMessage && (
                  <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', color: '#ef4444', padding: '0.75rem 1rem', borderRadius: '8px', fontSize: '0.95rem', fontWeight: 500 }}>
                    ⚠️ {errorMessage}
                  </div>
                )}

                {/* Grid Inputs */}
                <div className="contact-form-row">
                  <div className="contact-form-group">
                    <label htmlFor="name">Your Name</label>
                    <input
                      type="text"
                      id="name"
                      name="name"
                      className="contact-input"
                      placeholder="e.g., Alex Johnson"
                      value={formData.name}
                      onChange={handleInputChange}
                      disabled={isSubmitting}
                      required
                    />
                  </div>
                  <div className="contact-form-group">
                    <label htmlFor="email">Your Email</label>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      className="contact-input"
                      placeholder="e.g., alex@company.com"
                      value={formData.email}
                      onChange={handleInputChange}
                      disabled={isSubmitting}
                      required
                    />
                  </div>
                </div>

                {/* Dropdown & Subject */}
                <div className="contact-form-row">
                  <div className="contact-form-group">
                    <label htmlFor="category">Inquiry Category</label>
                    <select
                      id="category"
                      name="category"
                      className="contact-select"
                      value={formData.category}
                      onChange={handleInputChange}
                      disabled={isSubmitting}
                    >
                      <option value="General Inquiry">General Inquiry</option>
                      <option value="Bug Report / Connection issue">Bug Report / Connection issue</option>
                      <option value="Feature Suggestion">Feature Suggestion</option>
                      <option value="Business Cooperation">Business Cooperation</option>
                    </select>
                  </div>
                  <div className="contact-form-group">
                    <label htmlFor="subject">Subject</label>
                    <input
                      type="text"
                      id="subject"
                      name="subject"
                      className="contact-input"
                      placeholder="What is this regarding?"
                      value={formData.subject}
                      onChange={handleInputChange}
                      disabled={isSubmitting}
                      required
                    />
                  </div>
                </div>

                {/* Message */}
                <div className="contact-form-group">
                  <label htmlFor="message">Message</label>
                  <textarea
                    id="message"
                    name="message"
                    rows={6}
                    className="contact-textarea"
                    placeholder="Describe your issue or feedback in detail..."
                    value={formData.message}
                    onChange={handleInputChange}
                    disabled={isSubmitting}
                    required
                  ></textarea>
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  className={`btn btn-primary w-full ${isSubmitting ? 'submitting-btn' : ''}`}
                  disabled={isSubmitting}
                  style={{ display: 'inline-flex', padding: '1rem', marginTop: '0.5rem' }}
                >
                  {isSubmitting ? (
                    <>
                      <span className="spinner"></span>
                      Sending Message...
                    </>
                  ) : (
                    <>
                      <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ marginRight: '0.25rem' }}>
                        <line x1="22" y1="2" x2="11" y2="13"></line>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                      </svg>
                      Send Message
                    </>
                  )}
                </button>

              </form>
            ) : (
              <div className="success-message-container fade-in active" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '2rem 1rem' }}>
                <div className="success-checkmark-outer">
                  <div className="success-checkmark-inner">
                    <svg viewBox="0 0 24 24" width="48" height="48" stroke="#10b981" strokeWidth="3" fill="none" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </div>
                </div>
                <h2 style={{ fontSize: '2.25rem', marginBottom: '0.75rem', color: 'var(--text-primary)', fontFamily: 'var(--font-heading)' }}>
                  Message Sent!
                </h2>
                <p style={{ color: 'var(--text-secondary)', maxWidth: '400px', fontSize: '1.05rem', lineHeight: '1.6', marginBottom: '2rem' }}>
                  Thank you for reaching out. We have successfully received your inquiry and our support team will get back to you shortly.
                </p>
                <button
                  onClick={() => setIsSubmitted(false)}
                  className="btn btn-secondary"
                  style={{ padding: '0.75rem 1.5rem' }}
                >
                  Send Another Message
                </button>
              </div>
            )}

          </div>
        </div>
      </section>
    </main>
  );
}
