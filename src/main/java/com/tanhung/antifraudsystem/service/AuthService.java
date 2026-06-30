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

    public enum Operation{
        LOCK, UNLOCK
    }
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final RoleRepo roleRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Map<String, Object> register(UserRegistrationRequestDto userRequest) throws RegisterException {
        validateUserRegistration(userRequest);
        return save(userRequest);
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
        if(!isUsernameStartsWithReservedWord(username)){
            throw new UsernameReservedWordException("Username cannot start with reserved word!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isUsernameStartsWithReservedWord(String username){
        return !username.startsWith("admin") &&
                !username.startsWith("root");
    }

    private void checkUsernameAvailability(String username) throws UsernameConflictException{
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

    private void checkEmailAvailability(String email) throws EmailConflictException{
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

    private void checkPhoneNumberAvailability(String phoneNumber) throws PhoneNumberConflictException{
        if(isPhoneNumberTaken(phoneNumber)){
            throw new PhoneNumberConflictException("Phone number is already in used!", HttpStatus.CONFLICT);
        }
    }

    private boolean isPhoneNumberTaken(String phoneNumber){
        return userRepo.existsByPhoneNumber(phoneNumber);
    }

    private Map<String, Object> save(UserRegistrationRequestDto userRequest) {
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
    public DeleteStatusResponseDto deleteUser(String username) throws  UsernameNotFoundException{
        if(!isUsernameExist(username)){
            throw new UsernameNotFoundException("Username not found!");
        }
        return delete(username);
    }

    private DeleteStatusResponseDto delete(String username){
        userRepo.deleteUserByUsername(username.toLowerCase());
        return new DeleteStatusResponseDto(username, "Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(UserRoleChangeRequestDto request){
        checkRequestRoleValue(request.getRole());
        User targetUser = getUserByUsername(request.getUsername());
        processChange(targetUser, request.getRole());
        return userMapper.toDto(targetUser);
    }

    private void checkRequestRoleValue(String role) throws RoleNotAvailableException{
        if(!isValidRole(role)){
            throw new RoleNotAvailableException(role.toUpperCase() + " role is not available!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidRole(String role){
        return role.equalsIgnoreCase(RoleValue.SUPPORT.toString()) ||
                role.equalsIgnoreCase(RoleValue.MERCHANT.toString());
    }

    private User getUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepo.findByUsername(username.toLowerCase());
        if(user == null){
            throw new UsernameNotFoundException("Username not found!");
        }
        return user;
    }

    private void processChange(User targetUser, String newRole){
        validateCurrentUserRole(targetUser, newRole);
        applyNewRole(targetUser, newRole);
    }

    private void validateCurrentUserRole(User targetUser, String newRole) throws RoleConflictException{
        checkIfTargetUserIsAdmin(targetUser);
        if(isRoleAssigned(targetUser, newRole)){
            throw new RoleConflictException(newRole.toUpperCase() + " has already been assigned to " +
                                                    targetUser.getUsername() + "!", HttpStatus.CONFLICT);
        }
    }

    private void checkIfTargetUserIsAdmin(User user) throws RoleChangeException{
        if(isAdmin(user)){
            throw new RoleChangeException("This is admin. You can't change their role!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isAdmin(User user){
        return user.getRole().getRoleValue().equalsIgnoreCase(RoleValue.ADMINISTRATOR.toString());
    }

    private boolean isRoleAssigned(User user, String newRole){
        return user.getRole().getRoleValue().equalsIgnoreCase(newRole);
    }

    private void applyNewRole(User user, String newRole){
        user.getRole().setRoleValue(newRole.toUpperCase());
    }

    @Transactional
    public StatusResponseDto changeUserStatus(UserAccessChangeRequestDto request) throws UserStatusChangeException {
        User user = getUserByUsername(request.getUsername().toLowerCase());
        if (isAdmin(user)) {
            throw new UserStatusChangeException("This is admin. You can't deactivate!", HttpStatus.BAD_REQUEST);
        }
        return makeChangeStatus(user, request.getOperation());
    }

    private StatusResponseDto makeChangeStatus(User user, String operation){
        checkIfOperationIsValid(operation);
        return change(user, operation);
    }

    private void checkIfOperationIsValid(String operation) throws InvalidOperationChangeException{
        if(!isValidOperation(operation)){
            throw new InvalidOperationChangeException("Invalid operation!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidOperation(String operation){
        return operation.equalsIgnoreCase(Operation.LOCK.toString()) ||
                operation.equalsIgnoreCase(Operation.UNLOCK.toString());
    }

    private StatusResponseDto change(User user, String operation) throws UserStatusChangeException{
        validateStatusChange(user, operation);
        return applyStatusChange(user, operation);
    }

    private void validateStatusChange(User user, String operation) throws UserStatusChangeException{
        if(isAlreadyActivated(user, operation)){
            throw new UserStatusChangeException("User " + user.getUsername() +
                    " has already been activated!", HttpStatus.BAD_REQUEST);
        }
        if(isAlreadyDeactivated(user, operation)){
            throw new UserStatusChangeException("User " + user.getUsername() +
                    " has already been deactivated!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isAlreadyActivated(User user, String operation){
        return user.isActive() && operation.equalsIgnoreCase(Operation.UNLOCK.toString());
    }

    private boolean isAlreadyDeactivated(User user, String operation){
        return !user.isActive() && operation.equalsIgnoreCase(Operation.LOCK.toString());
    }

    private StatusResponseDto applyStatusChange(User user, String operation){
        if(isLockOperation(operation)){
            return deactivate(user);
        }
        return activate(user);
    }

    private boolean isLockOperation(String operation){
        return operation.equalsIgnoreCase(Operation.LOCK.toString());
    }

    private StatusResponseDto activate(User user){
        user.setActive(true);
        return new StatusResponseDto("User " + user.getUsername() + " unlocked!");
    }

    private StatusResponseDto deactivate(User user){
        user.setActive(false);
        return new StatusResponseDto("User " + user.getUsername() + " locked!");
    }
}
