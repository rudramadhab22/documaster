package com.documaster.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.documaster.dto.CompressPdfResult;
import com.documaster.exception.ApiException;
import com.documaster.util.PageRangeParser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfService {

    private static final float IMAGE_COMPRESS_QUALITY = 0.65f;
    private static final int RENDER_DPI = 150;

    public CompressPdfResult compress(MultipartFile file) {
        try {
            byte[] original = file.getBytes();
            long originalSize = original.length;

            try (PDDocument document = Loader.loadPDF(original)) {
                compressEmbeddedImages(document);
                document.setAllSecurityToBeRemoved(true);

                byte[] compressed = saveDocument(document);
                boolean reduced = compressed.length < originalSize;
                byte[] result = reduced ? compressed : original;
                String message = reduced
                        ? "PDF compressed successfully."
                        : "PDF could not be compressed further; original file returned.";

                return new CompressPdfResult(result, originalSize, result.length, reduced, message);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to compress PDF: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public byte[] imagesToPdf(MultipartFile[] images) {
        try (PDDocument document = new PDDocument()) {
            for (MultipartFile imageFile : images) {
                BufferedImage image;
                try (InputStream in = imageFile.getInputStream()) {
                    image = ImageIO.read(in);
                }
                if (image == null) {
                    throw new ApiException(
                            "Could not read image: " + imageFile.getOriginalFilename(),
                            HttpStatus.BAD_REQUEST);
                }

                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);

                PDImageXObject pdImage = toPdImage(document, image, imageFile.getOriginalFilename());
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(pdImage, 0, 0, image.getWidth(), image.getHeight());
                }
            }
            return saveDocument(document);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert images to PDF: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public byte[] merge(MultipartFile[] files) {
        PDFMergerUtility merger = new PDFMergerUtility();
        List<PDDocument> opened = new ArrayList<>();
        try (PDDocument destination = new PDDocument()) {
            for (MultipartFile file : files) {
                PDDocument source = Loader.loadPDF(file.getBytes());
                opened.add(source);
                merger.appendDocument(destination, source);
            }
            return saveDocument(destination);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to merge PDFs: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } finally {
            for (PDDocument doc : opened) {
                try {
                    doc.close();
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    public int pageCount(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return document.getNumberOfPages();
        } catch (Exception ex) {
            throw new ApiException("Failed to read PDF page count: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public byte[] split(MultipartFile file, String pages) {
        try (PDDocument source = Loader.loadPDF(file.getBytes());
             PDDocument target = new PDDocument()) {
            int total = source.getNumberOfPages();
            Set<Integer> selected = PageRangeParser.parse(pages, total);
            for (Integer pageNumber : selected) {
                target.importPage(source.getPage(pageNumber - 1));
            }
            return saveDocument(target);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to split PDF: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProcessedFile toImages(MultipartFile file, String pages, String format) {
        String normalizedFormat = format == null ? "png" : format.toLowerCase(Locale.ROOT).trim();
        if (!normalizedFormat.equals("png") && !normalizedFormat.equals("jpg") && !normalizedFormat.equals("jpeg")) {
            throw new ApiException("Format must be png or jpg.", HttpStatus.BAD_REQUEST);
        }
        String extension = normalizedFormat.equals("png") ? "png" : "jpg";
        String mime = extension.equals("png") ? "image/png" : "image/jpeg";

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            int total = document.getNumberOfPages();
            Set<Integer> selected = PageRangeParser.parse(pages, total);
            PDFRenderer renderer = new PDFRenderer(document);

            List<byte[]> images = new ArrayList<>();
            List<Integer> pageNumbers = new ArrayList<>(selected);
            for (Integer pageNumber : pageNumbers) {
                BufferedImage image = renderer.renderImageWithDPI(pageNumber - 1, RENDER_DPI, ImageType.RGB);
                images.add(writeImage(image, extension));
            }

            if (images.size() == 1) {
                return new ProcessedFile(
                        images.getFirst(),
                        mime,
                        "page-" + pageNumbers.getFirst() + "." + extension);
            }

            ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(zipBytes)) {
                for (int i = 0; i < images.size(); i++) {
                    ZipEntry entry = new ZipEntry("page-" + pageNumbers.get(i) + "." + extension);
                    zip.putNextEntry(entry);
                    zip.write(images.get(i));
                    zip.closeEntry();
                }
            }
            return new ProcessedFile(zipBytes.toByteArray(), "application/zip", "pdf-pages.zip");
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to convert PDF to images: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void compressEmbeddedImages(PDDocument document) throws IOException {
        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) {
                continue;
            }
            for (COSName name : resources.getXObjectNames()) {
                PDXObject xObject = resources.getXObject(name);
                if (!(xObject instanceof PDImageXObject image)) {
                    continue;
                }
                BufferedImage buffered = image.getImage();
                if (buffered == null) {
                    continue;
                }
                byte[] jpegBytes = encodeJpeg(buffered, IMAGE_COMPRESS_QUALITY);
                if (jpegBytes.length >= estimateImageBytes(image)) {
                    continue;
                }
                PDImageXObject compressed = JPEGFactory.createFromByteArray(document, jpegBytes);
                resources.put(name, compressed);
            }
        }
    }

    private long estimateImageBytes(PDImageXObject image) {
        try {
            return image.getCOSObject().getLength();
        } catch (Exception ex) {
            return Long.MAX_VALUE;
        }
    }

    private PDImageXObject toPdImage(PDDocument document, BufferedImage image, String filename) throws IOException {
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            byte[] jpeg = encodeJpeg(image, 0.9f);
            return JPEGFactory.createFromByteArray(document, jpeg);
        }
        return LosslessFactory.createFromImage(document, image);
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        BufferedImage rgb = image;
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgb.createGraphics().drawImage(image, 0, 0, null);
        }

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
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private byte[] writeImage(BufferedImage image, String extension) throws IOException {
        if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            return encodeJpeg(image, 0.9f);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", baos)) {
            throw new IOException("Failed to write PNG image.");
        }
        return baos.toByteArray();
    }

    private byte[] saveDocument(PDDocument document) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return baos.toByteArray();
    }

    public record ProcessedFile(byte[] data, String contentType, String filename) {
    }
}
