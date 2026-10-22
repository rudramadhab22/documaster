package com.documaster.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.documaster.exception.ApiException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelService {

    private static final int MAX_RENDER_ROWS = 200;
    private static final int MAX_RENDER_COLS = 25;
    private static final int MAX_CSV_ROWS = 100_000;
    private static final float FONT_SIZE = 9f;
    private static final float MARGIN = 36f;
    private static final float ROW_HEIGHT = 14f;
    private static final float COL_WIDTH = 70f;

    private final DataFormatter formatter = new DataFormatter();

    public ProcessedFile excelToPdf(MultipartFile file) {
        try (Workbook workbook = openWorkbook(file);
             PDDocument document = new PDDocument()) {
            List<SheetData> sheets = readSheets(workbook, MAX_RENDER_ROWS, MAX_RENDER_COLS);
            if (sheets.isEmpty()) {
                throw new ApiException("Workbook has no readable data.", HttpStatus.BAD_REQUEST);
            }
            for (SheetData sheet : sheets) {
                writeSheetAsPdfPages(document, sheet);
            }
            return new ProcessedFile(savePdf(document), "application/pdf", baseName(file) + ".pdf");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert Excel to PDF: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProcessedFile excelToCsv(MultipartFile file) {
        try (Workbook workbook = openWorkbook(file)) {
            List<SheetData> sheets = readSheets(workbook, MAX_CSV_ROWS, 256);
            if (sheets.isEmpty()) {
                throw new ApiException("Workbook has no readable data.", HttpStatus.BAD_REQUEST);
            }
            if (sheets.size() == 1) {
                byte[] csv = toCsvBytes(sheets.getFirst());
                return new ProcessedFile(csv, "text/csv", sanitize(sheets.getFirst().name()) + ".csv");
            }
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(zipBytes)) {
                for (SheetData sheet : sheets) {
                    zip.putNextEntry(new ZipEntry(sanitize(sheet.name()) + ".csv"));
                    zip.write(toCsvBytes(sheet));
                    zip.closeEntry();
                }
            }
            return new ProcessedFile(zipBytes.toByteArray(), "application/zip", baseName(file) + "-csv.zip");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert Excel to CSV: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProcessedFile csvToExcel(MultipartFile file) {
        try {
            List<List<String>> rows = parseCsv(file);
            if (rows.isEmpty()) {
                throw new ApiException("CSV file is empty.", HttpStatus.BAD_REQUEST);
            }
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Sheet1");
                for (int r = 0; r < rows.size(); r++) {
                    Row row = sheet.createRow(r);
                    List<String> cols = rows.get(r);
                    for (int c = 0; c < cols.size(); c++) {
                        row.createCell(c).setCellValue(cols.get(c));
                    }
                }
                for (int c = 0; c < rows.getFirst().size() && c < 50; c++) {
                    sheet.autoSizeColumn(c);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                return new ProcessedFile(
                        baos.toByteArray(),
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        baseName(file) + ".xlsx");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert CSV to Excel: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProcessedFile excelToImages(MultipartFile file, String format) {
        String extension = normalizeImageFormat(format);
        try (Workbook workbook = openWorkbook(file)) {
            List<SheetData> sheets = readSheets(workbook, MAX_RENDER_ROWS, MAX_RENDER_COLS);
            if (sheets.isEmpty()) {
                throw new ApiException("Workbook has no readable data.", HttpStatus.BAD_REQUEST);
            }
            List<byte[]> images = new ArrayList<>();
            for (SheetData sheet : sheets) {
                images.add(renderSheetImage(sheet, extension));
            }
            if (images.size() == 1) {
                String mime = extension.equals("png") ? "image/png" : "image/jpeg";
                return new ProcessedFile(
                        images.getFirst(),
                        mime,
                        sanitize(sheets.getFirst().name()) + "." + extension);
            }
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(zipBytes)) {
                for (int i = 0; i < sheets.size(); i++) {
                    zip.putNextEntry(new ZipEntry(sanitize(sheets.get(i).name()) + "." + extension));
                    zip.write(images.get(i));
                    zip.closeEntry();
                }
            }
            return new ProcessedFile(zipBytes.toByteArray(), "application/zip", baseName(file) + "-sheets.zip");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert Excel sheets to images: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProcessedFile excelToPdfsPerSheet(MultipartFile file) {
        try (Workbook workbook = openWorkbook(file)) {
            List<SheetData> sheets = readSheets(workbook, MAX_RENDER_ROWS, MAX_RENDER_COLS);
            if (sheets.isEmpty()) {
                throw new ApiException("Workbook has no readable data.", HttpStatus.BAD_REQUEST);
            }
            if (sheets.size() == 1) {
                try (PDDocument document = new PDDocument()) {
                    writeSheetAsPdfPages(document, sheets.getFirst());
                    return new ProcessedFile(
                            savePdf(document),
                            "application/pdf",
                            sanitize(sheets.getFirst().name()) + ".pdf");
                }
            }
            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(zipBytes)) {
                for (SheetData sheet : sheets) {
                    try (PDDocument document = new PDDocument()) {
                        writeSheetAsPdfPages(document, sheet);
                        zip.putNextEntry(new ZipEntry(sanitize(sheet.name()) + ".pdf"));
                        zip.write(savePdf(document));
                        zip.closeEntry();
                    }
                }
            }
            return new ProcessedFile(zipBytes.toByteArray(), "application/zip", baseName(file) + "-pdfs.zip");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert Excel sheets to PDFs: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private Workbook openWorkbook(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        byte[] bytes = file.getBytes();
        try {
            return WorkbookFactory.create(new ByteArrayInputStream(bytes));
        } catch (Exception ex) {
            if (name.endsWith(".xls")) {
                return new HSSFWorkbook(new ByteArrayInputStream(bytes));
            }
            throw new ApiException("Could not read Excel workbook.", HttpStatus.BAD_REQUEST);
        }
    }

    private List<SheetData> readSheets(Workbook workbook, int maxRows, int maxCols) {
        List<SheetData> sheets = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet == null) {
                continue;
            }
            SheetData data = extractSheet(sheet, maxRows, maxCols);
            if (!data.rows().isEmpty()) {
                sheets.add(data);
            }
        }
        return sheets;
    }

    private SheetData extractSheet(Sheet sheet, int maxRows, int maxCols) {
        String name = sheet.getSheetName() == null || sheet.getSheetName().isBlank()
                ? "Sheet" : sheet.getSheetName();
        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        if (lastRow < firstRow) {
            return new SheetData(name, List.of());
        }

        int endRow = Math.min(lastRow, firstRow + maxRows - 1);
        int maxCol = 0;
        List<List<String>> rows = new ArrayList<>();
        for (int r = firstRow; r <= endRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                rows.add(List.of());
                continue;
            }
            int lastCell = Math.min(row.getLastCellNum() > 0 ? row.getLastCellNum() - 1 : -1, maxCols - 1);
            List<String> cells = new ArrayList<>();
            for (int c = 0; c <= lastCell; c++) {
                Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String value = cell == null ? "" : formatter.formatCellValue(cell);
                cells.add(value);
            }
            while (!cells.isEmpty() && cells.getLast().isBlank()) {
                cells.removeLast();
            }
            maxCol = Math.max(maxCol, cells.size());
            rows.add(cells);
        }

        while (!rows.isEmpty() && rows.getLast().stream().allMatch(String::isBlank)) {
            rows.removeLast();
        }

        List<List<String>> normalized = new ArrayList<>();
        for (List<String> row : rows) {
            List<String> copy = new ArrayList<>(row);
            while (copy.size() < maxCol) {
                copy.add("");
            }
            if (copy.size() > maxCols) {
                copy = copy.subList(0, maxCols);
            }
            normalized.add(copy);
        }
        return new SheetData(name, normalized);
    }

    private void writeSheetAsPdfPages(PDDocument document, SheetData sheet) throws IOException {
        if (sheet.rows().isEmpty()) {
            return;
        }
        int cols = sheet.rows().getFirst().size();
        int rowsPerPage = Math.max(1, (int) ((PDRectangle.LETTER.getHeight() - (MARGIN * 2) - 24) / ROW_HEIGHT));
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        for (int start = 0; start < sheet.rows().size(); start += rowsPerPage) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN;
                cs.beginText();
                cs.setFont(bold, 11);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(truncate(safePdfText(sheet.name()), 80));
                cs.endText();
                y -= 20;

                int end = Math.min(sheet.rows().size(), start + rowsPerPage);
                for (int r = start; r < end; r++) {
                    List<String> row = sheet.rows().get(r);
                    float x = MARGIN;
                    for (int c = 0; c < cols; c++) {
                        String value = c < row.size() ? row.get(c) : "";
                        cs.beginText();
                        cs.setFont(font, FONT_SIZE);
                        cs.newLineAtOffset(x, y);
                        cs.showText(truncate(safePdfText(value), 12));
                        cs.endText();
                        x += COL_WIDTH;
                        if (x > page.getMediaBox().getWidth() - MARGIN) {
                            break;
                        }
                    }
                    y -= ROW_HEIGHT;
                }
            }
        }
    }

    private byte[] renderSheetImage(SheetData sheet, String extension) throws IOException {
        int cols = Math.max(1, sheet.rows().isEmpty() ? 1 : sheet.rows().getFirst().size());
        int rows = Math.max(1, sheet.rows().size());
        int cellW = 110;
        int cellH = 24;
        int pad = 16;
        int titleH = 28;
        int width = Math.min(4000, pad * 2 + cols * cellW);
        int height = Math.min(8000, pad * 2 + titleH + rows * cellH);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(15, 118, 110));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(sheet.name(), pad, pad + 14);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));

        for (int r = 0; r < rows; r++) {
            List<String> row = sheet.rows().get(r);
            for (int c = 0; c < cols; c++) {
                int x = pad + c * cellW;
                int y = pad + titleH + r * cellH;
                g.setColor(r == 0 ? new Color(240, 253, 250) : Color.WHITE);
                g.fillRect(x, y, cellW, cellH);
                g.setColor(new Color(203, 213, 225));
                g.drawRect(x, y, cellW, cellH);
                g.setColor(new Color(15, 23, 42));
                String value = c < row.size() ? row.get(c) : "";
                FontMetrics fm = g.getFontMetrics();
                String text = ellipsize(value, fm, cellW - 8);
                g.drawString(text, x + 4, y + cellH - 8);
            }
        }
        g.dispose();

        if ("png".equals(extension)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
        return encodeJpeg(image, 0.9f);
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer available.");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private byte[] toCsvBytes(SheetData sheet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            for (List<String> row : sheet.rows()) {
                writer.write(toCsvLine(row));
                writer.write('\n');
            }
        }
        return baos.toByteArray();
    }

    private List<List<String>> parseCsv(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<List<String>> rows = new ArrayList<>();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (++count > MAX_CSV_ROWS) {
                    throw new ApiException("CSV exceeds " + MAX_CSV_ROWS + " rows.", HttpStatus.BAD_REQUEST);
                }
                rows.add(parseCsvLine(line));
            }
            return rows;
        }
    }

    private String toCsvLine(List<String> row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            String value = row.get(i) == null ? "" : row.get(i);
            boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
            if (quote) {
                sb.append('"').append(value.replace("\"", "\"\"")).append('"');
            } else {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private byte[] savePdf(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }

    private String normalizeImageFormat(String format) {
        String normalized = format == null ? "png" : format.toLowerCase(Locale.ROOT).trim();
        if (!normalized.equals("png") && !normalized.equals("jpg") && !normalized.equals("jpeg")) {
            throw new ApiException("Format must be png or jpg.", HttpStatus.BAD_REQUEST);
        }
        return normalized.equals("png") ? "png" : "jpg";
    }

    private String baseName(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || name.isBlank()) {
            return "document";
        }
        int dot = name.lastIndexOf('.');
        return sanitize(dot > 0 ? name.substring(0, dot) : name);
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }

    private String safePdfText(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char ch : value.toCharArray()) {
            if (ch == '\t' || ch == '\n' || ch == '\r') {
                sb.append(' ');
            } else if (ch >= 32 && ch <= 126) {
                sb.append(ch);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private String truncate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String ellipsize(String value, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int width = fm.stringWidth(ellipsis);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (fm.stringWidth(sb.toString() + ch) + width > maxWidth) {
                break;
            }
            sb.append(ch);
        }
        return sb + ellipsis;
    }

    public record ProcessedFile(byte[] data, String contentType, String filename) {
    }

    private record SheetData(String name, List<List<String>> rows) {
    }
}
