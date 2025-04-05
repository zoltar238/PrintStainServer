package com.github.zoltar238.PrintStainServer.controller;

import com.github.zoltar238.PrintStainServer.dto.AllSalesDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.dto.SaleCreationDto;
import com.github.zoltar238.PrintStainServer.service.SaleServiceImp;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/deleteSale")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<String>> deleteSale(@RequestParam Long saleId) {
        log.info("Attempting to delete sale with id: {}", saleId);
        return saleServiceImp.deleteSale(saleId);
    }
}
