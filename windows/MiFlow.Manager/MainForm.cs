using System.Drawing.Drawing2D;

namespace MiFlow.Manager;

internal sealed class MainForm : Form
{
    private static readonly Color Background = Color.FromArgb(10, 11, 15);
    private static readonly Color Panel = Color.FromArgb(25, 26, 31);
    private static readonly Color Accent = Color.FromArgb(255, 105, 0);
    private static readonly Color Muted = Color.FromArgb(165, 170, 181);

    private readonly ComboBox deviceBox = new();
    private readonly Label statusLabel = new();
    private readonly Label deviceInfo = new();
    private readonly ProgressBar progress = new();
    private readonly RichTextBox log = new();
    private readonly Button searchButton;
    private readonly Button connectButton;
    private readonly Button installButton;
    private readonly Button apkButton;
    private readonly Button homeButton;
    private readonly Button storeButton;
    private readonly Button restoreButton;
    private readonly AdbClient? adb;
    private bool busy;

    public MainForm()
    {
        adb = AdbClient.Locate();
        Text = "MiFlow Manager";
        StartPosition = FormStartPosition.CenterScreen;
        MinimumSize = new Size(900, 650);
        Size = new Size(1040, 720);
        BackColor = Background;
        ForeColor = Color.White;
        Font = new Font("Segoe UI", 10f);

        var header = new Panel { Dock = DockStyle.Top, Height = 112, Padding = new Padding(32, 24, 32, 12), BackColor = Background };
        var logo = new Label { Text = "MF", TextAlign = ContentAlignment.MiddleCenter, Font = new Font("Segoe UI", 16, FontStyle.Bold), BackColor = Accent, ForeColor = Color.White, Size = new Size(56, 56), Location = new Point(32, 24) };
        Round(logo, 16);
        var title = new Label { Text = "MiFlow Manager", Font = new Font("Segoe UI", 22, FontStyle.Bold), AutoSize = true, Location = new Point(104, 23) };
        var subtitle = new Label { Text = "Instale e controle o MiFlow TV pela rede", ForeColor = Muted, AutoSize = true, Location = new Point(107, 62) };
        statusLabel.Text = adb is null ? "ADB não encontrado" : "Pronto para procurar";
        statusLabel.ForeColor = adb is null ? Color.OrangeRed : Color.FromArgb(88, 214, 141);
        statusLabel.AutoSize = true;
        statusLabel.Anchor = AnchorStyles.Top | AnchorStyles.Right;
        statusLabel.Location = new Point(820, 48);
        header.Controls.AddRange(new Control[] { logo, title, subtitle, statusLabel });

        var main = new TableLayoutPanel { Dock = DockStyle.Fill, Padding = new Padding(32, 8, 32, 28), RowCount = 3, ColumnCount = 1, BackColor = Background };
        main.RowStyles.Add(new RowStyle(SizeType.Absolute, 146));
        main.RowStyles.Add(new RowStyle(SizeType.Absolute, 196));
        main.RowStyles.Add(new RowStyle(SizeType.Percent, 100));

        var connection = Card("Conexão com a Mi Box");
        deviceBox.Location = new Point(24, 58);
        deviceBox.Size = new Size(520, 32);
        deviceBox.Font = new Font("Segoe UI", 11);
        deviceBox.DropDownStyle = ComboBoxStyle.DropDown;
        deviceBox.Text = "";
        searchButton = ActionButton("Procurar na rede", false, new Point(560, 56), 158);
        connectButton = ActionButton("Conectar", true, new Point(730, 56), 130);
        searchButton.Click += async (_, _) => await SearchAsync();
        connectButton.Click += async (_, _) => await ConnectAsync();
        deviceInfo.Text = "A busca verifica apenas a porta ADB 5555 na sua rede local.";
        deviceInfo.ForeColor = Muted;
        deviceInfo.AutoSize = true;
        deviceInfo.Location = new Point(25, 103);
        progress.Location = new Point(560, 99);
        progress.Size = new Size(300, 8);
        progress.Style = ProgressBarStyle.Continuous;
        connection.Controls.AddRange(new Control[] { deviceBox, searchButton, connectButton, deviceInfo, progress });

        var actions = Card("Ações rápidas");
        installButton = ActionButton("Instalar / atualizar MiFlow", true, new Point(24, 58), 250);
        apkButton = ActionButton("Instalar outro APK", false, new Point(288, 58), 190);
        homeButton = ActionButton("Usar como tela inicial", false, new Point(492, 58), 210);
        storeButton = ActionButton("Abrir Google Play", false, new Point(716, 58), 170);
        restoreButton = ActionButton("Restaurar launcher original", false, new Point(24, 112), 250);
        installButton.Click += async (_, _) => await InstallMiFlowAsync();
        apkButton.Click += async (_, _) => await InstallOtherApkAsync();
        homeButton.Click += async (_, _) => await MakeHomeAsync();
        storeButton.Click += async (_, _) => await OpenStoreAsync();
        restoreButton.Click += async (_, _) => await RestoreAsync();
        actions.Controls.AddRange(new Control[] { installButton, apkButton, homeButton, storeButton, restoreButton });

        var logCard = Card("Atividade");
        log.Dock = DockStyle.Fill;
        log.Margin = new Padding(20, 54, 20, 18);
        log.BackColor = Color.FromArgb(15, 16, 20);
        log.ForeColor = Color.FromArgb(220, 223, 230);
        log.BorderStyle = BorderStyle.None;
        log.ReadOnly = true;
        log.Font = new Font("Cascadia Mono", 9.5f);
        logCard.Controls.Add(log);

        main.Controls.Add(connection, 0, 0);
        main.Controls.Add(actions, 0, 1);
        main.Controls.Add(logCard, 0, 2);
        Controls.Add(main);
        Controls.Add(header);

        Shown += async (_, _) =>
        {
            Append(adb is null ? "ADB não encontrado. Instale o Android SDK Platform Tools ou coloque a pasta platform-tools ao lado do programa." : $"ADB encontrado: {adb.ExecutablePath}");
            if (adb is not null) await RefreshDevicesAsync();
        };
    }

    private Panel Card(string heading)
    {
        var panel = new Panel { Dock = DockStyle.Fill, Margin = new Padding(0, 0, 0, 14), BackColor = Panel };
        var label = new Label { Text = heading, Font = new Font("Segoe UI", 13, FontStyle.Bold), AutoSize = true, Location = new Point(24, 19) };
        panel.Controls.Add(label);
        panel.Resize += (_, _) => Round(panel, 18);
        return panel;
    }

    private Button ActionButton(string text, bool primary, Point location, int width)
    {
        var button = new Button { Text = text, Location = location, Size = new Size(width, 40), FlatStyle = FlatStyle.Flat, BackColor = primary ? Accent : Color.FromArgb(43, 44, 51), ForeColor = Color.White, Cursor = Cursors.Hand };
        button.FlatAppearance.BorderSize = primary ? 0 : 1;
        button.FlatAppearance.BorderColor = Color.FromArgb(73, 75, 84);
        return button;
    }

    private static void Round(Control control, int radius)
    {
        if (control.Width <= 0 || control.Height <= 0) return;
        using var path = new GraphicsPath();
        var d = radius * 2;
        path.AddArc(0, 0, d, d, 180, 90);
        path.AddArc(control.Width - d, 0, d, d, 270, 90);
        path.AddArc(control.Width - d, control.Height - d, d, d, 0, 90);
        path.AddArc(0, control.Height - d, d, d, 90, 90);
        path.CloseFigure();
        control.Region = new Region(path);
    }

    private async Task SearchAsync()
    {
        if (!EnsureAdb() || busy) return;
        SetBusy(true, "Procurando na rede…");
        try
        {
            Append("Procurando dispositivos já conectados e Mi Boxes com ADB na porta 5555…");
            await RefreshDevicesAsync();
            var reporter = new Progress<int>(value => progress.Value = Math.Clamp(value, 0, 100));
            var endpoints = await DeviceDiscovery.ScanAdbAsync(reporter);
            foreach (var endpoint in endpoints)
                if (!deviceBox.Items.Cast<object>().Any(x => x.ToString()?.Contains(endpoint) == true)) deviceBox.Items.Add(endpoint);
            if (deviceBox.SelectedIndex < 0 && deviceBox.Items.Count > 0) deviceBox.SelectedIndex = 0;
            Append(endpoints.Count == 0 ? "Nenhuma nova porta ADB encontrada. Digite o IP da Mi Box manualmente." : $"Encontrado(s): {string.Join(", ", endpoints)}");
        }
        catch (Exception ex) { Append("Falha na busca: " + ex.Message); }
        finally { progress.Value = 0; SetBusy(false, "Busca concluída"); }
    }

    private async Task RefreshDevicesAsync()
    {
        if (adb is null) return;
        var devices = await adb.ListDevicesAsync();
        foreach (var device in devices)
            if (!deviceBox.Items.Cast<object>().Any(x => x.ToString()?.Contains(device.Serial) == true)) deviceBox.Items.Add(device);
        var connected = devices.FirstOrDefault(x => x.State == "device");
        if (connected is not null)
        {
            deviceBox.SelectedItem = deviceBox.Items.Cast<object>().First(x => x.ToString()?.Contains(connected.Serial) == true);
            deviceInfo.Text = $"Conectado: {connected.Model} — {connected.Serial}";
            statusLabel.Text = "Mi Box conectada";
            statusLabel.ForeColor = Color.FromArgb(88, 214, 141);
        }
    }

    private async Task ConnectAsync()
    {
        if (!EnsureAdb() || busy) return;
        var endpoint = SelectedEndpoint();
        if (string.IsNullOrWhiteSpace(endpoint)) { MessageBox.Show("Digite ou selecione o IP da Mi Box.", "MiFlow Manager"); return; }
        if (!endpoint.Contains(':')) endpoint += ":5555";
        SetBusy(true, "Conectando…");
        try
        {
            var result = await adb!.RunAsync(new[] { "connect", endpoint });
            Append($"> adb connect {endpoint}\n{result.Output}");
            deviceBox.Text = endpoint;
            await RefreshDevicesAsync();
            if (result.Output.Contains("unauthorized", StringComparison.OrdinalIgnoreCase))
                MessageBox.Show("Aceite a chave RSA que apareceu na televisão e clique em Conectar novamente.", "Autorize na Mi Box");
        }
        finally { SetBusy(false, "Pronto"); }
    }

    private async Task InstallMiFlowAsync()
    {
        var apk = FindMiFlowApk();
        if (apk is null)
        {
            using var picker = new OpenFileDialog { Filter = "Aplicativo Android (*.apk)|*.apk", Title = "Selecione o APK do MiFlow TV" };
            if (picker.ShowDialog(this) != DialogResult.OK) return;
            apk = picker.FileName;
        }
        await InstallApkAsync(apk);
    }

    private async Task InstallOtherApkAsync()
    {
        using var picker = new OpenFileDialog { Filter = "Aplicativo Android (*.apk)|*.apk", Title = "Instalar APK na Mi Box" };
        if (picker.ShowDialog(this) == DialogResult.OK) await InstallApkAsync(picker.FileName);
    }

    private async Task InstallApkAsync(string apk)
    {
        var device = ConnectedDevice();
        if (!EnsureAdb() || device is null || busy) return;
        SetBusy(true, "Instalando APK…");
        try
        {
            Append($"Instalando {Path.GetFileName(apk)} em {device}…");
            var result = await adb!.RunForDeviceAsync(device, "install", "-r", apk);
            Append(result.Output);
            if (result.Success && result.Output.Contains("Success", StringComparison.OrdinalIgnoreCase)) MessageBox.Show("Aplicativo instalado com sucesso.", "MiFlow Manager");
            else MessageBox.Show("A instalação não foi concluída. Consulte a área Atividade.", "MiFlow Manager");
        }
        finally { SetBusy(false, "Pronto"); }
    }

    private async Task MakeHomeAsync()
    {
        var device = ConnectedDevice();
        if (!EnsureAdb() || device is null || busy) return;
        SetBusy(true, "Configurando launcher…");
        try
        {
            var setHome = await adb!.RunForDeviceAsync(device, "shell", "cmd", "package", "set-home-activity", "com.miflow.tv/.MainActivity");
            Append(setHome.Output);
            await adb.RunForDeviceAsync(device, "shell", "input", "keyevent", "KEYCODE_HOME");
            MessageBox.Show("A Mi Box abrirá a seleção se o Android exigir confirmação. Escolha MiFlow TV e marque Sempre.", "Tela inicial");
        }
        finally { SetBusy(false, "Pronto"); }
    }

    private async Task OpenStoreAsync()
    {
        var device = ConnectedDevice();
        if (!EnsureAdb() || device is null || busy) return;
        SetBusy(true, "Abrindo Google Play…");
        try
        {
            var result = await adb!.RunForDeviceAsync(device, "shell", "monkey", "-p", "com.android.vending", "-c", "android.intent.category.LAUNCHER", "1");
            Append(result.Output);
        }
        finally { SetBusy(false, "Pronto"); }
    }

    private async Task RestoreAsync()
    {
        var device = ConnectedDevice();
        if (!EnsureAdb() || device is null || busy) return;
        var answer = MessageBox.Show("Isso reativará o launcher original. O MiFlow continuará instalado e poderá ser escolhido novamente. Continuar?", "Restaurar launcher original", MessageBoxButtons.YesNo, MessageBoxIcon.Question);
        if (answer != DialogResult.Yes) return;
        SetBusy(true, "Restaurando…");
        try
        {
            foreach (var package in new[] { "com.google.android.tvlauncher", "com.google.android.apps.tv.launcherx" })
            {
                var result = await adb!.RunForDeviceAsync(device, "shell", "pm", "enable", package);
                Append(result.Output);
            }
            await adb!.RunForDeviceAsync(device, "shell", "cmd", "package", "clear-package-preferred-activities", "com.miflow.tv");
            await adb.RunForDeviceAsync(device, "shell", "input", "keyevent", "KEYCODE_HOME");
            MessageBox.Show("Launcher original reativado. Se aparecer uma escolha na TV, selecione o launcher do sistema.", "MiFlow Manager");
        }
        finally { SetBusy(false, "Pronto"); }
    }

    private string? ConnectedDevice()
    {
        var endpoint = SelectedEndpoint();
        if (string.IsNullOrWhiteSpace(endpoint)) { MessageBox.Show("Conecte uma Mi Box primeiro.", "MiFlow Manager"); return null; }
        return endpoint.Contains('—') ? endpoint.Split('—').Last().Split('(').First().Trim() : endpoint.Trim();
    }

    private string SelectedEndpoint() => deviceBox.SelectedItem is AdbDevice device ? device.Serial : deviceBox.Text.Trim();

    private string? FindMiFlowApk()
    {
        var candidates = new[]
        {
            Path.Combine(AppContext.BaseDirectory, "payload", "MiFlow-TV.apk"),
            Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..", "..", "..", "..", "app", "build", "outputs", "apk", "debug", "app-debug.apk")),
            Path.Combine(AppContext.BaseDirectory, "MiFlow-TV.apk")
        };
        return candidates.FirstOrDefault(File.Exists);
    }

    private bool EnsureAdb()
    {
        if (adb is not null) return true;
        MessageBox.Show("ADB não encontrado. Instale o Android SDK Platform Tools ou copie a pasta platform-tools ao lado do MiFlow Manager.", "ADB necessário");
        return false;
    }

    private void SetBusy(bool value, string status)
    {
        busy = value;
        statusLabel.Text = status;
        foreach (var button in new[] { searchButton, connectButton, installButton, apkButton, homeButton, storeButton, restoreButton }) button.Enabled = !value && adb is not null;
        UseWaitCursor = value;
    }

    private void Append(string message)
    {
        if (InvokeRequired) { BeginInvoke(() => Append(message)); return; }
        log.AppendText($"[{DateTime.Now:HH:mm:ss}] {message.Trim()}\n\n");
        log.SelectionStart = log.TextLength;
        log.ScrollToCaret();
    }
}
