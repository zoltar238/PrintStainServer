package com.github.zoltar238.PrintStainServer.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DialectOverride;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "item")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @NotBlank
    @Size(max = 80)
    private String name;

    @NotBlank
    @Size(max = 340)
    private String description;

    @jdk.jfr.Timestamp
    @NotNull
    private Timestamp postDate;

    @ColumnDefault("1")
    private Integer timesUploaded;

    @OneToMany(mappedBy = "item", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    private Set<SaleEntity> sales;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ImageEntity> images;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Set<PieceEntity> pieces;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = HashtagEntity.class)
    @JoinTable(name = "item_hashtag", joinColumns = @JoinColumn(name = "item_id"), inverseJoinColumns = @JoinColumn(name = "hashtag_id"))
    private Set<HashtagEntity> hashtags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private PersonEntity person;
}
