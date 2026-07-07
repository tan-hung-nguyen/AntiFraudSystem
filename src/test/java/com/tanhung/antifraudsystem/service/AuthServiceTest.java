package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.AuthenticationRequestDto;
import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequestDto;
import com.tanhung.antifraudsystem.dto.response.AuthenticationResponseDto;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponseDto;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

        private UserRegistrationRequestDto requestUser;
        private User userEntity;

        @BeforeEach
        void setUp(){

            requestUser = UserRegistrationRequestDto
                    .builder()
                    .firstName("TestFN")
                    .lastName("TestLN")
                    .username("TestUsername")
                    .password("Password123")
                    .email("Test@GmaiL.com")
                    .phoneNumber("1234567890").build();
        }

        @Test
        @DisplayName("Register first user as administration then return UserRegistrationResponseDto(id, name, username, role, token) " +
                "when providing valid all 6 fields (firstname, lastname, username, password, email, phone number)")
        void shouldRegisterFirstUserAsAdministrator_whenValidRequestProvided() {

            Mockito.when(userRepo.existsByUsername(Mockito.eq("testusername"))).thenReturn(false);
            Mockito.when(userRepo.existsByEmail(Mockito.eq("test@gmail.com"))).thenReturn(false);
            Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(false);

            Mockito.when(passwordEncoder.encode(requestUser.getPassword())).thenReturn("encodedPassword");


            Mockito.when(userRepo.count()).thenReturn(0L);
            Mockito.when(roleRepo.findRoleByRoleValue("ADMINISTRATOR"))
                    .thenReturn(new Role(1L, "ADMINISTRATOR"));
            Mockito.when(jwtService.generateToken(Mockito.any())).thenReturn("jwtToken");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);


            UserRegistrationResponseDto actual = authService.register(requestUser);

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


            //Check if response dto is returned as desired
            assertEquals("TestFN TestLN", actual.getName());
            assertEquals("testusername", actual.getUsername());
            assertEquals("ADMINISTRATOR", actual.getRole());
            assertEquals("jwtToken", actual.getJwtToken());

        }

        @Test
        @DisplayName("Register user as merchant role after first user then return UserRegistrationResponseDto(id, name, username, role, token) " +
                "when providing valid 4 required infos (firstname, lastname, username, password)")
        void shouldRegisterUserAsMerchant_whenUserNotTheFirstAndEmailAndPhoneNumberAreNull() {
            requestUser.setPhoneNumber(null);
            requestUser.setEmail(null);

            Mockito.when(passwordEncoder.encode(requestUser.getPassword()))
                    .thenReturn("encodedPassword");
            Mockito.when(userRepo.existsByUsername("testusername")).thenReturn(false);
            Mockito.when(userRepo.count()).thenReturn(1L);
            Mockito.when(roleRepo.findRoleByRoleValue("MERCHANT")).thenReturn(new Role(2L,"MERCHANT"));
            Mockito.when(jwtService.generateToken(Mockito.any())).thenReturn("jwtToken");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            UserRegistrationResponseDto actual = authService.register(requestUser);

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

            //Check response dto
            assertEquals("TestFN TestLN", actual.getName());
            assertEquals("testusername", actual.getUsername());
            assertEquals("MERCHANT", actual.getRole());
            assertEquals("jwtToken", actual.getJwtToken());

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
        @DisplayName("Throw EmailConflictException when user provides email is in used")
        void shouldThrowEmailConflictException_whenEmailIsInUsed() {
            Mockito.when(userRepo.existsByEmail(Mockito.eq("test@gmail.com")))
                    .thenReturn(true);

            RegisterException ex = assertThrows(EmailConflictException.class,
                    () -> authService.register(requestUser));
            assertEquals("Email is already in used!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo).existsByEmail(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).existsByPhoneNumber(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw UsernameConflictException when username is taken")
        void shouldThrowUsernameConflictException_whenUsernameIsInTaken() {
            Mockito.when(userRepo.existsByUsername(Mockito.eq("testusername"))).thenReturn(true);

            RegisterException ex = assertThrows(UsernameConflictException.class,
                    () -> authService.register(requestUser));
            assertEquals("Username is already taken!", ex.getMessage());
            assertEquals("Conflict", ex.getStatus().getReasonPhrase());
            Mockito.verify(userRepo).existsByUsername(Mockito.any());
            Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
        }

        @Test
        @DisplayName("Throw PhoneNumberConflictException when user provides phone number is in used")
        void shouldThrowPhoneNumberConflictException_whenPhoneNumberIsInUsed() {
            Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(true);

            RegisterException ex = assertThrows(PhoneNumberConflictException.class,
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
    @DisplayName("authenticate() method testing")
    class authenticateMethodTesting {

        private AuthenticationRequestDto loginRequest;
        private User authenticatedUser;

        @BeforeEach
        void setUp(){
            loginRequest = AuthenticationRequestDto.builder()
                    .username("testusername")
                    .password("Password123")
                    .build();

            authenticatedUser = User.builder()
                    .id(1L)
                    .firstName("TestFN")
                    .lastName("TestLN")
                    .username("testusername")
                    .password("encodedPassword")
                    .isActive(true)
                    .role(new Role(1L, "MERCHANT"))
                    .build();
        }

        @Test
        @DisplayName("Should return AuthenticationResponseDto with a jwt token when credentials are valid")
        void shouldReturnAuthenticationResponseWithToken_whenCredentialsAreValid() {
            Mockito.when(userRepo.findByUsername("testusername")).thenReturn(authenticatedUser);
            Mockito.when(jwtService.generateToken(authenticatedUser)).thenReturn("jwtToken");

            AuthenticationResponseDto actual = authService.authenticate(loginRequest);

            assertEquals("jwtToken", actual.getJwtToken());

            ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
            Mockito.verify(authenticationManager).authenticate(authenticationCaptor.capture());
            assertEquals("testusername", authenticationCaptor.getValue().getPrincipal());
            assertEquals("Password123", authenticationCaptor.getValue().getCredentials());

            Mockito.verify(jwtService).generateToken(authenticatedUser);
        }

        @Test
        @DisplayName("Should normalize the username to lowercase for both authentication and user lookup " +
                "when the username has mixed case")
        void shouldNormalizeUsernameToLowercase_whenUsernameHasMixedCase() {
            loginRequest.setUsername("TestUsername");
            Mockito.when(userRepo.findByUsername("testusername")).thenReturn(authenticatedUser);
            Mockito.when(jwtService.generateToken(authenticatedUser)).thenReturn("jwtToken");

            authService.authenticate(loginRequest);

            ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
            Mockito.verify(authenticationManager).authenticate(authenticationCaptor.capture());
            assertEquals("testusername", authenticationCaptor.getValue().getPrincipal());

            Mockito.verify(userRepo).findByUsername("testusername");
        }

        @Test
        @DisplayName("Should throw NullPointerException without authenticating when the request is null")
        void shouldThrowNullPointerException_whenRequestIsNull() {
            assertThrows(NullPointerException.class, () -> authService.authenticate(null));

            Mockito.verifyNoInteractions(authenticationManager, userRepo, jwtService);
        }
    }

}