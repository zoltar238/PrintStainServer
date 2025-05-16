package com.github.zoltar238.PrintStainServer.persistence.repository;

import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleEnum name);
}
