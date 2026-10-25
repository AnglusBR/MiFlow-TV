using System.Diagnostics;
using System.Text;

namespace MiFlow.Manager;

internal sealed record AdbResult(int ExitCode, string Output)
{
    public bool Success => ExitCode == 0;
}

internal sealed class AdbClient
{
    public string ExecutablePath { get; }

    private AdbClient(string executablePath) => ExecutablePath = executablePath;

    public static AdbClient? Locate()
    {
        var candidates = new List<string>();
        var baseDir = AppContext.BaseDirectory;
        candidates.Add(Path.Combine(baseDir, "platform-tools", "adb.exe"));
        candidates.Add(Path.Combine(baseDir, "adb.exe"));

        foreach (var variable in new[] { "ANDROID_HOME", "ANDROID_SDK_ROOT" })
        {
            var root = Environment.GetEnvironmentVariable(variable);
            if (!string.IsNullOrWhiteSpace(root)) candidates.Add(Path.Combine(root, "platform-tools", "adb.exe"));
        }

        var local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        candidates.Add(Path.Combine(local, "Android", "Sdk", "platform-tools", "adb.exe"));

        var path = Environment.GetEnvironmentVariable("PATH") ?? string.Empty;
        candidates.AddRange(path.Split(Path.PathSeparator, StringSplitOptions.RemoveEmptyEntries)
            .Select(part => Path.Combine(part.Trim(), "adb.exe")));

        var found = candidates.FirstOrDefault(File.Exists);
        return found is null ? null : new AdbClient(found);
    }

    public async Task<AdbResult> RunAsync(IEnumerable<string> arguments, CancellationToken cancellationToken = default)
    {
        var start = new ProcessStartInfo
        {
            FileName = ExecutablePath,
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            StandardOutputEncoding = Encoding.UTF8,
            StandardErrorEncoding = Encoding.UTF8
        };
        foreach (var argument in arguments) start.ArgumentList.Add(argument);

        using var process = new Process { StartInfo = start };
        process.Start();
        var stdout = process.StandardOutput.ReadToEndAsync(cancellationToken);
        var stderr = process.StandardError.ReadToEndAsync(cancellationToken);
        await process.WaitForExitAsync(cancellationToken);
        var output = ((await stdout) + Environment.NewLine + (await stderr)).Trim();
        return new AdbResult(process.ExitCode, output);
    }

    public Task<AdbResult> RunForDeviceAsync(string device, params string[] arguments)
    {
        var all = new List<string> { "-s", device };
        all.AddRange(arguments);
        return RunAsync(all);
    }

    public async Task<IReadOnlyList<AdbDevice>> ListDevicesAsync()
    {
        var result = await RunAsync(new[] { "devices", "-l" });
        var devices = new List<AdbDevice>();
        foreach (var raw in result.Output.Split('\n'))
        {
            var line = raw.Trim();
            if (line.Length == 0 || line.StartsWith("List of devices", StringComparison.OrdinalIgnoreCase)) continue;
            var parts = line.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
            if (parts.Length < 2) continue;
            var model = parts.FirstOrDefault(p => p.StartsWith("model:"))?.Substring(6).Replace('_', ' ') ?? "Android TV";
            devices.Add(new AdbDevice(parts[0], parts[1], model));
        }
        return devices;
    }
}

internal sealed record AdbDevice(string Serial, string State, string Model)
{
    public override string ToString() => $"{Model} — {Serial} ({State})";
}
