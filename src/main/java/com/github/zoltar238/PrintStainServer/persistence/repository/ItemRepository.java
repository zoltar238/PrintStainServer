package com.github.zoltar238.PrintStainServer.persistence.repository;

import com.github.zoltar238.PrintStainServer.persistence.entity.ItemEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends CrudRepository<ItemEntity, Long> {
    List<ItemEntity> getAllByItemIdIsNotNull();

    @Override
    Optional<ItemEntity> findById(Long aLong);
}
