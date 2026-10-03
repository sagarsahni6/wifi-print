using System.Net;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

/// <summary>
/// Web Admin Dashboard — provides an in-browser management UI accessible from any PC or mobile device on the LAN.
/// </summary>
[ApiController]
public class AdminController : ControllerBase
{
    private readonly AppSettings _settings;
    private readonly PrinterService _printerService;
    private readonly PrintQueueManager _queueManager;
    private readonly AuthService _authService;

    public AdminController(
        AppSettings settings,
        PrinterService printerService,
        PrintQueueManager queueManager,
        AuthService authService)
    {
        _settings = settings;
        _printerService = printerService;
        _queueManager = queueManager;
        _authService = authService;
    }

    [HttpGet("/api/admin/overview")]
    public IActionResult GetOverview()
    {
        var printers = _printerService.GetAllPrinters();
        var jobs = _queueManager.GetAllJobs().Take(30).ToList();
        var devices = _authService.GetPairedDevices();

        return Ok(new
        {
            success = true,
            serverName = _settings.ServerName,
            port = _settings.ServerPort,
            printers = printers,
            jobs = jobs,
            devices = devices
        });
    }

    [HttpGet("/")]
    [HttpGet("/admin")]
    [Produces("text/html")]
    public ContentResult Index()
    {
        var localIp = DiscoveryService.GetLocalIpAddress();
        var serverName = _settings.ServerName;
        var port = _settings.ServerPort;

        var html = $$"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{serverName}} — SpoolDrop Dashboard</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --bg-base: #0B0F19;
            --bg-card: rgba(30, 41, 59, 0.7);
            --bg-card-hover: rgba(51, 65, 85, 0.8);
            --border: rgba(255, 255, 255, 0.08);
            --primary: #6366F1;
            --primary-glow: rgba(99, 102, 241, 0.25);
            --text-main: #F8FAFC;
            --text-sub: #94A3B8;
            --success: #10B981;
            --warning: #F59E0B;
            --danger: #EF4444;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, sans-serif;
        }

        body {
            background-color: var(--bg-base);
            color: var(--text-main);
            min-height: 100vh;
            padding: 24px;
            background-image: 
                radial-gradient(circle at 15% 15%, rgba(99, 102, 241, 0.12) 0%, transparent 40%),
                radial-gradient(circle at 85% 85%, rgba(16, 185, 129, 0.08) 0%, transparent 40%);
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 32px;
            padding-bottom: 20px;
            border-bottom: 1px solid var(--border);
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .brand-logo {
            width: 44px;
            height: 44px;
            background: linear-gradient(135deg, #6366F1, #8B5CF6);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 22px;
            box-shadow: 0 4px 16px var(--primary-glow);
        }

        .brand-title {
            font-size: 22px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }

        .brand-subtitle {
            font-size: 13px;
            color: var(--text-sub);
        }

        .status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background: rgba(16, 185, 129, 0.12);
            color: var(--success);
            padding: 8px 16px;
            border-radius: 9999px;
            font-size: 13px;
            font-weight: 600;
            border: 1px solid rgba(16, 185, 129, 0.25);
        }

        .pulse-dot {
            width: 8px;
            height: 8px;
            background-color: var(--success);
            border-radius: 50%;
            box-shadow: 0 0 10px var(--success);
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.4; transform: scale(1.2); }
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 16px;
            margin-bottom: 32px;
        }

        .stat-card {
            background: var(--bg-card);
            backdrop-filter: blur(12px);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 20px;
            transition: transform 0.2s, background 0.2s;
        }

        .stat-card:hover {
            transform: translateY(-2px);
            background: var(--bg-card-hover);
        }

        .stat-label {
            font-size: 13px;
            color: var(--text-sub);
            margin-bottom: 6px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .stat-value {
            font-size: 28px;
            font-weight: 700;
            letter-spacing: -0.5px;
        }

        .section-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 16px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .printers-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 16px;
            margin-bottom: 32px;
        }

        .printer-card {
            background: var(--bg-card);
            backdrop-filter: blur(12px);
            border: 1px solid var(--border);
            border-radius: 16px;
            padding: 20px;
            position: relative;
        }

        .printer-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;
        }

        .printer-name {
            font-size: 16px;
            font-weight: 600;
            color: var(--text-main);
        }

        .badge {
            font-size: 11px;
            padding: 4px 8px;
            border-radius: 6px;
            font-weight: 600;
            text-transform: uppercase;
        }

        .badge-default {
            background: var(--primary-glow);
            color: var(--primary);
            border: 1px solid var(--primary);
        }

        .printer-meta {
            font-size: 12px;
            color: var(--text-sub);
            margin-bottom: 14px;
        }

        .printer-specs {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .spec-tag {
            background: rgba(255, 255, 255, 0.05);
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 12px;
            color: var(--text-sub);
        }

        .table-card {
            background: var(--bg-card);
            backdrop-filter: blur(12px);
            border: 1px solid var(--border);
            border-radius: 16px;
            overflow: hidden;
            margin-bottom: 32px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 13px;
        }

        th {
            background: rgba(255, 255, 255, 0.02);
            color: var(--text-sub);
            padding: 14px 20px;
            font-weight: 600;
            border-bottom: 1px solid var(--border);
        }

        td {
            padding: 14px 20px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.04);
            color: var(--text-main);
        }

        tr:last-child td {
            border-bottom: none;
        }

        .job-status {
            display: inline-flex;
            align-items: center;
            padding: 4px 10px;
            border-radius: 9999px;
            font-size: 11px;
            font-weight: 600;
        }

        .status-completed { background: rgba(16, 185, 129, 0.15); color: #34D399; }
        .status-printing { background: rgba(99, 102, 241, 0.15); color: #818CF8; }
        .status-pending { background: rgba(245, 158, 11, 0.15); color: #FBBF24; }
        .status-failed { background: rgba(239, 68, 68, 0.15); color: #F87171; }

        .connection-card {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.05));
            border: 1px solid rgba(99, 102, 241, 0.25);
            border-radius: 16px;
            padding: 24px;
            margin-bottom: 32px;
        }

        .connection-details {
            max-width: 600px;
        }

        .connection-details h3 {
            font-size: 18px;
            margin-bottom: 6px;
        }

        .connection-details p {
            color: var(--text-sub);
            font-size: 13px;
            margin-bottom: 12px;
        }

        .code-box {
            background: rgba(0, 0, 0, 0.3);
            border: 1px solid rgba(255, 255, 255, 0.08);
            padding: 8px 14px;
            border-radius: 8px;
            font-family: monospace;
            display: inline-block;
            color: #A5B4FC;
            font-size: 13px;
        }

        @media (max-width: 768px) {
            body { padding: 16px; }
            .connection-card { flex-direction: column; gap: 16px; text-align: center; }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <div class="brand">
                <div class="brand-logo">🖨️</div>
                <div>
                    <h1 class="brand-title">{{serverName}}</h1>
                    <p class="brand-subtitle">SpoolDrop Server Management</p>
                </div>
            </div>
            <div class="status-pill">
                <div class="pulse-dot"></div>
                <span>Server Online</span>
            </div>
        </header>

        <!-- Connection Card -->
        <div class="connection-card">
            <div class="connection-details">
                <h3>Connect Android App</h3>
                <p>Ensure your Android device is connected to the same Wi-Fi network. Open the app, and it will automatically discover this server via mDNS. Or manually enter the server endpoint below:</p>
                <div class="code-box">https://{{localIp}}:{{port}}</div>
            </div>
            <div>
                <img src="/api/auth/qr" alt="QR Code" style="width: 110px; height: 110px; border-radius: 12px; border: 2px solid rgba(255,255,255,0.1); background: white; padding: 4px;" onerror="this.style.display='none'">
            </div>
        </div>

        <!-- Stats -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label">⚡ Active Jobs</div>
                <div class="stat-value" id="activeJobs">...</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">✅ Completed Jobs</div>
                <div class="stat-value" id="completedJobs">...</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">🖨️ Connected Printers</div>
                <div class="stat-value" id="printersCount">...</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">📱 Paired Devices</div>
                <div class="stat-value" id="devicesCount">...</div>
            </div>
        </div>

        <!-- Printers Section -->
        <h2 class="section-title">🖨️ Printers</h2>
        <div class="printers-grid" id="printersContainer">
            <div class="stat-card">Loading printers...</div>
        </div>

        <!-- Recent Jobs Section -->
        <h2 class="section-title">📋 Recent Print Jobs</h2>
        <div class="table-card">
            <table>
                <thead>
                    <tr>
                        <th>File Name</th>
                        <th>Printer</th>
                        <th>Device</th>
                        <th>Status</th>
                        <th>Created</th>
                    </tr>
                </thead>
                <tbody id="jobsTableBody">
                    <tr><td colspan="5" style="text-align: center; color: var(--text-sub);">Loading print jobs...</td></tr>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        async function fetchDashboardData() {
            try {
                const res = await fetch('/api/admin/overview');
                if (res.ok) {
                    const data = await res.json();
                    const printers = data.printers || [];
                    const jobs = data.jobs || [];
                    const devices = data.devices || [];

                    document.getElementById('printersCount').textContent = printers.length;
                    document.getElementById('devicesCount').textContent = devices.length;

                    const active = jobs.filter(j => j.status === 'Printing' || j.status === 'Pending' || j.status === 'Queued').length;
                    const completed = jobs.filter(j => j.status === 'Completed').length;
                    document.getElementById('activeJobs').textContent = active;
                    document.getElementById('completedJobs').textContent = completed;

                    renderPrinters(printers);
                    renderJobs(jobs);
                }
            } catch (err) {
                console.error('Dashboard refresh error:', err);
            }
        }

        function renderPrinters(printers) {
            const container = document.getElementById('printersContainer');
            if (!printers.length) {
                container.innerHTML = '<div class="stat-card">No printers found on host machine.</div>';
                return;
            }

            container.innerHTML = printers.map(p => `
                <div class="printer-card">
                    <div class="printer-header">
                        <span class="printer-name">${escapeHtml(p.name)}</span>
                        ${p.isDefault ? '<span class="badge badge-default">Default</span>' : ''}
                    </div>
                    <div class="printer-meta">Status: <strong style="color: ${p.isOnline ? '#34D399' : '#F87171'}">${p.status || (p.isOnline ? 'Ready' : 'Offline')}</strong></div>
                    <div class="printer-specs">
                        <span class="spec-tag">${p.supportsColor ? '🎨 Color' : '⬛ Mono'}</span>
                        <span class="spec-tag">${p.supportsDuplex ? '🔄 Duplex' : '📄 Simplex'}</span>
                        ${p.tonerLevelPercent !== null && p.tonerLevelPercent !== undefined ? `<span class="spec-tag">Toner: ${p.tonerLevelPercent}%</span>` : ''}
                    </div>
                </div>
            `).join('');
        }

        function renderJobs(jobs) {
            const tbody = document.getElementById('jobsTableBody');
            if (!jobs.length) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-sub);">No print jobs recorded yet.</td></tr>';
                return;
            }

            tbody.innerHTML = jobs.map(j => {
                const statusClass = 'status-' + (j.status || '').toLowerCase();
                const date = j.createdAt ? new Date(j.createdAt).toLocaleTimeString() : '-';
                return `
                    <tr>
                        <td><strong>${escapeHtml(j.originalFileName || j.filePath?.split('\\\\').pop() || 'Document')}</strong></td>
                        <td>${escapeHtml(j.printerName || 'Default')}</td>
                        <td>${escapeHtml(j.deviceId || 'Local')}</td>
                        <td><span class="job-status ${statusClass}">${escapeHtml(j.status)}</span></td>
                        <td>${date}</td>
                    </tr>
                `;
            }).join('');
        }

        function escapeHtml(text) {
            if (!text) return '';
            return text.toString()
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        fetchDashboardData();
        setInterval(fetchDashboardData, 3500);
    </script>
</body>
</html>
""";

        return base.Content(html, "text/html", Encoding.UTF8);
    }
}
