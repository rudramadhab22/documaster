import { useRef, useState } from 'react'

export default function FileDropzone({
  accept,
  multiple = false,
  onFiles,
  label = 'Drag and drop files here, or tap to browse',
  hint,
}) {
  const inputRef = useRef(null)
  const [dragging, setDragging] = useState(false)

  function handleFiles(fileList) {
    const files = Array.from(fileList || [])
    if (files.length === 0) return
    onFiles(multiple ? files : files.slice(0, 1))
  }

  return (
    <div
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') inputRef.current?.click()
      }}
      onClick={() => inputRef.current?.click()}
      onDragOver={(e) => {
        e.preventDefault()
        setDragging(true)
      }}
      onDragLeave={() => setDragging(false)}
      onDrop={(e) => {
        e.preventDefault()
        setDragging(false)
        handleFiles(e.dataTransfer.files)
      }}
      className={`cursor-pointer rounded-2xl border-2 border-dashed px-4 py-9 text-center transition sm:px-6 sm:py-11 ${
        dragging
          ? 'border-teal-500 bg-teal-50'
          : 'border-slate-300 bg-gradient-to-b from-slate-50 to-white hover:border-teal-400 hover:from-teal-50/40'
      }`}
    >
      <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-[#0b3d3a] to-teal-600 text-white shadow-md">
        <svg className="h-6 w-6" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M12 16V4M8 8l4-4 4 4M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2" />
        </svg>
      </div>
      <p className="text-sm font-semibold text-slate-800 sm:text-base">{label}</p>
      {hint && <p className="mt-2 text-xs text-slate-500 sm:text-sm">{hint}</p>}
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        className="hidden"
        onChange={(e) => {
          handleFiles(e.target.files)
          e.target.value = ''
        }}
      />
    </div>
  )
}
