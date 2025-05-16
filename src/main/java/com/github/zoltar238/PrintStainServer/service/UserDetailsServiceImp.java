package com.github.zoltar238.PrintStainServer.service;

import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //get user from database
        PersonEntity personEntity = personRepository
                .findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));

        Collection<? extends GrantedAuthority> authorities =
                personEntity
                        .getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_".concat(String.valueOf(role.getName()))))
                        .collect(Collectors.toSet());


        // create UserDetails object
        return new User(personEntity.getName(),
                personEntity.getPassword(),
                true,
                true,
                true,
                true,
                authorities);

    }
}
