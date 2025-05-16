package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ItemDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface ItemService {

    ResponseEntity<ResponseApi<List<ItemDto>>> getAllItems();

    ResponseEntity<ResponseApi<ItemDto>> postItem(Long posterId, ItemDto itemDto);

    ResponseEntity<ResponseApi<String>> deleteItems(@NotNull @Valid List<ItemDto> itemDtos);

    ResponseEntity<ResponseApi<ItemDto>> updateItem(@NotNull @Valid ItemDto itemDto);

    ResponseEntity<ResponseApi<String>> uploadFiles(MultipartFile file, Long itemId, String fileStructure);

    ResponseEntity<?> downloadFiles(Long itemId);

    ResponseEntity<ResponseApi<String>> deleteFiles(Long itemId);
}