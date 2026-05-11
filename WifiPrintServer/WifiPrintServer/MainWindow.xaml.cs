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
    private readonly Dictionary<string, Grid> _pages = new();
    private string? _currentApprovalId;
    private bool _eventsWired = false;

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

        // Display server IP
        var ip = DiscoveryService.GetLocalIpAddress();
        IpText.Text = $"IP: {ip}";
        PortText.Text = $"Port: {Program.Settings.ServerPort}";

        // Auto-refresh timer for dashboard stats
        _refreshTimer = new DispatcherTimer { Interval = TimeSpan.FromSeconds(3) };
        _refreshTimer.Tick += (s, e) => RefreshDashboard();
        _refreshTimer.Start();

        // Reliable event wiring — poll every 500ms until services are ready
        // This replaces the old flaky Task.Delay(2000) approach
        _eventWiringTimer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(500) };
        _eventWiringTimer.Tick += TryWireEvents;
        _eventWiringTimer.Start();

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
                    AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Device approved: {device.Name}"));
            };

            // Wire up job status events
            Program.QueueManager.OnJobStatusChanged += update =>
            {
                Dispatcher.BeginInvoke(() =>
                    AppendLog($"[{update.Timestamp:HH:mm:ss}] Job {update.JobId}: {update.Status} - {update.Message}"));
            };

            _eventsWired = true;
            _eventWiringTimer.Stop();
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Server ready — all services connected");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Device Approval Notifications
    // ═══════════════════════════════════════════════════════════════

    /// <summary>
    /// Shows the approval banner when a phone requests connection.
    /// Brings the window to front and plays a notification sound.
    /// </summary>
    private void ShowApprovalNotification(PendingApproval approval)
    {
        _currentApprovalId = approval.Id;
        ApprovalDeviceName.Text = $"📱 {approval.DeviceName} ({approval.DeviceModel})";
        ApprovalDeviceIp.Text = $"IP: {approval.IpAddress}";
        ApprovalBanner.Visibility = Visibility.Visible;
        ApprovalIdle.Visibility = Visibility.Collapsed;

        // Bring window to front
        Show();
        WindowState = WindowState.Normal;
        Activate();
        Topmost = true;
        Topmost = false;

        // Play notification sound
        System.Media.SystemSounds.Asterisk.Play();

        AppendLog($"[{DateTime.Now:HH:mm:ss}] 📲 Connection request from: {approval.DeviceName} ({approval.IpAddress})");
    }

    private void HideApprovalNotification()
    {
        ApprovalBanner.Visibility = Visibility.Collapsed;
        ApprovalIdle.Visibility = Visibility.Visible;
        _currentApprovalId = null;
    }

    private void ApproveDevice_Click(object sender, RoutedEventArgs e)
    {
        if (_currentApprovalId != null && Program.AuthServiceInstance != null)
        {
            Program.AuthServiceInstance.ApproveDevice(_currentApprovalId);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✓ Device APPROVED");
            HideApprovalNotification();
        }
    }

    private void DenyDevice_Click(object sender, RoutedEventArgs e)
    {
        if (_currentApprovalId != null && Program.AuthServiceInstance != null)
        {
            Program.AuthServiceInstance.DenyDevice(_currentApprovalId);
            AppendLog($"[{DateTime.Now:HH:mm:ss}] ✗ Device DENIED");
            HideApprovalNotification();
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
        }
        catch { /* ignore refresh errors */ }
    }

    private void RefreshJobsList()
    {
        if (Program.QueueManager == null) return;
        JobsList.ItemsSource = Program.QueueManager.GetAllJobs();
    }

    private void RefreshPrintersList()
    {
        if (Program.PrinterServiceInstance == null) return;
        PrintersList.ItemsSource = Program.PrinterServiceInstance.GetAllPrinters();
    }

    private void RefreshDevicesList()
    {
        if (Program.AuthServiceInstance == null) return;
        DevicesList.ItemsSource = Program.AuthServiceInstance.GetPairedDevices();
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

    private void SaveSettings_Click(object sender, RoutedEventArgs e)
    {
        if (int.TryParse(PortInput.Text, out int port))
            Program.Settings.ServerPort = port;
        Program.Settings.ServerName = ServerNameInput.Text;
        Program.Settings.AutoStart = AutoStartCheck.IsChecked == true;
        Program.Settings.MinimizeToTray = MinimizeToTrayCheck.IsChecked == true;

        // Save default printer selection
        if (DefaultPrinterCombo.SelectedItem is string selectedPrinter && !string.IsNullOrEmpty(selectedPrinter))
        {
            Program.PrinterServiceInstance?.SetDefaultPrinter(selectedPrinter);
        }

        Program.Settings.Save();
        SetAutoStart(Program.Settings.AutoStart);

        MessageBox.Show("Settings saved. Some changes require a restart.",
            "Settings", MessageBoxButton.OK, MessageBoxImage.Information);
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
                    $"\"{System.AppContext.BaseDirectory}\"");
            else
                key?.DeleteValue("WifiPrintServer", false);
        }
        catch (Exception ex) { AppendLog($"Auto-start error: {ex.Message}"); }
    }

    private void AppendLog(string message)
    {
        LogsTextBox.Text += message + "\n";
        LogsTextBox.ScrollToEnd();
    }

    protected override void OnStateChanged(EventArgs e)
    {
        if (WindowState == WindowState.Minimized && Program.Settings.MinimizeToTray)
            Hide();
        base.OnStateChanged(e);
    }
}