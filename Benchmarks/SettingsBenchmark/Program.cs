using System;
using System.Diagnostics;
using System.IO;
using System.Text.Json;
using System.Threading.Tasks;

public class AppSettings
{
    public int ServerPort { get; set; } = 5000;
    public string ServerName { get; set; } = "BenchmarkServer";
    public string JwtSecret { get; set; } = Guid.NewGuid().ToString("N");
    public int JwtExpirationDays { get; set; } = 365;
    public string? DefaultPrinter { get; set; }
    public bool AutoStart { get; set; } = false;
    public bool MinimizeToTray { get; set; } = true;
}

class Program
{
    private static readonly string SettingsFilePath = "settings.json";

    static async Task Main(string[] args)
    {
        Console.WriteLine("--- Settings I/O Benchmark ---");

        var settings = new AppSettings();
        var json = JsonSerializer.Serialize(settings, new JsonSerializerOptions { WriteIndented = true });
        File.WriteAllText(SettingsFilePath, json);

        int iterations = 100;

        // Synchronous Benchmark
        Console.WriteLine($"Running {iterations} iterations of Synchronous Load...");
        var swSync = Stopwatch.StartNew();
        for (int i = 0; i < iterations; i++)
        {
            var content = File.ReadAllText(SettingsFilePath);
            var loaded = JsonSerializer.Deserialize<AppSettings>(content);
        }
        swSync.Stop();
        Console.WriteLine($"Synchronous Total: {swSync.ElapsedMilliseconds}ms, Avg: {(double)swSync.ElapsedMilliseconds / iterations}ms");

        // Asynchronous Benchmark
        Console.WriteLine($"Running {iterations} iterations of Asynchronous Load...");
        var swAsync = Stopwatch.StartNew();
        for (int i = 0; i < iterations; i++)
        {
            using (var stream = File.OpenRead(SettingsFilePath))
            {
                var loaded = await JsonSerializer.DeserializeAsync<AppSettings>(stream);
            }
        }
        swAsync.Stop();
        Console.WriteLine($"Asynchronous Total: {swAsync.ElapsedMilliseconds}ms, Avg: {(double)swAsync.ElapsedMilliseconds / iterations}ms");

        // UI Blocking Simulation
        Console.WriteLine("\nUI Blocking Simulation (Load 1000 times):");

        var swBlocking = Stopwatch.StartNew();
        for (int i = 0; i < 1000; i++)
        {
            File.ReadAllText(SettingsFilePath);
        }
        swBlocking.Stop();
        Console.WriteLine($"Synchronous Load (1000x) blocked calling thread for: {swBlocking.ElapsedMilliseconds}ms");

        var swNonBlocking = Stopwatch.StartNew();
        var tasks = new Task[1000];
        for (int i = 0; i < 1000; i++)
        {
            tasks[i] = File.ReadAllTextAsync(SettingsFilePath);
        }
        Console.WriteLine($"Asynchronous Load (1000x) initiated in: {swNonBlocking.ElapsedMilliseconds}ms (Before awaiting all)");
        await Task.WhenAll(tasks);
        swNonBlocking.Stop();
        Console.WriteLine($"Asynchronous Load (1000x) completed in: {swNonBlocking.ElapsedMilliseconds}ms total");

        File.Delete(SettingsFilePath);
    }
}
