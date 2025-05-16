package com.github.zoltar238.PrintStainServer.utils;

import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseBuilder {
    public <T> ResponseApi<T> buildResponse(boolean success, String response, T data) {
        return ResponseApi.<T>builder()
                .success(success)
                .response(response)
                .data(data)
                .build();
    }
}
