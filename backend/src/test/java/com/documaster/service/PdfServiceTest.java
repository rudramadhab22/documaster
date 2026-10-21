package com.documaster.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    void pageCountReturnsCorrectValue() throws Exception {
        byte[] pdf = createTextPdf(3);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdf);
        assertEquals(3, pdfService.pageCount(file));
    }

    @Test
    void splitExtractsSelectedPages() throws Exception {
        byte[] pdf = createTextPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", pdf);
        byte[] split = pdfService.split(file, "1,3-4");
        try (PDDocument document = Loader.loadPDF(split)) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    @Test
    void mergeCombinesPdfs() throws Exception {
        MockMultipartFile a = new MockMultipartFile("files", "a.pdf", "application/pdf", createTextPdf(1));
        MockMultipartFile b = new MockMultipartFile("files", "b.pdf", "application/pdf", createTextPdf(2));
        byte[] merged = pdfService.merge(new MockMultipartFile[]{a, b});
        try (PDDocument document = Loader.loadPDF(merged)) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    @Test
    void imagesToPdfCreatesOnePagePerImage() throws Exception {
        MockMultipartFile img1 = pngFile("one.png", Color.RED);
        MockMultipartFile img2 = pngFile("two.png", Color.BLUE);
        byte[] pdf = pdfService.imagesToPdf(new MockMultipartFile[]{img1, img2});
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertEquals(2, document.getNumberOfPages());
        }
    }

    @Test
    void compressReturnsResultWithSizes() throws Exception {
        byte[] pdf = createImageHeavyPdf();
        MockMultipartFile file = new MockMultipartFile("file", "heavy.pdf", "application/pdf", pdf);
        var result = pdfService.compress(file);
        assertTrue(result.originalSize() > 0);
        assertTrue(result.compressedSize() > 0);
        assertTrue(result.data().length > 0);
    }

    private byte[] createTextPdf(int pages) throws Exception {
        try (PDDocument document = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                    cs.newLineAtOffset(50, 700);
                    cs.showText("Page " + (i + 1));
                    cs.endText();
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createImageHeavyPdf() throws Exception {
        BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        for (int y = 0; y < 600; y++) {
            for (int x = 0; x < 800; x++) {
                g.setColor(new Color((x * 3) % 256, (y * 5) % 256, (x + y) % 256));
                g.fillRect(x, y, 1, 1);
            }
        }
        g.dispose();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(800, 600));
            document.addPage(page);
            var pdImage = JPEGFactory.createFromImage(document, image, 1.0f);
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                cs.drawImage(pdImage, 0, 0, 800, 600);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private MockMultipartFile pngFile(String name, Color color) throws Exception {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new MockMultipartFile("files", name, "image/png", baos.toByteArray());
    }
}
