package com.example.This_App_Backend.testController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.This_App_Backend.controller.AuthenticationController;
import com.example.This_App_Backend.dto.AuthenticationDTO.AuthenticationRequest;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.security.CustomUserDetailsService;
import com.example.This_App_Backend.security.JwtRequestFilter;
import com.example.This_App_Backend.security.JwtUtil;
import com.example.This_App_Backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(value = AuthenticationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    private User createSampleUser(Long id, String username, String email, User.UserType userType) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail(email);
        user.setPhoneNumber("1234567890");
        user.setPassword("password");
        user.setUserType(userType);
        user.setProfilePhotoUrl("http://example.com/photo.jpg");
        return user;
    }

    private UserDetails createSpringUserDetails(String username) {
        return new org.springframework.security.core.userdetails.User(
                username,
                "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    private Authentication createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    @Test
    void login_whenCredentialsValid_shouldReturnAuthenticationResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "secret");
        User user = createSampleUser(1L, "testuser", "testuser@example.com", User.UserType.CUSTOMER);
        UserDetails userDetails = createSpringUserDetails("testuser");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(createAuthentication(userDetails));
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("fake-jwt"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.userType").value("CUSTOMER"));
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("baduser", "wrong");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void register_whenUserIsNew_shouldReturnCreatedAuthenticationResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("newuser", "password");
        User newUser = createSampleUser(null, "newuser", "newuser@example.com", User.UserType.CUSTOMER);
        User savedUser = createSampleUser(2L, "newuser", "newuser@example.com", User.UserType.CUSTOMER);
        UserDetails userDetails = createSpringUserDetails("newuser");

        when(userService.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(customUserDetailsService.loadUserByUsername("newuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("created-jwt");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").value("created-jwt"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void registerDriver_whenUserIsNew_shouldReturnCreatedDriverResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("driveruser", "password");
        User newUser = createSampleUser(null, "driveruser", "driveruser@example.com", User.UserType.DRIVER);
        User savedUser = createSampleUser(3L, "driveruser", "driveruser@example.com", User.UserType.DRIVER);
        UserDetails userDetails = createSpringUserDetails("driveruser");

        when(userService.existsByEmail("driveruser@example.com")).thenReturn(false);
        when(userService.existsByUsername("driveruser")).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(customUserDetailsService.loadUserByUsername("driveruser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("driver-jwt");

        mockMvc.perform(post("/api/auth/register/driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").value("driver-jwt"))
                .andExpect(jsonPath("$.username").value("driveruser"))
                .andExpect(jsonPath("$.userType").value("DRIVER"));
    }

    @Test
    void registerAdmin_whenUserIsNew_shouldReturnCreatedAdminResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("adminuser", "password");
        User newUser = createSampleUser(null, "adminuser", "adminuser@example.com", User.UserType.ADMIN);
        User savedUser = createSampleUser(4L, "adminuser", "adminuser@example.com", User.UserType.ADMIN);
        UserDetails userDetails = createSpringUserDetails("adminuser");

        when(userService.existsByEmail("adminuser@example.com")).thenReturn(false);
        when(userService.existsByUsername("adminuser")).thenReturn(false);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);
        when(customUserDetailsService.loadUserByUsername("adminuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("admin-jwt");

        mockMvc.perform(post("/api/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jwt").value("admin-jwt"))
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void register_whenEmailAlreadyExists_shouldReturnConflict() throws Exception {
        User duplicateUser = createSampleUser(null, "existinguser", "existing@example.com", User.UserType.CUSTOMER);

        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void loginDriver_whenUserIsNotDriver_shouldReturnForbidden() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "secret");
        User user = createSampleUser(1L, "testuser", "testuser@example.com", User.UserType.CUSTOMER);
        UserDetails userDetails = createSpringUserDetails("testuser");

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(createAuthentication(userDetails));
        when(customUserDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt");
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login/driver")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied for user type"));
    }
}
