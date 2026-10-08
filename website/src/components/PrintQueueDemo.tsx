"use client";

import { useState } from "react";
import { 
  ListOrdered, 
  Play, 
  Pause, 
  RotateCcw, 
  Trash2, 
  ArrowUp, 
  ArrowDown, 
  CheckCircle2, 
  FileText
} from "lucide-react";

type QueueJob = {
  id: string;
  name: string;
  user: string;
  priority: "High" | "Normal" | "Low";
  status: "Printing" | "Queued" | "Paused" | "Failed";
  progress: number;
  pages: number;
};

const INITIAL_JOBS: QueueJob[] = [
  { id: "job-1", name: "Invoice_#8201.pdf", user: "Android (Pixel 8)", priority: "High", status: "Printing", progress: 72, pages: 2 },
  { id: "job-2", name: "Contract_Final.docx", user: "iPhone (Web Print)", priority: "Normal", status: "Queued", progress: 0, pages: 14 },
  { id: "job-3", name: "Product_Photo_HD.jpg", user: "Android (Galaxy S23)", priority: "Low", status: "Queued", progress: 0, pages: 1 },
];

export function PrintQueueDemo() {
  const [jobs, setJobs] = useState<QueueJob[]>(INITIAL_JOBS);

  // Toggle pause/resume
  const togglePause = (id: string) => {
    setJobs((prev) =>
      prev.map((job) => {
        if (job.id === id) {
          if (job.status === "Paused") {
            return { ...job, status: job.progress > 0 ? "Printing" : "Queued" };
          } else {
            return { ...job, status: "Paused" };
          }
        }
        return job;
      })
    );
  };

  // Cancel / remove job
  const cancelJob = (id: string) => {
    setJobs((prev) => prev.filter((j) => j.id !== id));
  };

  // Change priority
  const boostPriority = (id: string) => {
    setJobs((prev) =>
      prev.map((job) => {
        if (job.id === id) {
          const nextPriority = job.priority === "Low" ? "Normal" : job.priority === "Normal" ? "High" : "Low";
          return { ...job, priority: nextPriority };
        }
        return job;
      })
    );
  };

  // Move up in queue
  const moveJob = (index: number, direction: -1 | 1) => {
    const newIndex = index + direction;
    if (newIndex < 0 || newIndex >= jobs.length) return;
    const newJobs = [...jobs];
    const temp = newJobs[index];
    newJobs[index] = newJobs[newIndex];
    newJobs[newIndex] = temp;
    setJobs(newJobs);
  };

  const resetJobs = () => {
    setJobs(INITIAL_JOBS);
  };

  return (
    <section className="py-20 sm:py-28 bg-slate-50/70 dark:bg-slate-900/40 border-t border-slate-200/80 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300 text-xs font-semibold mb-3">
            <span>Interactive Spooler Queue</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-extrabold text-slate-900 dark:text-white tracking-tight">
            Control the queue instead of blindly sending jobs.
          </h2>
          <p className="mt-3 text-base text-slate-600 dark:text-slate-400">
            Pause stuck prints, prioritize urgent documents, retry failed jobs, or reorder the queue directly from the Windows desktop host.
          </p>
        </div>

        {/* Interactive Queue Container */}
        <div className="max-w-4xl mx-auto rounded-3xl bg-slate-950 border border-slate-800 p-6 sm:p-8 text-white shadow-2xl">
          {/* Header */}
          <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-slate-800">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-blue-600/20 text-blue-400 flex items-center justify-center">
                <ListOrdered className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-slate-100">Live Print Queue Manager</h3>
                <span className="text-xs text-slate-400">{jobs.length} Active Print Jobs in Memory</span>
              </div>
            </div>

            <button
              type="button"
              onClick={resetJobs}
              className="text-xs px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              Reset Demo
            </button>
          </div>

          {/* Job List */}
          <div className="divide-y divide-slate-800/80 my-4">
            {jobs.length === 0 ? (
              <div className="py-12 text-center text-slate-400">
                <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto mb-2" />
                <p className="text-sm font-medium">Queue is empty. All documents printed!</p>
                <button
                  type="button"
                  onClick={resetJobs}
                  className="mt-3 text-xs text-blue-400 underline cursor-pointer"
                >
                  Restore sample jobs
                </button>
              </div>
            ) : (
              jobs.map((job, idx) => (
                <div 
                  key={job.id} 
                  className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-slate-900/40 px-3 rounded-2xl transition-colors"
                >
                  {/* Left: Job Info */}
                  <div className="flex items-start gap-3">
                    <span className="text-xs font-mono text-slate-400 mt-1 w-5">
                      #{idx + 1}
                    </span>
                    <div className="w-8 h-8 rounded-lg bg-slate-800 text-slate-300 flex items-center justify-center shrink-0 mt-0.5">
                      <FileText className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-slate-100">{job.name}</span>
                        <button
                          type="button"
                          onClick={() => boostPriority(job.id)}
                          className={`text-[10px] px-2 py-0.5 rounded-full font-mono cursor-pointer transition-colors border ${
                            job.priority === "High"
                              ? "bg-red-500/20 text-red-400 border-red-500/30"
                              : job.priority === "Normal"
                              ? "bg-blue-500/20 text-blue-400 border-blue-500/30"
                              : "bg-slate-700 text-slate-300 border-slate-600"
                          }`}
                          title={`Click to cycle priority for ${job.name}`}
                        >
                          {job.priority} Priority
                        </button>
                      </div>
                      <div className="text-xs text-slate-400 mt-0.5 flex items-center gap-2">
                        <span>Device: {job.user}</span>
                        <span>•</span>
                        <span>{job.pages} Pages</span>
                      </div>

                      {/* Progress bar if printing */}
                      {job.status === "Printing" && (
                        <div className="mt-2 w-48 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div 
                            className="bg-blue-500 h-full rounded-full transition-all duration-300" 
                            style={{ width: `${job.progress}%` }}
                          />
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Right: Status badge & Actions */}
                  <div className="flex items-center gap-2 self-end sm:self-center">
                    <span className={`text-xs px-2.5 py-1 rounded-xl font-medium ${
                      job.status === "Printing"
                        ? "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20"
                        : job.status === "Paused"
                        ? "bg-amber-500/10 text-amber-400 border border-amber-500/20"
                        : "bg-slate-800 text-slate-300"
                    }`}>
                      {job.status === "Printing" ? `Printing ${job.progress}%` : job.status}
                    </span>

                    {/* Pause / Resume */}
                    <button
                      type="button"
                      onClick={() => togglePause(job.id)}
                      className="min-w-[36px] min-h-[36px] p-2 flex items-center justify-center rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 transition-colors cursor-pointer"
                      title={job.status === "Paused" ? "Resume Job" : "Pause Job"}
                      aria-label={job.status === "Paused" ? `Resume ${job.name}` : `Pause ${job.name}`}
                    >
                      {job.status === "Paused" ? <Play className="w-4 h-4 text-emerald-400" /> : <Pause className="w-4 h-4 text-amber-400" />}
                    </button>

                    {/* Reorder Up */}
                    <button
                      type="button"
                      onClick={() => moveJob(idx, -1)}
                      disabled={idx === 0}
                      className="min-w-[36px] min-h-[36px] p-2 flex items-center justify-center rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors cursor-pointer"
                      title="Move Up"
                      aria-label={`Move ${job.name} up in queue`}
                    >
                      <ArrowUp className="w-4 h-4" />
                    </button>

                    {/* Reorder Down */}
                    <button
                      type="button"
                      onClick={() => moveJob(idx, 1)}
                      disabled={idx === jobs.length - 1}
                      className="min-w-[36px] min-h-[36px] p-2 flex items-center justify-center rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors cursor-pointer"
                      title="Move Down"
                      aria-label={`Move ${job.name} down in queue`}
                    >
                      <ArrowDown className="w-4 h-4" />
                    </button>

                    {/* Cancel */}
                    <button
                      type="button"
                      onClick={() => cancelJob(job.id)}
                      className="min-w-[36px] min-h-[36px] p-2 flex items-center justify-center rounded-lg bg-red-950/40 hover:bg-red-900/60 text-red-400 transition-colors cursor-pointer"
                      title="Cancel Job"
                      aria-label={`Cancel print job ${job.name}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Footer note */}
          <div className="pt-4 border-t border-slate-800/80 flex flex-wrap items-center justify-between text-xs text-slate-400">
            <span>Supports parallel processing across multiple Windows printers</span>
            <span className="text-blue-400">Try reordering or clicking Priority badges</span>
          </div>
        </div>
      </div>
    </section>
  );
}
