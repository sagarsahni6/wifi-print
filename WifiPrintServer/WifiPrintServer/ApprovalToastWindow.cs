using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Effects;
using System.Windows.Threading;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer;

/// <summary>
/// Lightweight, interactive desktop toast notification window positioned in the bottom-right corner.
/// Allows the PC owner to approve or deny incoming print requests with 1-click
/// directly from the notification WITHOUT opening or focusing the main server application window.
/// </summary>
public class ApprovalToastWindow : Window
{
    private readonly PendingApproval _approval;
    private readonly AuthService _authService;
    private readonly DispatcherTimer _autoDismissTimer;

    public ApprovalToastWindow(PendingApproval approval, AuthService authService)
    {
        _approval = approval;
        _authService = authService;

        // Window style configuration
        WindowStyle = WindowStyle.None;
        AllowsTransparency = true;
        Background = Brushes.Transparent;
        Topmost = true;
        ShowInTaskbar = false;
        ShowActivated = false; // Does NOT steal keyboard or window focus!

        Width = 380;
        Height = 150;

        // Position in bottom-right corner above taskbar
        var workArea = SystemParameters.WorkArea;
        Left = workArea.Right - Width - 16;
        Top = workArea.Bottom - Height - 16;

        Content = BuildUi();

        // 60-second auto-dismiss countdown
        _autoDismissTimer = new DispatcherTimer
        {
            Interval = TimeSpan.FromSeconds(60)
        };
        _autoDismissTimer.Tick += (s, e) =>
        {
            _autoDismissTimer.Stop();
            Close();
        };
        _autoDismissTimer.Start();
    }

    private UIElement BuildUi()
    {
        var border = new Border
        {
            Background = new SolidColorBrush(Color.FromRgb(15, 23, 42)), // Slate 900
            BorderBrush = new SolidColorBrush(Color.FromRgb(51, 65, 85)), // Slate 700
            BorderThickness = new Thickness(1),
            CornerRadius = new CornerRadius(14),
            Padding = new Thickness(14, 12, 14, 12),
            Effect = new DropShadowEffect
            {
                Color = Colors.Black,
                BlurRadius = 24,
                ShadowDepth = 6,
                Opacity = 0.5
            }
        };

        var mainGrid = new Grid();
        mainGrid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto }); // Header
        mainGrid.RowDefinitions.Add(new RowDefinition { Height = new GridLength(1, GridUnitType.Star) }); // Content
        mainGrid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto }); // Actions

        // Row 0: Header with icon, title, tunnel tag, and close button
        var headerDock = new DockPanel { LastChildFill = false, Margin = new Thickness(0, 0, 0, 8) };

        var iconText = new TextBlock
        {
            Text = "\U0001f5a8",
            FontSize = 16,
            VerticalAlignment = VerticalAlignment.Center,
            Margin = new Thickness(0, 0, 6, 0)
        };
        DockPanel.SetDock(iconText, Dock.Left);
        headerDock.Children.Add(iconText);

        var titleText = new TextBlock
        {
            Text = "Web Print Approval Request",
            Foreground = Brushes.White,
            FontWeight = FontWeights.Bold,
            FontSize = 13,
            VerticalAlignment = VerticalAlignment.Center
        };
        DockPanel.SetDock(titleText, Dock.Left);
        headerDock.Children.Add(titleText);

        if (_approval.ConnectedViaTunnel)
        {
            var tunnelBadge = new Border
            {
                Background = new SolidColorBrush(Color.FromArgb(50, 245, 130, 32)),
                BorderBrush = new SolidColorBrush(Color.FromRgb(245, 130, 32)),
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(4),
                Padding = new Thickness(5, 1, 5, 1),
                Margin = new Thickness(8, 0, 0, 0),
                VerticalAlignment = VerticalAlignment.Center
            };
            tunnelBadge.Child = new TextBlock
            {
                Text = "Cloud Relay",
                Foreground = new SolidColorBrush(Color.FromRgb(245, 130, 32)),
                FontSize = 10,
                FontWeight = FontWeights.SemiBold
            };
            DockPanel.SetDock(tunnelBadge, Dock.Left);
            headerDock.Children.Add(tunnelBadge);
        }

        var btnClose = new Button
        {
            Content = "X",
            Foreground = new SolidColorBrush(Color.FromRgb(148, 163, 184)),
            Background = Brushes.Transparent,
            BorderThickness = new Thickness(0),
            Cursor = System.Windows.Input.Cursors.Hand,
            FontSize = 12,
            FontWeight = FontWeights.Bold,
            Padding = new Thickness(4)
        };
        btnClose.Click += (s, e) =>
        {
            _autoDismissTimer.Stop();
            Close();
        };
        DockPanel.SetDock(btnClose, Dock.Right);
        headerDock.Children.Add(btnClose);

        Grid.SetRow(headerDock, 0);
        mainGrid.Children.Add(headerDock);

        // Row 1: Device Name & Subtitle
        var infoStack = new StackPanel { Margin = new Thickness(0, 0, 0, 10) };
        var deviceText = new TextBlock
        {
            Text = $"{_approval.DeviceName} ({_approval.IpAddress})",
            Foreground = new SolidColorBrush(Color.FromRgb(241, 245, 249)),
            FontWeight = FontWeights.SemiBold,
            FontSize = 13,
            TextTrimming = TextTrimming.CharacterEllipsis
        };
        var subText = new TextBlock
        {
            Text = "Requested permission to print documents on this PC.",
            Foreground = new SolidColorBrush(Color.FromRgb(148, 163, 184)),
            FontSize = 11,
            Margin = new Thickness(0, 2, 0, 0)
        };
        infoStack.Children.Add(deviceText);
        infoStack.Children.Add(subText);

        Grid.SetRow(infoStack, 1);
        mainGrid.Children.Add(infoStack);

        // Row 2: Action Buttons (Approve & Deny)
        var actionsGrid = new Grid();
        actionsGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1.4, GridUnitType.Star) }); // Approve
        actionsGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(10) }); // Spacer
        actionsGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); // Deny

        var btnApprove = new Button
        {
            Content = "Approve (2 Hours)",
            Background = new SolidColorBrush(Color.FromRgb(16, 185, 129)), // Emerald 500
            Foreground = Brushes.White,
            FontWeight = FontWeights.Bold,
            FontSize = 12,
            Height = 32,
            Cursor = System.Windows.Input.Cursors.Hand,
            BorderThickness = new Thickness(0)
        };
        var approveTemplate = new ControlTemplate(typeof(Button));
        var approveBorderFactory = new FrameworkElementFactory(typeof(Border));
        approveBorderFactory.SetValue(Border.CornerRadiusProperty, new CornerRadius(8));
        approveBorderFactory.SetValue(Border.BackgroundProperty, new TemplateBindingExtension(BackgroundProperty));
        var approvePresenter = new FrameworkElementFactory(typeof(ContentPresenter));
        approvePresenter.SetValue(HorizontalAlignmentProperty, HorizontalAlignment.Center);
        approvePresenter.SetValue(VerticalAlignmentProperty, VerticalAlignment.Center);
        approveBorderFactory.AppendChild(approvePresenter);
        approveTemplate.VisualTree = approveBorderFactory;
        btnApprove.Template = approveTemplate;

        btnApprove.Click += (s, e) =>
        {
            _autoDismissTimer.Stop();
            try
            {
                _authService.ApproveDevice(_approval.Id);
                System.Media.SystemSounds.Asterisk.Play();
            }
            catch { }
            Close();
        };

        var btnDeny = new Button
        {
            Content = "Deny",
            Background = new SolidColorBrush(Color.FromRgb(51, 65, 85)), // Slate 700
            Foreground = new SolidColorBrush(Color.FromRgb(226, 232, 240)),
            FontWeight = FontWeights.Medium,
            FontSize = 12,
            Height = 32,
            Cursor = System.Windows.Input.Cursors.Hand,
            BorderThickness = new Thickness(0)
        };
        var denyTemplate = new ControlTemplate(typeof(Button));
        var denyBorderFactory = new FrameworkElementFactory(typeof(Border));
        denyBorderFactory.SetValue(Border.CornerRadiusProperty, new CornerRadius(8));
        denyBorderFactory.SetValue(Border.BackgroundProperty, new TemplateBindingExtension(BackgroundProperty));
        var denyPresenter = new FrameworkElementFactory(typeof(ContentPresenter));
        denyPresenter.SetValue(HorizontalAlignmentProperty, HorizontalAlignment.Center);
        denyPresenter.SetValue(VerticalAlignmentProperty, VerticalAlignment.Center);
        denyBorderFactory.AppendChild(denyPresenter);
        denyTemplate.VisualTree = denyBorderFactory;
        btnDeny.Template = denyTemplate;

        btnDeny.Click += (s, e) =>
        {
            _autoDismissTimer.Stop();
            try
            {
                _authService.DenyDevice(_approval.Id);
            }
            catch { }
            Close();
        };

        Grid.SetColumn(btnApprove, 0);
        Grid.SetColumn(btnDeny, 2);
        actionsGrid.Children.Add(btnApprove);
        actionsGrid.Children.Add(btnDeny);

        Grid.SetRow(actionsGrid, 2);
        mainGrid.Children.Add(actionsGrid);

        border.Child = mainGrid;
        return border;
    }

    /// <summary>
    /// Displays the non-intrusive interactive desktop toast notification for the approval request.
    /// Does NOT bring the main server window to front or steal keyboard focus.
    /// </summary>
    public static void ShowToast(PendingApproval approval, AuthService authService)
    {
        if (Application.Current == null) return;

        Application.Current.Dispatcher.BeginInvoke(() =>
        {
            try
            {
                // Play notification sound
                System.Media.SystemSounds.Asterisk.Play();

                // Show toast notification window
                var toast = new ApprovalToastWindow(approval, authService);
                toast.Show();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Failed to show approval toast: {ex.Message}");
            }
        });
    }
}
