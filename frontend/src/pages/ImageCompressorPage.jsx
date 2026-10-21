import { useState } from 'react'
import ToolPage from '../components/ToolPage'
import FileDropzone from '../components/FileDropzone'
import FileList from '../components/FileList'
import ActionButton from '../components/ActionButton'
import Alert from '../components/Alert'
import { compressImage, downloadBlob, formatBytes } from '../api/client'

export default function ImageCompressorPage() {
  const [file, setFile] = useState(null)
  const [quality, setQuality] = useState(0.7)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)

  async function handleCompress() {
    if (!file) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const data = await compressImage(file, quality)
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <ToolPage
      title="Image Compressor"
      description="Upload a JPG or PNG image and compress it with adjustable JPEG quality."
    >
      <FileDropzone
        accept="image/jpeg,image/png,.jpg,.jpeg,.png"
        onFiles={(files) => {
          setFile(files[0])
          setResult(null)
          setError('')
        }}
        hint="JPG or PNG · max 50MB"
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
      <div className="mt-4 max-w-md">
        <label htmlFor="quality" className="mb-1 block text-sm font-medium text-slate-700">
          JPEG quality: {quality.toFixed(2)}
        </label>
        <input
          id="quality"
          type="range"
          min="0.1"
          max="1"
          step="0.05"
          value={quality}
          onChange={(e) => setQuality(Number(e.target.value))}
          className="w-full accent-teal-700"
        />
        <p className="mt-1 text-xs text-slate-500">
          Lower values produce smaller files. PNG inputs are converted to JPEG when compressed.
        </p>
      </div>
      <div className="mt-4">
        <ActionButton onClick={handleCompress} disabled={!file} loading={loading}>
          Compress Image
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
