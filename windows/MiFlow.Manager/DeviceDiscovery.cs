using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;

namespace MiFlow.Manager;

internal static class DeviceDiscovery
{
    public static IReadOnlyList<string> LocalSubnets()
    {
        var prefixes = new HashSet<string>();
        foreach (var adapter in NetworkInterface.GetAllNetworkInterfaces())
        {
            if (adapter.OperationalStatus != OperationalStatus.Up || adapter.NetworkInterfaceType == NetworkInterfaceType.Loopback) continue;
            foreach (var address in adapter.GetIPProperties().UnicastAddresses)
            {
                if (address.Address.AddressFamily != AddressFamily.InterNetwork) continue;
                var bytes = address.Address.GetAddressBytes();
                if (bytes[0] == 169 && bytes[1] == 254) continue;
                prefixes.Add($"{bytes[0]}.{bytes[1]}.{bytes[2]}");
            }
        }
        return prefixes.ToArray();
    }

    public static async Task<IReadOnlyList<string>> ScanAdbAsync(IProgress<int>? progress = null, CancellationToken cancellationToken = default)
    {
        var prefixes = LocalSubnets();
        var targets = prefixes.SelectMany(prefix => Enumerable.Range(1, 254).Select(i => $"{prefix}.{i}")).ToArray();
        var found = new List<string>();
        var gate = new object();
        var done = 0;

        await Parallel.ForEachAsync(targets,
            new ParallelOptions { MaxDegreeOfParallelism = 80, CancellationToken = cancellationToken },
            async (ip, token) =>
            {
                using var client = new TcpClient();
                try
                {
                    using var timeout = CancellationTokenSource.CreateLinkedTokenSource(token);
                    timeout.CancelAfter(280);
                    await client.ConnectAsync(IPAddress.Parse(ip), 5555, timeout.Token);
                    lock (gate) found.Add($"{ip}:5555");
                }
                catch (OperationCanceledException) when (!token.IsCancellationRequested) { }
                catch (SocketException) { }
                finally
                {
                    var current = Interlocked.Increment(ref done);
                    if (targets.Length > 0) progress?.Report(current * 100 / targets.Length);
                }
            });
        return found.OrderBy(x => x, StringComparer.OrdinalIgnoreCase).ToArray();
    }
}
