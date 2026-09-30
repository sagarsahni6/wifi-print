using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Reflection;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WifiPrintInstaller
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            ApplicationConfiguration.Initialize();
            Application.Run(new InstallerForm());
        }
    }

    public class InstallerForm : Form
    {
        private TextBox txtPath;
        private Button btnBrowse;
        private CheckBox chkDesktop;
        private CheckBox chkStartMenu;
        private CheckBox chkFirewall;
        private CheckBox chkLaunch;
        private ProgressBar progressBar;
        private Label lblStatus;
        private Button btnInstall;
        private Button btnCancel;
        private bool isInstalled = false;

        public InstallerForm()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Text = "WiFi Print Server — Setup";
            this.Size = new Size(560, 480);
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = true;
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.FromArgb(248, 249, 252);
            this.Font = new Font("Segoe UI", 9F, FontStyle.Regular, GraphicsUnit.Point);

            // Header Panel
            Panel headerPanel = new Panel
            {
                Dock = DockStyle.Top,
                Height = 85,
                BackColor = Color.FromArgb(26, 35, 60)
            };

            Label lblTitle = new Label
            {
                Text = "WiFi Print Server Setup",
                ForeColor = Color.White,
                Font = new Font("Segoe UI", 14F, FontStyle.Bold),
                Location = new Point(24, 16),
                AutoSize = true
            };

            Label lblSubtitle = new Label
            {
                Text = "Install desktop server to print directly from Android over Wi-Fi",
                ForeColor = Color.FromArgb(170, 195, 240),
                Font = new Font("Segoe UI", 9F),
                Location = new Point(25, 48),
                AutoSize = true
            };

            headerPanel.Controls.Add(lblTitle);
            headerPanel.Controls.Add(lblSubtitle);
            this.Controls.Add(headerPanel);

            // Destination Folder Label & Text
            Label lblDest = new Label
            {
                Text = "Destination Folder:",
                Location = new Point(25, 105),
                AutoSize = true,
                Font = new Font("Segoe UI", 9F, FontStyle.Bold)
            };
            this.Controls.Add(lblDest);

            string defaultPath = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                "Programs",
                "WifiPrintServer"
            );

            txtPath = new TextBox
            {
                Text = defaultPath,
                Location = new Point(28, 130),
                Width = 380,
                Font = new Font("Segoe UI", 9F)
            };
            this.Controls.Add(txtPath);

            btnBrowse = new Button
            {
                Text = "Browse...",
                Location = new Point(418, 128),
                Width = 95,
                Height = 27,
                BackColor = Color.White,
                FlatStyle = FlatStyle.System
            };
            btnBrowse.Click += (s, e) =>
            {
                using var dlg = new FolderBrowserDialog();
                dlg.SelectedPath = txtPath.Text;
                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    txtPath.Text = dlg.SelectedPath;
                }
            };
            this.Controls.Add(btnBrowse);

            // Options GroupBox
            GroupBox grpOptions = new GroupBox
            {
                Text = "Installation Options",
                Location = new Point(28, 175),
                Width = 485,
                Height = 145,
                Font = new Font("Segoe UI", 9F, FontStyle.Regular)
            };

            chkDesktop = new CheckBox
            {
                Text = "Create Desktop Shortcut",
                Checked = true,
                Location = new Point(16, 25),
                AutoSize = true
            };

            chkStartMenu = new CheckBox
            {
                Text = "Create Start Menu Shortcut",
                Checked = true,
                Location = new Point(16, 52),
                AutoSize = true
            };

            chkFirewall = new CheckBox
            {
                Text = "Allow port 5000 in Windows Firewall (Recommended for Wi-Fi)",
                Checked = true,
                Location = new Point(16, 79),
                AutoSize = true
            };

            chkLaunch = new CheckBox
            {
                Text = "Launch WiFi Print Server after installation",
                Checked = true,
                Location = new Point(16, 106),
                AutoSize = true
            };

            grpOptions.Controls.Add(chkDesktop);
            grpOptions.Controls.Add(chkStartMenu);
            grpOptions.Controls.Add(chkFirewall);
            grpOptions.Controls.Add(chkLaunch);
            this.Controls.Add(grpOptions);

            // Progress Bar
            progressBar = new ProgressBar
            {
                Location = new Point(28, 335),
                Width = 485,
                Height = 18,
                Style = ProgressBarStyle.Continuous,
                Value = 0,
                Visible = false
            };
            this.Controls.Add(progressBar);

            // Status label
            lblStatus = new Label
            {
                Text = "Click 'Install' to begin installation.",
                Location = new Point(28, 360),
                Width = 485,
                ForeColor = Color.FromArgb(70, 75, 90),
                AutoSize = false
            };
            this.Controls.Add(lblStatus);

            // Bottom Buttons
            Panel bottomPanel = new Panel
            {
                Dock = DockStyle.Bottom,
                Height = 55,
                BackColor = Color.FromArgb(238, 240, 246)
            };

            btnInstall = new Button
            {
                Text = "Install",
                Location = new Point(310, 12),
                Width = 100,
                Height = 32,
                BackColor = Color.FromArgb(0, 110, 230),
                ForeColor = Color.White,
                Font = new Font("Segoe UI", 9F, FontStyle.Bold),
                FlatStyle = FlatStyle.Flat
            };
            btnInstall.FlatAppearance.BorderSize = 0;
            btnInstall.Click += async (s, e) =>
            {
                if (isInstalled)
                {
                    this.Close();
                    return;
                }
                await RunInstallationAsync();
            };

            btnCancel = new Button
            {
                Text = "Cancel",
                Location = new Point(418, 12),
                Width = 95,
                Height = 32,
                BackColor = Color.White,
                FlatStyle = FlatStyle.System
            };
            btnCancel.Click += (s, e) => this.Close();

            bottomPanel.Controls.Add(btnInstall);
            bottomPanel.Controls.Add(btnCancel);
            this.Controls.Add(bottomPanel);
        }

        private async Task RunInstallationAsync()
        {
            btnInstall.Enabled = false;
            btnCancel.Enabled = false;
            btnBrowse.Enabled = false;
            txtPath.Enabled = false;
            progressBar.Visible = true;
            progressBar.Style = ProgressBarStyle.Marquee;

            string targetDir = txtPath.Text.Trim();
            if (string.IsNullOrEmpty(targetDir))
            {
                MessageBox.Show(this, "Please choose a valid destination folder.", "Invalid Path", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                btnInstall.Enabled = true;
                btnCancel.Enabled = true;
                btnBrowse.Enabled = true;
                txtPath.Enabled = true;
                progressBar.Visible = false;
                return;
            }

            try
            {
                // 1. Close any running instance
                lblStatus.Text = "Stopping any running instance of WiFi Print Server...";
                await Task.Run(() =>
                {
                    try
                    {
                        foreach (var proc in Process.GetProcessesByName("WifiPrintServer"))
                        {
                            proc.Kill();
                            proc.WaitForExit(3000);
                        }
                    }
                    catch { }
                });

                // 2. Ensure target directory exists
                lblStatus.Text = "Creating installation directory...";
                Directory.CreateDirectory(targetDir);

                // 3. Find source server binary
                lblStatus.Text = "Installing application files...";
                string sourceExe = FindSourceExe();
                if (string.IsNullOrEmpty(sourceExe) || !File.Exists(sourceExe))
                {
                    throw new FileNotFoundException("Could not locate WifiPrintServer.exe in the installation package.");
                }

                string destExe = Path.Combine(targetDir, "WifiPrintServer.exe");

                await Task.Run(() =>
                {
                    File.Copy(sourceExe, destExe, overwrite: true);

                    // Copy license or pdb if present
                    string sourceDir = Path.GetDirectoryName(sourceExe) ?? "";
                    string lic = Path.Combine(sourceDir, "LICENSE");
                    if (File.Exists(lic))
                    {
                        File.Copy(lic, Path.Combine(targetDir, "LICENSE"), true);
                    }
                });

                // 4. Create Desktop shortcut
                if (chkDesktop.Checked)
                {
                    lblStatus.Text = "Creating Desktop shortcut...";
                    string desktopDir = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
                    string shortcutPath = Path.Combine(desktopDir, "WiFi Print Server.lnk");
                    CreateShortcut(destExe, shortcutPath, "WiFi Print Server — Print from Android");
                }

                // 5. Create Start Menu shortcut
                if (chkStartMenu.Checked)
                {
                    lblStatus.Text = "Creating Start Menu shortcut...";
                    string startMenuDir = Path.Combine(
                        Environment.GetFolderPath(Environment.SpecialFolder.Programs),
                        "WiFi Print Server"
                    );
                    Directory.CreateDirectory(startMenuDir);
                    string shortcutPath = Path.Combine(startMenuDir, "WiFi Print Server.lnk");
                    CreateShortcut(destExe, shortcutPath, "WiFi Print Server — Print from Android");

                    // Also create uninstaller shortcut
                    string uninstallBat = Path.Combine(targetDir, "Uninstall.bat");
                    WriteUninstaller(uninstallBat, targetDir, startMenuDir);
                    string uninstShortcut = Path.Combine(startMenuDir, "Uninstall WiFi Print Server.lnk");
                    CreateShortcut(uninstallBat, uninstShortcut, "Uninstall WiFi Print Server");
                }

                // 6. Windows Firewall rule
                if (chkFirewall.Checked)
                {
                    lblStatus.Text = "Configuring Windows Firewall rule for port 5000...";
                    await Task.Run(() =>
                    {
                        try
                        {
                            var psi = new ProcessStartInfo
                            {
                                FileName = "netsh",
                                Arguments = "advfirewall firewall add rule name=\"WiFi Print Server\" dir=in action=allow protocol=TCP localport=5000",
                                UseShellExecute = true,
                                CreateNoWindow = true,
                                WindowStyle = ProcessWindowStyle.Hidden
                            };
                            using var p = Process.Start(psi);
                            p?.WaitForExit(3000);
                        }
                        catch { }
                    });
                }

                progressBar.Style = ProgressBarStyle.Continuous;
                progressBar.Value = 100;
                lblStatus.Text = "Installation completed successfully!";
                lblStatus.ForeColor = Color.DarkGreen;

                // 7. Launch if requested
                if (chkLaunch.Checked && File.Exists(destExe))
                {
                    try
                    {
                        Process.Start(new ProcessStartInfo(destExe)
                        {
                            WorkingDirectory = targetDir,
                            UseShellExecute = true
                        });
                    }
                    catch { }
                }

                isInstalled = true;
                btnInstall.Text = "Finish";
                btnInstall.Enabled = true;
                btnCancel.Visible = false;
            }
            catch (Exception ex)
            {
                progressBar.Visible = false;
                lblStatus.Text = "Installation failed: " + ex.Message;
                lblStatus.ForeColor = Color.Red;
                btnInstall.Enabled = true;
                btnCancel.Enabled = true;
                btnBrowse.Enabled = true;
                txtPath.Enabled = true;
                MessageBox.Show(this, "An error occurred during installation:\n\n" + ex.Message, "Setup Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private string? FindSourceExe()
        {
            string appDir = AppDomain.CurrentDomain.BaseDirectory;

            // Direct check in same folder
            string candidate = Path.Combine(appDir, "WifiPrintServer.exe");
            if (File.Exists(candidate)) return candidate;

            // Check subfolder WifiPrintServer
            candidate = Path.Combine(appDir, "WifiPrintServer", "WifiPrintServer.exe");
            if (File.Exists(candidate)) return candidate;

            // Check publish folder
            candidate = Path.Combine(appDir, "..", "publish", "WifiPrintServer", "WifiPrintServer.exe");
            if (File.Exists(candidate)) return Path.GetFullPath(candidate);

            return null;
        }

        private static void CreateShortcut(string targetPath, string shortcutPath, string description)
        {
            try
            {
                Type? shellType = Type.GetTypeFromProgID("WScript.Shell");
                if (shellType != null)
                {
                    dynamic? shell = Activator.CreateInstance(shellType);
                    if (shell != null)
                    {
                        dynamic shortcut = shell.CreateShortcut(shortcutPath);
                        shortcut.TargetPath = targetPath;
                        shortcut.WorkingDirectory = Path.GetDirectoryName(targetPath);
                        shortcut.Description = description;
                        shortcut.IconLocation = targetPath + ",0";
                        shortcut.Save();
                    }
                }
            }
            catch { }
        }

        private static void WriteUninstaller(string batPath, string targetDir, string startMenuDir)
        {
            try
            {
                string script = $@"@echo off
echo Stopping WiFi Print Server...
taskkill /f /im WifiPrintServer.exe >nul 2>&1

echo Removing Shortcuts...
del ""%USERPROFILE%\Desktop\WiFi Print Server.lnk"" >nul 2>&1
rd /s /q ""{startMenuDir}"" >nul 2>&1

echo Removing Firewall rule...
netsh advfirewall firewall delete rule name=""WiFi Print Server"" >nul 2>&1

echo Removing Installation Directory...
rd /s /q ""{targetDir}"" >nul 2>&1

echo WiFi Print Server has been uninstalled.
pause
";
                File.WriteAllText(batPath, script);
            }
            catch { }
        }
    }
}
