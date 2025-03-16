package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface PersonService {

    ResponseEntity<ResponseApi<String>> registerPerson(PersonDto personDTO);

    ResponseEntity<ResponseApi<String>> deleteUser();

    ResponseEntity<ResponseApi<String>> updatePassword();
}
