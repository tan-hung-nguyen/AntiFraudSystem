package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRoleChangeRequestDto;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.mapper.UserMapperImpl;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepo userRepo;

    private final UserMapper userMapper = new UserMapperImpl();

    private UserAdminService userAdminService;

    @BeforeEach
    void setUpUserAdminService(){
        userAdminService = new UserAdminService(userRepo, userMapper);
    }

    @Nested
    @DisplayName("getAllUsers() method")
    class getAllUsersMethodTest{

        @Test
        @DisplayName("Return a list of user dto when users existed")
        void shouldReturnListOfUsers_whenUserExisted(){
            User user = new User(1L, "test","test","test",
                    "test","test","test",true,new Role(1L, "test"));
            User user1 = new User(1L, "test","test","test",
                    "test","test","test",true,new Role(1L, "test"));

            Mockito.when(userRepo.findAll(Sort.by("id"))).thenReturn(List.of(user, user1));

            List<UserResponseDto> actual = userAdminService.getAllUsers();

            assertEquals(2, actual.size());
            actual.forEach(dto -> assertInstanceOf(UserResponseDto.class, dto));
        }

        @Test
        @DisplayName("Return empty list if there is no users")
        void shouldReturnEmptyList_whenThereIsNoUsers(){
            Mockito.when(userRepo.findAll(Sort.by("id"))).thenReturn(List.of());

            List<UserResponseDto> actual = userAdminService.getAllUsers();

            assertTrue(actual.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteUser() method")
    class deleteUserMethodTest{
        @Test
        @DisplayName("Delete user successfully when username is found")
        void shouldDeleteUserSuccessfully_whenUsernameIsFound(){
            Mockito.when(userRepo.existsByUsername(Mockito.eq("test"))).thenReturn(true);

            DeleteStatusResponseDto actual = userAdminService.deleteUser("test");

            Mockito.verify(userRepo).deleteUserByUsername(Mockito.any());
            assertEquals("test", actual.getUsername());
            assertEquals("Deleted successfully!", actual.getStatus());
        }

        @Test
        @DisplayName("Throw UsernameNotFoundException when username is not existed")
        void shouldThrowUsernameNotFoundException_whenUsernameIsNotExisted() {
            Mockito.when(userRepo.existsByUsername(Mockito.eq("test"))).thenReturn(false);

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> userAdminService.deleteUser("test"));

            assertEquals("Username not found!", ex.getMessage());
            Mockito.verify(userRepo, Mockito.never()).deleteUserByUsername("test");
        }
    }

    @Nested
    @DisplayName("changeRole() method")
    class changeRoleMethodTest{

        private UserRoleChangeRequestDto userRoleChangeRequestDto;
        private User userFound;
        private UserResponseDto userDto;

        @BeforeEach
        void setUp(){
            userRoleChangeRequestDto = UserRoleChangeRequestDto
                                        .builder()
                                        .username("usernameTest")
                                        .role("support")
                                        .build();
            userFound = User
                        .builder()
                        .id(1L)
                        .firstName("Firstname")
                        .lastName("Lastname")
                        .username("usernametest")
                        .password("Test1234")
                        .email("test@gmail.com")
                        .phoneNumber("1234567890")
                        .isActive(true)
                        .role(new Role(1L, "MERCHANT")).build();
        }

        @Test
        @DisplayName("Should change role user to \"SUPPORT\" when the current user's role is other than admin")
        void shouldChangeUserRoleSuccessfullyToSupport_whenCurrentUserRoleIsOtherThanAdmin() {
            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userDto = userAdminService.changeRole(userRoleChangeRequestDto);

            //Check return user dto
            assertEquals("Firstname Lastname", userDto.getName());
            assertEquals("usernametest", userDto.getUsername());
            assertEquals("SUPPORT", userDto.getRole());
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when the username not found")
        void shouldThrowUsernameNotFoundException_whenUsernameNotFound() {

            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(null);

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> userAdminService.changeRole(userRoleChangeRequestDto));

            assertEquals("Username not found!", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw RoleNotAvailableException when the request role is not MERCHANT nor SUPPORT")
        void shouldThrowRoleNotAvailableException_whenRequestRoleIsNotMerchantNorSupport(){

            userRoleChangeRequestDto = new UserRoleChangeRequestDto("usernametest", "MODER");

            RoleChangeException ex = assertThrows(RoleNotAvailableException.class,
                    ()-> userAdminService.changeRole(userRoleChangeRequestDto));

            assertEquals("MODER role is not available!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw RoleConflictException when the request role is the same as current role")
        void shouldThrowRoleConflictException_whenRequestRoleSameAsCurrentRole(){

            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userRoleChangeRequestDto = new UserRoleChangeRequestDto("usernametest", "MERCHANT");

            RoleChangeException ex = assertThrows(RoleConflictException.class,
                    ()-> userAdminService.changeRole(userRoleChangeRequestDto));
            assertEquals("MERCHANT has already been assigned to usernametest!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());

        }

        @Test
        @DisplayName("Should throw RoleChangeException when making change role on admin")
         void shouldThrowRoleChangeException_whenRequestRoleChangeIsAdministrator() {
            userFound.setRole(new Role(1L, "ADMINISTRATOR"));
            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userRoleChangeRequestDto = new UserRoleChangeRequestDto("usernametest", "merchant");

            RoleChangeException ex = assertThrows(RoleChangeException.class,
                    () -> userAdminService.changeRole(userRoleChangeRequestDto));

            assertEquals("This is admin. You can't change their role!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

    }

    @Nested
    @DisplayName("setUserActiveStatus() method")
    class setUserActiveStatusMethodTest{

        private UserAccessChangeRequestDto userAccessChangeRequestDto;
        private User userFound;



        @BeforeEach
        void setUp(){
            userAccessChangeRequestDto = new UserAccessChangeRequestDto("test","unlock");
            userFound = new User(1L,
                    "Firstname",
                    "Lastname",
                    "test",
                    "Test1234",
                    "test@gmail.com",
                    "1234567890",
                    false, new Role(2L, "MERCHANT"));
        }

        @Test
        @DisplayName("Should activate user successfully when user is locked with \"UNLOCK\" operation")
        void shouldActivateUserSuccessfully_whenUserIsLockedWithValidOperation() {
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            StatusResponseDto actual = userAdminService.changeUserStatus(userAccessChangeRequestDto);

            assertEquals("User test unlocked!", actual.getStatus());
            assertTrue(userFound.isActive());
        }

        @Test
        @DisplayName("Should deactivate user successfully when user is unlocked with \"LOCK\" operation")
        void shouldDeactivateUserSuccessfully_whenUserIsUnlockedWithValidOperation() {
            userFound.setActive(true);
            userAccessChangeRequestDto = new UserAccessChangeRequestDto("test", "LOCK");
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            StatusResponseDto actual = userAdminService.changeUserStatus(userAccessChangeRequestDto);

            assertEquals("User test locked!", actual.getStatus());
            assertFalse(userFound.isActive());
        }

        @Test
        @DisplayName("Should throw InvalidOperationException when using other operation other than \"LOCK\" and \"UNLOCK\"")
        void shouldThrowInvalidOperationException_whenUsingInvalidOperation() {

            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);
            userAccessChangeRequestDto = new UserAccessChangeRequestDto("test", "OPEN");

            UserStatusChangeException ex = assertThrows(InvalidOperationChangeException.class,
                    () -> userAdminService.changeUserStatus(userAccessChangeRequestDto));
            assertEquals("Invalid operation!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when trying deactivate admin")
        void shouldThrowUserStatusException_whenDeactivatingAdmin(){
            userFound.setRole(new Role(1L, "ADMINISTRATOR"));
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusChangeException ex = assertThrows(UserStatusChangeException.class,
                    () -> userAdminService.changeUserStatus(userAccessChangeRequestDto));

            assertEquals("This is admin. You can't deactivate!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when activating an active user")
        void shouldThrowUserStatusException_whenActivatingAnActiveUser(){
            userFound.setActive(true);
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusChangeException ex = assertThrows(UserStatusChangeException.class,
                    () -> userAdminService.changeUserStatus(userAccessChangeRequestDto));

            assertEquals("User test has already been activated!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when deactivating an inactive user")
        void shouldThrowUserStatusException_whenDeactivatingAnInactiveUser(){

            userAccessChangeRequestDto = new UserAccessChangeRequestDto("test", "lock");
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusChangeException ex = assertThrows(UserStatusChangeException.class,
                    () -> userAdminService.changeUserStatus(userAccessChangeRequestDto));

            assertEquals("User test has already been deactivated!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }
    }

}