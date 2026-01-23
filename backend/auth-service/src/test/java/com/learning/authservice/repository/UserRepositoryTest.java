package com.learning.authservice.repository;

import com.learning.authservice.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        User user = new User("testUser", "password", "test@example.com", "ROLE_USER");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testUser");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void testExistsByUsername() {
        User user = new User("existingUser", "password", "existing@example.com", "ROLE_USER");
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("existingUser"));
        assertFalse(userRepository.existsByUsername("nonExistingUser"));
    }

    @Test
    void testExistsByEmail() {
        User user = new User("emailUser", "password", "email@example.com", "ROLE_USER");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("email@example.com"));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }
}
