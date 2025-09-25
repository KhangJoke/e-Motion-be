package com.swp391.e_Motion_be.service.document;

import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final CloudinaryService cloudinaryService;
    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");

    public static ITesseract getTesseract() {
        // Thư mục tạm để copy tessdata
        File tempTessDataDir = new File(System.getProperty("java.io.tmpdir"), "tessdata");
        if (!tempTessDataDir.exists()) {
            boolean created = tempTessDataDir.mkdirs();
            if (!created) {
                throw new AppException(ErrorCode.CREATE_FOLDER_FAILED);
            }
            copyTessDataFolder(tempTessDataDir.toPath());
        }
        // Khởi tạo Tesseract
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tempTessDataDir.getAbsolutePath());
        tesseract.setLanguage("eng");
        return tesseract;
    }

    private static void copyTessDataFolder(Path targetDir) {
        // Lấy URL của thư mục trong resources
        URI uri;
        Path sourcePath;
        try {
            uri = Objects.requireNonNull(OcrService.class.getClassLoader().getResource("tessdata")).toURI();
            if (uri.getScheme().equals("jar")) {
                // Nếu chạy trong JAR
                FileSystem fileSystem = null;
                fileSystem = FileSystems.newFileSystem(uri, new java.util.HashMap<>());
                sourcePath = fileSystem.getPath("/" + "tessdata");
            } else {
                sourcePath = Paths.get(uri);
            }
            // Copy toàn bộ file trong thư mục
            Files.walk(sourcePath).forEach(source -> {
                try {
                    Path destination = targetDir.resolve(sourcePath.relativize(source).toString());
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new AppException(ErrorCode.FAIL_COPY_DATASET);
                }
            });
        } catch (Exception e) {
            throw new AppException(ErrorCode.NOT_FOUND_FOLDER_DATASET);
        }
    }

    // Tiền xử lý: grayscale + resize
    private BufferedImage preprocess(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        // Chuyển ảnh sang grayscale (đen trắng)
        BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        // resize nếu width nhỏ hơn 1000px
        int targetWidth = Math.max(1000, w);
        BufferedImage resized = new BufferedImage(targetWidth, targetWidth * h / w, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = resized.createGraphics();
        g2.drawImage(gray, 0, 0, resized.getWidth(), resized.getHeight(), null);
        g2.dispose();

        return resized;
    }

    // OCR và trích số CCCD
    public String extractCccdFromUrl(String imageUrl) {
        try {
            BufferedImage img = ImageIO.read(new URL(imageUrl));
            BufferedImage preprocessed = preprocess(img);

            ITesseract tesseract = getTesseract();
            String text = tesseract.doOCR(preprocessed);

            return extractCccd(text)
                    .orElseThrow(() ->{
                        cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(imageUrl));
                        return new AppException(ErrorCode.NOT_FOUND_CCCD_IN_IMAGE);
                    });

        } catch (IOException e) {
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(imageUrl));
            throw new AppException(ErrorCode.UPLOAD_IMAGE_FAILED);
        } catch (TesseractException e) {
            cloudinaryService.delete(cloudinaryService.getPublicIdFromUrl(imageUrl));
            throw new AppException(ErrorCode.FAIL_OCR);
        }
    }

    private Optional<String> extractCccd(String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        Matcher matcher = CCCD_PATTERN.matcher(text);
        if (matcher.find()) return Optional.of(matcher.group());
        return Optional.empty();
    }
}

