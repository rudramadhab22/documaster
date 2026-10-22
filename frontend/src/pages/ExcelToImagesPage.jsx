import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, excelToImages } from '../api/client'

export default function ExcelToImagesPage() {
  const [file, setFile] = useState(null)
  const [format, setFormat] = useState('png')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleConvert() {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      setResult(await excelToImages(file, format))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="Excel to Images"
      description="Render each Excel sheet as a PNG or JPG image. Multiple sheets are returned as a ZIP."
    >
      <FileDropzone
        accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
        onFiles={(files) => {
          setFile(files[0])
          setResult(null)
          setError('')
        }}
        hint="Excel only · max 50MB"
      />
      {file && (
        <FileList
          files={[file]}
          onRemove={() => {
            setFile(null)
            setResult(null)
          }}
        />
      )}
      <div className="mt-4 max-w-xs">
        <label htmlFor="excel-image-format" className="mb-1 block text-sm font-medium text-slate-700">
          Output format
        </label>
        <select
          id="excel-image-format"
          value={format}
          onChange={(e) => setFormat(e.target.value)}
          className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none ring-teal-600 focus:ring-2"
        >
          <option value="png">PNG</option>
          <option value="jpg">JPG</option>
        </select>
      </div>
      <div className="mt-4">
        <ActionButton onClick={handleConvert} disabled={!file} loading={loading}>
          Convert to Images
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">Sheets rendered successfully.</Alert>
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
