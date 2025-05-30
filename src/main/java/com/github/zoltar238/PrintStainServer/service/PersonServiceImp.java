package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.dto.PersonDto;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.exceptions.*;
import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEnum;
import com.github.zoltar238.PrintStainServer.persistence.repository.PersonRepository;
import com.github.zoltar238.PrintStainServer.persistence.repository.RoleRepository;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
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
        // Process info
        String processCode = "000001";
        String processDescription = "User registration";

        log.info("[MSG-{}: {} - Starting process] -> Attempting to register user with username: '{}', email: '{}', roles: '{}'.",
                processCode, processDescription, personDTO.getUsername(), personDTO.getEmail(), personDTO.getRoles());

        try {
            Set<RoleEntity> roles = new HashSet<>();

            // Validate roles
            log.debug("[MSG-{}: {} - Process] -> Validating roles for user: \"{}\".",
                    processCode, processDescription, personDTO.getUsername());

            for (String roleName : personDTO.getRoles()) {
                RoleEntity userRole = roleRepository.findByName(RoleEnum.valueOf(roleName)).orElseThrow(() -> {
                    log.warn("[MSG-{}: {} - End of process] -> Role validation failed for user: \"{}\", role \"{}\" not found.",
                            processCode, processDescription, personDTO.getUsername(), roleName);
                    return new RoleNotFoundException("Role \"" + roleName + "\" not found");
                });
                roles.add(userRole);
            }

            // Check if the username was used by a deleted account
            log.debug("[MSG-{}: {} - Process] -> Checking for previously deleted username: \"{}\".",
                    processCode, processDescription, personDTO.getUsername());

            if (personRepository.findByPreDeleteUsername(personDTO.getUsername()).isPresent()) {
                log.warn("[MSG-{}: {} - End of process] -> Username \"{}\" was previously used by a deleted account.",
                        processCode, processDescription, personDTO.getUsername());
                throw new DataIntegrityViolationException("Key (username) already exists in database");
            }

            // Create new user
            log.debug("[MSG-{}: {} - Process] -> Creating new user entity for username: \"{}\".",
                    processCode, processDescription, personDTO.getUsername());

            PersonEntity person = PersonEntity.builder()
                    .name(personDTO.getName())
                    .surname(personDTO.getSurname())
                    .username(personDTO.getUsername())
                    .password(passwordEncoder.encode(personDTO.getPassword()))
                    .email(personDTO.getEmail())
                    .createDate(new Timestamp(System.currentTimeMillis()))
                    .roles(roles)
                    .build();

            PersonEntity savedPerson = personRepository.save(person);

            log.info("[MSG-{}: {} - End of process] -> Successfully registered user with ID: {}, username: \"{}\".",
                    processCode, processDescription, savedPerson.getPersonId(), personDTO.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseBuilder.buildResponse(true, "User registered successfully", "User registered successfully"));

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Key (email)") || e.getMessage().contains("person_email_key")) {
                log.warn("[MSG-{}: {} - End of process] -> Email conflict for user \"{}\", email \"{}\" already exists.",
                        processCode, processDescription, personDTO.getUsername(), personDTO.getEmail());
                throw new EmailAlreadyExistsException("This email is already registered. Please use a different one.");

            } else if (e.getMessage().contains("Key (username)") || e.getMessage().contains("person_username_key")) {
                log.warn("[MSG-{}: {} - End of process] -> Username conflict for user: \"{}\", username already exists.",
                        processCode, processDescription, personDTO.getUsername());
                throw new UsernameAlreadyExistsException("This username is already registered. Please use a different one.");

            } else {
                log.error("[MSG-{}: {} - End of process] -> Database integrity violation for user: \"{}\". Details: {}.",
                        processCode, processDescription, personDTO.getUsername(), e.getMessage());
                throw new UnexpectedException("Unexpected error while registering user");
            }

        } catch (IllegalArgumentException e) {
            // Para capturar errores de RoleEnum.valueOf()
            log.warn("[MSG-{}: {} - End of process] -> Invalid role provided for user: \"{}\". Details: {}.",
                    processCode, processDescription, personDTO.getUsername(), e.getMessage());
            throw new RoleNotFoundException("Invalid role provided: " + e.getMessage());

        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error registering user: \"{}\". Details: {}.",
                    processCode, processDescription, personDTO.getUsername(), e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while registering user");
        }
    }

    @Override
    public Optional<PersonEntity> getPersonById(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public ResponseEntity<ResponseApi<String>> resetPassword(PersonDto personDTO) {
        // Process info
        String processCode = "000002";
        String processDescription = "User password reset";

        log.info("[MSG-{}: {} - Starting process] -> Attempting to reset password for username: \"{}\".",
                processCode, processDescription, personDTO.getUsername());

        try {
            // Find user by username
            log.debug("[MSG-{}: {} - Process] -> Searching for user with username: \"{}\".",
                    processCode, processDescription, personDTO.getUsername());

            PersonEntity person = personRepository.findByUsername(personDTO.getUsername()).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> User not found for password reset, username: \"{}\".",
                        processCode, processDescription, personDTO.getUsername());
                return new UserNotFoundException("User not found with username: " + personDTO.getUsername());
            });

            // Update password
            log.debug("[MSG-{}: {} - Process] -> Updating password for user ID: {}, username: \"{}\".",
                    processCode, processDescription, person.getPersonId(), personDTO.getUsername());

            person.setPassword(passwordEncoder.encode(personDTO.getPassword()));
            personRepository.save(person);

            log.info("[MSG-{}: {} - End of process] -> Successfully reset password for user ID: {}, username: \"{}\".",
                    processCode, processDescription, person.getPersonId(), personDTO.getUsername());

            return ResponseEntity.ok(ResponseBuilder.buildResponse(true, "Password reset successfully", "Password reset successfully"));

        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while resetting password for user: \"{}\". Details: {}.",
                    processCode, processDescription, personDTO.getUsername(), e.getMessage(), e);
            throw new UnexpectedException("Database error while resetting password.");
        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error resetting password for username: \"{}\". Details: {}.",
                    processCode, processDescription, personDTO.getUsername(), e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while resetting password");
        }
    }

    @Override
    public ResponseEntity<ResponseApi<String>> deleteUser(Long userId) {
        // Process info
        String processCode = "000003";
        String processDescription = "User deletion (soft delete)";

        log.info("[MSG-{}: {} - Starting process] -> Attempting to delete user with ID: {}.",
                processCode, processDescription, userId);

        try {
            // Create a string to replace the username, email and password
            String deletedUserString = "deleted_user_" + userId;

            log.debug("[MSG-{}: {} - Process] -> Generated deletion string: \"{}\" for user ID: {}.",
                    processCode, processDescription, deletedUserString, userId);

            // Attempt to find user, else throw exception
            log.debug("[MSG-{}: {} - Process] -> Searching for user with ID: {}.",
                    processCode, processDescription, userId);

            PersonEntity person = personRepository.findById(userId).orElseThrow(() -> {
                log.warn("[MSG-{}: {} - End of process] -> User not found for deletion, ID: {}.",
                        processCode, processDescription, userId);
                return new UserNotFoundException("User not found");
            });

            String originalUsername = person.getUsername();
            String originalEmail = person.getEmail();

            log.debug("[MSG-{}: {} - Process] -> Found user for deletion - ID: {}, username: \"{}\", email: \"{}\".",
                    processCode, processDescription, userId, originalUsername, originalEmail);

            // Update to delete user parameters
            log.debug("[MSG-{}: {} - Process] -> Applying soft delete to user ID: {}, original username: \"{}\".",
                    processCode, processDescription, userId, originalUsername);

            person.setPreDeleteUsername(person.getUsername());
            person.setUsername(deletedUserString);
            person.setEmail(deletedUserString + "@deleted.com");
            person.setPassword(deletedUserString);
            person.setIsActive(false);

            personRepository.save(person);

            log.info("[MSG-{}: {} - End of process] -> Successfully deleted user ID: {}, original username: \"{}\", original email: \"{}\".",
                    processCode, processDescription, userId, originalUsername, originalEmail);

            return ResponseEntity.ok(new ResponseApi<>(true, "User deleted successfully", "User deleted successfully"));

        } catch (DataAccessException e) {
            log.error("[MSG-{}: {} - End of process] -> Database error while deleting user with ID: {}. Details: {}.",
                    processCode, processDescription, userId, e.getMessage(), e);
            throw new UnexpectedException("Database error while deleting user.");
        } catch (Exception e) {
            log.error("[MSG-{}: {} - End of process] -> Unexpected error deleting user with ID: {}. Details: {}.",
                    processCode, processDescription, userId, e.getMessage(), e);
            throw new UnexpectedException("Unexpected error while deleting user");
        }
    }
}