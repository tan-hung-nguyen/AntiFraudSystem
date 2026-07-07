package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRoleChangeRequestDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.enums.Operation;
import com.tanhung.antifraudsystem.enums.RoleValue;
import com.tanhung.antifraudsystem.exception.InvalidOperationChangeException;
import com.tanhung.antifraudsystem.exception.RoleChangeException;
import com.tanhung.antifraudsystem.exception.RoleConflictException;
import com.tanhung.antifraudsystem.exception.RoleNotAvailableException;
import com.tanhung.antifraudsystem.exception.UserStatusChangeException;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

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

    private boolean isUsernameExist(String username){
        return userRepo.existsByUsername(username.toLowerCase());
    }

    private DeleteStatusResponseDto delete(String username){
        userRepo.deleteUserByUsername(username.toLowerCase());
        return new DeleteStatusResponseDto(username, "Deleted successfully!");
    }

    @Transactional
    public UserResponseDto changeRole(UserRoleChangeRequestDto request){
        checkRequestRoleValue(request.getRole());
        User targetUser = getUserByUsername(request.getUsername());
        proceedChange(targetUser, request.getRole());
        return userMapper.toDto(targetUser);
    }

    private void checkRequestRoleValue(String role){
        if(!isValidRole(role)){
            throw new RoleNotAvailableException(role.toUpperCase() + " role is not available!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidRole(String role){
        return role.equalsIgnoreCase(RoleValue.SUPPORT.toString()) ||
                role.equalsIgnoreCase(RoleValue.MERCHANT.toString());
    }

    private User getUserByUsername(String username){
        User user = userRepo.findByUsername(username.toLowerCase());
        if(user == null){
            throw new UsernameNotFoundException("Username not found!");
        }
        return user;
    }

    private void proceedChange(User targetUser, String newRole){
        validateCurrentUserRole(targetUser, newRole);
        applyNewRole(targetUser, newRole);
    }

    private void validateCurrentUserRole(User targetUser, String newRole){
        checkIfTargetUserIsAdmin(targetUser);
        if(isRoleAssigned(targetUser, newRole)){
            throw new RoleConflictException(newRole.toUpperCase() + " has already been assigned to " +
                                                    targetUser.getUsername() + "!", HttpStatus.CONFLICT);
        }
    }

    private void checkIfTargetUserIsAdmin(User user){
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
    public StatusResponseDto changeUserStatus(UserAccessChangeRequestDto request){
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

    private void checkIfOperationIsValid(String operation){
        if(!isValidOperation(operation)){
            throw new InvalidOperationChangeException("Invalid operation!", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isValidOperation(String operation){
        return operation.equalsIgnoreCase(Operation.LOCK.toString()) ||
                operation.equalsIgnoreCase(Operation.UNLOCK.toString());
    }

    private StatusResponseDto change(User user, String operation){
        validateStatusChange(user, operation);
        return applyStatusChange(user, operation);
    }

    private void validateStatusChange(User user, String operation){
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
