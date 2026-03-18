package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponse;
import com.tanhung.antifraudsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    @Autowired
    public AuthController(AuthService service){
        authService = service;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @RequestBody @Valid UserRegistrationRequest userRegistrationRequest){

        UserRegistrationResponse res = authService.register(userRegistrationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
