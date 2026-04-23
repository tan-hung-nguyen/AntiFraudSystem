package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.AuthenticationRequest;
import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequest;
import com.tanhung.antifraudsystem.dto.request.UserChangeRoleRequest;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponse;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.UserActiveStatusException;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.exception.RoleChangeException;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.RoleRepo;
import com.tanhung.antifraudsystem.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Map<String, Object> register(UserRegistrationRequest userRequest) throws RegistrationException{

        if(userRequest == null) {
            throw new RegistrationException("Object must not be null!", HttpStatus.BAD_REQUEST);
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
        User user = userMapper.toEntity(userRequest);
        user.setPassword(encodedPassword);

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
        String jwtToken = jwtService.generateToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("user_info", userMapper.toDto(user));
        response.put("token", jwtToken);
        return response;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().toLowerCase(), request.getPassword()));
        User user = userRepo.findByUsername(request.getUsername().toLowerCase());
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }
    public List<UserResponseDto> getAllUsers(){
        return userRepo.findAll(Sort.by("id"))
                .stream()
                .map(userMapper::toDto)
                .toList();

    }

    @Transactional
    public DeleteStatusResponse deleteUser(String username) throws UsernameNotFoundException{
        if(username == null) throw new NullPointerException("Username is null");
        if(!userRepo.existsByUsername(username)){
            throw new UsernameNotFoundException("Username not found!");
        }
        userRepo.deleteUserByUsername(username);
        return new DeleteStatusResponse(username, "Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(UserChangeRoleRequest user) throws UsernameNotFoundException, RoleChangeException{
        User userFound = userRepo.findByUsername(user.getUsername().toLowerCase());
        if(userFound == null){
            throw new UsernameNotFoundException("Username not found!");
        }

        if(!user.getRole().equalsIgnoreCase("support") &&
        !user.getRole().equalsIgnoreCase("merchant")){
            throw new RoleChangeException("Only Support or Merchant role are available!", HttpStatus.BAD_REQUEST);
        }

        if(user.getRole().equalsIgnoreCase(userFound.getRole().getRoleValue())){
            throw new RoleChangeException(userFound.getUsername() + " has been provided this role!", HttpStatus.CONFLICT);
        }

        if(userFound.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new RoleChangeException("This is admin! You cannot make change role on admin.", HttpStatus.BAD_REQUEST);
        }
        userFound.getRole().setRoleValue(user.getRole().toUpperCase());
        return userMapper.toDto(userFound);
    }

    @Transactional
    public StatusResponse setUserActiveStatus(UserAccessChangeRequest request) throws UsernameNotFoundException, UserActiveStatusException {
        User found = userRepo.findByUsername(request.getUsername().toLowerCase());
        if(found == null) throw new UsernameNotFoundException("User not found!");
        if(found.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new UserActiveStatusException("You cannot deactivate administrator!", HttpStatus.BAD_REQUEST);
        }

        if(found.isActive() && request.getOperation().equalsIgnoreCase("unlock")){
            throw new UserActiveStatusException("User " + found.getUsername() + " has already been activated!",
                                            HttpStatus.BAD_REQUEST);
        } else if(!found.isActive() && request.getOperation().equalsIgnoreCase("lock")){
            throw new UserActiveStatusException("User " + found.getUsername() + " has already been deactivated!",
                                            HttpStatus.BAD_REQUEST);
        }

        if(request.getOperation().equalsIgnoreCase("unlock")){
            found.setActive(true);
            return new StatusResponse("User " + found.getUsername() + " unlocked!");
        } else if(request.getOperation().equalsIgnoreCase("lock")){
            found.setActive(false);
            return new StatusResponse("User " + found.getUsername() + " locked!");
        } else {
            throw new UserActiveStatusException("Invalid operation!", HttpStatus.BAD_REQUEST);
        }
    }
}
