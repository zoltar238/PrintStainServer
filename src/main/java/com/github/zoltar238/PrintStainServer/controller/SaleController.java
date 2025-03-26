package com.github.zoltar238.PrintStainServer.controller;

import com.github.zoltar238.PrintStainServer.dto.AllSalesDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.dto.ResponsesEnum;
import com.github.zoltar238.PrintStainServer.dto.SaleCreationDto;
import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.SaleEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.ItemRepository;
import com.github.zoltar238.PrintStainServer.persistence.repository.SaleRepository;
import com.github.zoltar238.PrintStainServer.service.SaleService;
import com.github.zoltar238.PrintStainServer.service.SaleServiceImp;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/sale")
public class SaleController {


    private final SaleServiceImp saleServiceImp;

    public SaleController(SaleServiceImp saleServiceImp) {
        this.saleServiceImp = saleServiceImp;
    }


    @PostMapping("/newSale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<Long>> createNewSale(@Valid @RequestBody SaleCreationDto saleCreationDto) {
        log.info("Attempting to create a new sale");
        return saleServiceImp.createNewSale(saleCreationDto);
    }

    @GetMapping("/getAllSales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<List<AllSalesDto>>> getAllSales() {
        log.info("Attempting to retrieve all sales");
        return saleServiceImp.getAllSales();
    }
}
