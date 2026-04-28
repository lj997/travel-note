package com.travelnote.service;

import com.travelnote.entity.Photo;
import com.travelnote.entity.User;
import com.travelnote.repository.PhotoRepository;
import com.travelnote.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    private static final int THUMBNAIL_MAX_WIDTH = 300;
    private static final int THUMBNAIL_MAX_HEIGHT = 300;
    
    private final Path fileStorageLocation;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    
    public FileStorageService(@Value("${file.upload.path:uploads/}") String uploadPath,
                              PhotoRepository photoRepository,
                              UserRepository userRepository) {
        this.fileStorageLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建上传目录", ex);
        }
    }
    
    @Transactional
    public Photo storeFile(MultipartFile file, Long userId) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        if (originalFileName.contains("..")) {
            throw new RuntimeException("文件名包含非法路径序列: " + originalFileName);
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }
        
        byte[] fileContent = file.getBytes();
        
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path dateDirectory = this.fileStorageLocation.resolve(datePath);
        Files.createDirectories(dateDirectory);
        
        Path targetLocation = dateDirectory.resolve(uniqueFileName);
        Files.write(targetLocation, fileContent);
        
        int[] dimensions = getImageDimensions(new ByteArrayInputStream(fileContent), contentType);
        int width = dimensions[0];
        int height = dimensions[1];
        
        String thumbnailFileName = createThumbnailFromBytes(fileContent, dateDirectory, uniqueFileName, contentType);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Photo photo = new Photo();
        photo.setUser(user);
        photo.setUrl("/uploads/" + datePath + "/" + uniqueFileName);
        photo.setThumbnailUrl("/uploads/" + datePath + "/" + thumbnailFileName);
        photo.setOriginalName(originalFileName);
        photo.setContentType(contentType);
        photo.setFileSize(file.getSize());
        photo.setWidth(width);
        photo.setHeight(height);
        photo.setDisplayOrder(0);
        
        return photoRepository.save(photo);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private int[] getImageDimensions(InputStream inputStream, String contentType) throws IOException {
        try {
            BufferedImage image = ImageIO.read(inputStream);
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
        } catch (Exception e) {
            logger.warn("无法读取图片尺寸", e);
        }
        return new int[]{0, 0};
    }
    
    private String createThumbnailFromBytes(byte[] fileContent, Path directory, String originalFileName, String contentType) {
        try {
            String thumbnailFileName = "thumb_" + originalFileName;
            Path thumbnailPath = directory.resolve(thumbnailFileName);
            
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(fileContent));
            if (originalImage == null) {
                logger.warn("无法读取图片，跳过缩略图生成: {}", contentType);
                return originalFileName;
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            double scaleRatio = Math.min(
                    (double) THUMBNAIL_MAX_WIDTH / originalWidth,
                    (double) THUMBNAIL_MAX_HEIGHT / originalHeight
            );
            
            if (scaleRatio >= 1.0) {
                return originalFileName;
            }
            
            int newWidth = (int) (originalWidth * scaleRatio);
            int newHeight = (int) (originalHeight * scaleRatio);
            
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage thumbnailImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D g2d = thumbnailImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            
            String formatName = getSafeFormatName(contentType);
            boolean success = ImageIO.write(thumbnailImage, formatName, thumbnailPath.toFile());
            
            if (!success) {
                logger.warn("无法写入缩略图，使用原文件名: {}", contentType);
                return originalFileName;
            }
            
            return thumbnailFileName;
        } catch (Exception e) {
            logger.warn("创建缩略图失败", e);
            return originalFileName;
        }
    }
    
    private String getSafeFormatName(String contentType) {
        if ("image/webp".equals(contentType)) {
            return "jpg";
        }
        return getFormatName(contentType);
    }
    
    private String getFormatName(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
    
    public Path loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                throw new RuntimeException("文件未找到: " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("文件未找到: " + fileName, ex);
        }
    }
    
    @Transactional
    public void deletePhoto(Long photoId, Long userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("照片不存在"));
        
        if (!photo.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此照片");
        }
        
        try {
            Path photoPath = this.fileStorageLocation.resolve(
                    photo.getUrl().replaceFirst("/uploads/", ""));
            Files.deleteIfExists(photoPath);
            
            if (photo.getThumbnailUrl() != null) {
                Path thumbnailPath = this.fileStorageLocation.resolve(
                        photo.getThumbnailUrl().replaceFirst("/uploads/", ""));
                Files.deleteIfExists(thumbnailPath);
            }
        } catch (Exception e) {
            logger.warn("删除文件失败", e);
        }
        
        photoRepository.delete(photo);
    }
}
