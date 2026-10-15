package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createSampleUser(Long id, String username, String email, User.UserType type) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail(email);
        user.setPhoneNumber("0123456789");
        user.setPassword("plainPassword");
        user.setUserType(type);
        return user;
    }

    @Test
    void createUser_whenUsernameAndEmailAreNew_shouldEncodeAndSave() {
        User input = createSampleUser(null, "newuser", "newuser@example.com", User.UserType.CUSTOMER);
        input.setPassword("pass123");
        User saved = createSampleUser(1L, "newuser", "newuser@example.com", User.UserType.CUSTOMER);
        saved.setPassword("encoded-pass");

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
        when(userRepo.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(input);

        assertEquals(1L, result.getUserId());
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded-pass", result.getPassword());
        verify(userRepo).save(any(User.class));
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowRuntimeException() {
        User input = createSampleUser(null, "existing", "new@example.com", User.UserType.CUSTOMER);

        when(userRepo.existsByUsername("existing")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(input));
        assertEquals("Username already exist", exception.getMessage());
    }

    @Test
    void createUser_whenEmailExists_shouldThrowRuntimeException() {
        User input = createSampleUser(null, "newuser", "existing@example.com", User.UserType.CUSTOMER);

        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(input));
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void getAllUsers_shouldReturnListFromRepository() {
        User first = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        User second = createSampleUser(2L, "user2", "user2@example.com", User.UserType.DRIVER);

        when(userRepo.findAll()).thenReturn(List.of(first, second));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    void getUserByEmail_shouldReturnOptionalUser() {
        User user = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByEmail("user1@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("user1@example.com");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void getUserById_shouldReturnOptionalUser() {
        User user = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByUserId(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
    }

    @Test
    void getUserByUsername_shouldReturnOptionalUser() {
        User user = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByUsername("user1")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("user1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void getUserByType_shouldReturnUsersOfThatType() {
        User customer = createSampleUser(1L, "customer1", "customer1@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByUserType(User.UserType.CUSTOMER)).thenReturn(List.of(customer));

        List<User> result = userService.getUserByType(User.UserType.CUSTOMER);

        assertEquals(1, result.size());
        assertEquals(User.UserType.CUSTOMER, result.get(0).getUserType());
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUser() {
        User existing = createSampleUser(1L, "olduser", "old@example.com", User.UserType.CUSTOMER);
        existing.setPassword("oldPass");

        User details = createSampleUser(null, "newuser", "new@example.com", User.UserType.DRIVER);
        details.setPassword("newPass");

        User updated = createSampleUser(1L, "newuser", "new@example.com", User.UserType.DRIVER);
        updated.setPassword("encoded-newPass");

        when(userRepo.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass")).thenReturn("encoded-newPass");
        when(userRepo.save(any(User.class))).thenReturn(updated);

        User result = userService.updateUser(1L, details);

        assertEquals(1L, result.getUserId());
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded-newPass", result.getPassword());
    }

    @Test
    void updateUser_whenNotFound_shouldThrowRuntimeException() {
        User details = createSampleUser(null, "newuser", "new@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByUserId(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(99L, details));
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void deleteUser_whenExists_shouldDeleteUser() {
        User existing = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        when(userRepo.findByUserId(1L)).thenReturn(Optional.of(existing));
        doNothing().when(userRepo).delete(existing);

        userService.deleteUser(1L);

        verify(userRepo).delete(existing);
    }

    @Test
    void deleteUser_whenNotFound_shouldThrowRuntimeException() {
        when(userRepo.findByUserId(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(99L));
        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void userExists_shouldReturnRepositoryValue() {
        when(userRepo.existsById(5L)).thenReturn(true);

        assertTrue(userService.userExists(5L));
    }

    @Test
    void existsByEmail_shouldReturnRepositoryValue() {
        when(userRepo.existsByEmail("email@example.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("email@example.com"));
    }

    @Test
    void existsByUsername_shouldReturnRepositoryValue() {
        when(userRepo.existsByUsername("user1")).thenReturn(false);

        assertFalse(userService.existsByUsername("user1"));
    }

    @Test
    void updateUser_withUserObject_shouldSaveAndReturnUser() {
        User user = createSampleUser(1L, "user1", "user1@example.com", User.UserType.CUSTOMER);
        when(userRepo.save(user)).thenReturn(user);

        User result = userService.updateUser(user);

        assertEquals(user, result);
        verify(userRepo).save(user);
    }
}
