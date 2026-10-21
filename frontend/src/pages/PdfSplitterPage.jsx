import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, getPageCount, splitPdf } from '../api/client'

export default function PdfSplitterPage() {
  const [file, setFile] = useState(null)
  const [pageCount, setPageCount] = useState(null)
  const [pages, setPages] = useState('')
  const [loading, setLoading] = useState(false)
  const [counting, setCounting] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleFile(files) {
    const selected = files[0]
    setFile(selected)
    setResult(null)
    setError('')
    setPages('')
    setPageCount(null)
    setCounting(true)
    try {
      const count = await getPageCount(selected)
      setPageCount(count)
    } catch (err) {
      setFile(null)
      setError(err.message)
    } finally {
      setCounting(false)
    }
  }

  async function handleSplit() {
    if (!file || !pages.trim()) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await splitPdf(file, pages.trim())
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="PDF Splitter"
      description="Upload a PDF, view the page count, and extract pages with selections such as 1-3,5,8-10."
    >
      <FileDropzone
        accept="application/pdf,.pdf"
        onFiles={handleFile}
        hint="PDF only · max 50MB"
      />
      {file && (
        <FileList
          files={[file]}
          onRemove={() => {
            setFile(null)
            setPageCount(null)
            setPages('')
            setResult(null)
          }}
        />
      )}
      {counting && <p className="mt-3 text-sm text-slate-600">Reading page count…</p>}
      {pageCount != null && (
        <p className="mt-3 text-sm font-medium text-slate-800">
          Total pages: {pageCount}
        </p>
      )}
      <div className="mt-4">
        <label htmlFor="page-selection" className="mb-1 block text-sm font-medium text-slate-700">
          Page selection
        </label>
        <input
          id="page-selection"
          type="text"
          value={pages}
          onChange={(e) => setPages(e.target.value)}
          placeholder="e.g. 1-3,5,8-10"
          disabled={!file}
          className="w-full max-w-md rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none ring-teal-600 focus:ring-2 disabled:bg-slate-100"
        />
      </div>
      <div className="mt-4">
        <ActionButton
          onClick={handleSplit}
          disabled={!file || !pages.trim()}
          loading={loading}
        >
          Split PDF
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">Selected pages extracted successfully.</Alert>
          <div className="mt-3">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download Split PDF
            </ActionButton>
          </div>
        </div>
      )}
    </ToolPage>
  )
}
