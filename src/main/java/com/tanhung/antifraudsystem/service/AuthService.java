package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponse;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Autowired
    public AuthService(PasswordEncoder encoder, UserRepo repo, UserMapper mapper){
        passwordEncoder = encoder;
        userRepo = repo;
        userMapper = mapper;
    }

    public UserRegistrationResponse register(UserRegistrationRequest user){
        if(userRepo.existsByUsername(user.getUsername())){
            throw new RegistrationException("Username is already taken!");
        }
        if(user.getEmail() != null && userRepo.existsByEmail(user.getEmail())){
            throw new RegistrationException("Email is already been used!");
        }
        if(user.getPhoneNumber() != null && userRepo.existsByPhoneNumber(user.getPhoneNumber())){
            throw new RegistrationException("Phone number is already been used!");
        }


        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);
        User user1 = userMapper.toEntity(user);

        userRepo.save(user1);

        return userMapper.toDto(user1);
    }
}
