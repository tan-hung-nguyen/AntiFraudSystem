package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.AuthenticationRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequestDto;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.enums.RoleValue;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.RoleRepo;
import com.tanhung.antifraudsystem.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserRegistrationResponseDto register(UserRegistrationRequestDto userRequest) {
        validateUserRegistration(userRequest);
        return save(userRequest);
    }

    private void validateUserRegistration(UserRegistrationRequestDto registrationRequest){
        if(registrationRequest == null){
            throw new RegisterNullException("Object must not be null!", HttpStatus.BAD_REQUEST);
        }
        validateUsername(registrationRequest.getUsername());
        validateEmail(registrationRequest.getEmail());
        validatePhoneNumber(registrationRequest.getPhoneNumber());
    }

    private void validateUsername(String username){
        validateUsernameFormat(username);
        checkUsernameAvailability(username);
    }

    private void validateUsernameFormat(String username){
        if(!isUsernameStartsWithReservedWord(username)){
            throw new UsernameReservedWordException("Username cannot start with reserved word!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isUsernameStartsWithReservedWord(String username){
        return !username.startsWith("admin") &&
                !username.startsWith("root");
    }

    private void checkUsernameAvailability(String username){
        if(isUsernameExist(username)){
            throw new UsernameConflictException("Username is already taken!", HttpStatus.CONFLICT);
        }
    }

    private boolean isUsernameExist(String username){
        return userRepo.existsByUsername(username.toLowerCase());
    }

    private void validateEmail(String email){
        if(email != null){
            checkEmailAvailability(email);
        }
    }

    private void checkEmailAvailability(String email) {
        if(isEmailExist(email)){
            throw new EmailConflictException("Email is already in used!", HttpStatus.CONFLICT);
        }
    }

    private boolean isEmailExist(String email){
        return userRepo.existsByEmail(email.toLowerCase());
    }

    private void validatePhoneNumber(String phoneNumber){
        if(phoneNumber != null){
            checkPhoneNumberAvailability(phoneNumber);
        }
    }

    private void checkPhoneNumberAvailability(String phoneNumber){
        if(isPhoneNumberTaken(phoneNumber)){
            throw new PhoneNumberConflictException("Phone number is already in used!", HttpStatus.CONFLICT);
        }
    }

    private boolean isPhoneNumberTaken(String phoneNumber){
        return userRepo.existsByPhoneNumber(phoneNumber);
    }

    private UserRegistrationResponseDto save(UserRegistrationRequestDto userRequest) {
        User user = prepareUser(userRequest);
        userRepo.save(user);
        return buildRegistrationResponse(user);
    }

    private User prepareUser(UserRegistrationRequestDto userRequest){
        User user = userMapper.toEntity(userRequest);
        boolean firstUser = isFirstUser();
        user.setUsername(normalizeUsername(user.getUsername()));
        user.setEmail(normalizeEmail(user.getEmail()));
        user.setPassword(encodeUserPassword(user.getPassword()));
        user.setRole(determineRole(firstUser));
        user.setActive(firstUser);
        return user;
    }

    private String normalizeUsername(String username){
        return username.toLowerCase();
    }

    private String normalizeEmail(String email){
        return email != null ? email.toLowerCase() : null;
    }

    private String encodeUserPassword(String password){
        return passwordEncoder.encode(password);
    }

    private Role determineRole(boolean firstUser){
        RoleValue role = firstUser ? RoleValue.ADMINISTRATOR : RoleValue.MERCHANT;
        return fetchRoleFromDb(role);
    }


    private boolean isFirstUser(){
        return userRepo.count() == 0;
    }

    private Role fetchRoleFromDb(RoleValue role){
        return roleRepo.findRoleByRoleValue(role.toString());
    }

    private UserRegistrationResponseDto buildRegistrationResponse(User user){
        String jwtToken = jwtService.generateToken(user);
        UserResponseDto userInfo = userMapper.toDto(user);
        return UserRegistrationResponseDto.builder()
                .id(userInfo.getId())
                .name(userInfo.getName())
                .username(userInfo.getUsername())
                .role(userInfo.getRole())
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponseDto authenticate(AuthenticationRequestDto request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().toLowerCase(), request.getPassword()));
        User user = userRepo.findByUsername(request.getUsername().toLowerCase());
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponseDto
                .builder()
                .token(jwtToken)
                .build();
    }
}
