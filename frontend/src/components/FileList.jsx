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
    <ul className="mt-4 space-y-2">
      {files.map((file, index) => (
        <li
          key={`${file.name}-${file.size}-${index}`}
          className="flex items-center gap-3 rounded-lg border border-slate-200 bg-white px-3 py-2"
        >
          {previewUrls[index] && (
            <img
              src={previewUrls[index]}
              alt={file.name}
              className="h-12 w-12 rounded object-cover"
            />
          )}
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-slate-800">{file.name}</p>
            <p className="text-xs text-slate-500">{formatBytes(file.size)}</p>
          </div>
          {showReorder && (
            <div className="flex gap-1">
              <button
                type="button"
                disabled={index === 0}
                onClick={() => onMoveUp?.(index)}
                className="rounded border border-slate-200 px-2 py-1 text-xs disabled:opacity-40"
              >
                Up
              </button>
              <button
                type="button"
                disabled={index === files.length - 1}
                onClick={() => onMoveDown?.(index)}
                className="rounded border border-slate-200 px-2 py-1 text-xs disabled:opacity-40"
              >
                Down
              </button>
            </div>
          )}
          <button
            type="button"
            onClick={() => onRemove(index)}
            className="rounded px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-50"
          >
            Remove
          </button>
        </li>
      ))}
    </ul>
  )
}
