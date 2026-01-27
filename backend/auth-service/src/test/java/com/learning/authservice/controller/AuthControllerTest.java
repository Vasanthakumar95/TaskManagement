package com.learning.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.authservice.model.User;
import com.learning.authservice.payload.request.LoginRequest;
import com.learning.authservice.payload.request.SignupRequest;
import com.learning.authservice.repository.UserRepository;
import com.learning.authservice.security.JwtUtils;
import com.learning.authservice.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder encoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuthenticateUser_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testUser");
        loginRequest.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class); // Mock concrete class

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(userDetails.getId()).thenReturn(1L); // Mock ID
        when(userDetails.getEmail()).thenReturn("test@test.com"); // Mock Email

        when(userRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(new User("testUser", "password", "test@test.com", "ROLE_USER")));
        when(jwtUtils.generateJwtToken(any())).thenReturn("jwtToken");

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newUser");
        signupRequest.setPassword("password");
        signupRequest.setEmail("new@example.com");

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(encoder.encode(any())).thenReturn("encodedPassword");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRegisterUser_UsernameTaken() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existingUser");
        signupRequest.setPassword("password");
        signupRequest.setEmail("new@example.com");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterUser_EmailTaken() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newUser");
        signupRequest.setPassword("password");
        signupRequest.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }
}
