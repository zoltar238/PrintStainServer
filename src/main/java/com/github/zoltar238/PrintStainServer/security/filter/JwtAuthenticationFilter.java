package com.github.zoltar238.PrintStainServer.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zoltar238.PrintStainServer.dto.ResponseApi;
import com.github.zoltar238.PrintStainServer.persistence.entity.PersonEntity;
import com.github.zoltar238.PrintStainServer.persistence.repository.PersonRepository;
import com.github.zoltar238.PrintStainServer.security.jwt.JwtUtils;
import com.github.zoltar238.PrintStainServer.utils.ResponseBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final PersonRepository personRepository;

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, PersonRepository personRepository) {
        this.jwtUtils = jwtUtils;
        this.personRepository = personRepository;
    }

    // when user tries to authenticate
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        PersonEntity personEntity;
        PersonEntity personRequest;

        try {
            // request is a JSON -> map entity with username and password from JSON
            personRequest = new ObjectMapper().readValue(request.getInputStream(), PersonEntity.class);
            personEntity = personRepository
                    .findByUsername(personRequest.getUsername())
                    .orElseThrow(() -> new IOException("Nickname not found"));
            personEntity.setPassword(personRequest.getPassword());
        } catch (IOException e) {
            // configure response in case of invalid request, if failed authorization return UNAUTHORIZED
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            try {
                ResponseApi<String> errorResponse = ResponseBuilder.buildResponse(false, "Incorrect login data", "Incorrect login data");
                new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                response.getWriter().flush();

                throw new AuthenticationException("Authentication failed") {
                };
            } catch (IOException ex) {
                throw new RuntimeException("Error writing JSON response", ex);
            }
        }

        //generate an authentication token from name and password
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(personEntity.getUsername(), personEntity.getPassword());
        return getAuthenticationManager().authenticate(authenticationToken);
    }

    //what to do if authentication is correct
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();
        PersonEntity personEntity = personRepository.findByUsername(user.getUsername()).orElseThrow();
        String token = jwtUtils.generateAccessToken(personEntity.getPersonId(), user.getUsername());

        response.addHeader("Authorization", token);

        // Successful authentication response
        ResponseApi<String> responseApi = new ResponseApi<>(true, "User has logged in correctly", token);

        response.getWriter().write(new ObjectMapper().writeValueAsString(responseApi));
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, authResult);
    }
}
