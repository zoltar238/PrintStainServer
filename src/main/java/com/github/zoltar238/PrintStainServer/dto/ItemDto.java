package com.github.zoltar238.PrintStainServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private Long itemId;
    private String name;
    private String description;
    private Timestamp postDate;
    private Integer timesUploaded;
    private List<String> hashtags;
    private List<ImageDto> images;
    private PersonDto person;
}
