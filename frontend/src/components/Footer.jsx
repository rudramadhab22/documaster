export default function Footer() {
  return (
    <footer className="mt-auto border-t border-slate-200 bg-white">
      <div className="mx-auto max-w-6xl px-4 py-6 text-center text-sm text-slate-500">
        <p>© {new Date().getFullYear()} DocuMaster. All rights reserved.</p>
      </div>
    </footer>
  )
}
