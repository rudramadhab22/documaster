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
      <section className="hero-panel animate-rise mb-8 rounded-3xl px-5 py-8 text-white sm:mb-10 sm:px-10 sm:py-12">
        <div className="relative z-10 mx-auto max-w-3xl text-center">
          <h1 className="font-display text-4xl font-semibold tracking-tight sm:text-6xl">
            DocuMaster
          </h1>
          <p className="animate-rise-delay mx-auto mt-3 max-w-xl text-sm text-teal-50/95 sm:text-lg">
            All your PDF, image, and Excel tools in one place.
          </p>
          <div className="animate-rise-delay-2 mx-auto mt-6 max-w-xl sm:mt-8">
            <label htmlFor="tool-search" className="sr-only">
              Search tools
            </label>
            <div className="rounded-2xl bg-white/15 p-1.5 ring-1 ring-white/25 backdrop-blur">
              <input
                id="tool-search"
                type="search"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search PDF, Excel, image tools…"
                className="w-full rounded-xl border-0 bg-white px-4 py-3.5 text-sm text-slate-900 outline-none ring-0 placeholder:text-slate-400 sm:text-base"
              />
            </div>
          </div>
        </div>
      </section>

      <section>
        <div className="mb-4 flex items-end justify-between gap-3 sm:mb-5">
          <h2 className="font-display text-xl font-semibold tracking-tight text-slate-900 sm:text-2xl">
            Tools
          </h2>
          <p className="text-xs font-medium text-slate-500 sm:text-sm">
            {filtered.length} available
          </p>
        </div>
        {filtered.length === 0 ? (
          <p className="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-600">
            No tools match your search.
          </p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((tool) => (
              <ToolCard key={tool.id} tool={tool} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
