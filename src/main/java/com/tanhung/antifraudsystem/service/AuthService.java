package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.AuthenticationRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRoleChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequestDto;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponseDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.RoleRepo;
import com.tanhung.antifraudsystem.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    public enum RoleValue{
        ADMINISTRATOR, MERCHANT, SUPPORT
    }
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Map<String, Object> register(UserRegistrationRequestDto userRequest) throws RegisterException {
        validateUserRegistration(userRequest);
        User user = userMapper.toEntity(userRequest);
        return save(user);
    }

    private void validateUserRegistration(UserRegistrationRequestDto registrationRequest) throws RegisterException {
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

    private void validateUsernameFormat(String username) throws UsernameReservedWordException{
        if(!isValidUsername(username)){
            throw new UsernameReservedWordException("Username cannot start with reserved word!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidUsername(String username){
        return !username.startsWith("admin") &&
                !username.startsWith("root");
    }

    private void checkUsernameAvailability(String username) throws UsernameConflictException{
        if(isUsernameExist(username)){
            throw new UsernameConflictException("Username is already taken!", HttpStatus.CONFLICT);
        }
    }

    private boolean isUsernameExist(String username){
        return userRepo.existsByUsername(username);
    }

    private void validateEmail(String email){
        if(email != null){
            checkEmailAvailability(email);
        }
    }

    private void checkEmailAvailability(String email) throws EmailConflictException{
        if(isEmailExist(email)){
            throw new UsernameConflictException("Email is already in used!", HttpStatus.CONFLICT);
        }
    }

    private boolean isEmailExist(String email){
        return userRepo.existsByEmail(email);
    }

    private void validatePhoneNumber(String phoneNumber){
        if(phoneNumber != null){
            checkPhoneNumberAvailability(phoneNumber);
        }
    }

    private void checkPhoneNumberAvailability(String phoneNumber) throws PhoneNumberConflictException{
        if(isPhoneNumberConflict(phoneNumber)){
            throw new PhoneNumberConflictException("Phone number is already in used!", HttpStatus.CONFLICT);
        }
    }

    private boolean isPhoneNumberConflict(String phoneNumber){
        return userRepo.existsByPhoneNumber(phoneNumber);
    }

    private Map<String, Object> save(User user) {
        prepareUser(user);
        userRepo.save(user);
        return buildRegistrationResponse(user);
    }

    private void prepareUser(User user){
        normalizedUserField(user);
        encodeUserPassword(user);
        assignRoleToUser(user);
    }

    private void normalizedUserField(User user){
        user.usernameToLowerCase();
        if(user.getEmail() != null){
            user.emailToLowerCase();
        }
    }

    private void encodeUserPassword(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    private void assignRoleToUser(User user){
        if(isFirstUser()){
            assignAdminRole(user);
        } else {
            assignMerchantRole(user);
        }
    }

    private boolean isFirstUser(){
        return userRepo.count() == 0;
    }

    private void assignAdminRole(User user){
        Role adminRole = roleRepo.findRoleByRoleValue(RoleValue.ADMINISTRATOR.toString());
        user.setRole(adminRole);
        user.setActive(true);
    }

    private void assignMerchantRole(User user){
        Role merchantRole = roleRepo.findRoleByRoleValue(RoleValue.MERCHANT.toString());
        user.setRole(merchantRole);
        user.setActive(false);
    }

    private Map<String, Object> buildRegistrationResponse(User user){
        String jwtToken = jwtService.generateToken(user);
        Map<String, Object> response = new HashMap<>();
        response.put("user_info", userMapper.toDto(user));
        response.put("token", jwtToken);
        return response;
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

    public List<UserResponseDto> getAllUsers(){
        return userRepo.findAll(Sort.by("id"))
                .stream()
                .map(userMapper::toDto)
                .toList();

    }

    @Transactional
    public DeleteStatusResponseDto deleteUser(String username){
        if(!isUsernameExist(username)){
            throw new UsernameNotFoundException("Username not found!");
        }
        return delete(username);
    }

    private DeleteStatusResponseDto delete(String username){
        userRepo.deleteUserByUsername(username);
        return new DeleteStatusResponseDto(username, " Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(UserRoleChangeRequestDto user) throws UsernameNotFoundException, RoleChangeException{
        User userFound = userRepo.findByUsername(user.getUsername().toLowerCase());
        if(userFound == null){
            throw new UsernameNotFoundException("Username not found!");
        }

        if(!user.getRole().equalsIgnoreCase("support") &&
        !user.getRole().equalsIgnoreCase("merchant")){
            throw new RoleNotAvailableException("Only Support or Merchant role are available!", HttpStatus.BAD_REQUEST);
        }

        if(user.getRole().equalsIgnoreCase(userFound.getRole().getRoleValue())){
            throw new RoleConflictException(userFound.getUsername() + " has been provided this role!", HttpStatus.CONFLICT);
        }

        if(userFound.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new RoleChangeException("This is admin! You cannot make change role on admin.", HttpStatus.BAD_REQUEST);
        }
        userFound.getRole().setRoleValue(user.getRole().toUpperCase());
        return userMapper.toDto(userFound);
    }

    @Transactional
    public StatusResponseDto setUserStatus(UserAccessChangeRequestDto request) throws UsernameNotFoundException, UserStatusException {
        User found = userRepo.findByUsername(request.getUsername().toLowerCase());
        if(found == null) throw new UsernameNotFoundException("User not found!");
        if(found.getRole().getRoleValue().equalsIgnoreCase("administrator")){
            throw new UserStatusException("You cannot deactivate administrator!", HttpStatus.BAD_REQUEST);
        }

        if(found.isActive() && request.getOperation().equalsIgnoreCase("unlock")){
            throw new UserStatusException("User " + found.getUsername() + " has already been activated!",
                                            HttpStatus.BAD_REQUEST);
        } else if(!found.isActive() && request.getOperation().equalsIgnoreCase("lock")){
            throw new UserStatusException("User " + found.getUsername() + " has already been deactivated!",
                                            HttpStatus.BAD_REQUEST);
        }

        if(request.getOperation().equalsIgnoreCase("unlock")){
            found.setActive(true);
            return new StatusResponseDto("User " + found.getUsername() + " unlocked!");
        } else if(request.getOperation().equalsIgnoreCase("lock")){
            found.setActive(false);
            return new StatusResponseDto("User " + found.getUsername() + " locked!");
        } else {
            throw new InvalidOperationException("Invalid operation!", HttpStatus.BAD_REQUEST);
        }
    }
}
