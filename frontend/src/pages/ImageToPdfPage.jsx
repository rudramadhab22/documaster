import { useEffect, useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, imagesToPdf } from '../api/client'

function moveItem(list, index, delta) {
  const next = [...list]
  const target = index + delta
  if (target < 0 || target >= next.length) return list
  ;[next[index], next[target]] = [next[target], next[index]]
  return next
}

export default function ImageToPdfPage() {
  const [files, setFiles] = useState([])
  const [previews, setPreviews] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  useEffect(() => {
    const urls = {}
    files.forEach((file, index) => {
      urls[index] = URL.createObjectURL(file)
    })
    setPreviews(urls)
    return () => {
      Object.values(urls).forEach((url) => URL.revokeObjectURL(url))
    }
  }, [files])

  async function handleConvert() {
    if (!files.length) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await imagesToPdf(files)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="Image to PDF"
      description="Upload multiple JPG or PNG images, reorder them, and convert into one PDF."
    >
      <FileDropzone
        accept="image/jpeg,image/png,.jpg,.jpeg,.png"
        multiple
        onFiles={(incoming) => {
          setFiles((prev) => [...prev, ...incoming])
          setResult(null)
          setError('')
        }}
        hint="JPG or PNG · multiple files supported"
      />
      <FileList
        files={files}
        previewUrls={previews}
        showReorder
        onRemove={(index) => {
          setFiles((prev) => prev.filter((_, i) => i !== index))
          setResult(null)
        }}
        onMoveUp={(index) => setFiles((prev) => moveItem(prev, index, -1))}
        onMoveDown={(index) => setFiles((prev) => moveItem(prev, index, 1))}
      />
      <div className="mt-4">
        <ActionButton onClick={handleConvert} disabled={!files.length} loading={loading}>
          Convert to PDF
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">PDF created successfully.</Alert>
          <div className="mt-3">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download PDF
            </ActionButton>
          </div>
        </div>
      )}
    </ToolPage>
  )
}
