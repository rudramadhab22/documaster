import { useMemo, useState } from 'react'
import { tools } from '../data/tools'
import ToolCard from '../components/ToolCard'

export default function HomePage() {
  const [query, setQuery] = useState('')

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase()
    if (!q) return tools
    return tools.filter(
      (tool) =>
        tool.title.toLowerCase().includes(q) ||
        tool.description.toLowerCase().includes(q) ||
        tool.keywords.some((k) => k.includes(q)),
    )
  }, [query])

  return (
    <div>
      <section className="mb-10 text-center sm:mb-12">
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
          All Your PDF and Image Tools in One Place
        </h1>
        <p className="mx-auto mt-3 max-w-2xl text-slate-600">
          Compress, convert, merge, and split documents locally through DocuMaster.
        </p>
        <div className="mx-auto mt-6 max-w-xl">
          <label htmlFor="tool-search" className="sr-only">
            Search tools
          </label>
          <input
            id="tool-search"
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search tools…"
            className="w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm shadow-sm outline-none ring-teal-600 focus:ring-2"
          />
        </div>
      </section>

      <section>
        <div className="mb-4 flex items-end justify-between gap-3">
          <h2 className="text-lg font-semibold text-slate-900">Tools</h2>
          <p className="text-sm text-slate-500">{filtered.length} available</p>
        </div>
        {filtered.length === 0 ? (
          <p className="rounded-xl border border-slate-200 bg-white p-6 text-sm text-slate-600">
            No tools match your search.
          </p>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((tool) => (
              <ToolCard key={tool.id} tool={tool} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
