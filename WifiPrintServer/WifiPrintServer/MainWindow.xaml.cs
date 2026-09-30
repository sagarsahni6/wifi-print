using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer;

public partial class MainWindow : Window
{
    private readonly DispatcherTimer _refreshTimer;
    private readonly DispatcherTimer _eventWiringTimer;
    private readonly DispatcherTimer _cleanupTimer;
    private readonly DispatcherTimer _pinRefreshTimer;
    private readonly Dictionary<string, Grid> _pages = new();
    private readonly Queue<PendingApproval> _pendingApprovals = new();
    private PendingApproval? _currentApproval;
    private bool _eventsWired = false;
    private int _logLineCount = 0;
    private const int MaxLogLines = 500;

    public MainWindow()
    {
        InitializeComponent();

        // Map nav tags to page grids
        _pages["Dashboard"] = DashboardPage;
        _pages["Jobs"] = JobsPage;
        _pages["Printers"] = PrintersPage;
        _pages["Devices"] = DevicesPage;
        _pages["Logs"] = LogsPage;
        _pages["Settings"] = SettingsPage;

        // Initialize settings UI
        PortInput.Text = Program.Settings.ServerPort.ToString();
        ServerNameInput.Text = Program.Settings.ServerName;
        AutoStartCheck.IsChecked = Program.Settings.AutoStart;
        MinimizeToTrayCheck.IsChecked = Program.Settings.MinimizeToTray;
        AutoApproveSameNetworkCheck.IsChecked = Program.Settings.AutoApproveSameNetwork;
        RequireQrCodeOutsideLocalNetworkCheck.IsChecked = Program.Settings.RequireQrCodeOutsideLocalNetwork;

        // Display server IP & PIN
        var ip = DiscoveryService.GetLocalIpAddress();
        IpText.Text = $"IP: {ip}";
        PortText.Text = $"Port: {Program.Settings.ServerPort}";
        UpdatePinDisplay();

        // Auto-refresh PIN every 5 minutes (QR stays fixed, PIN refreshes)
        _pinRefreshTimer = new DispatcherTimer { Interval = TimeSpan.FromMinutes(5) };
        _pinRefreshTimer.Tick += (s, e) => RotatePin();
        _pinRefreshTimer.Start();

        // Auto-refresh timer for dashboard stats
        _refreshTimer = new DispatcherTimer { Interval = TimeSpan.FromSeconds(3) };
        _refreshTimer.Tick += (s, e) => RefreshDashboard();
        _refreshTimer.Start();

        // Reliable event wiring — poll every 500ms until services are ready
        // This replaces the old flaky Task.Delay(2000) approach
        _eventWiringTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(500) };
        _eventWiringTimer.Tick += TryWireEvents;
        _eventWiringTimer.Start();

        // Cleanup old uploaded files every hour
        _cleanupTimer = new DispatcherTimer { Interval = TimeSpan.FromHours(1) };
        _cleanupTimer.Tick += (s, e) =>
        {
            try
            {
                var fileService = Program.WebApp?.Services.GetService<FileProcessingService>();
                fileService?.CleanupOldFiles();
            }
            catch { /* ignore cleanup errors */ }
        };
        _cleanupTimer.Start();

        AppendLog($"[{DateTime.Now:HH:mm:ss}] WiFi Print Server starting...");
        AppendLog($"[{DateTime.Now:HH:mm:ss}] Listening on https://{ip}:{Program.Settings.ServerPort}");
    }

    /// <summary>
    /// Polls until Program.AuthServiceInstance and QueueManager are available,
    /// then wires up all event handlers. Stops polling after success.
    /// </summary>
    private void TryWireEvents(object? sender, EventArgs e)
    {
        if (_eventsWired) { _eventWiringTimer.Stop(); return; }

        if (Program.AuthServiceInstance != null && Program.QueueManager != null)
        {
            // Wire up approval request notification
            Program.AuthServiceInstance.OnApprovalRequested += approval =>
            {
                Dispatcher.BeginInvoke(() => ShowApprovalNotification(approval));
            };

            Program.AuthServiceInstance.OnDevicePaired += device =>
            {
                Dispatcher.BeginInvoke(() =>
                {
                    AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Device connected: {device.Name}");
                    App.ShowTrayNotification("Device Connected", $"{device.Name} paired & connected successfully.", System.Windows.Forms.ToolTipIcon.Info);
                });
            };

            // Wire up job status events
            Program.QueueManager.OnJobStatusChanged += update =>
            {
                Dispatcher.BeginInvoke(() =>
                {
                    AppendLog($"[{update.Timestamp:HH:mm:ss}] Job {update.JobId}: {update.Status} - {update.Message}");
                    if (string.Equals(update.Status, nameof(PrintJobStatus.Completed), StringComparison.OrdinalIgnoreCase))
                    {
                        App.ShowTrayNotification("✓ Print Completed", $"Job {update.JobId} completed successfully", System.Windows.Forms.ToolTipIcon.Info);
                    }
                    else if (string.Equals(update.Status, nameof(PrintJobStatus.Failed), StringComparison.OrdinalIgnoreCase))
                    {
                        App.ShowTrayNotification("✗ Print Failed", $"Job {update.JobId}: {update.Message}", System.Windows.Forms.ToolTipIcon.Error);
                    }
                });
            };

            _eventsWired = true;
            _eventWiringTimer.Stop();
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Server ready — all services connected");

            // Generate the connection QR code for the dashboard
            GenerateConnectionQrCode();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Device Approval Notifications
    // ═══════════════════════════════════════════════════════════════

    /// <summary>
    /// Shows the approval banner when a phone requests connection.
    /// Uses a queue so concurrent requests don't overwrite each other (BUG-2 fix).
    /// </summary>
    private void ShowApprovalNotification(PendingApproval approval)
    {
        _pendingApprovals.Enqueue(approval);
        if (_currentApproval == null)
        {
            DisplayNextApproval();
        }
        else
        {
            UpdateApprovalCounter();
        }
    }

    private void DisplayNextApproval()
    {
        while (_pendingApprovals.Count > 0)
        {
            var next = _pendingApprovals.Dequeue();
            if (!next.CompletionSource.Task.IsCompleted)
            {
                _currentApproval = next;
                ApprovalDeviceName.Text = $"📱 {next.DeviceName} ({next.DeviceModel})";
                ApprovalDeviceIp.Text = $"IP: {next.IpAddress}" + (_pendingApprovals.Count > 0 ? $"  (+{_pendingApprovals.Count} more queued)" : "");
                ApprovalBanner.Visibility = Visibility.Visible;
                ApprovalIdle.Visibility = Visibility.Collapsed;

                // Bring window to front
                Show();
                WindowState = WindowState.Normal;
                Activate();
                Topmost = true;
                Topmost = false;

                // Play notification sound & show system tray notification
                System.Media.SystemSounds.Asterisk.Play();
                App.ShowTrayNotification("📲 Connection Request", $"{next.DeviceName} ({next.DeviceModel}) wants to connect", System.Windows.Forms.ToolTipIcon.Info);

                AppendLog($"[{DateTime.Now:HH:mm:ss}] 📲 Connection request from: {next.DeviceName} ({next.IpAddress})");
                return;
            }
        }

        HideApprovalNotification();
    }

    private void UpdateApprovalCounter()
    {
        if (_currentApproval != null)
        {
            ApprovalDeviceIp.Text = $"IP: {_currentApproval.IpAddress}" + (_pendingApprovals.Count > 0 ? $"  (+{_pendingApprovals.Count} more queued)" : "");
        }
    }

    private void HideApprovalNotification()
    {
        ApprovalBanner.Visibility = Visibility.Collapsed;
        ApprovalIdle.Visibility = Visibility.Visible;
        _currentApproval = null;
    }

    private void ApproveDevice_Click(object sender, RoutedEventArgs e)
    {
        if (_currentApproval != null && Program.AuthServiceInstance != null)
        {
            var approval = _currentApproval;
            _currentApproval = null;
            Program.AuthServiceInstance.ApproveDevice(approval.Id);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Device APPROVED: {approval.DeviceName}");
            DisplayNextApproval();
        }
    }

    private void DenyDevice_Click(object sender, RoutedEventArgs e)
    {
        if (_currentApproval != null && Program.AuthServiceInstance != null)
        {
            var approval = _currentApproval;
            _currentApproval = null;
            Program.AuthServiceInstance.DenyDevice(approval.Id);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✗ Device DENIED: {approval.DeviceName}");
            DisplayNextApproval();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Navigation
    // ═══════════════════════════════════════════════════════════════

    private void Nav_Checked(object sender, RoutedEventArgs e)
    {
        if (sender is RadioButton rb && rb.Tag is string tag)
        {
            foreach (var (key, page) in _pages)
                page.Visibility = key == tag ? Visibility.Visible : Visibility.Collapsed;

            switch (tag)
            {
                case "Jobs": RefreshJobsList(); break;
                case "Printers": RefreshPrintersList(); break;
                case "Devices": RefreshDevicesList(); break;
                case "Settings": RefreshPrinterCombo(); break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Dashboard
    // ═══════════════════════════════════════════════════════════════

    private void RefreshDashboard()
    {
        try
        {
            if (Program.QueueManager == null) return;

            var jobs = Program.QueueManager.GetAllJobs();
            int active = jobs.Count(j => j.Status is PrintJobStatus.Printing or PrintJobStatus.Pending
                or PrintJobStatus.Queued or PrintJobStatus.Validating or PrintJobStatus.Converting);
            int completed = jobs.Count(j => j.Status == PrintJobStatus.Completed);

            ActiveJobsCount.Text = active.ToString();
            CompletedCount.Text = completed.ToString();

            if (Program.PrinterServiceInstance != null)
                PrinterCount.Text = Program.PrinterServiceInstance.GetAllPrinters().Count.ToString();

            if (Program.AuthServiceInstance != null)
                DeviceCount.Text = Program.AuthServiceInstance.GetPairedDevices().Count.ToString();

            RecentJobsList.ItemsSource = jobs.Take(10).ToList();
            RecentJobsEmptyState.Visibility = jobs.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        }
        catch { /* ignore refresh errors */ }
    }

    private void RefreshJobsList()
    {
        if (Program.QueueManager == null) return;
        var jobs = Program.QueueManager.GetAllJobs();
        JobsList.ItemsSource = jobs;
        JobsEmptyState.Visibility = jobs.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
    }

    private void RefreshPrintersList()
    {
        if (Program.PrinterServiceInstance == null) return;
        PrintersList.ItemsSource = Program.PrinterServiceInstance.GetAllPrinters();
    }

    private void RefreshDevicesList()
    {
        if (Program.AuthServiceInstance == null) return;
        var devices = Program.AuthServiceInstance.GetPairedDevices();
        DevicesList.ItemsSource = devices;
        DevicesEmptyState.Visibility = devices.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
    }

    private void RefreshJobs_Click(object sender, RoutedEventArgs e)
    {
        RefreshJobsList();
        RefreshDashboard();
    }

    private void ClearCompletedJobs_Click(object sender, RoutedEventArgs e)
    {
        if (Program.QueueManager == null) return;
        int cleared = Program.QueueManager.ClearCompletedJobs();
        RefreshJobsList();
        RefreshDashboard();
        AppendLog($"[{DateTime.Now:HH:mm:ss}] 🧹 Cleared {cleared} completed/cancelled jobs from queue");
    }

    private void RefreshPrinters_Click(object sender, RoutedEventArgs e)
    {
        RefreshPrintersList();
        RefreshDashboard();
        AppendLog($"[{DateTime.Now:HH:mm:ss}] 🔄 Printers list refreshed");
    }

    private void RefreshDevices_Click(object sender, RoutedEventArgs e)
    {
        RefreshDevicesList();
        RefreshDashboard();
        AppendLog($"[{DateTime.Now:HH:mm:ss}] 🔄 Devices list refreshed");
    }

    private void CopyLogs_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            Clipboard.SetText(LogsTextBox.Text);
            App.ShowTrayNotification("Logs Copied", "Server log text copied to clipboard.", System.Windows.Forms.ToolTipIcon.Info);
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to copy logs: {ex.Message}");
        }
    }

    private void ClearLogs_Click(object sender, RoutedEventArgs e)
    {
        LogsTextBox.Text = $"[{DateTime.Now:HH:mm:ss}] Logs cleared.\n";
        _logLineCount = 1;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Drag & Drop File Printing
    // ═══════════════════════════════════════════════════════════════

    private void Window_DragOver(object sender, DragEventArgs e)
    {
        if (e.Data.GetDataPresent(DataFormats.FileDrop))
        {
            e.Effects = DragDropEffects.Copy;
        }
        else
        {
            e.Effects = DragDropEffects.None;
        }
        e.Handled = true;
    }

    private void Window_Drop(object sender, DragEventArgs e)
    {
        if (!e.Data.GetDataPresent(DataFormats.FileDrop)) return;

        var files = (string[]?)e.Data.GetData(DataFormats.FileDrop);
        if (files == null || files.Length == 0 || Program.QueueManager == null) return;

        var defaultPrinter = Program.PrinterServiceInstance?.GetDefaultPrinter()?.Name
            ?? Program.PrinterServiceInstance?.GetAllPrinters().FirstOrDefault()?.Name;

        if (string.IsNullOrEmpty(defaultPrinter))
        {
            MessageBox.Show("No printer available on this system to print the dropped file(s).",
                "No Printers Found", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        foreach (var file in files)
        {
            if (!File.Exists(file)) continue;

            var ext = Path.GetExtension(file).ToLowerInvariant();
            var job = new PrintJob
            {
                FilePath = file,
                OriginalFileName = Path.GetFileName(file),
                FileSize = new FileInfo(file).Length,
                FileType = ext switch
                {
                    ".pdf" => "PDF",
                    ".jpg" or ".jpeg" or ".png" or ".bmp" or ".gif" => "Image",
                    ".txt" or ".log" or ".csv" or ".cs" or ".json" => "Text",
                    _ => "Unknown"
                },
                PrinterName = defaultPrinter,
                DeviceId = "LocalPC",
                Settings = new PrintSettings { Copies = 1 }
            };

            Program.QueueManager.EnqueueJob(job);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 📄 Drag & Drop print job: {job.OriginalFileName} -> {defaultPrinter}");
        }

        RefreshJobsList();
        RefreshDashboard();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Device Management (Block / Unblock / Remove)
    // ═══════════════════════════════════════════════════════════════

    private void BlockDevice_Click(object sender, RoutedEventArgs e)
    {
        if (sender is System.Windows.Controls.Button btn && btn.Tag is string deviceId)
        {
            Program.AuthServiceInstance?.BlockDevice(deviceId);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🚫 Device blocked: {deviceId}");
            RefreshDevicesList();
        }
    }

    private void UnblockDevice_Click(object sender, RoutedEventArgs e)
    {
        if (sender is System.Windows.Controls.Button btn && btn.Tag is string deviceId)
        {
            Program.AuthServiceInstance?.UnblockDevice(deviceId);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🔓 Device unblocked: {deviceId}");
            RefreshDevicesList();
        }
    }

    private void RemoveDevice_Click(object sender, RoutedEventArgs e)
    {
        if (sender is System.Windows.Controls.Button btn && btn.Tag is string deviceId)
        {
            var result = MessageBox.Show("Remove this device? It will need to reconnect.",
                "Confirm", MessageBoxButton.YesNo, MessageBoxImage.Warning);
            if (result == MessageBoxResult.Yes)
            {
                Program.AuthServiceInstance?.RemoveDevice(deviceId);
                AppendLog($"[{DateTime.Now:HH:mm:ss}] 🗑️ Device removed: {deviceId}");
                RefreshDevicesList();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Settings
    // ═══════════════════════════════════════════════════════════════

    private async void SaveSettings_Click(object sender, RoutedEventArgs e)
    {
        if (int.TryParse(PortInput.Text, out int port))
            Program.Settings.ServerPort = port;
        Program.Settings.ServerName = ServerNameInput.Text;
        Program.Settings.AutoStart = AutoStartCheck.IsChecked == true;
        Program.Settings.MinimizeToTray = MinimizeToTrayCheck.IsChecked == true;
        Program.Settings.AutoApproveSameNetwork = AutoApproveSameNetworkCheck.IsChecked == true;
        Program.Settings.RequireQrCodeOutsideLocalNetwork = RequireQrCodeOutsideLocalNetworkCheck.IsChecked == true;

        // Save default printer selection
        if (DefaultPrinterCombo.SelectedItem is string selectedPrinter && !string.IsNullOrEmpty(selectedPrinter))
        {
            if (Program.PrinterServiceInstance != null)
                await Program.PrinterServiceInstance.SetDefaultPrinterAsync(selectedPrinter);
        }

        Program.Settings.Save();
        SetAutoStart(Program.Settings.AutoStart);

        MessageBox.Show("Settings saved. Some changes require a restart.",
            "Settings", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    private void RegenerateQrToken_Click(object sender, RoutedEventArgs e)
    {
        Program.Settings.CurrentQrPairingToken = AppSettings.GenerateSecureToken(16);
        Program.Settings.Save();
        GenerateConnectionQrCode();
        AppendLog($"[{DateTime.Now:HH:mm:ss}] 🔄 Generated new QR pairing token");
        MessageBox.Show("A new secure QR token has been generated and updated on the dashboard.",
            "QR Token Rotated", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    private void CopyUrl_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            Clipboard.SetText($"https://{ip}:{Program.Settings.ServerPort}");
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 📋 Server address copied to clipboard: https://{ip}:{Program.Settings.ServerPort}");
            App.ShowTrayNotification("Copied to Clipboard", $"https://{ip}:{Program.Settings.ServerPort}", System.Windows.Forms.ToolTipIcon.Info);
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to copy URL: {ex.Message}");
        }
    }

    /// <summary>
    /// Populates the default printer combo box when Settings page is shown.
    /// </summary>
    private void RefreshPrinterCombo()
    {
        if (Program.PrinterServiceInstance == null) return;

        var printers = Program.PrinterServiceInstance.GetAllPrinters();
        DefaultPrinterCombo.ItemsSource = printers.Select(p => p.Name).ToList();

        // Select the current default
        var currentDefault = printers.FirstOrDefault(p => p.IsDefault);
        if (currentDefault != null)
            DefaultPrinterCombo.SelectedItem = currentDefault.Name;
    }

    private void SetAutoStart(bool enable)
    {
        try
        {
            var key = Microsoft.Win32.Registry.CurrentUser.OpenSubKey(
                @"SOFTWARE\Microsoft\Windows\CurrentVersion\Run", true);
            if (enable)
                key?.SetValue("WifiPrintServer",
                    $"\"{Path.Combine(System.AppContext.BaseDirectory, "WifiPrintServer.exe")}\"");
            else
                key?.DeleteValue("WifiPrintServer", false);
        }
        catch (Exception ex) { AppendLog($"Auto-start error: {ex.Message}"); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  QR Code Generation
    // ═══════════════════════════════════════════════════════════════

    /// <summary>
    /// Generates and displays the connection QR code on the dashboard.
    /// The QR encodes: { ip, port, name, cert } so phones can connect instantly.
    /// </summary>
    private void GenerateConnectionQrCode()
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            var port = Program.Settings.ServerPort;
            var name = Program.Settings.ServerName;
            var cert = Program.ServerCertificate;

            var qrImage = QrCodeService.GenerateConnectionQrCode(ip, port, name, cert, Program.Settings.CurrentQrPairingToken);
            QrCodeImage.Source = qrImage;
            QrInfoText.Text = $"{ip}:{port}";

            AppendLog($"[{DateTime.Now:HH:mm:ss}] 📷 QR code ready — scan from your phone to connect");
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ QR code generation failed: {ex.Message}");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Rotating PIN for Cross-Network Authentication
    //  QR code stays fixed, while PIN refreshes periodically or on-demand
    // ═══════════════════════════════════════════════════════════════

    private void UpdatePinDisplay()
    {
        var pin = Program.Settings.CurrentConnectionPin;
        if (!string.IsNullOrEmpty(pin) && pin.Length == 6)
        {
            PinDisplay.Text = $"{pin.Substring(0, 3)} {pin.Substring(3, 3)}";
        }
        else
        {
            PinDisplay.Text = pin ?? "------";
        }
    }

    private void RotatePin()
    {
        Program.Settings.CurrentConnectionPin = AppSettings.GeneratePin();
        Program.Settings.Save();
        UpdatePinDisplay();
        AppendLog($"[{DateTime.Now:HH:mm:ss}] 🔄 Connection PIN refreshed: {PinDisplay.Text}");
    }

    private void RefreshPin_Click(object sender, RoutedEventArgs e)
    {
        RotatePin();
        _pinRefreshTimer.Stop();
        _pinRefreshTimer.Start();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Cross-Promotion: Download Android App
    // ═══════════════════════════════════════════════════════════════

    private void DownloadApp_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = "https://github.com/sagarsahni6/wifi-print/releases",
                UseShellExecute = true
            });
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open browser: {ex.Message}");
        }
    }

    /// <summary>
    /// Appends a log line to the on-screen log, rotating out old entries
    /// to prevent unbounded memory growth.
    /// </summary>
    private void AppendLog(string message)
    {
        _logLineCount++;

        // Rotate: keep only the last MaxLogLines to prevent unbounded memory growth
        if (_logLineCount > MaxLogLines + 100)
        {
            var lines = LogsTextBox.Text.Split('\n');
            if (lines.Length > MaxLogLines)
            {
                LogsTextBox.Text = string.Join("\n", lines.Skip(lines.Length - MaxLogLines));
                _logLineCount = MaxLogLines;
            }
        }

        LogsTextBox.Text += message + "\n";
        LogsTextBox.ScrollToEnd();
    }

    protected override void OnClosing(System.ComponentModel.CancelEventArgs e)
    {
        if (Program.Settings.MinimizeToTray)
        {
            e.Cancel = true;
            Hide();
        }
        else
        {
            base.OnClosing(e);
            Application.Current.Shutdown();
        }
    }

    protected override void OnStateChanged(EventArgs e)
    {
        if (WindowState == WindowState.Minimized && Program.Settings.MinimizeToTray)
            Hide();
        base.OnStateChanged(e);
    }
}