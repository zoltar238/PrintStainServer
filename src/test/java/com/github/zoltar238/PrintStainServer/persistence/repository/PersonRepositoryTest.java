package com.github.zoltar238.PrintStainServer.persistence.repository;

import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository underTest;

    @Test
    void findByUsername() {
        // Given
        RoleEntity roleEntity = RoleEntity.builder()
                .name(RoleEnum.ADMIN)
                .description("admin role")
                .build();

        PersonEntity person = PersonEntity.builder()
                .name("test")
                .surname("test")
                .username("test")
                .email("test@test.com")
                .password("test")
                .createDate(new Timestamp(System.currentTimeMillis()))
                .roles(new HashSet<>(Collections.singleton(roleEntity)))
                .build();

        underTest.save(person);
        // When
        underTest.findByUsername("test");
        // Then
        assertNotNull(person);
    }

    @Test
    void findByPreDeleteUsername() {
    }
}
