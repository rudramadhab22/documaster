export default function ActionButton({
  children,
  onClick,
  disabled,
  loading,
  variant = 'primary',
}) {
  const base =
    'inline-flex w-full min-h-12 items-center justify-center rounded-xl px-5 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto'
  const styles =
    variant === 'secondary'
      ? 'border border-slate-300 bg-white text-slate-800 hover:bg-slate-50'
      : 'bg-[var(--forest)] text-white shadow-sm hover:bg-teal-800'

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled || loading}
      className={`${base} ${styles}`}
    >
      {loading ? 'Processing…' : children}
    </button>
  )
}
