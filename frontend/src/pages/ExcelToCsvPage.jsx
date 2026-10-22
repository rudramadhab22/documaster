import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, excelToCsv } from '../api/client'

export default function ExcelToCsvPage() {
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
      setResult(await excelToCsv(file))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="Excel to CSV"
      description="Export Excel sheets to CSV. One sheet returns a CSV file; multiple sheets return a ZIP."
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
      <div className="mt-4">
        <ActionButton onClick={handleConvert} disabled={!file} loading={loading}>
          Convert to CSV
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">Excel exported successfully.</Alert>
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
