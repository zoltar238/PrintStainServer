package com.github.zoltar238.PrintStainServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleCreationDto {
    private Timestamp date;
    private BigDecimal cost;
    private BigDecimal price;
    private Long saleId;
    private Long itemId;
    private String itemName;
    private String status;
}
