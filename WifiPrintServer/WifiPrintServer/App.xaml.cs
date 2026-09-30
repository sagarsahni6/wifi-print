using System.Windows;

namespace WifiPrintServer;

public partial class App : Application
{

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        AppDomain.CurrentDomain.UnhandledException += (s, args) =>
        {
            try
            {
                var dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer");
                File.WriteAllText(Path.Combine(dir, "crash_domain.log"), args.ExceptionObject?.ToString() ?? "Unknown domain error");
            }
            catch { }
        };

        DispatcherUnhandledException += (s, args) =>
        {
            try
            {
                var dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer");
                File.WriteAllText(Path.Combine(dir, "crash_dispatcher.log"), args.Exception?.ToString() ?? "Unknown dispatcher error");
            }
            catch { }
        };

        // Ensure app does not exit when window is hidden or minimized to system tray
        ShutdownMode = ShutdownMode.OnExplicitShutdown;

        try
        {
            // Initialize settings
            Program.Initialize();

            // Ensure upload/log directories exist
            Directory.CreateDirectory(Program.Settings.UploadDirectory);
            Directory.CreateDirectory(Program.Settings.LogDirectory);

            // Initialize and show main window manually since StartupUri was removed
            var mainWindow = new MainWindow();
            mainWindow.Show();

            // Start the web server on a background thread
            _ = Task.Run(async () =>
            {
                try
                {
                    await Program.StartWebServerAsync();
                }
                catch (Exception ex)
                {
                    Dispatcher.Invoke(() =>
                    {
                        MessageBox.Show($"Failed to start server: {ex.Message}",
                            "WiFi Print Server", MessageBoxButton.OK, MessageBoxImage.Error);
                    });
                }
            });

            // Setup system tray icon
            SetupTrayIcon();
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"FATAL: {ex}");
            MessageBox.Show($"Startup failed:\n{ex}", "WiFi Print Server — Fatal Error",
                MessageBoxButton.OK, MessageBoxImage.Error);
            Shutdown(1);
        }
    }

    private System.Windows.Forms.NotifyIcon? _trayIcon;
    private static System.Windows.Forms.NotifyIcon? _staticTrayIcon;

    public static void ShowTrayNotification(string title, string message, System.Windows.Forms.ToolTipIcon icon = System.Windows.Forms.ToolTipIcon.Info)
    {
        Current?.Dispatcher.BeginInvoke(() =>
        {
            try { _staticTrayIcon?.ShowBalloonTip(4000, title, message, icon); }
            catch { /* Ignore notification failures */ }
        });
    }

    private void SetupTrayIcon()
    {
        _trayIcon = new System.Windows.Forms.NotifyIcon
        {
            Text = "WiFi Print Server",
            Visible = true
        };
        _staticTrayIcon = _trayIcon;

        // Use a default system icon
        _trayIcon.Icon = System.Drawing.SystemIcons.Application;

        var menu = new System.Windows.Forms.ContextMenuStrip();
        menu.Items.Add("Open Dashboard", null, (s, e) =>
        {
            if (MainWindow is { } mainWindow)
            {
                mainWindow.Show();
                mainWindow.WindowState = WindowState.Normal;
                mainWindow.Activate();
            }
        });
        menu.Items.Add("-");
        menu.Items.Add("Exit", null, (s, e) =>
        {
            _trayIcon.Visible = false;
            Program.Discovery?.Dispose();
            Shutdown();
        });

        _trayIcon.ContextMenuStrip = menu;
        _trayIcon.DoubleClick += (s, e) =>
        {
            if (MainWindow is { } mainWindow)
            {
                mainWindow.Show();
                mainWindow.WindowState = WindowState.Normal;
                mainWindow.Activate();
            }
        };
    }

    protected override void OnExit(ExitEventArgs e)
    {
        try
        {
            var dir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "WifiPrintServer");
            File.AppendAllText(Path.Combine(dir, "exit.log"), $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] OnExit called with exit code: {e.ApplicationExitCode}\n");
        }
        catch { }
        _trayIcon?.Dispose();
        Program.Discovery?.Dispose();
        base.OnExit(e);
    }
}
