package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserAccessChangeRequest;
import com.tanhung.antifraudsystem.dto.request.UserChangeRoleRequest;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.DeleteStatusResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.mapper.UserMapperImpl;
import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.RoleRepo;
import com.tanhung.antifraudsystem.repo.UserRepo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepo roleRepo;

    private final UserMapper userMapper = new UserMapperImpl();

    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUpAuthService(){
        authService = new AuthService(passwordEncoder, userRepo, userMapper, roleRepo, jwtService, authenticationManager);
    }

    @Nested
    @DisplayName("register() method testing")
    class registerMethodTesting {

        private UserRegistrationRequest requestUser;
        private User userEntity;

        @BeforeEach
        void setUp(){

            requestUser = new UserRegistrationRequest();
            requestUser.setFirstName("TestFN");
            requestUser.setLastName("TestLN");
            requestUser.setUsername("TestUsername");
            requestUser.setPassword("Password123");
            requestUser.setEmail("Test@GmaiL.com");
            requestUser.setPhoneNumber("1234567890");


        }

        @Test
        @DisplayName("Register first user as administration then return UserDto(id, name, username, role) " +
                "when providing valid all 6 fields (firstname, lastname, username, password, email, phone number)")
        void shouldRegisterFirstUserAsAdministrator_whenValidRequestProvided() {

            Mockito.when(userRepo.existsByUsername(Mockito.eq("testusername"))).thenReturn(false);
            Mockito.when(userRepo.existsByEmail(Mockito.eq("test@gmail.com"))).thenReturn(false);
            Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(false);

            Mockito.when(passwordEncoder.encode(requestUser.getPassword())).thenReturn("encodedPassword");


            Mockito.when(userRepo.count()).thenReturn(0L);
            Mockito.when(roleRepo.findRoleByRoleValue("ADMINISTRATOR"))
                    .thenReturn(new Role(1L, "ADMINISTRATOR"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);


            Map<String, Object> actual = authService.register(requestUser);

            Mockito.verify(userRepo).save(captor.capture());

            userEntity = captor.getValue();

            //Check if user entity is saved correctly
            assertEquals("TestFN", userEntity.getFirstName());
            assertEquals("TestLN", userEntity.getLastName());
            assertEquals("testusername", userEntity.getUsername());
            assertEquals("1234567890", userEntity.getPhoneNumber());
            assertEquals("test@gmail.com", userEntity.getEmail());
            assertEquals("encodedPassword", userEntity.getPassword());
            assertEquals("ADMINISTRATOR", userEntity.getRole().getRoleValue());
            assertTrue(userEntity.isActive());


            //Check if user dto return as desired
            UserResponseDto actualDto = (UserResponseDto) actual.get("user_info");
            assertEquals("TestFN TestLN", actualDto.getName());
            assertEquals("testusername", actualDto.getUsername());
            assertEquals("ADMINISTRATOR", actualDto.getRole());

        }

        @Test
        @DisplayName("Register user as merchant role after first user then return userDTO(id, name, username, role) " +
                "when providing valid 4 required infos (firstname, lastname, username, password)")
        void shouldRegisterUserAsMerchant_whenUserNotTheFirstAndEmailAndPhoneNumberAreNull() {
            requestUser.setPhoneNumber(null);
            requestUser.setEmail(null);

            Mockito.when(passwordEncoder.encode(requestUser.getPassword()))
                    .thenReturn("encodedPassword");
            Mockito.when(userRepo.existsByUsername("testusername")).thenReturn(false);
            Mockito.when(userRepo.count()).thenReturn(1L);
            Mockito.when(roleRepo.findRoleByRoleValue("MERCHANT")).thenReturn(new Role(2L,"MERCHANT"));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            Map<String, Object> actual = authService.register(requestUser);

            //Capture user entity object inside the method
            Mockito.verify(userRepo).save(captor.capture());

            //Get user entity object inside the method
            userEntity = captor.getValue();
            //Check saved entity
            assertNull(userEntity.getEmail());
            assertNull(userEntity.getPhoneNumber());
            assertEquals("TestFN", userEntity.getFirstName());
            assertEquals("TestLN", userEntity.getLastName());
            assertEquals("encodedPassword", userEntity.getPassword());
            assertEquals("MERCHANT", userEntity.getRole().getRoleValue());
            assertFalse(userEntity.isActive());

            //Check dto
            UserResponseDto actualDto = (UserResponseDto) actual.get("user_info");
            assertEquals("TestFN TestLN", actualDto.getName());
            assertEquals("testusername", actualDto.getUsername());
            assertEquals("MERCHANT", actualDto.getRole());

            Mockito.verify(userRepo, Mockito.never()).existsByEmail(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).existsByPhoneNumber(Mockito.any());
        }

        @Test
        @DisplayName("Throw UsernameReservedWordException when username start with admin")
        void shouldThrowUsernameReservedWordException_whenUsernameStartWithAdmin(){
            requestUser.setUsername("adminusername");
            RegisterException ex = assertThrows(UsernameReservedWordException.class,
                    () -> authService.register(requestUser));
            assertEquals("Username cannot start with reserved word!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());

            Mockito.verify(userRepo, Mockito.never()).existsByUsername(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw UsernameReservedWordException when username start with root")
        void shouldThrowUsernameReservedWordException_whenUsernameStartWithRoot(){
            requestUser.setUsername("rootusername");
            RegisterException ex = assertThrows(UsernameReservedWordException.class,
                    () -> authService.register(requestUser));
            assertEquals("Username cannot start with reserved word!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());

            Mockito.verify(userRepo, Mockito.never()).existsByUsername(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw RegisterConflictException when user provides email is in used")
        void shouldThrowRegisterConflictException_whenEmailIsInUsed() {
            Mockito.when(userRepo.existsByEmail(Mockito.eq("test@gmail.com")))
                    .thenReturn(true);

            RegisterException ex = assertThrows(RegisterConflictException.class,
                    () -> authService.register(requestUser));
            assertEquals("Email is already in used!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo).existsByEmail(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).existsByPhoneNumber(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw RegisterConflictException when username is taken")
        void shouldThrowRegisterConflictException_whenUsernameIsInTaken() {
            Mockito.when(userRepo.existsByUsername(Mockito.eq("testusername"))).thenReturn(true);

            RegisterException ex = assertThrows(RegisterConflictException.class,
                    () -> authService.register(requestUser));
            assertEquals("Username is already taken!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo).existsByUsername(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).existsByEmail(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw RegisterConflictException when user provides phone number is in used")
        void shouldThrowRegisterConflictException_whenPhoneNumberIsInUsed() {
            Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(true);

            RegisterException ex = assertThrows(RegisterConflictException.class,
                    () -> authService.register(requestUser));

            assertEquals("Phone number is already in used!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo).existsByPhoneNumber(Mockito.any());
            Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw RegisterNullException when user object is null")
        void shouldThrowRegisterNullException_whenUserIsNull() {
            RegisterException ex = assertThrows(RegisterNullException.class, () -> authService.register(null));

            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo, Mockito.never()).existsByUsername(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());

        }
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

            List<UserResponseDto> actual = authService.getAllUsers();

            assertEquals(2, actual.size());
            actual.forEach(dto -> assertInstanceOf(UserResponseDto.class, dto));
        }

        @Test
        @DisplayName("Return empty list if there is no users")
        void shouldReturnEmptyList_whenThereIsNoUsers(){
            Mockito.when(userRepo.findAll(Sort.by("id"))).thenReturn(List.of());

            List<UserResponseDto> actual = authService.getAllUsers();

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

            DeleteStatusResponse actual = authService.deleteUser("test");

            Mockito.verify(userRepo).deleteUserByUsername(Mockito.any());
            assertEquals("test", actual.getUsername());
            assertEquals("Deleted successfully!", actual.getStatus());
        }

        @Test
        @DisplayName("Throw UsernameNotFoundException when username is not existed")
        void shouldThrowUsernameNotFoundException_whenUsernameIsNotExisted() {
            Mockito.when(userRepo.existsByUsername(Mockito.eq("test"))).thenReturn(false);

            UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                    () -> authService.deleteUser("test"));

            assertEquals("Username not found!", ex.getMessage());
            Mockito.verify(userRepo, Mockito.never()).deleteUserByUsername("test");
        }
    }

    @Nested
    @DisplayName("changeRole() method")
    class changeRoleMethodTest{

        private UserChangeRoleRequest userChangeRoleRequest;
        private User userFound;
        private UserResponseDto userDto;

        @BeforeEach
        void setUp(){
            userChangeRoleRequest = new UserChangeRoleRequest("usernameTest", "support");
            userFound = new User(1L,
                    "Firstname",
                    "Lastname",
                    "usernametest",
                    "Test1234",
                    "test@gmail.com",
                    "1234567890",
                    true, new Role(1L, "MERCHANT"));
        }

        @Test
        @DisplayName("Should change role user to \"SUPPORT\" when the current user's role is other than admin")
        void shouldChangeUserRoleSuccessfullyToSupport_whenCurrentUserRoleIsOtherThanAdmin() {
            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userDto = authService.changeRole(userChangeRoleRequest);

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
                    () -> authService.changeRole(userChangeRoleRequest));

            assertEquals("Username not found!", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw RoleNotAvailableException when the request role is not MERCHANT nor SUPPORT")
        void shouldThrowRoleNotAvailableException_whenRequestRoleIsNotMerchantNorSupport(){

            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userChangeRoleRequest = new UserChangeRoleRequest("usernametest", "MODER");

            RoleChangeException ex = assertThrows(RoleNotAvailableException.class,
                    ()-> authService.changeRole(userChangeRoleRequest));
            assertEquals("Only Support or Merchant role are available!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw RoleConflictException when the request role is the same as current role")
        void shouldThrowRoleConflictException_whenRequestRoleSameAsCurrentRole(){

            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userChangeRoleRequest = new UserChangeRoleRequest("usernametest", "MERCHANT");

            RoleChangeException ex = assertThrows(RoleConflictException.class,
                    ()-> authService.changeRole(userChangeRoleRequest));
            assertEquals("usernametest has been provided this role!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());

        }

        @Test
        @DisplayName("Should throw RoleChangeException when making change role on admin")
         void shouldThrowRoleChangeException_whenRequestRoleChangeIsAdministrator() {
            userFound.setRole(new Role(1L, "ADMINISTRATOR"));
            Mockito.when(userRepo.findByUsername("usernametest")).thenReturn(userFound);

            userChangeRoleRequest = new UserChangeRoleRequest("usernametest", "merchant");

            RoleChangeException ex = assertThrows(RoleChangeException.class,
                    () -> authService.changeRole(userChangeRoleRequest));

            assertEquals("This is admin! You cannot make change role on admin.", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

    }

    @Nested
    @DisplayName("setUserActiveStatus() method")
    class setUserActiveStatusMethodTest{

        private UserAccessChangeRequest userAccessChangeRequest;
        private User userFound;



        @BeforeEach
        void setUp(){
            userAccessChangeRequest = new UserAccessChangeRequest("test","unlock");
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

            StatusResponse actual = authService.setUserActiveStatus(userAccessChangeRequest);

            assertEquals("User test unlocked!", actual.getStatus());
            assertTrue(userFound.isActive());
        }

        @Test
        @DisplayName("Should deactivate user successfully when user is unlocked with \"LOCK\" operation")
        void shouldDeactivateUserSuccessfully_whenUserIsUnlockedWithValidOperation() {
            userFound.setActive(true);
            userAccessChangeRequest = new UserAccessChangeRequest("test", "LOCK");
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            StatusResponse actual = authService.setUserActiveStatus(userAccessChangeRequest);

            assertEquals("User test locked!", actual.getStatus());
            assertFalse(userFound.isActive());
        }

        @Test
        @DisplayName("Should throw InvalidOperationException when using other operation other than \"LOCK\" and \"UNLOCK\"")
        void shouldThrowInvalidOperationException_whenUsingInvalidOperation() {

            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);
            userAccessChangeRequest = new UserAccessChangeRequest("test", "OPEN");

            UserStatusException ex = assertThrows(InvalidOperationException.class,
                    () -> authService.setUserActiveStatus(userAccessChangeRequest));
            assertEquals("Invalid operation!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when trying deactivate admin")
        void shouldThrowUserStatusException_whenDeactivatingAdmin(){
            userFound.setRole(new Role(1L, "ADMINISTRATOR"));
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusException ex = assertThrows(UserStatusException.class,
                    () -> authService.setUserActiveStatus(userAccessChangeRequest));

            assertEquals("You cannot deactivate administrator!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when activating an active user")
        void shouldThrowUserStatusException_whenActivatingAnActiveUser(){
            userFound.setActive(true);
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusException ex = assertThrows(UserStatusException.class,
                    () -> authService.setUserActiveStatus(userAccessChangeRequest));

            assertEquals("User test has already been activated!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }

        @Test
        @DisplayName("Should throw UserStatusException when deactivating an inactive user")
        void shouldThrowUserStatusException_whenDeactivatingAnInactiveUser(){

            userAccessChangeRequest = new UserAccessChangeRequest("test", "lock");
            Mockito.when(userRepo.findByUsername("test")).thenReturn(userFound);

            UserStatusException ex = assertThrows(UserStatusException.class,
                    () -> authService.setUserActiveStatus(userAccessChangeRequest));

            assertEquals("User test has already been deactivated!", ex.getMessage());
            assertEquals("Bad Request", ex.getStatus().getReasonPhrase());
        }
    }


}