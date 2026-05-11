using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

/// <summary>
/// Lists available printers and manages default printer settings.
/// </summary>
[ApiController]
[Route("api/[controller]")]
public class PrintersController : ControllerBase
{
    private readonly PrinterService _printerService;

    public PrintersController(PrinterService printerService)
    {
        _printerService = printerService;
    }

    /// <summary>
    /// GET /api/printers — Returns all installed printers.
    /// </summary>
    [AllowAnonymous]
    [HttpGet]
    public IActionResult GetAll()
    {
        var printers = _printerService.GetAllPrinters();
        return Ok(ApiResponse<List<PrinterInfo>>.Ok(printers));
    }

    /// <summary>
    /// GET /api/printers/{id} — Returns a specific printer with capabilities.
    /// </summary>
    [AllowAnonymous]
    [HttpGet("{id}")]
    public IActionResult GetById(string id)
    {
        var printer = _printerService.GetPrinter(id);
        if (printer == null)
            return NotFound(ApiResponse<object>.Fail("Printer not found"));
        return Ok(ApiResponse<PrinterInfo>.Ok(printer));
    }

    /// <summary>
    /// POST /api/printers/default — Set the default printer for WiFi Print.
    /// Expects a JSON body: { "printerName": "HP LaserJet Pro" }
    /// </summary>
    [AllowAnonymous]
    [HttpPost("default")]
    public async Task<IActionResult> SetDefault([FromBody] SetDefaultPrinterRequest request)
    {
        if (string.IsNullOrEmpty(request.PrinterName))
            return BadRequest(ApiResponse<object>.Fail("Printer name required"));

        // Verify the printer exists
        var allPrinters = _printerService.GetAllPrinters();
        var match = allPrinters.FirstOrDefault(p => p.Name == request.PrinterName);
        if (match == null)
            return NotFound(ApiResponse<object>.Fail($"Printer '{request.PrinterName}' not found"));

        await _printerService.SetDefaultPrinterAsync(request.PrinterName);
        return Ok(ApiResponse<object>.Ok(null, $"Default printer set to: {request.PrinterName}"));
    }
}

public class SetDefaultPrinterRequest
{
    public string PrinterName { get; set; } = string.Empty;
}
