package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.AllSalesDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.dto.SaleCreationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SaleService {

    ResponseEntity<ResponseApi<List<AllSalesDto>>> getAllSales();

    ResponseEntity<ResponseApi<Long>> createNewSale(SaleCreationDto saleCreationDto);

    ResponseEntity<ResponseApi<String>> deleteSale(Long saleId);

    ResponseEntity<ResponseApi<String>> updateSale(SaleCreationDto saleCreationDto);
}
