export default function ActionButton({
  children,
  onClick,
  disabled,
  loading,
  variant = 'primary',
}) {
  const base =
    'inline-flex items-center justify-center rounded-lg px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50'
  const styles =
    variant === 'secondary'
      ? 'border border-slate-300 bg-white text-slate-800 hover:bg-slate-50'
      : 'bg-teal-700 text-white hover:bg-teal-600'

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
