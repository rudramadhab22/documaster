import { formatBytes } from '../api/client'

export default function FileList({
  files,
  onRemove,
  onMoveUp,
  onMoveDown,
  showReorder = false,
  previewUrls = {},
}) {
  if (!files.length) return null

  return (
    <ul className="mt-4 space-y-2.5">
      {files.map((file, index) => (
        <li
          key={`${file.name}-${file.size}-${index}`}
          className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50/80 px-3.5 py-3.5 sm:flex-row sm:items-center"
        >
          <div className="flex min-w-0 flex-1 items-center gap-3">
            {previewUrls[index] && (
              <img
                src={previewUrls[index]}
                alt={file.name}
                className="h-12 w-12 shrink-0 rounded-xl object-cover ring-1 ring-slate-200"
              />
            )}
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-slate-800">{file.name}</p>
              <p className="text-xs text-slate-500">{formatBytes(file.size)}</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2 sm:justify-end">
            {showReorder && (
              <>
                <button
                  type="button"
                  disabled={index === 0}
                  onClick={() => onMoveUp?.(index)}
                  className="min-h-10 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700 disabled:opacity-40"
                >
                  Up
                </button>
                <button
                  type="button"
                  disabled={index === files.length - 1}
                  onClick={() => onMoveDown?.(index)}
                  className="min-h-10 rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-700 disabled:opacity-40"
                >
                  Down
                </button>
              </>
            )}
            <button
              type="button"
              onClick={() => onRemove(index)}
              className="min-h-10 rounded-xl px-3 py-2 text-xs font-semibold text-red-700 hover:bg-red-50"
            >
              Remove
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}
