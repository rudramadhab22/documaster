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
import ExcelToPdfPage from './pages/ExcelToPdfPage'
import ExcelToCsvPage from './pages/ExcelToCsvPage'
import CsvToExcelPage from './pages/CsvToExcelPage'
import ExcelToImagesPage from './pages/ExcelToImagesPage'
import ExcelToPdfsPage from './pages/ExcelToPdfsPage'

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
        <Route path="tools/excel-to-pdf" element={<ExcelToPdfPage />} />
        <Route path="tools/excel-to-csv" element={<ExcelToCsvPage />} />
        <Route path="tools/csv-to-excel" element={<CsvToExcelPage />} />
        <Route path="tools/excel-to-images" element={<ExcelToImagesPage />} />
        <Route path="tools/excel-to-pdfs" element={<ExcelToPdfsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
