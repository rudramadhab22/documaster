import { tools } from '../data/tools'
import ToolCard from '../components/ToolCard'

export default function AllToolsPage() {
  return (
    <div>
      <div className="mb-6 rounded-3xl border border-teal-100 bg-gradient-to-br from-white to-teal-50/70 px-5 py-6 sm:px-8 sm:py-8">
        <h1 className="font-display text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl">
          All Tools
        </h1>
        <p className="mt-2 max-w-2xl text-sm text-slate-600 sm:text-base">
          Choose a tool to upload files and process them instantly.
        </p>
      </div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {tools.map((tool) => (
          <ToolCard key={tool.id} tool={tool} />
        ))}
      </div>
    </div>
  )
}
