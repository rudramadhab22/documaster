# DocuMaster

Full-stack PDF and image processing app with a React frontend and a Spring Boot backend.

## Tools

1. **PDF Compressor** — optimize PDFs and report original vs compressed size  
2. **Image to PDF** — combine JPG/PNG images into one PDF  
3. **PDF Merger** — merge multiple PDFs with reorder support  
4. **PDF Splitter** — extract pages using selections like `1-3,5,8-10`  
5. **Image Compressor** — compress images with adjustable JPEG quality  
6. **PDF to Image** — convert pages to PNG/JPG (single image or ZIP)

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

Limits: 50MB per file, 100MB per request.
