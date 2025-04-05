package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ImageDto;
import com.github.zoltar238.PrintStainServer.persistence.entity.ImageEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ImageService {

    void saveImages(List<ImageEntity> image);

    void updateOrInsertImages(ItemEntity item, List<ImageDto> imageDtos);
}
