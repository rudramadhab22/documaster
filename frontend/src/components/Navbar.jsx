import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'

export default function Navbar() {
  const [open, setOpen] = useState(false)

  const linkClass = ({ isActive }) =>
    `block rounded-lg px-3 py-2.5 text-sm font-semibold transition-colors sm:px-1 sm:py-1 ${
      isActive
        ? 'bg-teal-50 text-teal-800 sm:bg-transparent sm:text-teal-700'
        : 'text-slate-600 hover:bg-slate-50 hover:text-teal-800 sm:hover:bg-transparent'
    }`

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200/70 bg-[#f7faf9]/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3.5 sm:px-6">
        <Link to="/" className="flex min-w-0 items-center gap-3" onClick={() => setOpen(false)}>
          <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-[var(--forest)] text-sm font-bold text-white shadow-sm">
            DM
          </span>
          <span className="font-display truncate text-xl font-semibold tracking-tight text-[var(--ink)]">
            DocuMaster
          </span>
        </Link>

        <button
          type="button"
          className="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 sm:hidden"
          aria-expanded={open}
          aria-label="Toggle navigation"
          onClick={() => setOpen((value) => !value)}
        >
          <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            {open ? <path d="M6 6l12 12M18 6L6 18" /> : <path d="M4 7h16M4 12h16M4 17h16" />}
          </svg>
        </button>

        <nav
          className={`${
            open
              ? 'absolute inset-x-0 top-full block border-b border-slate-200 bg-white/95 px-4 py-3 shadow-lg backdrop-blur'
              : 'hidden'
          } sm:static sm:flex sm:items-center sm:gap-7 sm:border-0 sm:bg-transparent sm:p-0 sm:shadow-none`}
        >
          <NavLink to="/" end className={linkClass} onClick={() => setOpen(false)}>
            Home
          </NavLink>
          <NavLink to="/tools" className={linkClass} onClick={() => setOpen(false)}>
            All Tools
          </NavLink>
        </nav>
      </div>
    </header>
  )
}
