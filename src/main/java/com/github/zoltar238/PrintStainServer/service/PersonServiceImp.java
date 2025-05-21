package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.exceptions.EmailAlreadyExistsException;
import com.github.zoltar238.PrintStainServer.exceptions.UnexpectedException;
import com.github.zoltar238.PrintStainServer.exceptions.UserNotFoundException;
import com.github.zoltar238.PrintStainServer.exceptions.UsernameAlreadyExistsException;
import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEnum;
import com.github.zoltar238.PrintStainServer.persistence.repository.PersonRepository;
import com.github.zoltar238.PrintStainServer.persistence.repository.RoleRepository;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class PersonServiceImp implements PersonService {

    private final PersonRepository personRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public PersonServiceImp(PasswordEncoder passwordEncoder, PersonRepository personRepository, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ResponseEntity<ResponseApi<String>> registerPerson(PersonDto personDTO) {
        log.info("Attempting registration of new user with username: {}", personDTO.getUsername());

        try {
            Set<RoleEntity> roles = new HashSet<>();

            // look for the role
            for (String roleName : personDTO.getRoles()) {
                Optional<RoleEntity> roleOpt = roleRepository.findByName(RoleEnum.valueOf(roleName));
                RoleEntity role;

                if (roleOpt.isPresent()) {
                    // if the role exists, use it
                    role = roleOpt.get();
                } else {
                    // else, create it
                    role = RoleEntity.builder()
                            .name(RoleEnum.valueOf(roleName))
                            .build();
                    // save the new role
                    roleRepository.save(role);
                }
                roles.add(role);
            }

            // Check if the username was used by a deleted account
            if (personRepository.findByPreDeleteUsername(personDTO.getUsername()).isPresent()) {
                throw new DataIntegrityViolationException("Key (username) already exists in database");
            }

            PersonEntity person = PersonEntity.builder()
                    .name(personDTO.getName())
                    .surname(personDTO.getSurname())
                    .username(personDTO.getUsername())
                    .password(passwordEncoder.encode(personDTO.getPassword()))
                    .email(personDTO.getEmail())
                    .createDate(new Timestamp(System.currentTimeMillis()))
                    .roles(roles)
                    .build();

            personRepository.save(person);

            log.info("User registered successfully with username: {}", personDTO.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseBuilder.buildResponse(true, "User registered successfully", null));

            // todo: improve error registration error handling
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Key (email)") || e.getMessage().contains("person_email_key")) {
                log.error("User registration error, user with email \"{}\" already exists: {}", personDTO.getEmail(), e.getMessage(), e);
                throw new EmailAlreadyExistsException("This email is already registered. Please use a different one.");
            } else if (e.getMessage().contains("Key (username)") || e.getMessage().contains("person_username_key")) {
                log.error("User registration error, user with username \"{}\" already exists: {}", personDTO.getUsername(), e.getMessage(), e);
                throw new UsernameAlreadyExistsException("This username is already registered. Please use a different one.");
            } else {
                log.error("User registration unexpected error: {}", e.getMessage(), e);
                throw new UnexpectedException("Unexpected error while registering user");
            }
        } catch (Exception e) {
            log.error("User registration unexpected error: {}", e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while registering user");
        }
    }

    @Override
    public Optional<PersonEntity> getPersonById(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public ResponseEntity<ResponseApi<String>> deleteUser(Long userId) {
        log.info("Attempting to delete user with username: {}", userId);

        try {
            // Delete user
            PersonEntity person = personRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
            person.setPreDeleteUsername(person.getUsername());
            person.setUsername("deleted_user_" + userId);
            person.setEmail("deleted_user_" + userId + "@deleted.com");
            person.setPassword("deleted_user_" + userId);
            person.setIsActive(false);
            personRepository.save(person);

            log.info("Successfully deleted user: {} with id: {}", person.getPreDeleteUsername(), userId);

            return ResponseEntity.ok(new ResponseApi<>(true, "User deleted successfully", "User deleted successfully"));
        } catch (Exception e) {
            log.error("Unexpected error deleting user with id : {} {}", userId,  e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while deleting user");
        }
    }
    //Todo: implement password update

    @Override
    public ResponseEntity<ResponseApi<String>> updatePassword() {
        return null;
    }
}
