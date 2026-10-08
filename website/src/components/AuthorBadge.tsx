import { CheckCircle2, ShieldCheck, Clock, Award } from "lucide-react";

export interface AuthorBadgeProps {
  reviewedBy?: string;
  testedEnvironment?: string;
  lastUpdated?: string;
}

export function AuthorBadge({
  reviewedBy = "Printora Systems Engineering Team",
  testedEnvironment = "Windows 11 24H2, Android 14 & iOS 17.5",
  lastUpdated = "October 2026",
}: AuthorBadgeProps) {
  return (
    <div className="rounded-2xl bg-slate-50 dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 p-4 sm:p-5 text-xs text-slate-600 dark:text-slate-400">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-200/60 dark:border-slate-800">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-blue-100 dark:bg-blue-950/80 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
            <Award className="w-4 h-4" />
          </div>
          <div>
            <div className="font-bold text-slate-900 dark:text-white text-xs">
              {reviewedBy}
            </div>
            <div className="text-[11px] text-slate-500 dark:text-slate-400">
              Technical Verification &amp; Architecture Review
            </div>
          </div>
        </div>

        <div className="flex items-center gap-1.5 text-[11px] text-slate-500 dark:text-slate-400">
          <Clock className="w-3.5 h-3.5 text-blue-500" />
          <span>Last updated: {lastUpdated}</span>
        </div>
      </div>

      <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-2 text-[11px]">
        <div className="flex items-center gap-1.5">
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
          <span>Tested on: <strong className="text-slate-800 dark:text-slate-200">{testedEnvironment}</strong></span>
        </div>
        <div className="flex items-center gap-1.5">
          <ShieldCheck className="w-3.5 h-3.5 text-blue-500 shrink-0" />
          <span>Privacy verified: <strong className="text-slate-800 dark:text-slate-200">Zero cloud storage &amp; instant cleanup</strong></span>
        </div>
      </div>
    </div>
  );
}
