using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer;

public partial class MainWindow : Window
{
    private readonly DispatcherTimer _refreshTimer;
    private bool _isWebQrMode = true;
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
        EnableCloudRelayCheck.IsChecked = Program.Settings.EnableCloudRelay;
        CustomPublicUrlInput.Text = Program.Settings.CustomPublicUrl ?? string.Empty;
        SessionDurationInput.Text = Program.Settings.SessionDurationMinutes.ToString();
        MaxPrintsInput.Text = Program.Settings.MaxPrintsPerSession.ToString();
        PrintCooldownInput.Text = Program.Settings.PrintCooldownSeconds.ToString();

        // Display server IP & PIN
        var ip = DiscoveryService.GetLocalIpAddress();
        IpText.Text = $"IP: {ip}";
        PortText.Text = $"Port: {Program.Settings.ServerPort}";
        UpdateSidebarCloudStatus();
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

        AppendLog($"[{DateTime.Now:HH:mm:ss}] Printora Cloud Print Server starting...");
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

            // Wire up cloud relay tunnel events
            if (Program.TunnelServiceInstance != null)
            {
                StartCloudRelayEventWiring();

                if (Program.Settings.EnableCloudRelay && !Program.TunnelServiceInstance.IsActive && !Program.TunnelServiceInstance.IsStarting)
                {
                    StartCloudRelay();
                }
            }

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
        // Show interactive bottom-right desktop toast notification with 1-click Approve/Deny
        if (Program.AuthServiceInstance != null) ApprovalToastWindow.ShowToast(approval, Program.AuthServiceInstance);

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
                var remoteTag = next.ConnectedViaTunnel ? " • ☁ Remote User" : "";
                ApprovalDeviceName.Text = $"📱 {next.DeviceName} ({next.DeviceModel}){remoteTag}";
                ApprovalDeviceIp.Text = $"IP: {next.IpAddress}" + (next.ConnectedViaTunnel ? " [Cloud Relay]" : "") + (_pendingApprovals.Count > 0 ? $"  (+{_pendingApprovals.Count} more queued)" : "");
                ApprovalBanner.Visibility = Visibility.Visible;
                ApprovalIdle.Visibility = Visibility.Collapsed;

                // Do NOT force-open or steal focus with MainWindow; let ApprovalToastWindow handle it non-intrusively
                // If MainWindow is already visible, update its state smoothly
                if (IsVisible && WindowState != WindowState.Minimized)
                {
                    // Already visible on screen
                }

                // Play notification sound & show system tray notification
                System.Media.SystemSounds.Asterisk.Play();
                var trayTitle = next.ConnectedViaTunnel ? "☁ Remote Connection Request" : "📲 Connection Request";
                App.ShowTrayNotification(trayTitle, $"{next.DeviceName} wants to connect to print. Click to open.", System.Windows.Forms.ToolTipIcon.Info);

                AppendLog($"[{DateTime.Now:HH:mm:ss}] 📲 Connection request from: {next.DeviceName} ({next.IpAddress}){(next.ConnectedViaTunnel ? " [Remote Cloud Relay]" : "")}");
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
        if (files != null && files.Length > 0)
        {
            ProcessDroppedFiles(files);
        }
    }

    private void BrowsePrintFile_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var dialog = new Microsoft.Win32.OpenFileDialog
            {
                Title = "Select Documents or Images to Print",
                Filter = "Supported Files (*.pdf;*.jpg;*.jpeg;*.png;*.txt)|*.pdf;*.jpg;*.jpeg;*.png;*.bmp;*.gif;*.txt;*.log;*.csv|All Files (*.*)|*.*",
                Multiselect = true
            };

            if (dialog.ShowDialog() == true && dialog.FileNames.Length > 0)
            {
                ProcessDroppedFiles(dialog.FileNames);
            }
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open file picker: {ex.Message}");
        }
    }

    private void ProcessDroppedFiles(string[] files)
    {
        if (files == null || files.Length == 0 || Program.QueueManager == null) return;

        var defaultPrinter = Program.PrinterServiceInstance?.GetDefaultPrinter()?.Name
            ?? Program.PrinterServiceInstance?.GetAllPrinters().FirstOrDefault()?.Name;

        if (string.IsNullOrEmpty(defaultPrinter))
        {
            MessageBox.Show("No printer available on this system to print the file(s).",
                "No Printers Found", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }

        int count = 0;
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
            AppendLog($"[{DateTime.Now:HH:mm:ss}] Document queued for printing: {job.OriginalFileName} -> {defaultPrinter}");
            count++;
        }

        if (count > 0)
        {
            App.ShowTrayNotification("Document Queued", $"{count} document(s) sent to {defaultPrinter}.", System.Windows.Forms.ToolTipIcon.Info);
        }

        RefreshJobsList();
        RefreshDashboard();
    }

    private void SearchJobs_TextChanged(object sender, TextChangedEventArgs e)
    {
        if (Program.QueueManager == null) return;
        var query = SearchJobsInput?.Text?.Trim().ToLowerInvariant() ?? "";
        var allJobs = Program.QueueManager.GetAllJobs();

        if (string.IsNullOrEmpty(query))
        {
            JobsList.ItemsSource = allJobs;
            JobsEmptyState.Visibility = allJobs.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        }
        else
        {
            var filtered = allJobs.Where(j =>
                (j.OriginalFileName?.ToLowerInvariant().Contains(query) == true) ||
                (j.PrinterName?.ToLowerInvariant().Contains(query) == true) ||
                (j.Status.ToString().ToLowerInvariant().Contains(query) == true) ||
                (j.Id?.ToLowerInvariant().Contains(query) == true)).ToList();
            JobsList.ItemsSource = filtered;
            JobsEmptyState.Visibility = filtered.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        }
    }

    private void OpenUploadsFolder_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var dir = Program.Settings.UploadDirectory;
            if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = dir,
                UseShellExecute = true
            });
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open uploads folder: {ex.Message}");
        }
    }

    private void OpenLogsFolder_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var dir = Program.Settings.LogDirectory;
            if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = dir,
                UseShellExecute = true
            });
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open logs folder: {ex.Message}");
        }
    }

    private void OpenWebPortal_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            var url = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);
            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = url,
                UseShellExecute = true
            });
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🌐 Opened Web Portal: {url}");
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open web portal: {ex.Message}");
        }
    }

    private void UpdateSidebarCloudStatus()
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            if (!string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl))
            {
                if (Uri.TryCreate(Program.Settings.CustomPublicUrl.Trim(), UriKind.Absolute, out var uri))
                {
                    CloudStatusText.Text = $"Cloud: {uri.Host}";
                }
                else
                {
                    CloudStatusText.Text = "Cloud: Active";
                }
                CloudStatusText.ToolTip = Program.Settings.GetWebPrintUrl(Program.Settings.CustomPublicUrl, ip);
                CloudStatusText.Visibility = Visibility.Visible;
            }
            else if (!string.IsNullOrWhiteSpace(tunnelUrl))
            {
                if (Uri.TryCreate(tunnelUrl, UriKind.Absolute, out var uri))
                {
                    CloudStatusText.Text = $"Cloud: {uri.Host}";
                }
                else
                {
                    CloudStatusText.Text = "Cloud: Active";
                }
                CloudStatusText.ToolTip = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);
                CloudStatusText.Visibility = Visibility.Visible;
            }
            else
            {
                CloudStatusText.Visibility = Visibility.Collapsed;
            }
        }
        catch
        {
            CloudStatusText.Visibility = Visibility.Collapsed;
        }
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

        // Save cloud relay setting
        bool wasRelayEnabled = Program.Settings.EnableCloudRelay;
        Program.Settings.EnableCloudRelay = EnableCloudRelayCheck.IsChecked == true;
        Program.Settings.CustomPublicUrl = string.IsNullOrWhiteSpace(CustomPublicUrlInput.Text)
            ? null
            : CustomPublicUrlInput.Text.Trim();

        // Save rate limiting settings
        if (int.TryParse(SessionDurationInput.Text, out int sessionMins))
            Program.Settings.SessionDurationMinutes = Math.Max(0, sessionMins);
        if (int.TryParse(MaxPrintsInput.Text, out int maxPrints))
            Program.Settings.MaxPrintsPerSession = Math.Max(0, maxPrints);
        if (int.TryParse(PrintCooldownInput.Text, out int cooldown))
            Program.Settings.PrintCooldownSeconds = Math.Max(0, cooldown);

        // Save default printer selection
        if (DefaultPrinterCombo.SelectedItem is string selectedPrinter && !string.IsNullOrEmpty(selectedPrinter))
        {
            if (Program.PrinterServiceInstance != null)
                await Program.PrinterServiceInstance.SetDefaultPrinterAsync(selectedPrinter);
        }

        Program.Settings.Save();
        SetAutoStart(Program.Settings.AutoStart);

        // Handle cloud relay toggle change
        if (Program.Settings.EnableCloudRelay && !wasRelayEnabled)
        {
            // Start tunnel
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ☁ Starting Cloud Relay tunnel...");
            StartCloudRelay();
        }
        else if (!Program.Settings.EnableCloudRelay && wasRelayEnabled)
        {
            // Stop tunnel
            Program.TunnelServiceInstance?.Stop();
            UpdateTunnelStatus(null);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ☁ Cloud Relay tunnel stopped");
        }

        // Refresh permanent QR code on dashboard
        GenerateConnectionQrCode();

        MessageBox.Show("Settings saved. Some changes require a restart.",
            "Settings", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    private void CopyUrl_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string urlToCopy = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : (Program.TunnelServiceInstance?.PublicUrl ?? $"https://{ip}:{Program.Settings.ServerPort}");

            Clipboard.SetText(urlToCopy);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 📋 Server address copied to clipboard: {urlToCopy}");
            App.ShowTrayNotification("Copied to Clipboard", urlToCopy, System.Windows.Forms.ToolTipIcon.Info);
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
    /// Generates and displays the permanent connection QR code on the dashboard.
    /// The QR encodes: { ip, port, name, cert, token, tunnel } so phones can connect.
    /// PERMANENT QR: To ensure paper-printed QR codes NEVER change and remain permanently valid:
    /// - Uses the permanent pairing token (never rotates automatically).
    /// - Uses CustomPublicUrl if configured (permanent domain/named tunnel).
    /// - Ephemeral quick tunnel subdomains do NOT mutate the permanent QR code.
    /// </summary>
    private void GenerateConnectionQrCode()
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            var port = Program.Settings.ServerPort;
            var name = Program.Settings.ServerName;
            var cert = Program.ServerCertificate;

            if (ShopServerName != null)
            {
                ShopServerName.Text = name;
            }

            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            var webUrl = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);
            if (ShopServerName != null)
            {
                ShopServerName.Text = Program.Settings.HostIdentifier;
            }

            if (_isWebQrMode)
            {
                // Web Studio mode: QR code encodes the direct HTTP/HTTPS web address
                // Any smartphone camera scans this directly to open the website without an app!
                var qrImage = QrCodeService.GenerateUrlQrCode(webUrl, 10);
                QrCodeImage.Source = qrImage;
                QrInfoText.Text = webUrl;
                AppendLog($"[{DateTime.Now:HH:mm:ss}] 📷 Web Studio QR ready: {webUrl}");
            }
            else
            {
                // Android App mode: QR encodes JSON pairing payload for SpoolDrop Android App
                var qrImage = QrCodeService.GenerateConnectionQrCode(
                    ip, port, name, cert, Program.Settings.CurrentQrPairingToken, tunnelUrl);
                QrCodeImage.Source = qrImage;

                if (!string.IsNullOrEmpty(tunnelUrl))
                {
                    QrInfoText.Text = $"☁ {tunnelUrl}";
                }
                else
                {
                    QrInfoText.Text = $"{ip}:{port} (local network only)";
                }
                AppendLog($"[{DateTime.Now:HH:mm:ss}] 📱 Android App Pairing QR ready");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ QR code generation failed: {ex.Message}");
        }
    }

    private void QrMode_Checked(object sender, RoutedEventArgs e)
    {
        if (QrHeaderTitle == null) return;

        if (QrModeWeb?.IsChecked == true)
        {
            _isWebQrMode = true;
            QrHeaderTitle.Text = "Scan to Print";
            QrHeaderSubtitle.Text = "Open your phone camera, scan this QR code, and upload any document to print. No app needed!";
            if (FindResource("IconQrScannerMini") is System.Windows.Media.Geometry geo)
                QrHeaderIcon.Data = geo;
            if (StepsRowPanel != null) StepsRowPanel.Visibility = Visibility.Visible;
            if (FormatPillsPanel != null) FormatPillsPanel.Visibility = Visibility.Visible;
        }
        else
        {
            _isWebQrMode = false;
            QrHeaderTitle.Text = "Android App Pairing";
            QrHeaderSubtitle.Text = "Scan with Printora Android App for zero-config pairing.";
            if (FindResource("IconDevice") is System.Windows.Media.Geometry geo)
                QrHeaderIcon.Data = geo;
            if (StepsRowPanel != null) StepsRowPanel.Visibility = Visibility.Collapsed;
            if (FormatPillsPanel != null) FormatPillsPanel.Visibility = Visibility.Collapsed;
        }

        GenerateConnectionQrCode();
    }

    private void PrintPoster_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            var webUrl = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);
            var posterUrl = $"{webUrl}/print-qr";

            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = posterUrl,
                UseShellExecute = true
            });

            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🖨 Opened printable QR poster in browser: {posterUrl}");
            App.ShowTrayNotification("Printable QR Poster", "Opening A4 poster in browser for printing...", System.Windows.Forms.ToolTipIcon.Info);
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open poster: {ex.Message}");
            MessageBox.Show($"Could not open poster: {ex.Message}", "Poster Error", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }

    private void OpenWebStudio_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            var webUrl = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);

            System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo
            {
                FileName = webUrl,
                UseShellExecute = true
            });

            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🌐 Opened Web Print Studio: {webUrl}");
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to open Web Studio: {ex.Message}");
        }
    }

    private void SaveQrImage_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            var ip = DiscoveryService.GetLocalIpAddress();
            string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
                ? Program.Settings.CustomPublicUrl.Trim()
                : Program.TunnelServiceInstance?.PublicUrl;

            var webUrl = Program.Settings.GetWebPrintUrl(tunnelUrl, ip);

            var sfd = new Microsoft.Win32.SaveFileDialog
            {
                Filter = "PNG Image (*.png)|*.png",
                FileName = _isWebQrMode ? "Printora-Web-Print-QR.png" : "Printora-App-Pairing-QR.png",
                Title = "Save QR Code Image"
            };

            if (sfd.ShowDialog() == true)
            {
                byte[] bytes;
                if (_isWebQrMode)
                {
                    bytes = QrCodeService.GenerateUrlQrBytes(webUrl, 16);
                }
                else
                {
                    var cert = Program.ServerCertificate;
                    var token = Program.Settings.CurrentQrPairingToken;
                    var payload = QrCodeService.GetConnectionPayloadJson(ip, Program.Settings.ServerPort, Program.Settings.ServerName, cert, token, tunnelUrl);
                    using var qrGen = new QRCoder.QRCodeGenerator();
                    var data = qrGen.CreateQrCode(payload, QRCoder.QRCodeGenerator.ECCLevel.M);
                    using var qr = new QRCoder.PngByteQRCode(data);
                    bytes = qr.GetGraphic(16);
                }

                File.WriteAllBytes(sfd.FileName, bytes);
                AppendLog($"[{DateTime.Now:HH:mm:ss}] 💾 Saved QR code image to: {sfd.FileName}");
                App.ShowTrayNotification("QR Image Saved", $"Saved to {Path.GetFileName(sfd.FileName)}", System.Windows.Forms.ToolTipIcon.Info);
            }
        }
        catch (Exception ex)
        {
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Failed to save QR image: {ex.Message}");
            MessageBox.Show($"Could not save image: {ex.Message}", "Save Error", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
    }

    /// <summary>
    /// Prints a high-resolution, professional A4/Letter sign containing the permanent QR code
    /// and step-by-step instructions for customers and users to connect and print.
    /// </summary>
    private void PrintQrCode_Click(object sender, RoutedEventArgs e)
    {
        try
        {
            // Warn if no permanent CustomPublicUrl is set — printed QR with ephemeral URL will break
            bool hasCustomUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl);
            bool tunnelEnabled = Program.Settings.EnableCloudRelay;

            if (tunnelEnabled && !hasCustomUrl)
            {
                var result = MessageBox.Show(
                    "⚠ Cloud Relay is enabled but no Custom Public URL is set.\n\n" +
                    "The printed QR code will only work for LOCAL network users.\n" +
                    "Remote users won't be able to connect via the printed QR because " +
                    "the ephemeral tunnel URL changes every time the server restarts.\n\n" +
                    "To enable remote access via printed QR, set a Custom Public URL in Settings " +
                    "(e.g., using a Cloudflare named tunnel or your own domain).\n\n" +
                    "Print anyway for local users only?",
                    "Printed QR Code Warning",
                    MessageBoxButton.YesNo,
                    MessageBoxImage.Warning);

                if (result != MessageBoxResult.Yes) return;
            }

            var printDlg = new PrintDialog();
            if (printDlg.ShowDialog() != true) return;

            var visual = CreatePrintableQrSign();
            printDlg.PrintVisual(visual, "Printora Server - Connection QR Code Sign");
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🖨 QR Code sign sent to printer");
            App.ShowTrayNotification("🖨 Printing QR Sign", "Permanent QR code sign sent to printer.", System.Windows.Forms.ToolTipIcon.Info);
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Failed to print QR code sign: {ex.Message}", "Print Error", MessageBoxButton.OK, MessageBoxImage.Error);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Print failed: {ex.Message}");
        }
    }

    private FrameworkElement CreatePrintableQrSign()
    {
        var ip = DiscoveryService.GetLocalIpAddress();
        var port = Program.Settings.ServerPort;
        var name = Program.Settings.ServerName;
        var cert = Program.ServerCertificate;
        string? tunnelUrl = !string.IsNullOrWhiteSpace(Program.Settings.CustomPublicUrl)
            ? Program.Settings.CustomPublicUrl.Trim()
            : Program.TunnelServiceInstance?.PublicUrl;

        var webUrl = !string.IsNullOrWhiteSpace(tunnelUrl) ? tunnelUrl : $"https://{ip}:{port}";

        // Use direct URL QR so standard phone cameras can scan it immediately
        var qrBitmap = _isWebQrMode
            ? QrCodeService.GenerateUrlQrCode(webUrl, 12)
            : QrCodeService.GenerateConnectionQrCode(ip, port, name, cert, Program.Settings.CurrentQrPairingToken, tunnelUrl);

        var border = new Border
        {
            Width = 650,
            Height = 880,
            Background = System.Windows.Media.Brushes.White,
            BorderBrush = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#E2E8F0")!,
            BorderThickness = new Thickness(2),
            CornerRadius = new CornerRadius(12),
            Padding = new Thickness(36),
            Margin = new Thickness(10)
        };

        var stack = new StackPanel
        {
            HorizontalAlignment = HorizontalAlignment.Center
        };

        var headerPanel = new StackPanel { Margin = new Thickness(0, 0, 0, 18) };
        var title = new TextBlock
        {
            Text = "🖨 Printora Print Station",
            FontSize = 32,
            FontWeight = FontWeights.Bold,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#0F172A")!,
            TextAlignment = TextAlignment.Center
        };
        var subtitle = new TextBlock
        {
            Text = "Scan to connect & print documents directly from your phone",
            FontSize = 14,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#64748B")!,
            TextAlignment = TextAlignment.Center,
            Margin = new Thickness(0, 4, 0, 0)
        };
        headerPanel.Children.Add(title);
        headerPanel.Children.Add(subtitle);
        stack.Children.Add(headerPanel);

        // QR Code Card
        var qrCard = new Border
        {
            Background = System.Windows.Media.Brushes.White,
            BorderBrush = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#CBD5E1")!,
            BorderThickness = new Thickness(2),
            CornerRadius = new CornerRadius(16),
            Padding = new Thickness(16),
            Margin = new Thickness(0, 0, 0, 16),
            HorizontalAlignment = HorizontalAlignment.Center
        };
        var qrImg = new Image
        {
            Source = qrBitmap,
            Width = 300,
            Height = 300
        };
        qrCard.Child = qrImg;
        stack.Children.Add(qrCard);

        var serverInfo = new TextBlock
        {
            Text = $"Server: {name}   •   {(string.IsNullOrEmpty(tunnelUrl) ? $"{ip}:{port}" : tunnelUrl)}",
            FontSize = 13,
            FontWeight = FontWeights.SemiBold,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#334155")!,
            TextAlignment = TextAlignment.Center,
            Margin = new Thickness(0, 0, 0, 18)
        };
        stack.Children.Add(serverInfo);

        // Instructions Card
        var instructionsBorder = new Border
        {
            Background = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#F8FAFC")!,
            BorderBrush = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#E2E8F0")!,
            BorderThickness = new Thickness(1),
            CornerRadius = new CornerRadius(10),
            Padding = new Thickness(24, 16, 24, 16),
            Margin = new Thickness(0, 0, 0, 14),
            Width = 520
        };

        var instructionsStack = new StackPanel();
        var stepHeader = new TextBlock
        {
            Text = "HOW TO PRINT FROM YOUR PHONE",
            FontSize = 12,
            FontWeight = FontWeights.Bold,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#6366F1")!,
            Margin = new Thickness(0, 0, 0, 8)
        };
        instructionsStack.Children.Add(stepHeader);

        var step1 = new TextBlock
        {
            Text = "1. Connect to Wi-Fi (or use Mobile Data for remote printing)",
            FontSize = 12.5,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#1E293B")!,
            Margin = new Thickness(0, 0, 0, 6)
        };
        var step2 = new TextBlock
        {
            Text = "2. Open the Printora app and tap 'Scan QR Code'",
            FontSize = 12.5,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#1E293B")!,
            Margin = new Thickness(0, 0, 0, 6)
        };
        var step3 = new TextBlock
        {
            Text = "3. Point camera at this QR sign — wait for admin approval on the PC screen",
            FontSize = 12.5,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#1E293B")!,
            Margin = new Thickness(0, 0, 0, 6)
        };
        var step4 = new TextBlock
        {
            Text = "4. Once approved, select PDF, Word documents or Photos and tap Print!",
            FontSize = 12.5,
            FontWeight = FontWeights.SemiBold,
            Foreground = (System.Windows.Media.Brush)new System.Windows.Media.BrushConverter().ConvertFromString("#0F172A")!
        };
        instructionsStack.Children.Add(step1);
        instructionsStack.Children.Add(step2);
        instructionsStack.Children.Add(step3);
        instructionsStack.Children.Add(step4);
        instructionsBorder.Child = instructionsStack;
        stack.Children.Add(instructionsBorder);

        border.Child = stack;

        // Measure and arrange for high-resolution printing
        border.Measure(new Size(650, 880));
        border.Arrange(new Rect(0, 0, 650, 880));
        border.UpdateLayout();

        return border;
    }

    /// <summary>
    /// Wires up tunnel status events to update the UI and QR code.
    /// Safe to call multiple times — uses a flag to prevent duplicate wiring.
    /// </summary>
    private bool _tunnelEventsWired = false;
    private void StartCloudRelayEventWiring()
    {
        if (_tunnelEventsWired || Program.TunnelServiceInstance == null) return;
        _tunnelEventsWired = true;

        Program.TunnelServiceInstance.OnLog += msg =>
        {
            Dispatcher.BeginInvoke(() =>
            {
                AppendLog($"[{DateTime.Now:HH:mm:ss}] {msg}");
            });
        };

        Program.TunnelServiceInstance.OnTunnelReady += url =>
        {
            Dispatcher.BeginInvoke(() =>
            {
                UpdateTunnelStatus(url);
                // Re-generate the on-screen QR to include the live tunnel URL
                GenerateConnectionQrCode();
                AppendLog($"[{DateTime.Now:HH:mm:ss}] 🌐 Cloud Relay active: {url}");
                App.ShowTrayNotification("☁ Cloud Relay Active",
                    $"Print from anywhere: {url}", System.Windows.Forms.ToolTipIcon.Info);
            });
        };

        Program.TunnelServiceInstance.OnTunnelStopped += reason =>
        {
            Dispatcher.BeginInvoke(() =>
            {
                UpdateTunnelStatus(null);
                // Re-generate QR without the tunnel URL
                GenerateConnectionQrCode();
                AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Cloud Relay stopped: {reason}");
            });
        };

        // If the tunnel is already active (startup race), update UI immediately
        if (Program.TunnelServiceInstance.IsActive && !string.IsNullOrEmpty(Program.TunnelServiceInstance.PublicUrl))
        {
            UpdateTunnelStatus(Program.TunnelServiceInstance.PublicUrl);
            GenerateConnectionQrCode();
            AppendLog($"[{DateTime.Now:HH:mm:ss}] 🌐 Cloud Relay already active: {Program.TunnelServiceInstance.PublicUrl}");
        }
    }

    /// <summary>
    /// Starts the cloud relay tunnel and wires up status events.
    /// </summary>
    private void StartCloudRelay()
    {
        if (Program.TunnelServiceInstance == null) return;

        StartCloudRelayEventWiring();

        _ = Task.Run(async () =>
        {
            try
            {
                await Program.TunnelServiceInstance.StartAsync(Program.Settings.ServerPort);
            }
            catch (Exception ex)
            {
                _ = Dispatcher.BeginInvoke(() =>
                {
                    AppendLog($"[{DateTime.Now:HH:mm:ss}] ⚠ Cloud Relay failed: {ex.Message}");
                    UpdateTunnelStatus(null);
                });
            }
        });
    }

    /// <summary>
    /// Updates the tunnel status indicator in the Settings page.
    /// </summary>
    private void UpdateTunnelStatus(string? tunnelUrl)
    {
        if (!string.IsNullOrEmpty(tunnelUrl))
        {
            TunnelStatusBorder.Visibility = Visibility.Visible;
            TunnelStatusBorder.Background = new System.Windows.Media.SolidColorBrush(
                (System.Windows.Media.Color)System.Windows.Media.ColorConverter.ConvertFromString("#F0FDF4"));
            TunnelStatusBorder.BorderBrush = new System.Windows.Media.SolidColorBrush(
                (System.Windows.Media.Color)System.Windows.Media.ColorConverter.ConvertFromString("#BBF7D0"));
            TunnelStatusText.Text = "🌐 Tunnel: Active";
            TunnelUrlText.Text = tunnelUrl;
        }
        else
        {
            TunnelStatusBorder.Visibility = Visibility.Collapsed;
            TunnelStatusText.Text = "🌐 Tunnel: Not active";
            TunnelUrlText.Text = "";
        }
        UpdateSidebarCloudStatus();
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