import { Link, NavLink } from 'react-router-dom'

export default function Navbar() {
  const linkClass = ({ isActive }) =>
    `text-sm font-medium transition-colors ${
      isActive ? 'text-teal-700' : 'text-slate-600 hover:text-teal-700'
    }`

  return (
    <header className="border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <Link to="/" className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-teal-700 text-sm font-bold text-white">
            DM
          </span>
          <span className="text-lg font-semibold tracking-tight text-slate-900">
            DocuMaster
          </span>
        </Link>
        <nav className="flex items-center gap-6">
          <NavLink to="/" end className={linkClass}>
            Home
          </NavLink>
          <NavLink to="/tools" className={linkClass}>
            All Tools
          </NavLink>
        </nav>
      </div>
    </header>
  )
}
