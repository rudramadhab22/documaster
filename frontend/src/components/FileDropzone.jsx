import { useRef, useState } from 'react'

export default function FileDropzone({
  accept,
  multiple = false,
  onFiles,
  label = 'Drag and drop files here, or click to browse',
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
      className={`cursor-pointer rounded-xl border-2 border-dashed px-6 py-10 text-center transition ${
        dragging
          ? 'border-teal-500 bg-teal-50'
          : 'border-slate-300 bg-white hover:border-teal-400'
      }`}
    >
      <p className="text-sm font-medium text-slate-800">{label}</p>
      {hint && <p className="mt-2 text-xs text-slate-500">{hint}</p>}
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
