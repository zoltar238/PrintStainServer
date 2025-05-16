package com.github.zoltar238.PrintStainServer.persistence.repository;

import com.github.zoltar238.PrintStainServer.persistence.entity.SaleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends CrudRepository<SaleEntity, Long> {
}
