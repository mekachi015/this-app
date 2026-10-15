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
import org.springframework.security.test.context.support.WithMockUser;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(value = AuthenticationController.class, excludeAutoConfiguration = {
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

    private User createSampleUser(Long id, String username, String email, String password, User.UserType userType) {
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
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());
    }

    @Test
    void login_whenCredentialsValid_shouldReturnAuthenticationResponse() throws Exception {
        String username = "testuser";
        String email = "testuser@example.com";

        // Ensure the DTO is populated correctly
        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("secret");

        User user = createSampleUser(1L, username, email, "secret", User.UserType.CUSTOMER);
        UserDetails userDetails = createSpringUserDetails(username);

        // Mocks
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(createAuthentication(userDetails));

        when(customUserDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);

        when(jwtUtil.generateToken(any(UserDetails.class)))
                .thenReturn("fake-jwt");

        // USE ANYSTRING() HERE TO PREVENT 404
        when(userService.getUserByUsernameOrEmail(anyString()))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // LOOK AT THE "Body" IN THE CONSOLE OUTPUT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("fake-jwt"))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("baduser", "wrong", null);

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager).authenticate(any(Authentication.class));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email/username or password"));
    }

    @Test
    void register_whenUserIsNew_shouldReturnCreatedAuthenticationResponse() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("newuser", "password", "testpassword");
        User newUser = createSampleUser(null, "newuser", "newuser@example.com", "testpassword", User.UserType.CUSTOMER);
        User savedUser = createSampleUser(2L, "newuser", "newuser@example.com", "testpassword", User.UserType.CUSTOMER);
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
        AuthenticationRequest request = new AuthenticationRequest("driveruser", "password", "testpassword");
        User newUser = createSampleUser(null, "driveruser", "driveruser@example.com", "testpassword",
                User.UserType.DRIVER);
        User savedUser = createSampleUser(3L, "driveruser", "driveruser@example.com", "testpassword",
                User.UserType.DRIVER);
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
        AuthenticationRequest request = new AuthenticationRequest("adminuser", "password", "testpassword");
        User newUser = createSampleUser(null, "adminuser", "adminuser@example.com", "testpassword",
                User.UserType.ADMIN);
        User savedUser = createSampleUser(4L, "adminuser", "adminuser@example.com", "testpassword",
                User.UserType.ADMIN);
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
        User duplicateUser = createSampleUser(null, "existinguser", "existing@example.com", "testpassword",
                User.UserType.CUSTOMER);

        when(userService.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void loginDriver_whenUserIsNotDriver_shouldReturnForbidden() throws Exception {
        String testUsername = "testuser";
        // Ensure constructor matches: (username, email, password)
        AuthenticationRequest request = new AuthenticationRequest(testUsername, "testuser@example.com",
                "secretPassword");

        // Create a CUSTOMER (to trigger 403)
        User user = createSampleUser(1L, testUsername, "testuser@example.com", "secretPassword",
                User.UserType.CUSTOMER);
        UserDetails userDetails = createSpringUserDetails(testUsername);

        // 1. Mock the Auth Manager
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(createAuthentication(userDetails));

        // 2. Mock using the INTERFACE type (UserDetailsService)
        // Make sure your @MockBean at the top of the class is:
        // @MockBean private UserDetailsService userDetailsService;
        when(customUserDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // 3. Mock the JWT Util
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("fake-jwt");

        // 4. THE CRITICAL MOCKS: Use anyString() to bypass potential string mismatches
        // This covers both the /login and /login/driver logic
        when(userService.getUserByUsername(anyString())).thenReturn(Optional.of(user));
        when(userService.getUserByUsernameOrEmail(anyString())).thenReturn(Optional.of(user));

        // 5. Perform the request
        mockMvc.perform(post("/api/auth/login/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // CHECK THE CONSOLE OUTPUT FOR THE BODY!
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied for user type"));
    }
}
