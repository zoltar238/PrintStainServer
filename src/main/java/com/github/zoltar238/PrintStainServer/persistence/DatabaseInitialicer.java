package com.github.zoltar238.PrintStainServer.persistence;

import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEntity;
import com.github.zoltar238.PrintStainServer.persistence.entity.RoleEnum;
import com.github.zoltar238.PrintStainServer.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class DatabaseInitialicer implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        initializeNecessaryRoles();
    }

    private void initializeNecessaryRoles() {
//        Optional<RoleEntity> adminRole = roleRepository.findByName(RoleEnum.ADMIN);
//        Optional<RoleEntity> userRole = roleRepository.findByName(RoleEnum.USER);
        RoleEntity admin = new RoleEntity(1L, RoleEnum.ADMIN, "Admin role");
        RoleEntity user = new RoleEntity(2L, RoleEnum.USER, "User role");
//        if (adminRole.isEmpty()) {
//            roleRepository.save(new RoleEntity(1L, RoleEnum.ADMIN, "Admin role"));

//        }
//        if (userRole.isEmpty()) {
//            roleRepository.save(new RoleEntity(1L, RoleEnum.USER, "User role"));
//        }
        roleRepository.saveAll(List.of(admin, user));
    }
}
