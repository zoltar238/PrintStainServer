package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ImageDto;
import com.github.zoltar238.PrintStainServer.dto.ItemDto;
import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.exceptions.ImageProcessingException;
import com.github.zoltar238.PrintStainServer.exceptions.ItemNotFoundException;
import com.github.zoltar238.PrintStainServer.exceptions.UnexpectedException;
import com.github.zoltar238.PrintStainServer.exceptions.UserNotFoundException;
import com.github.zoltar238.PrintStainServer.persistence.entity.ImageEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.ItemRepository;
import com.github.zoltar238.PrintStainServer.utils.ImageTransformer;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImp implements ItemService {

    private final ItemRepository itemRepository;
    private final PersonService personService;
    private final ImageService imageService;

    @Value("${downloads.storage.location}")
    private String downloadPath;

    @Value("${image.storage.location}")
    private String imageStorageLocation;


    public ItemServiceImp(ItemRepository itemRepository, PersonService personService, ImageService imageService) {
        this.itemRepository = itemRepository;
        this.personService = personService;
        this.imageService = imageService;
    }

    @Override
    public ResponseEntity<ResponseApi<List<ItemDto>>> getAllItems() {
        final String processCode = "001001";
        final String processDescription = "Get All Items";
        log.info("[MSG-{}: {} - Starting process] -> Retrieving all items from the database.", processCode, processDescription);

        try {
            log.debug("[MSG-{}: {} - Process] -> Fetching all item entities.", processCode, processDescription);
            List<ItemEntity> items = (List<ItemEntity>) itemRepository.findAll();

            List<ItemDto> itemsDto = new ArrayList<>();
            for (ItemEntity item : items) {
                log.debug("[MSG-{}: {} - Process] -> Processing item with ID: {}.", processCode, processDescription, item.getItemId());

                // Process images
                List<ImageDto> imageDtos = new ArrayList<>();
                for (ImageEntity image : item.getImages()) {
                    try {
                        imageDtos.add(ImageDto.builder()
                                .imageId(image.getImageId())
                                .base64Image(ImageTransformer.transformImageToBase64(image.getUrl()))
                                .build());
                    } catch (IOException e) {
                        log.error("[MSG-{}: {} - End of process] -> Failed to process image with ID: {} for item ID: {}.",
                                processCode, processDescription, image.getImageId(), item.getItemId(), e);
                        throw new ImageProcessingException("Error processing image for item " + item.getName());
                    }
                }

                // Get poster data
                PersonDto personDto = PersonDto.builder()
                        .name(item.getPerson().getName())
                        .username(item.getPerson().getUsername())
                        .personId(item.getPerson().getPersonId())
                        .isActive(item.getPerson().getIsActive())
                        .build();

                // Build item DTO
                itemsDto.add(ItemDto.builder()
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .fileStructure(item.getFileStructure())
                        .description(item.getDescription())
                        .postDate(item.getPostDate())
                        .timesUploaded(item.getTimesUploaded())
                        .person(personDto)
                        .images(imageDtos)
                        .build());
            }

            log.info("[MSG-{}: {} - End of process] -> Successfully retrieved {} items.", processCode, processDescription, itemsDto.size());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Items retrieved successfully", itemsDto));

        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error while getting all items. Details: {}.",
                    processCode, processDescription, e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while getting all items");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<ItemDto>> postItem(Long posterId, ItemDto itemDto) {
        final String processCode = "001002";
        final String processDescription = "Post New Item";
        log.info("[MSG-{}: {} - Starting process] -> Attempting to post new item '{}' by user ID: {}.",
                processCode, processDescription, itemDto.getName(), posterId);

        try {
            // Get the user from the database
            log.debug("[MSG-{}: {} - Process] -> Validating poster with ID: {}.", processCode, processDescription, posterId);
            PersonEntity poster = personService.getPersonById(posterId).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> User with id {} not found.", processCode, processDescription, posterId);
                return new UserNotFoundException("User not found");
            });

            // Save item to the database
            log.debug("[MSG-{}: {} - Process] -> Saving new item entity for '{}'.", processCode, processDescription, itemDto.getName());
            ItemEntity item = ItemEntity.builder()
                    .description(itemDto.getDescription())
                    .name(itemDto.getName())
                    .postDate(Timestamp.from(Instant.now()))
                    .person(poster).build();
            itemRepository.save(item);

            // Save images
            List<ImageEntity> images = new ArrayList<>();
            log.debug("[MSG-{}: {} - Process] -> Processing {} images for item '{}'.", processCode, processDescription, itemDto.getImages().size(), item.getName());
            for (ImageDto imageDto : itemDto.getImages()) {
                try {
                    File storageDir = new File(imageStorageLocation);
                    if (!storageDir.exists()) {
                        storageDir.mkdirs();
                    }
                    String url = imageStorageLocation + System.currentTimeMillis() + ".jpg";
                    ImageTransformer.saveImageToDisk(url, imageDto.getBase64Image());
                    images.add(ImageEntity.builder().url(url).link("empty").item(item).build());
                } catch (IOException e) {
                    log.error("[MSG-{}: {} - End of process] -> Error saving image to disk for item '{}'. Details: {}.",
                            processCode, processDescription, item.getName(), e.getMessage(), e);
                    throw new ImageProcessingException("Unexpected error while saving images to disk");
                }
            }
            imageService.saveImages(images);

            // Update item with images
            item.setImages(images);
            itemRepository.save(item);

            // Prepare response DTO
            itemDto.setItemId(item.getItemId());
            // ... (Rest of DTO preparation is fine)

            log.info("[MSG-{}: {} - End of process] -> Successfully posted item with new ID: {}.", processCode, processDescription, item.getItemId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Item saved successfully", itemDto));

        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while posting item. Details: {}.",
                    processCode, processDescription, e.getMessage(), e);
            throw new UnexpectedException("Database error occurred while posting item.");
        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error while posting item. Details: {}.",
                    processCode, processDescription, e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while posting item");
        }
    }


    @Override
    public ResponseEntity<ResponseApi<String>> deleteItems(List<ItemDto> itemDtos) {
        final String processCode = "001003";
        final String processDescription = "Delete Items";
        List<Long> itemIds = itemDtos.stream().map(ItemDto::getItemId).collect(Collectors.toList());
        log.info("[MSG-{}: {} - Starting process] -> Attempting to delete {} items with IDs: {}.",
                processCode, processDescription, itemDtos.size(), itemIds);

        try {
            itemRepository.deleteAllById(itemIds);
            String successMessage = itemDtos.size() > 1 ? "Items deleted successfully" : "Item deleted successfully";
            log.info("[MSG-{}: {} - End of process] -> {}.", processCode, processDescription, successMessage);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, successMessage, successMessage));
        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while deleting items with IDs: {}. Details: {}.",
                    processCode, processDescription, itemIds, e.getMessage(), e);
            throw new UnexpectedException("A database error occurred while deleting items.");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<ItemDto>> updateItem(ItemDto itemDto) {
        final String processCode = "001004";
        final String processDescription = "Update Item";
        log.info("[MSG-{}: {} - Starting process] -> Attempting to update item with ID: {}.",
                processCode, processDescription, itemDto.getItemId());

        try {
            ItemEntity itemEntity = itemRepository.findById(itemDto.getItemId()).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> Item with ID {} not found for update.",
                        processCode, processDescription, itemDto.getItemId());
                return new ItemNotFoundException("Item not found");
            });

            log.debug("[MSG-{}: {} - Process] -> Updating item details for ID: {}.", processCode, processDescription, itemDto.getItemId());
            itemEntity.setName(itemDto.getName());
            itemEntity.setDescription(itemDto.getDescription());

            log.debug("[MSG-{}: {} - Process] -> Updating or inserting images for item ID: {}.", processCode, processDescription, itemDto.getItemId());
            imageService.updateOrInsertImages(itemEntity, itemDto.getImages());

            itemRepository.save(itemEntity);

            // Re-process images for the response
            itemDto.getImages().clear();
            for (ImageEntity image : itemEntity.getImages()) {
                itemDto.getImages().add(ImageDto.builder()
                        .imageId(image.getImageId())
                        .base64Image(ImageTransformer.transformImageToBase64(image.getUrl()))
                        .build());
            }

            log.info("[MSG-{}: {} - End of process] -> Successfully updated item with ID: {}.", processCode, processDescription, itemDto.getItemId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Item updated successfully", itemDto));

        } catch (IOException e) {
            log.error("[MSG-{}: {} - End of process] -> Error processing images for updated item ID: {}. Details: {}.",
                    processCode, processDescription, itemDto.getItemId(), e.getMessage(), e);
            throw new ImageProcessingException("Error processing images on update.");
        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while updating item ID: {}. Details: {}.",
                    processCode, processDescription, itemDto.getItemId(), e.getMessage(), e);
            throw new UnexpectedException("A database error occurred while updating the item.");
        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error while updating item ID: {}. Details: {}.",
                    processCode, processDescription, itemDto.getItemId(), e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while updating item.");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<String>> uploadFiles(MultipartFile file, Long itemId, String fileStructure) {
        final String processCode = "001005";
        final String processDescription = "Upload Item Files";
        log.info("[MSG-{}: {} - Starting process] -> Attempting to upload file '{}' for item ID: {}.",
                processCode, processDescription, file.getOriginalFilename(), itemId);

        try {
            ItemEntity item = itemRepository.findById(itemId).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> Item with ID {} not found for file upload.",
                        processCode, processDescription, itemId);
                return new ItemNotFoundException("Item not found");
            });

            String previousFiles = item.getFilesUrl();
            if (previousFiles != null && new File(previousFiles).exists()) {
                log.debug("[MSG-{}: {} - Process] -> Deleting previous file at path: {}.", processCode, processDescription, previousFiles);
                new File(previousFiles).delete();
            }

            File downloadDir = new File(downloadPath);
            if (!downloadDir.exists()) {
                log.debug("[MSG-{}: {} - Process] -> Creating download directory at: {}.", processCode, processDescription, downloadPath);
                downloadDir.mkdirs();
            }

            log.debug("[MSG-{}: {} - Process] -> Saving file '{}' to disk.", processCode, processDescription, file.getOriginalFilename());
            String filePath = downloadPath + file.getOriginalFilename();
            file.transferTo(new java.io.File(filePath));

            log.debug("[MSG-{}: {} - Process] -> Updating item metadata in database for item ID: {}.", processCode, processDescription, itemId);
            item.setFileStructure(fileStructure);
            item.setFilesUrl(filePath);
            itemRepository.save(item);

            log.info("[MSG-{}: {} - End of process] -> File '{}' uploaded successfully for item ID: {}.",
                    processCode, processDescription, file.getOriginalFilename(), itemId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Files uploaded successfully", "Files uploaded successfully"));

        } catch (IOException e) {
            log.error("[MSG-{}: {} - End of process] -> I/O error while uploading file for item ID: {}. Details: {}.",
                    processCode, processDescription, itemId, e.getMessage(), e);
            throw new UnexpectedException("Error while saving the uploaded file.");
        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while uploading file for item ID: {}. Details: {}.",
                    processCode, processDescription, itemId, e.getMessage(), e);
            throw new UnexpectedException("A database error occurred while uploading the file.");
        }
    }

    @Override
    public ResponseEntity<?> downloadFiles(Long itemId) {
        final String processCode = "001006";
        final String processDescription = "Download Item Files";
        log.info("[MSG-{}: {} - Starting process] -> Attempting to download files for item ID: {}.",
                processCode, processDescription, itemId);

        try {
            ItemEntity item = itemRepository.findById(itemId).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> Item with ID {} not found for download.",
                        processCode, processDescription, itemId);
                return new ItemNotFoundException("Item not found");
            });

            if (item.getFilesUrl() == null || item.getFilesUrl().isEmpty()) {
                log.warn("[MSG-{}: {} - End of process] -> Item with ID {} has no file associated for download.",
                        processCode, processDescription, itemId);
                throw new ItemNotFoundException("File for this item does not exist.");
            }

            log.debug("[MSG-{}: {} - Process] -> Creating resource from path: {}.", processCode, processDescription, item.getFilesUrl());
            Path path = Paths.get(item.getFilesUrl());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("[MSG-{}: {} - End of process] -> File for item ID {} not found on disk at path: {}",
                        processCode, processDescription, itemId, item.getFilesUrl());
                throw new ItemNotFoundException("File not found on storage.");
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

            log.info("[MSG-{}: {} - End of process] -> Successfully prepared file '{}' for download.", processCode, processDescription, resource.getFilename());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("[MSG-{}: {} - End of process] -> Invalid file path for item ID: {}. Details: {}",
                    processCode, processDescription, itemId, e.getMessage(), e);
            throw new UnexpectedException("File path is invalid.");
        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error while downloading file for item ID: {}. Details: {}",
                    processCode, processDescription, itemId, e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while downloading file.");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<String>> deleteFiles(Long itemId) {
        final String processCode = "001007";
        final String processDescription = "Delete Item Files";
        log.info("[MSG-{}: {} - Starting process] -> Attempting to delete files for item ID: {}.",
                processCode, processDescription, itemId);

        try {
            ItemEntity item = itemRepository.findById(itemId).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> Item with ID {} not found for file deletion.",
                        processCode, processDescription, itemId);
                return new ItemNotFoundException("Item not found");
            });

            if (item.getFilesUrl() != null && !item.getFilesUrl().isEmpty()) {
                File file = new File(item.getFilesUrl());
                if (file.exists()) {
                    log.debug("[MSG-{}: {} - Process] -> Deleting file from storage: {}.", processCode, processDescription, item.getFilesUrl());
                    if (!file.delete()) {
                        log.warn("[MSG-{}: {} - Process] -> Could not delete file from storage: {}.", processCode, processDescription, item.getFilesUrl());
                    }
                }
            } else {
                log.info("[MSG-{}: {} - Process] -> No file was associated with item ID {}. No file to delete.", processCode, processDescription, itemId);
            }

            log.debug("[MSG-{}: {} - Process] -> Clearing file metadata in database for item ID: {}.", processCode, processDescription, itemId);
            item.setFilesUrl(null);
            item.setFileStructure(null);
            itemRepository.save(item);

            log.info("[MSG-{}: {} - End of process] -> Files deleted successfully for item ID: {}.", processCode, processDescription, itemId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Files deleted successfully", "Files deleted successfully"));

        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while deleting files for item ID: {}. Details: {}",
                    processCode, processDescription, itemId, e.getMessage(), e);
            throw new UnexpectedException("A database error occurred while deleting item files.");
        }
    }
}