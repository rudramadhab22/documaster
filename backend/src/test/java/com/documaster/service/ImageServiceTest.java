package com.documaster.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageServiceTest {

    private final ImageService imageService = new ImageService();

    @Test
    void compressReducesJpegSizeAtLowQuality() throws Exception {
        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        for (int y = 0; y < 400; y++) {
            for (int x = 0; x < 400; x++) {
                g.setColor(new Color((x * 7) % 256, (y * 11) % 256, (x + y) % 256));
                g.fillRect(x, y, 1, 1);
            }
        }
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", baos.toByteArray());

        var result = imageService.compress(file, 0.2f);
        assertTrue(result.originalSize() > 0);
        assertTrue(result.compressedSize() > 0);
        assertTrue(result.data().length > 0);
        assertTrue(result.reduced());
        assertTrue(result.compressedSize() < result.originalSize());
    }
}
