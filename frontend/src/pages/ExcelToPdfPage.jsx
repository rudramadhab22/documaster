import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { downloadBlob, excelToPdf } from '../api/client'

export default function ExcelToPdfPage() {
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
      setResult(await excelToPdf(file))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="Excel to PDF"
      description="Upload an Excel workbook (.xlsx or .xls) and convert all sheets into one PDF."
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
          Convert to PDF
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <div className="mt-4">
          <Alert type="success">Excel converted to PDF successfully.</Alert>
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
