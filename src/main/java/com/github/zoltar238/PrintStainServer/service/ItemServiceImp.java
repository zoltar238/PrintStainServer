package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ImageDto;
import com.github.zoltar238.PrintStainServer.dto.ItemDto;
import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.exceptions.ImageProcessingException;
import com.github.zoltar238.PrintStainServer.exceptions.UnexpectedException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImp implements ItemService {

    private final ItemRepository itemRepository;
    private final PersonService personService;

    public ItemServiceImp(ItemRepository itemRepository, PersonService personService) {
        this.itemRepository = itemRepository;
        this.personService = personService;
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
    public ResponseEntity<?> postItem(Long posterId, ItemDto itemDto) {
        Optional<PersonEntity> poster = personService.getPersonById(posterId);
        // Todo: add image porocessing
        if (poster.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseBuilder.buildResponse(false, "User not found", null));
        }else {
            itemRepository.save(ItemEntity.builder()
                    .itemId(itemDto.getItemId())
                    .description(itemDto.getDescription())
                    .name(itemDto.getName())
                    .timesUploaded(itemDto.getTimesUploaded())
                    // Add images
                    .person(poster.get()).build());
        }
        return null;
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
