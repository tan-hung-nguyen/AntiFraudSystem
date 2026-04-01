package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponse;
import com.tanhung.antifraudsystem.exception.RegistrationException;
import com.tanhung.antifraudsystem.mapper.UserMapper;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequest requestUser;
    private User user;
    private UserRegistrationResponse responseUser;

    @BeforeEach
    void setUp(){
        requestUser = new UserRegistrationRequest();
        requestUser.setFirstName("Hung");
        requestUser.setLastName("Nguyen");
        requestUser.setUsername("TanHUng");
        requestUser.setPassword("password");
        requestUser.setEmail("TanHuNg@GmaiL.com");
        requestUser.setPhoneNumber("1234567890");

        user = new User();

        responseUser = new UserRegistrationResponse();
        responseUser.setUsername("tanhung");
        responseUser.setId(1L);
        responseUser.setName("Hung Nguyen");

    }
    @Test
    void shouldSucceedToRegister_whenAllInfoProvidedAndNoConflict(){
        Mockito.when(userRepo.existsByUsername(Mockito.eq("tanhung"))).thenReturn(false);
        Mockito.when(userRepo.existsByEmail(Mockito.eq("tanhung@gmail.com"))).thenReturn(false);
        Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(false);
        Mockito.when(passwordEncoder.encode(requestUser.getPassword())).thenReturn("encodedPassword");
        Mockito.when(userMapper.toEntity(requestUser)).thenReturn(user);

        Mockito.when(userRepo.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(responseUser);

        UserRegistrationResponse actual = authService.register(requestUser);

        assertEquals("encodedPassword", requestUser.getPassword());
        assertEquals("tanhung@gmail.com", requestUser.getEmail());
        assertEquals("Hung Nguyen", actual.getName());
        assertEquals("tanhung", actual.getUsername());
    }

    @Test
    void shouldSucceedToRegister_whenEmailAndPhoneNumberAreNull(){
        requestUser.setPhoneNumber(null);
        requestUser.setEmail(null);

        Mockito.when(passwordEncoder.encode(requestUser.getPassword()))
                .thenReturn("encodedPassword");
        Mockito.when(userMapper.toEntity(requestUser)).thenReturn(user);
        Mockito.when(userRepo.save(user)).thenReturn(user);
        Mockito.when(userMapper.toDto(user)).thenReturn(responseUser);

        UserRegistrationResponse actual = authService.register(requestUser);

        assertEquals("encodedPassword", requestUser.getPassword());
        assertEquals("Hung Nguyen", actual.getName());
        assertEquals("tanhung", actual.getUsername());

        Mockito.verify(userRepo, Mockito.never()).existsByEmail(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).existsByPhoneNumber(Mockito.any());
    }
    @Test
    void shouldThrowRegistrationException_whenEmailIsInUsed(){
        Mockito.when(userRepo.existsByEmail(Mockito.eq("tanhung@gmail.com")))
                .thenReturn(true);

        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> authService.register(requestUser));
        assertEquals("Email is already in used!", ex.getMessage());
        Mockito.verify(userRepo).existsByEmail(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).existsByPhoneNumber(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowRegistrationException_whenUsernameIsInUsed(){
        Mockito.when(userRepo.existsByUsername(Mockito.eq("tanhung"))).thenReturn(true);

        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> authService.register(requestUser));
        assertEquals("Username is already taken!", ex.getMessage());
        Mockito.verify(userRepo).existsByUsername(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).existsByEmail(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowRegistrationException_whenPhoneNumberIsInUsed(){
        Mockito.when(userRepo.existsByPhoneNumber(Mockito.any())).thenReturn(true);

        RegistrationException ex = assertThrows(RegistrationException.class,
                () -> authService.register(requestUser));

        assertEquals("Phone number is already in used!", ex.getMessage());
        Mockito.verify(userRepo).existsByPhoneNumber(Mockito.any());
        Mockito.verify(passwordEncoder, Mockito.never()).encode(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldThrowRegistrationException_whenUserIsNull(){
        assertThrows(RegistrationException.class, () -> authService.register(null));

        Mockito.verify(userRepo, Mockito.never()).existsByUsername(Mockito.any());
        Mockito.verify(userRepo, Mockito.never()).save(Mockito.any());

    }
}