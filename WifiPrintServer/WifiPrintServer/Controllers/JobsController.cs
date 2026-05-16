using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WifiPrintServer.Models;
using WifiPrintServer.Services;

namespace WifiPrintServer.Controllers;

/// <summary>
/// Manages print jobs — list, cancel, retry, reorder, pause, resume, priority.
/// </summary>
[Authorize]
[ApiController]
[Route("api/[controller]")]
public class JobsController : ControllerBase
{
    private readonly PrintQueueManager _queueManager;

    public JobsController(PrintQueueManager queueManager)
    {
        _queueManager = queueManager;
    }

    /// <summary>
    /// GET /api/jobs — List all jobs, optionally filter by status.
    /// </summary>
    [HttpGet]
    public IActionResult GetAll([FromQuery] string? status)
    {
        PrintJobStatus? filter = null;
        if (!string.IsNullOrEmpty(status) && Enum.TryParse<PrintJobStatus>(status, true, out var s))
            filter = s;

        var deviceId = User.FindFirst("deviceId")?.Value;
        var jobs = _queueManager.GetAllJobs(filter)
            .Where(job => string.Equals(job.DeviceId, deviceId, StringComparison.OrdinalIgnoreCase))
            .ToList();
        return Ok(ApiResponse<List<PrintJob>>.Ok(jobs));
    }

    /// <summary>
    /// GET /api/jobs/{id} — Get a specific job by ID.
    /// </summary>
    [HttpGet("{id}")]
    public IActionResult GetById(string id)
    {
        var job = _queueManager.GetJob(id);
        var deviceId = User.FindFirst("deviceId")?.Value;
        if (job == null || !string.Equals(job.DeviceId, deviceId, StringComparison.OrdinalIgnoreCase))
            return NotFound(ApiResponse<object>.Fail("Job not found"));
        return Ok(ApiResponse<PrintJob>.Ok(job));
    }

    /// <summary>
    /// POST /api/jobs/{id}/cancel — Cancel a job.
    /// </summary>
    [HttpPost("{id}/cancel")]
    public IActionResult Cancel(string id)
    {
        if (!CanAccessJob(id))
            return NotFound(ApiResponse<object>.Fail("Job not found"));

        if (_queueManager.CancelJob(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Job cancelled"));
        return BadRequest(ApiResponse<object>.Fail("Cannot cancel this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/retry — Retry a failed job.
    /// </summary>
    [HttpPost("{id}/retry")]
    public IActionResult Retry(string id)
    {
        if (!CanAccessJob(id))
            return NotFound(ApiResponse<object>.Fail("Job not found"));

        if (_queueManager.RetryJob(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Job requeued for retry"));
        return BadRequest(ApiResponse<object>.Fail("Cannot retry this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/priority — Set job priority (High, Normal, Low).
    /// </summary>
    [HttpPost("{id}/priority")]
    public IActionResult SetPriority(string id, [FromBody] PriorityRequest request)
    {
        if (!CanAccessJob(id))
            return NotFound(ApiResponse<object>.Fail("Job not found"));

        if (_queueManager.SetJobPriority(id, request.Priority))
            return Ok(ApiResponse<object>.Ok(new object(), $"Priority set to {request.Priority}"));
        return BadRequest(ApiResponse<object>.Fail("Cannot change priority of this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/pause — Pause a pending job (holds it from printing).
    /// </summary>
    [HttpPost("{id}/pause")]
    public IActionResult Pause(string id)
    {
        if (!CanAccessJob(id))
            return NotFound(ApiResponse<object>.Fail("Job not found"));

        if (_queueManager.PauseJob(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Job paused"));
        return BadRequest(ApiResponse<object>.Fail("Cannot pause this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/resume — Resume a paused job.
    /// </summary>
    [HttpPost("{id}/resume")]
    public IActionResult Resume(string id)
    {
        if (!CanAccessJob(id))
            return NotFound(ApiResponse<object>.Fail("Job not found"));

        if (_queueManager.ResumeJob(id))
            return Ok(ApiResponse<object>.Ok(new object(), "Job resumed"));
        return BadRequest(ApiResponse<object>.Fail("Cannot resume this job"));
    }

    private bool CanAccessJob(string jobId)
    {
        var deviceId = User.FindFirst("deviceId")?.Value;
        var job = _queueManager.GetJob(jobId);
        return job != null && string.Equals(job.DeviceId, deviceId, StringComparison.OrdinalIgnoreCase);
    }
}

/// <summary>
/// Request body for setting job priority.
/// </summary>
public class PriorityRequest
{
    public string Priority { get; set; } = "Normal";
}
