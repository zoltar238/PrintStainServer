package com.github.zoltar238.PrintStainServer.controller;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.security.jwt.JwtUtils;
import com.github.zoltar238.PrintStainServer.service.PersonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
@Slf4j
public class PersonController {

    private final PersonService personService;

    private final JwtUtils jwtUtils;

    public PersonController(PersonService personService, JwtUtils jwtUtils) {
        this.personService = personService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseApi<String>> registerPerson(@Valid @RequestBody PersonDto personDTO) {
        return personService.registerPerson(personDTO);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ResponseApi<String>> resetPassword(@Valid @RequestBody PersonDto personDTO) {
        return personService.resetPassword(personDTO);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseApi<String>> deletePerson(@NotNull HttpServletRequest request) {
        // Get the user id from the token
        String tokenHeader = request.getHeader("Authorization");
        String token = tokenHeader.substring(7);
        Long posterId = jwtUtils.getIdFromToken(token);
        return personService.deleteUser(posterId);
    }
}