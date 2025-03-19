package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.ItemDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ItemService {

    ResponseEntity<ResponseApi<List<ItemDto>>> getAllItems();

    ResponseEntity<ResponseApi<List<ItemDto>>> getAllItemsByUser(String username);

    ResponseEntity<?> deleteItemById(Long id);

    ResponseEntity<?> modifyItemById(Long id, ItemDto itemDto);

    ResponseEntity<?> postItem(Long posterId, ItemDto itemDto);
}
