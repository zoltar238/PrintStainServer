package com.github.zoltar238.PrintStainServer.persistence.repository;

import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends CrudRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByName(String name);

    Optional<PersonEntity> findByUsername(String username);
}
