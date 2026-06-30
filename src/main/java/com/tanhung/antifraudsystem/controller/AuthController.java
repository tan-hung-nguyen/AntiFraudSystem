package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.AuthenticationRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRoleChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequestDto;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponseDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    @Autowired
    public AuthController(AuthService service){
        authService = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestBody @Valid UserRegistrationRequestDto userRegistrationRequestDto){

        Map<String, Object> res = authService.register(userRegistrationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDto> authenticateUser(@RequestBody @Valid AuthenticationRequestDto request){
        AuthenticationResponseDto response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<DeleteStatusResponseDto> deleteUserByUsername(@PathVariable String username){
        DeleteStatusResponseDto deletedUser = authService.deleteUser(username);
        return ResponseEntity.ok(deletedUser);
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@RequestBody @Valid UserRoleChangeRequestDto request){
        UserResponseDto responseDto = authService.changeRole(request);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/access")
    public ResponseEntity<StatusResponseDto> changeUserActiveStatus(@RequestBody @Valid UserAccessChangeRequestDto request){
        StatusResponseDto response = authService.changeUserStatus(request);
        return ResponseEntity.ok(response);
    }

}
