package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.persistence.entity.ImageEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ImageService {

    void saveImages(List<ImageEntity> image);
}
