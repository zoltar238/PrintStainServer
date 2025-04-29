package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ImageDto;
import com.github.zoltar238.PrintStainServer.exceptions.ImageProcessingException;
import com.github.zoltar238.PrintStainServer.persistence.entity.ImageEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.ImageRepository;
import com.github.zoltar238.PrintStainServer.utils.ImageTransformer;
import com.github.zoltar238.PrintStainServer.utils.OsChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void saveImages(List<ImageEntity> images) {
        imageRepository.saveAll(images);
    }

    @Override
    public void updateOrInsertImages(ItemEntity item, List<ImageDto> imageDtos) {
        List<ImageEntity> images = item.getImages();
        if (images != null) {
            images.forEach(image -> {
                // Delete the old image file if it exists
                File oldImage = new File(image.getUrl());
                if (!oldImage.exists()) {
                    log.warn("Failed to delete old image file: {}", image.getUrl());
                } else {
                    oldImage.delete();
                    log.info("Old image file deleted successfully: {}", image.getUrl());
                }
            });
            // Delete all the old images associated with the item
            imageRepository.deleteAll(images);
            // Clear the old images from the item
            item.getImages().clear();
        }

        // Save the new images to the database and disk
        imageDtos.forEach(imageDto -> {
            try {
                // Weed out empty images
                if (imageDto.getBase64Image().length() > 90) {
                    String url = "src/main/resources/images/" + System.currentTimeMillis() + ".jpg";
                    ImageEntity newImage = imageRepository.save(ImageEntity.builder().
                            url(url).
                            item(item).
                            build());

                    // Add the new image to the item
                    item.getImages().add(newImage);

                    // Save the new image to disk
                    ImageTransformer.saveImageToDisk(url, imageDto.getBase64Image());
                }
            } catch (IOException e) {
                log.error("Error saving image to disk: {}", e.getMessage(), e);
                throw new ImageProcessingException("Unexpected error while saving or processing images");
            }
        });
    }
}
