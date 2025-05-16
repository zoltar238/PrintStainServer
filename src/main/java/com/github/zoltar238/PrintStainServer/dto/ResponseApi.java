package com.github.zoltar238.PrintStainServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseApi<T> {
    private boolean success;
    private String response;
    private T data;
}
