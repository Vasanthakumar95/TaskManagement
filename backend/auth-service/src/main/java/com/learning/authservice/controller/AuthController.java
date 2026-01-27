package com.learning.authservice.controller;

import com.learning.authservice.model.User;
import com.learning.authservice.payload.request.LoginRequest;
import com.learning.authservice.payload.request.SignupRequest;
import com.learning.authservice.payload.response.JwtResponse;
import com.learning.authservice.repository.UserRepository;
import com.learning.authservice.security.JwtUtils;
import com.learning.authservice.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
        @Autowired
        AuthenticationManager authenticationManager;

        @Autowired
        UserRepository userRepository;

        @Autowired
        PasswordEncoder encoder;

        @Autowired
        JwtUtils jwtUtils;

        @PostMapping("/signin")
        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt,
                                userDetails.getId(),
                                userDetails.getUsername(),
                                userDetails.getEmail(),
                                roles));
        }

        @PostMapping("/signup")
        public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
                if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                        return ResponseEntity
                                        .badRequest()
                                        .body("Error: Username is already taken!");
                }

                if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                        return ResponseEntity
                                        .badRequest()
                                        .body("Error: Email is already in use!");
                }

                User user = new User(signUpRequest.getUsername(),
                                encoder.encode(signUpRequest.getPassword()),
                                signUpRequest.getEmail(),
                                signUpRequest.getRole() == null ? "ROLE_USER" : signUpRequest.getRole());

                userRepository.save(user);

                return ResponseEntity.ok("User registered successfully!");
        }
}
