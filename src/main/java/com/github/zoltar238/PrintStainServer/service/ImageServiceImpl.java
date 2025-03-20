package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.persistence.entity.ImageEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void saveImages(List<ImageEntity> images) {
        imageRepository.saveAll(images);
    }
}
