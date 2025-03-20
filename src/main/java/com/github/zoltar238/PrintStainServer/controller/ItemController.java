package com.github.zoltar238.PrintStainServer.controller;

import com.github.zoltar238.PrintStainServer.dto.ItemDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.security.jwt.JwtUtils;
import com.github.zoltar238.PrintStainServer.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/item")
public class ItemController {

    final ItemService itemService;

    @Autowired
    private JwtUtils jwtUtils;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/getAllItems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<ItemDto>>> getAllItems() {
        return itemService.getAllItems();
    }

    @PostMapping("/postItem")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<ItemDto>> postItem(@NotNull HttpServletRequest request, @NotNull @RequestBody @Valid ItemDto itemDto) {
        // Get the user id from the token
        String tokenHeader = request.getHeader("Authorization");
        String token = tokenHeader.substring(7);
        Long posterId = jwtUtils.getIdFromToken(token);
        return itemService.postItem(posterId, itemDto);
    }


    @DeleteMapping("/deleteItems")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<String>> deleteItems(@NotNull @RequestBody @Valid List<ItemDto> itemDtos) {
        return itemService.deleteItems(itemDtos);
    }
}