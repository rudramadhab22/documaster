import { Link } from 'react-router-dom'

export default function ToolPage({ title, description, children }) {
  return (
    <div className="mx-auto max-w-3xl">
      <Link
        to="/tools"
        className="inline-flex items-center gap-1.5 text-sm font-semibold text-teal-700 transition hover:text-teal-900"
      >
        <span aria-hidden="true">←</span> All Tools
      </Link>
      <div className="mt-4 rounded-3xl border border-teal-100 bg-gradient-to-br from-white via-white to-teal-50/60 px-5 py-6 sm:px-7 sm:py-7">
        <h1 className="font-display text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl">
          {title}
        </h1>
        <p className="mt-2 max-w-2xl text-sm leading-relaxed text-slate-600 sm:text-base">
          {description}
        </p>
      </div>
      <div className="mt-4 rounded-3xl border border-slate-200/90 bg-white p-4 shadow-[0_10px_30px_rgba(16,42,67,0.06)] sm:mt-5 sm:p-7">
        {children}
      </div>
    </div>
  )
}
