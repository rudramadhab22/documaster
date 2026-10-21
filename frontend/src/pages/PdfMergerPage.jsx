import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, mergePdfs } from '../api/client'

function moveItem(list, index, delta) {
  const next = [...list]
  const target = index + delta
  if (target < 0 || target >= next.length) return list
  ;[next[index], next[target]] = [next[target], next[index]]
  return next
}

export default function PdfMergerPage() {
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleMerge() {
    if (files.length < 2) {
      setError('Upload at least two PDF files to merge.')
      return
    }
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await mergePdfs(files)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="PDF Merger"
      description="Upload multiple PDFs, reorder or remove files, then merge them into one document."
    >
      <FileDropzone
        accept="application/pdf,.pdf"
        multiple
        onFiles={(incoming) => {
          setFiles((prev) => [...prev, ...incoming])
          setResult(null)
          setError('')
        }}
        hint="PDF only · at least two files required"
      />
      <FileList
        files={files}
        showReorder
        onRemove={(index) => {
          setFiles((prev) => prev.filter((_, i) => i !== index))
          setResult(null)
        }}
        onMoveUp={(index) => setFiles((prev) => moveItem(prev, index, -1))}
        onMoveDown={(index) => setFiles((prev) => moveItem(prev, index, 1))}
      />
      <div className="mt-4">
        <ActionButton onClick={handleMerge} disabled={files.length < 2} loading={loading}>
          Merge PDFs
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">PDFs merged successfully.</Alert>
          <div className="mt-3">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download Merged PDF
            </ActionButton>
          </div>
        </div>
      )}
    </ToolPage>
  )
}
