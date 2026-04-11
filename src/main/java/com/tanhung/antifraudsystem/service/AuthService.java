package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserActivationRequest;
import com.tanhung.antifraudsystem.dto.request.UserChangeRoleRequest;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.ActivationException;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.exception.RoleChangeException;
import com.tanhung.antifraudsystem.exception.StolenCardException;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.RoleRepo;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;

    @Autowired
    public AuthService(PasswordEncoder encoder, UserRepo userRepo,
                       UserMapper userMapper, RoleRepo roleRepo){
        this.passwordEncoder = encoder;
        this.userRepo = userRepo;
        this.userMapper = userMapper;
        this.roleRepo = roleRepo;
    }

    public UserResponseDto register(UserRegistrationRequest userRequest) throws RegistrationException{

        if(userRequest == null) {
            throw new RegistrationException("Object cannot be null!", HttpStatus.BAD_REQUEST);
        }

        userRequest.usernameToLowerCase();
        if (userRequest.getEmail() != null) {
            userRequest.emailToLowerCase();
        }

        if(userRequest.getUsername().startsWith("admin") ||
        userRequest.getUsername().startsWith("root")){
            throw new RegistrationException("Username cannot start with reserved word!", HttpStatus.BAD_REQUEST);
        }
        if (userRepo.existsByUsername(userRequest.getUsername())) {
            throw new RegistrationException("Username is already taken!", HttpStatus.CONFLICT);
        }
        if (userRequest.getEmail() != null &&
                userRepo.existsByEmail(userRequest.getEmail())) {
            throw new RegistrationException("Email is already in used!", HttpStatus.CONFLICT);
        }
        if (userRequest.getPhoneNumber() != null &&
                userRepo.existsByPhoneNumber(userRequest.getPhoneNumber())) {
            throw new RegistrationException("Phone number is already in used!", HttpStatus.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        userRequest.setPassword(encodedPassword);
        User user = userMapper.toEntity(userRequest);

        if(userRepo.count() == 0){
            Role adminRole = roleRepo.findRoleByRoleValue("ADMINISTRATOR");
            user.setRole(adminRole);
            user.setActive(true);
        } else {
            Role merchantRole = roleRepo.findRoleByRoleValue("MERCHANT");
            user.setRole(merchantRole);
            user.setActive(false);
        }
        userRepo.save(user);
        return userMapper.toDto(user);
    }

    public List<UserResponseDto> getAllUsers(){
        return userRepo.findAll(Sort.by("id"))
                .stream()
                .map(userMapper::toDto)
                .toList();

    }

    @Transactional
    public DeleteStatusResponse deleteUser(String username) throws UsernameNotFoundException{
        if(!userRepo.existsByUsername(username)){
            throw new UsernameNotFoundException("Username not found!");
        }
        User deletedUser = userRepo.findByUsername(username);
        userRepo.deleteUserByUsername(username);
        return new DeleteStatusResponse(deletedUser.getUsername(), "Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(UserChangeRoleRequest user) throws UsernameNotFoundException, RoleChangeException{
        User userFound = userRepo.findByUsername(user.username().toLowerCase());
        if(userFound == null){
            throw new UsernameNotFoundException("Username not found!");
        }

        if(!user.role().equalsIgnoreCase("support") &&
        !user.role().equalsIgnoreCase("merchant")){
            throw new RoleChangeException("Only Support or Merchant role are available!", HttpStatus.BAD_REQUEST);
        }

        if(user.role().equalsIgnoreCase(userFound.getRole().getRoleValue())){
            throw new RoleChangeException(userFound.getUsername() + " has been provided this role!", HttpStatus.CONFLICT);
        }

        if(userFound.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new RoleChangeException("You are admin! You cannot make change your own role.", HttpStatus.BAD_REQUEST);
        }
        userFound.getRole().setRoleValue(user.role().toUpperCase());
        return userMapper.toDto(userFound);
    }

    @Transactional
    public StatusResponse activateUser(UserActivationRequest request) throws UsernameNotFoundException, ActivationException{
        User found = userRepo.findByUsername(request.username().toLowerCase());
        if(found == null) throw new UsernameNotFoundException("User not found!");
        if(found.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new ActivationException("You cannot deactivate administrator!", HttpStatus.BAD_REQUEST);
        }

        if(found.isActive() && request.operation().equalsIgnoreCase("unlock")){
            throw new ActivationException("User " + found.getUsername() + " has been already activated!",
                                            HttpStatus.BAD_REQUEST);
        } else if(!found.isActive() && request.operation().equalsIgnoreCase("lock")){
            throw new ActivationException("User " + found.getUsername() + " has been already deactivated!",
                                            HttpStatus.BAD_REQUEST);
        }

        if(request.operation().equalsIgnoreCase("unlock")){
            found.setActive(true);
            return new StatusResponse("User " + found.getUsername() + " unlocked!");
        } else if(request.operation().equalsIgnoreCase("lock")){
            found.setActive(false);
            return new StatusResponse("User " + found.getUsername() + " locked!");
        } else {
            throw new ActivationException("Invalid operation!", HttpStatus.BAD_REQUEST);
        }

    }
}
