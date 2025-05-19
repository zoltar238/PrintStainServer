package com.github.zoltar238.PrintStainServer.controller;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.service.PersonService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/person")
@Slf4j
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseApi<String>> registerPerson(@Valid @RequestBody PersonDto personDTO) {
        return personService.registerPerson(personDTO);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseApi<String>> deletePerson(@RequestParam String username) {
        return personService.deleteUser(username);
    }
}   