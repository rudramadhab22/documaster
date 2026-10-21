import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  timeout: 120000,
})

function getErrorMessage(error) {
  if (error.response?.data instanceof Blob) {
    return error.response.data.text().then((text) => {
      try {
        const parsed = JSON.parse(text)
        return parsed.error || 'Request failed.'
      } catch {
        return text || 'Request failed.'
      }
    })
  }
  return Promise.resolve(
    error.response?.data?.error || error.message || 'Request failed.',
  )
}

async function postFile(url, formData, config = {}) {
  try {
    const response = await api.post(url, formData, {
      responseType: 'blob',
      headers: { 'Content-Type': 'multipart/form-data' },
      ...config,
    })
    return response
  } catch (error) {
    const message = await getErrorMessage(error)
    throw new Error(message)
  }
}

export function formatBytes(bytes) {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  const value = bytes / 1024 ** i
  return `${value.toFixed(value >= 10 || i === 0 ? 0 : 1)} ${units[i]}`
}

export function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

export async function compressPdf(file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await postFile('/api/pdf/compress', formData)
  return {
    blob: response.data,
    originalSize: Number(response.headers['x-original-size'] || 0),
    compressedSize: Number(response.headers['x-compressed-size'] || 0),
    reduced: response.headers['x-reduced'] === 'true',
    message: response.headers['x-message'] || 'Done.',
    filename: 'compressed.pdf',
  }
}

export async function imagesToPdf(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  const response = await postFile('/api/pdf/image-to-pdf', formData)
  return { blob: response.data, filename: 'images.pdf' }
}

export async function mergePdfs(files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  const response = await postFile('/api/pdf/merge', formData)
  return { blob: response.data, filename: 'merged.pdf' }
}

export async function splitPdf(file, pages) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('pages', pages)
  const response = await postFile('/api/pdf/split', formData)
  return { blob: response.data, filename: 'split.pdf' }
}

export async function getPageCount(file) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const response = await api.post('/api/pdf/page-count', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data.pageCount
  } catch (error) {
    const message = await getErrorMessage(error)
    throw new Error(message)
  }
}

export async function compressImage(file, quality) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('quality', String(quality))
  const response = await postFile('/api/image/compress', formData)
  const disposition = response.headers['content-disposition'] || ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return {
    blob: response.data,
    originalSize: Number(response.headers['x-original-size'] || 0),
    compressedSize: Number(response.headers['x-compressed-size'] || 0),
    reduced: response.headers['x-reduced'] === 'true',
    message: response.headers['x-message'] || 'Done.',
    filename: match?.[1] || 'compressed.jpg',
  }
}

export async function pdfToImages(file, pages, format) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('pages', pages)
  formData.append('format', format)
  const response = await postFile('/api/pdf/to-images', formData)
  const disposition = response.headers['content-disposition'] || ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return {
    blob: response.data,
    filename: match?.[1] || (format === 'png' ? 'page.png' : 'page.jpg'),
  }
}
