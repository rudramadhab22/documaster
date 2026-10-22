export default function Alert({ type = 'error', children }) {
  const styles =
    type === 'success'
      ? 'border-teal-200 bg-teal-50 text-teal-900'
      : type === 'info'
        ? 'border-slate-200 bg-slate-50 text-slate-700'
        : 'border-red-200 bg-red-50 text-red-800'

  return (
    <div className={`mt-4 rounded-xl border px-3.5 py-3 text-sm leading-relaxed ${styles}`}>
      {children}
    </div>
  )
}
