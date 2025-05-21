package com.github.zoltar238.PrintStainServer.security;

import com.github.zoltar238.PrintStainServer.persistence.repository.PersonRepository;
import com.github.zoltar238.PrintStainServer.security.filter.JwtAuthenticationFilter;
import com.github.zoltar238.PrintStainServer.security.filter.JwtAuthorizationFilter;
import com.github.zoltar238.PrintStainServer.security.jwt.JwtUtils;
import com.github.zoltar238.PrintStainServer.service.UserDetailsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserDetailsServiceImp userDetailsServiceImp;

    @Autowired
    JwtAuthorizationFilter jwtAuthorizationFilter;

    @Autowired
    PersonRepository personRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {

        final String LOGIN_URL = "/person/login";

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtils, personRepository);
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAuthenticationFilter.setFilterProcessesUrl(LOGIN_URL);

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/healthcheck", "person/register").permitAll()
                        .requestMatchers(
                                "/person/delete",
                                "/item/getAllItems",
                                "/item/postItem",
                                "/item/deleteItems",
                                "/item/download",
                                "/item/upload",
                                "/item/deleteFiles",
                                "/item/updateItem",
                                "sale/newSale",
                                "/sale/deleteSale",
                                "/sale/updateSale").hasRole("ADMIN")
                        .anyRequest().authenticated())
                //.formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .sessionManagement(session -> session
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl(LOGIN_URL)
                        .maximumSessions(1)
                        .expiredUrl(LOGIN_URL)
                        .sessionRegistry(sessionRegistry()))
                .httpBasic(Customizer.withDefaults())
                .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(LogoutConfigurer::permitAll);

        return httpSecurity.build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // create authentication manager
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder
                .userDetailsService(userDetailsServiceImp)
                .passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
