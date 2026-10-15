package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.UserController;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "testuser", roles = "ADMIN")  // default auth for all tests
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private FileStorageService fileStorageService;
    
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User createSampleUser(Long id, String username, String email) {
        User user = new User();
        user.setUserId(id);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber("1234567890");
        user.setUserType(User.UserType.CUSTOMER);
        user.setPassword("encodedPass");
        return user;
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        List<User> users = List.of(
                createSampleUser(1L, "user1", "user1@test.com"),
                createSampleUser(2L, "user2", "user2@test.com")
        );
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_whenExists_shouldReturnUser() throws Exception {
        User user = createSampleUser(1L, "existing", "existing@test.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("existing"));
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByType_shouldReturnFilteredList() throws Exception {
        User driver = createSampleUser(3L, "driver1", "driver@test.com");
        driver.setUserType(User.UserType.DRIVER);
        List<User> drivers = List.of(driver);
        when(userService.getUserByType(User.UserType.DRIVER)).thenReturn(drivers);

        mockMvc.perform(get("/api/users/type/DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userType").value("DRIVER"));
    }

    @Test
    void createUser_whenValid_shouldReturnCreated() throws Exception {
        User inputUser = createSampleUser(null, "newuser", "new@test.com");
        inputUser.setPassword("plainPass");
        User savedUser = createSampleUser(10L, "newuser", "new@test.com");

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void createUser_whenDuplicate_shouldReturnBadRequest() throws Exception {
        User inputUser = createSampleUser(null, "duplicate", "dup@test.com");
        when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Username already exist"));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_whenExists_shouldReturnUpdated() throws Exception {
        User updated = createSampleUser(1L, "updatedUser", "updated@test.com");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"));
    }

    @Test
    void updateUser_whenNotFound_shouldReturnNotFound() throws Exception {
        User user = createSampleUser(99L, "ghost", "ghost@test.com");
        when(userService.updateUser(eq(99L), any(User.class))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_whenExists_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_whenNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/api/users/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // --- Profile picture endpoints (require authentication & ownership) ---

    @Test
    @WithMockUser(username = "owner", authorities = "ROLE_USER")
    void getUserProfilePicture_whenOwnProfile_shouldReturnUrl() throws Exception {
        User ownerUser = createSampleUser(5L, "owner", "owner@test.com");
        ownerUser.setProfilePhotoUrl("http://cloudinary.com/photo.jpg");

        when(userService.getUserByUsername("owner")).thenReturn(Optional.of(ownerUser));
        when(userService.getUserById(5L)).thenReturn(Optional.of(ownerUser));

        mockMvc.perform(get("/api/users/5/profile-picture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePhotoUrl").value("http://cloudinary.com/photo.jpg"));
    }

    @Test
    @WithMockUser(username = "otherUser")
    void getUserProfilePicture_whenNotOwnProfile_shouldReturnForbidden() throws Exception {
        User owner = createSampleUser(5L, "owner", "owner@test.com");
        when(userService.getUserByUsername("otherUser")).thenReturn(Optional.of(owner)); // current user is "otherUser", but the user fetched is owner? Actually we need to mock the current user
        // Better: mock current user retrieval
        User currentUser = createSampleUser(10L, "otherUser", "other@test.com");
        when(userService.getUserByUsername("otherUser")).thenReturn(Optional.of(currentUser));
        when(userService.getUserById(5L)).thenReturn(Optional.of(createSampleUser(5L, "owner", "owner@test.com")));

        mockMvc.perform(get("/api/users/5/profile-picture"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserProfilePicture_whenNoPhoto_shouldReturnNotFound() throws Exception {
        User user = createSampleUser(5L, "nophoto", "nophoto@test.com");
        user.setProfilePhotoUrl(null);
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));
        when(userService.getUserById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/5/profile-picture"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void uploadProfilePicture_shouldReturnPhotoUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "profilePhoto",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        User user = createSampleUser(1L, "testuser", "test@test.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.storeProfilePhoto(any(), eq(1L))).thenReturn("http://cloudinary.com/uploaded.jpg");
        when(userService.updateUser(any(User.class))).thenReturn(user);

        mockMvc.perform(multipart(HttpMethod.POST, "/api/users/1/profile-photo")
                        .file(file)
                        .with(csrf())
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("http://cloudinary.com/uploaded.jpg"));
    }

    @Test
    @WithMockUser
    void uploadProfilePicture_whenFileEmpty_shouldReturnBadRequest() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "profilePhoto",
                "",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart(HttpMethod.POST, "/api/users/1/profile-photo")
                        .file(emptyFile)
                        .with(csrf())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deleteProfilePicture_shouldRemoveAndReturnNoContent() throws Exception {
        User user = createSampleUser(1L, "testuser", "test@test.com");
        user.setProfilePhotoUrl("http://cloudinary.com/old.jpg");
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        doNothing().when(fileStorageService).deleteImage(anyString());
        when(userService.updateUser(any(User.class))).thenReturn(user);

        mockMvc.perform(delete("/api/users/1/profile-picture")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(fileStorageService, times(1)).deleteImage("profile_photos/user1");
        verify(userService, times(1)).updateUser(argThat(u -> u.getProfilePhotoUrl() == null));
    }
}
