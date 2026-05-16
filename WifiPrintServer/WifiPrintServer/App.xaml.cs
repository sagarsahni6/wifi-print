using System.Windows;

namespace WifiPrintServer;

public partial class App : Application
{
    private System.Windows.Forms.NotifyIcon? _trayIcon;

    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);

        // Initialize settings asynchronously
        await Program.InitializeAsync();

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

    private void SetupTrayIcon()
    {
        _trayIcon = new System.Windows.Forms.NotifyIcon
        {
            Text = "WiFi Print Server",
            Visible = true
        };

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
        _trayIcon?.Dispose();
        Program.Discovery?.Dispose();
        base.OnExit(e);
    }
}
