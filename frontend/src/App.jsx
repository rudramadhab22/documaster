import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import AllToolsPage from './pages/AllToolsPage'
import PdfCompressorPage from './pages/PdfCompressorPage'
import ImageToPdfPage from './pages/ImageToPdfPage'
import PdfMergerPage from './pages/PdfMergerPage'
import PdfSplitterPage from './pages/PdfSplitterPage'
import ImageCompressorPage from './pages/ImageCompressorPage'
import PdfToImagePage from './pages/PdfToImagePage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="tools" element={<AllToolsPage />} />
        <Route path="tools/pdf-compressor" element={<PdfCompressorPage />} />
        <Route path="tools/image-to-pdf" element={<ImageToPdfPage />} />
        <Route path="tools/pdf-merger" element={<PdfMergerPage />} />
        <Route path="tools/pdf-splitter" element={<PdfSplitterPage />} />
        <Route path="tools/image-compressor" element={<ImageCompressorPage />} />
        <Route path="tools/pdf-to-image" element={<PdfToImagePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
