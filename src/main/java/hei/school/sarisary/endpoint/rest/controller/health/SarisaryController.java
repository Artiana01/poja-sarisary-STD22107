package hei.school.sarisary.endpoint.rest.controller;

import hei.school.sarisary.file.BucketComponent;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.CompletableFuture;

@RestController
@AllArgsConstructor
public class SarisaryController {

    BucketComponent bucketComponent;
    @PostMapping(value = "/black/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> toBlackAndWhite(@RequestBody MultipartFile img, @PathVariable String id)
    {
        try {
            CompletableFuture<Void> uploadTask = CompletableFuture.runAsync(() ->
            {
                try {
                    File file = File.createTempFile("temp", null);
                    img.transferTo(file);
                    bucketComponent.upload(file, id+".png");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(img.getBytes()));
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            BufferedImage blackAndWhiteImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = bufferedImage.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int gray = (r + g + b) / 3;
                    int newPixel = (gray << 16) + (gray << 8) + gray;
                    blackAndWhiteImage.setRGB(x, y, newPixel);
                }
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(blackAndWhiteImage, "jpg", byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();
            File fileConverted = File.createTempFile(img.getName(), null);

            try (FileOutputStream fos = new FileOutputStream(fileConverted)) {
                fos.write(imageData);
            }
            bucketComponent.upload(fileConverted , id+".png");

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}