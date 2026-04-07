package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.UserActivationRequest;
import com.tanhung.antifraudsystem.dto.request.UserChangeRoleRequest;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    @Autowired
    public AuthController(AuthService service){
        authService = service;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponseDto> register(
            @RequestBody @Valid UserRegistrationRequest userRegistrationRequest){

        UserResponseDto res = authService.register(userRegistrationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/auth/list")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = authService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/auth/user/{username}")
    public ResponseEntity<DeleteStatusResponse> deleteUser(@PathVariable String username){
        DeleteStatusResponse deletedUser = authService.deleteUser(username);

        return ResponseEntity.ok(deletedUser);
    }

    @PutMapping("/auth/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@RequestBody UserChangeRoleRequest user){
        UserResponseDto responseDto = authService.changeRole(user);

        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/auth/access")
    public ResponseEntity<StatusResponse> activateUser(@RequestBody UserActivationRequest request){
        StatusResponse response = authService.activateUser(request);

        return ResponseEntity.ok(response);
    }

}
