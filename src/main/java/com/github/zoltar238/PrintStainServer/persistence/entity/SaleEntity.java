package com.github.zoltar238.PrintStainServer.persistence.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
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
@Entity
@Table(name = "sale")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    @jdk.jfr.Timestamp
    @NotNull
    private Timestamp date;

    @NotNull
    @Digits(integer = 4, fraction = 2)
    private BigDecimal cost;

    @NotNull
    @Digits(integer = 4, fraction = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private TaskStatusEnum status = TaskStatusEnum.IN_PROGRESS;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEntity item;

}
