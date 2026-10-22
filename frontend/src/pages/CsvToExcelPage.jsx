import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { csvToExcel, downloadBlob } from '../api/client'

export default function CsvToExcelPage() {
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleConvert() {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      setResult(await csvToExcel(file))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="CSV to Excel"
      description="Upload a CSV file and convert it into an Excel workbook (.xlsx)."
    >
      <FileDropzone
        accept=".csv,text/csv,application/csv"
        onFiles={(files) => {
          setFile(files[0])
          setResult(null)
          setError('')
        }}
        hint="CSV only · max 50MB"
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
      <div className="mt-4">
        <ActionButton onClick={handleConvert} disabled={!file} loading={loading}>
          Convert to Excel
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">CSV converted to Excel successfully.</Alert>
          <div className="mt-3">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download Excel
            </ActionButton>
          </div>
        </div>
      )}
    </ToolPage>
  )
}
