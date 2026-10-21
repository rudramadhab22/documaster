import { tools } from '../data/tools'
import ToolCard from '../components/ToolCard'

export default function AllToolsPage() {
  return (
    <div>
      <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
        All Tools
      </h1>
      <p className="mt-2 text-slate-600">
        Choose a tool to upload files and process them with the DocuMaster API.
      </p>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {tools.map((tool) => (
          <ToolCard key={tool.id} tool={tool} />
        ))}
      </div>
    </div>
  )
}
