package com.github.zoltar238.PrintStainServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonDto {

    private Long personId;
    private Boolean isActive;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Set<String> roles;
}
