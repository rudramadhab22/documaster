# DocuMaster

Full-stack PDF, image, and Excel processing app with a React frontend and a Spring Boot backend.

## Tools

1. **PDF Compressor** — optimize PDFs and report original vs compressed size  
2. **Image to PDF** — combine JPG/PNG images into one PDF  
3. **PDF Merger** — merge multiple PDFs with reorder support  
4. **PDF Splitter** — extract pages using selections like `1-3,5,8-10`  
5. **Image Compressor** — compress images with adjustable JPEG quality  
6. **PDF to Image** — convert pages to PNG/JPG (single image or ZIP)  
7. **Excel to PDF** — convert workbook sheets into one PDF  
8. **Excel to CSV** — export sheets to CSV (or ZIP for multiple sheets)  
9. **CSV to Excel** — convert CSV into an `.xlsx` workbook  
10. **Excel to Images** — render sheets as PNG/JPG  
11. **Excel to PDFs per Sheet** — one PDF per sheet (or ZIP)

Uploaded files are processed in memory and are not stored permanently.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- Docker (optional)

## Local development

### Backend

```bash
cd backend
mvn spring-boot:run
```

API: `http://localhost:8080`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

App: `http://localhost:5173` (Vite proxies `/api` to the backend)

### Tests and production build

```bash
cd backend && mvn test
cd frontend && npm run build
```

## Docker

```bash
docker compose up --build
```

- Frontend: `http://localhost`
- Backend: `http://localhost:8080`

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/pdf/compress` | Compress PDF (`file`) |
| POST | `/api/pdf/image-to-pdf` | Images to PDF (`files`) |
| POST | `/api/pdf/merge` | Merge PDFs (`files`) |
| POST | `/api/pdf/split` | Split PDF (`file`, `pages`) |
| POST | `/api/pdf/page-count` | Page count (`file`) |
| POST | `/api/image/compress` | Compress image (`file`, `quality`) |
| POST | `/api/pdf/to-images` | PDF to images (`file`, `pages`, `format`) |
| POST | `/api/excel/to-pdf` | Excel to PDF (`file`) |
| POST | `/api/excel/to-csv` | Excel to CSV (`file`) |
| POST | `/api/excel/from-csv` | CSV to Excel (`file`) |
| POST | `/api/excel/to-images` | Excel sheets to images (`file`, `format`) |
| POST | `/api/excel/to-pdfs-per-sheet` | One PDF per sheet (`file`) |

Limits: 50MB per file, 100MB per request.
