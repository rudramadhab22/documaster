import { Link } from 'react-router-dom'

const accents = {
  'pdf-compressor': 'from-teal-700 to-teal-500',
  'image-to-pdf': 'from-emerald-700 to-teal-500',
  'pdf-merger': 'from-[#0b3d3a] to-teal-600',
  'pdf-splitter': 'from-cyan-800 to-teal-500',
  'image-compressor': 'from-slate-700 to-teal-600',
  'pdf-to-image': 'from-teal-800 to-emerald-500',
  'excel-to-pdf': 'from-[#1f6f4a] to-teal-500',
  'excel-to-csv': 'from-[#1b5e4a] to-emerald-500',
  'csv-to-excel': 'from-[#14532d] to-teal-600',
  'excel-to-images': 'from-[#0f766e] to-cyan-600',
  'excel-to-pdfs': 'from-[#115e59] to-teal-500',
}

export default function ToolCard({ tool }) {
  const accent = accents[tool.id] || 'from-teal-700 to-teal-500'

  return (
    <Link
      to={tool.path}
      className="tool-card group flex h-full flex-col rounded-2xl border border-slate-200/90 bg-white p-5 sm:p-6"
    >
      <div className="mb-4 flex items-start justify-between gap-3">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br ${accent} text-white shadow-sm`}
        >
          <ToolIcon id={tool.id} />
        </div>
        <span className="tool-card-arrow mt-1 text-teal-700" aria-hidden="true">
          →
        </span>
      </div>
      <h3 className="mb-2 text-lg font-semibold tracking-tight text-slate-900">{tool.title}</h3>
      <p className="text-sm leading-relaxed text-slate-600">{tool.description}</p>
    </Link>
  )
}

function ToolIcon({ id }) {
  const common = 'h-5 w-5'
  switch (id) {
    case 'pdf-compressor':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M4 14h6v6M20 10h-6V4M14 10l7-7M10 14L3 21" />
        </svg>
      )
    case 'image-to-pdf':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <circle cx="9" cy="9" r="2" />
          <path d="M21 15l-5-5L5 21" />
        </svg>
      )
    case 'pdf-merger':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01" />
        </svg>
      )
    case 'pdf-splitter':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="6" cy="6" r="3" />
          <circle cx="6" cy="18" r="3" />
          <path d="M20 4L8.12 15.88M14.47 14.48L20 20M8.12 8.12L12 12" />
        </svg>
      )
    case 'image-compressor':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M12 20V10M18 20V4M6 20v-4" />
        </svg>
      )
    case 'pdf-to-image':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <path d="M14 2v6h6M9 15l2 2 4-4" />
        </svg>
      )
    case 'excel-to-pdf':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M4 4h16v16H4zM4 9h16M9 4v16" />
        </svg>
      )
    case 'excel-to-csv':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <path d="M14 2v6h6M8 13h8M8 17h5" />
        </svg>
      )
    case 'csv-to-excel':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M3 5h18v14H3zM3 10h18M9 5v14" />
        </svg>
      )
    case 'excel-to-images':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <rect x="3" y="5" width="18" height="14" rx="2" />
          <circle cx="9" cy="10" r="1.5" />
          <path d="M21 16l-4-4-6 6" />
        </svg>
      )
    case 'excel-to-pdfs':
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M8 3h7l5 5v13H8zM15 3v5h5M10 13h6M10 17h4" />
        </svg>
      )
    default:
      return (
        <svg className={common} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
          <path d="M14 2v6h6" />
        </svg>
      )
  }
}
