package com.documaster.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.documaster.dto.CompressImageResult;
import com.documaster.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    public CompressImageResult compress(MultipartFile file, float quality) {
        if (quality < 0.1f || quality > 1.0f) {
            throw new ApiException("Quality must be between 0.1 and 1.0.", HttpStatus.BAD_REQUEST);
        }

        try {
            byte[] originalBytes = file.getBytes();
            long originalSize = originalBytes.length;

            BufferedImage image;
            try (InputStream in = file.getInputStream()) {
                image = ImageIO.read(in);
            }
            if (image == null) {
                throw new ApiException("Could not read the uploaded image.", HttpStatus.BAD_REQUEST);
            }

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
            String lower = originalName.toLowerCase(Locale.ROOT);
            boolean isPng = lower.endsWith(".png") || "image/png".equalsIgnoreCase(file.getContentType());

            byte[] compressed;
            String contentType;
            String outName;

            if (isPng) {
                // PNG has no quality knob; recompress via JPEG for size reduction,
                // or rewrite PNG. Prefer JPEG output when quality is used.
                compressed = encodeJpeg(image, quality);
                contentType = "image/jpeg";
                outName = baseName(originalName) + "-compressed.jpg";
            } else {
                compressed = encodeJpeg(image, quality);
                contentType = "image/jpeg";
                outName = baseName(originalName) + "-compressed.jpg";
            }

            boolean reduced = compressed.length < originalSize;
            byte[] result = reduced ? compressed : originalBytes;
            String message = reduced
                    ? "Image compressed successfully."
                    : "Image could not be compressed further at the selected quality; original returned.";

            if (!reduced) {
                contentType = file.getContentType() != null ? file.getContentType() : contentType;
                outName = originalName;
            }

            return new CompressImageResult(
                    result,
                    originalSize,
                    result.length,
                    contentType,
                    outName,
                    reduced,
                    message);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Failed to compress image: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) throws IOException {
        BufferedImage rgb = toRgb(image);
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

    private BufferedImage toRgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private String baseName(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
