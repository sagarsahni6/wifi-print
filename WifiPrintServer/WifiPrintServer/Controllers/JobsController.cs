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

        var jobs = _queueManager.GetAllJobs(filter);
        return Ok(ApiResponse<List<PrintJob>>.Ok(jobs));
    }

    /// <summary>
    /// GET /api/jobs/{id} — Get a specific job by ID.
    /// </summary>
    [HttpGet("{id}")]
    public IActionResult GetById(string id)
    {
        var job = _queueManager.GetJob(id);
        if (job == null) return NotFound(ApiResponse<object>.Fail("Job not found"));
        return Ok(ApiResponse<PrintJob>.Ok(job));
    }

    /// <summary>
    /// POST /api/jobs/{id}/cancel — Cancel a job.
    /// </summary>
    [HttpPost("{id}/cancel")]
    public IActionResult Cancel(string id)
    {
        if (_queueManager.CancelJob(id))
            return Ok(ApiResponse<object>.Ok(new { }, "Job cancelled"));
        return BadRequest(ApiResponse<object>.Fail("Cannot cancel this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/retry — Retry a failed job.
    /// </summary>
    [HttpPost("{id}/retry")]
    public IActionResult Retry(string id)
    {
        if (_queueManager.RetryJob(id))
            return Ok(ApiResponse<object>.Ok(new { }, "Job requeued for retry"));
        return BadRequest(ApiResponse<object>.Fail("Cannot retry this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/priority — Set job priority (High, Normal, Low).
    /// </summary>
    [HttpPost("{id}/priority")]
    public IActionResult SetPriority(string id, [FromBody] PriorityRequest request)
    {
        if (_queueManager.SetJobPriority(id, request.Priority))
            return Ok(ApiResponse<object>.Ok(new { }, $"Priority set to {request.Priority}"));
        return BadRequest(ApiResponse<object>.Fail("Cannot change priority of this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/pause — Pause a pending job (holds it from printing).
    /// </summary>
    [HttpPost("{id}/pause")]
    public IActionResult Pause(string id)
    {
        if (_queueManager.PauseJob(id))
            return Ok(ApiResponse<object>.Ok(new { }, "Job paused"));
        return BadRequest(ApiResponse<object>.Fail("Cannot pause this job"));
    }

    /// <summary>
    /// POST /api/jobs/{id}/resume — Resume a paused job.
    /// </summary>
    [HttpPost("{id}/resume")]
    public IActionResult Resume(string id)
    {
        if (_queueManager.ResumeJob(id))
            return Ok(ApiResponse<object>.Ok(new { }, "Job resumed"));
        return BadRequest(ApiResponse<object>.Fail("Cannot resume this job"));
    }
}

/// <summary>
/// Request body for setting job priority.
/// </summary>
public class PriorityRequest
{
    public string Priority { get; set; } = "Normal";
}
