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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ItemServiceImp implements ItemService {

    private final ItemRepository itemRepository;
    private final PersonService personService;
    private final ImageService imageService;

    public ItemServiceImp(ItemRepository itemRepository, PersonService personService, ImageService imageService) {
        this.itemRepository = itemRepository;
        this.personService = personService;
        this.imageService = imageService;
    }

    @Override
    public ResponseEntity<ResponseApi<List<ItemDto>>> getAllItems() {
        try {
            // Get all items from the database
            List<ItemEntity> items = (List<ItemEntity>) itemRepository.findAll();

            // Create a dto list with each item
            List<ItemDto> itemsDto = new ArrayList<>();
            for (ItemEntity item : items) {


                // Process each image in the item and transform it to base64 format
                List<ImageDto> imageDtos = new ArrayList<>();
                for (ImageEntity image : item.getImages()) {
                    imageDtos.add(ImageDto.builder()
                            .imageId(image.getImageId())
                            .base64Image(ImageTransformer.transformImageToBase64(image.getUrl()))
                            .build());
                }

                // Get poster data
                PersonDto personDto = PersonDto.builder()
                        .name(item.getPerson().getName())
                        .personId(item.getPerson().getPersonId())
                        .build();

                // Add item to the list to be transferred
                ItemDto itemDTO = ItemDto.builder()
                        .itemId(item.getItemId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .postDate(item.getPostDate())
                        .timesUploaded(item.getTimesUploaded())
                        .person(personDto)
                        .images(imageDtos)
                        .build();
                itemsDto.add(itemDTO);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Items retrieved successfully", itemsDto));
        } catch (IOException e) {
            log.error("Error processing images: {}", e.getMessage(), e);
            throw new ImageProcessingException("Error processing images");
        } catch (Exception e) {
            log.error("Unexpected error while getting all items: {}", e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while getting all items");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<ItemDto>> postItem(Long posterId, ItemDto itemDto) {
        // Get the user from the database
        Optional<PersonEntity> poster = personService.getPersonById(posterId);
        if (poster.isEmpty()) {
            log.warn("User with id {} not found", posterId);
            throw new UserNotFoundException("User not found");
        }

        // Save item to the database
        ItemEntity item = itemRepository.save(ItemEntity.builder()
                .itemId(itemDto.getItemId())
                .description(itemDto.getDescription())
                .name(itemDto.getName())
                .postDate(Timestamp.from(Instant.now()))
                .person(poster.get()).build());

        // Save images to disk and database
        List<ImageEntity> images = new ArrayList<>();
        for (ImageDto imageDto : itemDto.getImages()) {
            try {
                String url = "src/main/resources/images/" + System.currentTimeMillis() + ".jpg";
                ImageTransformer.saveImageToDisk(url, imageDto.getBase64Image());
                ImageEntity imageEntity = ImageEntity.builder()
                        .url(url)
                        .link("empty")
                        .item(item)
                        .build();
                images.add(imageEntity);
            } catch (IOException e) {
                log.error("Error saving image to disk: {}", e.getMessage(), e);
                throw new ImageProcessingException("Unexpected error while saving images to disk");
            }
        }
        imageService.saveImages(images);

        // Update item with images
        item.setImages(images);
        itemRepository.save(item);

        itemDto.setItemId(item.getItemId());
        itemDto.setPostDate(item.getPostDate());
        itemDto.setTimesUploaded(item.getTimesUploaded());
        itemDto.setPerson(PersonDto.builder()
                .name(poster.get().getName())
                .personId(poster.get().getPersonId())
                .build());
        for (int i = 0; i < itemDto.getImages().size(); i++) {
            itemDto.getImages().get(i).setImageId(item.getImages().get(i).getImageId());
            try {
                itemDto.getImages().get(i).setBase64Image(ImageTransformer.transformImageToBase64(item.getImages().get(i).getUrl()));
            } catch (IOException e) {
                log.error("Error processing images: {}", e.getMessage(), e);
                throw new ImageProcessingException("Error processing images");
            }
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseBuilder.buildResponse(true, "Item saved successfully", itemDto));
    }


    @Override
    public ResponseEntity<ResponseApi<String>> deleteItems(List<ItemDto> itemDtos) {
        itemRepository.deleteAllById(itemDtos.stream().map(ItemDto::getItemId).toList());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseBuilder.buildResponse(true, itemDtos.size() > 1 ? "Items deleted successfully" : "Item deleted successfully", itemDtos.size() > 1 ? "Items deleted successfully" : "Item deleted successfully"));
    }

    @Override
    public ResponseEntity<ResponseApi<ItemDto>> updateItem(ItemDto itemDto) {
        Optional<ItemEntity> item = itemRepository.findById(itemDto.getItemId());
        if (item.isEmpty()) {
            log.warn("Item with id {} not found", itemDto.getItemId());
            throw new ItemNotFoundException("Item not found");
        } else {
            ItemEntity itemEntity = item.get();
            itemEntity.setName(itemDto.getName());
            itemEntity.setDescription(itemDto.getDescription());

            // Update images
            imageService.updateOrInsertImages(item.get(), itemDto.getImages());

            // Update item with new images and description
            itemRepository.save(itemEntity);

            // Reload item from the database to get updated images
//            item = itemRepository.findByIdWithImages(itemDto.getItemId());
            itemDto.getImages().clear();

            // Process each image in the item and transform it to base64 format
            for (ImageEntity image : item.get().getImages()) {
                try {
                    itemDto.getImages().add(ImageDto.builder()
                            .imageId(image.getImageId())
                            .base64Image(ImageTransformer.transformImageToBase64(image.getUrl()))
                            .build());
                } catch (IOException e) {
                    log.error("Error processing images: {}", e.getMessage(), e);
                    throw new ImageProcessingException("Error processing images");
                }
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseBuilder.buildResponse(true, "Item updated successfully", itemDto));
        }
    }

    @Override
    public ResponseEntity<ResponseApi<List<ItemDto>>> getAllItemsByUser(String username) {
        return null;
    }

    @Override
    public ResponseEntity<?> deleteItemById(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<?> modifyItemById(Long id, ItemDto itemDto) {
        return null;
    }
}
