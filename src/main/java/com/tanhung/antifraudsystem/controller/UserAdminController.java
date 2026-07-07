package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRoleChangeRequestDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.service.UserAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserAdminController {
    private final UserAdminService userAdminService;

    @Autowired
    public UserAdminController(UserAdminService service){
        this.userAdminService = service;
    }
    @GetMapping("/list")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(){
        List<UserResponseDto> users = userAdminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<DeleteStatusResponseDto> deleteUserByUsername(@PathVariable
                                                                        @NotNull(message = "Username must not be null")
                                                                        String username){
        DeleteStatusResponseDto deletedUser = userAdminService.deleteUser(username);
        return ResponseEntity.ok(deletedUser);
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@RequestBody @Valid UserRoleChangeRequestDto request){
        UserResponseDto responseDto = userAdminService.changeRole(request);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/access")
    public ResponseEntity<StatusResponseDto> changeUserActiveStatus(@RequestBody @Valid UserAccessChangeRequestDto request){
        StatusResponseDto response = userAdminService.changeUserStatus(request);
        return ResponseEntity.ok(response);
    }
}
