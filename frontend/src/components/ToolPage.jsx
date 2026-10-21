import { Link } from 'react-router-dom'

export default function ToolPage({ title, description, children }) {
  return (
    <div>
      <Link to="/tools" className="text-sm font-medium text-teal-700 hover:text-teal-800">
        ← All Tools
      </Link>
      <h1 className="mt-3 text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
        {title}
      </h1>
      <p className="mt-2 max-w-2xl text-slate-600">{description}</p>
      <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50/80 p-5 sm:p-6">
        {children}
      </div>
    </div>
  )
}
