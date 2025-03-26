package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.AllSalesDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.dto.ResponsesEnum;
import com.github.zoltar238.PrintStainServer.dto.SaleCreationDto;
import com.github.zoltar238.PrintStainServer.exceptions.CostOrPriceInvalidException;
import com.github.zoltar238.PrintStainServer.exceptions.ItemNotFoundException;
import com.github.zoltar238.PrintStainServer.exceptions.UnexpectedException;
import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.SaleEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.ItemRepository;
import com.github.zoltar238.PrintStainServer.persistence.repository.SaleRepository;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SaleServiceImp implements SaleService {

    private final SaleRepository saleRepository;
    private final ItemRepository itemRepository;

    public SaleServiceImp(SaleRepository saleRepository, ItemRepository itemRepository) {
        this.saleRepository = saleRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ResponseEntity<ResponseApi<List<AllSalesDto>>> getAllSales() {
        // Retrieve all sales from database
        Iterable<SaleEntity> sales = saleRepository.findAll();

        // Return list of sale data to the client
        List<AllSalesDto> allSaleData = new ArrayList<>();

        sales.forEach(sale -> {
            AllSalesDto allSalesDto = AllSalesDto.builder()
                    .saleId(sale.getSaleId())
                    .cost(sale.getCost())
                    .price(sale.getPrice())
                    .date(sale.getDate())
                    .itemName(sale.getItem().getName())
                    .itemId(sale.getItem().getItemId())
                    .build();

            allSaleData.add(allSalesDto);
        });

        log.info("All sales retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseBuilder.buildResponse(true,
                        "All sales retrieved successfully",
                        allSaleData));
    }

    @Override
    public ResponseEntity<ResponseApi<Long>> createNewSale(SaleCreationDto saleCreationDto) {
        // Find item by id
        Optional<ItemEntity> item = itemRepository.findById(saleCreationDto.getItemId());

        // Check if item exists
        if (item.isEmpty()) {
            log.error("Item with ID {} associated with sale not found", saleCreationDto.getItemId());
            throw new ItemNotFoundException("Item with name " + saleCreationDto.getItemName() + " associated with sale not found");
        }

        // Validate cost and price
        if (((saleCreationDto.getCost() == null) || (saleCreationDto.getCost().longValue() <= 0)) || (saleCreationDto.getPrice() == null || saleCreationDto.getPrice().longValue() <= 0)) {
            log.error("Cost or Price not found or invalid");
            throw new CostOrPriceInvalidException("Cost or Price not found or invalid");
        }

        // Cost cannot be greater than Price
        if (saleCreationDto.getCost().compareTo(saleCreationDto.getPrice()) > 0) {
            log.error("Cost cannot be greater than Price");
            throw new CostOrPriceInvalidException("Cost cannot be greater than Price");
        }

        try {
            // Create new sale and save to the database
            SaleEntity saleEntity = SaleEntity.builder()
                    .cost(saleCreationDto.getCost())
                    .price(saleCreationDto.getPrice())
                    .date(saleCreationDto.getDate())
                    .item(item.get())
                    .build();

            SaleEntity sale = saleRepository.save(saleEntity);

            // Return success response
            log.info("Sale created successfully");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseBuilder.buildResponse(true,
                            "Sale created successfully",
                            sale.getSaleId()));
        } catch (Exception e) {
            // Handle unexpected exceptions
            log.error("An unexpected error occurred: {}", e.getMessage(), e);
            throw new UnexpectedException("An unexpected error occurred while getting all sales");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<String>> deleteSale(Long saleId) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseApi<String>> updateSale(SaleCreationDto saleCreationDto) {
        return null;
    }
}
