import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, getPageCount, pdfToImages } from '../api/client'

export default function PdfToImagePage() {
  const [file, setFile] = useState(null)
  const [pageCount, setPageCount] = useState(null)
  const [pages, setPages] = useState('')
  const [format, setFormat] = useState('png')
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

  async function handleConvert() {
    if (!file || !pages.trim()) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await pdfToImages(file, pages.trim(), format)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="PDF to Image"
      description="Convert selected PDF pages to PNG or JPG. A single page returns one image; multiple pages return a ZIP."
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
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <div>
          <label htmlFor="pages" className="mb-1 block text-sm font-medium text-slate-700">
            Page selection
          </label>
          <input
            id="pages"
            type="text"
            value={pages}
            onChange={(e) => setPages(e.target.value)}
            placeholder="e.g. 1 or 1-3,5"
            disabled={!file}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none ring-teal-600 focus:ring-2 disabled:bg-slate-100"
          />
        </div>
        <div>
          <label htmlFor="format" className="mb-1 block text-sm font-medium text-slate-700">
            Output format
          </label>
          <select
            id="format"
            value={format}
            onChange={(e) => setFormat(e.target.value)}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none ring-teal-600 focus:ring-2"
          >
            <option value="png">PNG</option>
            <option value="jpg">JPG</option>
          </select>
        </div>
      </div>
      <div className="mt-4">
        <ActionButton
          onClick={handleConvert}
          disabled={!file || !pages.trim()}
          loading={loading}
        >
          Convert to Images
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">Conversion complete. Download your file below.</Alert>
          <div className="mt-3">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download Result
            </ActionButton>
          </div>
        </div>
      )}
    </ToolPage>
  )
}
