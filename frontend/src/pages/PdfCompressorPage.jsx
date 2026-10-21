import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { compressPdf, downloadBlob, formatBytes } from '../api/client'

export default function PdfCompressorPage() {
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleCompress() {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await compressPdf(file)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="PDF Compressor"
      description="Upload a PDF to compress embedded images and optimize the file. Original and compressed sizes are reported."
    >
      <FileDropzone
        accept="application/pdf,.pdf"
        onFiles={(files) => {
          setFile(files[0])
          setResult(null)
          setError('')
        }}
        hint="PDF only · max 50MB"
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
        <ActionButton onClick={handleCompress} disabled={!file} loading={loading}>
          Compress PDF
        </ActionButton>
      </div>
      {error && <Alert>{error}</Alert>}
      {result && (
        <>
          <Alert type={result.reduced ? 'success' : 'info'}>{result.message}</Alert>
          <div className="mt-3 grid gap-2 text-sm text-slate-700 sm:grid-cols-2">
            <p>Original size: {formatBytes(result.originalSize)}</p>
            <p>Output size: {formatBytes(result.compressedSize)}</p>
          </div>
          <div className="mt-4">
            <ActionButton onClick={() => downloadBlob(result.blob, result.filename)}>
              Download Result
            </ActionButton>
          </div>
        </>
      )}
    </ToolPage>
  )
}
