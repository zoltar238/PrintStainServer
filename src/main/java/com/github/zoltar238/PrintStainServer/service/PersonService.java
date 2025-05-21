package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface PersonService {

    ResponseEntity<ResponseApi<String>> registerPerson(PersonDto personDTO);

    ResponseEntity<ResponseApi<String>> deleteUser(Long userId);

    ResponseEntity<ResponseApi<String>> updatePassword();

    Optional<PersonEntity> getPersonById(Long id);
}
