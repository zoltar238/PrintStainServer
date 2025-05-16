package com.github.zoltar238.PrintStainServer.persistence.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "piece")
public class PieceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pieceId;

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotBlank
    private int scale;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private StatusEntity status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEntity item;

}
