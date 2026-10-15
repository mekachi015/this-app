package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.example.This_App_Backend.dto.StoresDTO.StoreDTO;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.StoreOwnerRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.FileStorageService;
import com.example.This_App_Backend.service.StoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {

    @Mock
    private StoreRepository storeRepo;

    @Mock
    private StoreOwnerRepository storeOwnerRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private StoreService storeService;

    private User createUser(Long id, User.UserType type, String username) {
        User user = new User();
        user.setUserId(id);
        user.setUsername(username);
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmail(username + "@example.com");
        user.setPhoneNumber("0123456789");
        user.setPassword("password");
        user.setUserType(type);
        return user;
    }

    private StoreDTO createStoreDTO() {
        StoreDTO dto = new StoreDTO();
        dto.setStoreName("Test Store");
        dto.setStoreDescription("A sample store");
        dto.setStoreAddress("123 Test Avenue");
        dto.setStoreEmail("store@example.com");
        dto.setStorePhoneNumber("0123456789");
        dto.setStoreBusinessHours("9am-5pm");
        return dto;
    }

    private Store_Owners createStoreOwner(Long ownerId, User user) {
        Store_Owners owner = new Store_Owners();
        owner.setOwnerId(ownerId);
        owner.setUser(user);
        return owner;
    }

    private Stores createStore(Long storeId, Store_Owners owner) {
        Stores store = new Stores();
        store.setStoreId(storeId);
        store.setStoreName("Existing Store");
        store.setStoreOwner(owner);
        store.setStoreAddress("Existing Address");
        store.setStoreEmail("existing@example.com");
        store.setStorePhoneNumber("0123456789");
        store.setStoreBusinessHours("9am-5pm");
        store.setStoreDescription("Existing description");
        return store;
    }

    @Test
    void createStore_whenAdminAndNoLogo_shouldSaveStore() {
        User admin = createUser(1L, User.UserType.ADMIN, "adminUser");
        StoreDTO dto = createStoreDTO();
        Store_Owners owner = createStoreOwner(10L, admin);
        Stores savedStore = createStore(100L, owner);

        when(userRepo.findByUsername("adminUser")).thenReturn(Optional.of(admin));
        when(storeOwnerRepo.findByUser(admin)).thenReturn(Optional.empty());
        when(storeOwnerRepo.save(any(Store_Owners.class))).thenReturn(owner);
        when(storeRepo.save(any(Stores.class))).thenReturn(savedStore);

        Stores result = storeService.createStore(dto, "adminUser", null);

        assertEquals(100L, result.getStoreId());
        assertEquals("Existing Store", result.getStoreName());
        assertEquals(owner, result.getStoreOwner());
        verify(storeRepo).save(any(Stores.class));
    }

    @Test
    void createStore_whenUserIsNotAdmin_shouldThrowRuntimeException() {
        User user = createUser(2L, User.UserType.CUSTOMER, "customerUser");
        StoreDTO dto = createStoreDTO();

        when(userRepo.findByUsername("customerUser")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storeService.createStore(dto, "customerUser", null));

        assertEquals("User does not have permission to create stores", exception.getMessage());
    }

    @Test
    void updateStore_whenOwnerMatches_shouldReturnUpdatedStore() {
        User ownerUser = createUser(3L, User.UserType.ADMIN, "ownerUser");
        Store_Owners owner = createStoreOwner(20L, ownerUser);
        Stores existing = createStore(200L, owner);
        StoreDTO dto = createStoreDTO();
        dto.setStoreName("Updated Store");
        dto.setStoreDescription("Updated description");

        Stores saved = createStore(200L, owner);
        saved.setStoreName(dto.getStoreName());
        saved.setStoreDescription(dto.getStoreDescription());

        when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
        when(storeRepo.findById(200L)).thenReturn(Optional.of(existing));
        when(storeRepo.save(existing)).thenReturn(saved);

        Stores result = storeService.updateStore(200L, dto, "ownerUser");

        assertEquals("Updated Store", result.getStoreName());
        assertEquals("Updated description", result.getStoreDescription());
        assertEquals(200L, result.getStoreId());
    }

    @Test
    void updateStore_whenOwnerDoesNotMatch_shouldThrowRuntimeException() {
        User ownerUser = createUser(4L, User.UserType.ADMIN, "ownerUser");
        User otherUser = createUser(5L, User.UserType.ADMIN, "otherUser");
        Store_Owners owner = createStoreOwner(30L, otherUser);
        Stores existing = createStore(300L, owner);
        StoreDTO dto = createStoreDTO();

        when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
        when(storeRepo.findById(300L)).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> storeService.updateStore(300L, dto, "ownerUser"));

        assertEquals("You do not have permission to update this store", exception.getMessage());
    }

    @Test
    void deleteStore_whenOwnerMatches_shouldDeleteStore() {
        User ownerUser = createUser(6L, User.UserType.ADMIN, "ownerUser");
        Store_Owners owner = createStoreOwner(40L, ownerUser);
        Stores existing = createStore(400L, owner);

        when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
        when(storeRepo.findById(400L)).thenReturn(Optional.of(existing));
        doNothing().when(storeRepo).delete(existing);

        storeService.deleteStore(400L, "ownerUser");

        verify(storeRepo).delete(existing);
    }

    @Test
    void getMyStores_whenUserHasNoOwner_returnsEmptyList() {
        User user = createUser(7L, User.UserType.ADMIN, "ownerUser");

        when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(user));
        when(storeOwnerRepo.findByUser(user)).thenReturn(Optional.empty());

        List<Stores> result = storeService.getMyStores("ownerUser");

        assertTrue(result.isEmpty());
    }

    @Test
    void getStoresByUserId_whenOwnerExists_returnsStores() {
        User user = createUser(8L, User.UserType.ADMIN, "ownerUser");
        Store_Owners owner = createStoreOwner(50L, user);
        Stores storeA = createStore(500L, owner);
        Stores storeB = createStore(501L, owner);

        when(userRepo.findByUserId(8L)).thenReturn(Optional.of(user));
        when(storeOwnerRepo.findByUser(user)).thenReturn(Optional.of(owner));
        when(storeRepo.findByStoreOwner(owner)).thenReturn(List.of(storeA, storeB));

        List<Stores> result = storeService.getStoresByUserId(8L);

        assertEquals(2, result.size());
        assertEquals(500L, result.get(0).getStoreId());
    }

    @Test
    void uploadStoreLogo_whenOwnerMatches_shouldReturnLogoUrl() throws IOException {
        User ownerUser = createUser(9L, User.UserType.ADMIN, "ownerUser");
        Store_Owners owner = createStoreOwner(60L, ownerUser);
        Stores existing = createStore(600L, owner);
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());

        when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
        when(storeRepo.findById(600L)).thenReturn(Optional.of(existing));
        when(fileStorageService.storeStoreLogo(file, "600")).thenReturn("http://cdn.example.com/logo.png");
        when(storeRepo.save(existing)).thenReturn(existing);

        String url = storeService.uploadStoreLogo(600L, file, "ownerUser");

        assertEquals("http://cdn.example.com/logo.png", url);
        assertEquals("http://cdn.example.com/logo.png", existing.getStoreLogo());
    }

    @Test
    void convertToDTO_shouldMapFieldsCorrectly() {
        User user = createUser(10L, User.UserType.ADMIN, "ownerUser");
        Store_Owners owner = createStoreOwner(70L, user);
        Stores store = createStore(700L, owner);
        store.setStoreName("DTO Store");
        store.setStoreDescription("DTO Description");
        store.setStoreLogo("http://example.com/logo.png");

        StoreDTO dto = storeService.convertToDTO(store);

        assertEquals(700L, dto.getStoreId());
        assertEquals("DTO Store", dto.getStoreName());
        assertEquals("http://example.com/logo.png", dto.getStoreLogo());
        assertEquals(70L, dto.getOwnerId());
    }

    @Test
void createStore_whenUserNotFound_throwsException() {
    when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.createStore(createStoreDTO(), "unknown", null));

    assertEquals("User not found", ex.getMessage());
}

@Test
void updateStore_whenStoreNotFound_throwsException() {
    User ownerUser = createUser(3L, User.UserType.ADMIN, "ownerUser");
    when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
    when(storeRepo.findById(999L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.updateStore(999L, createStoreDTO(), "ownerUser"));

    assertEquals("Store not found", ex.getMessage());
}

@Test
void updateStore_whenUserNotFound_throwsException() {
    when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.updateStore(200L, createStoreDTO(), "unknown"));

    assertEquals("User not found", ex.getMessage());
}

@Test
void deleteStore_whenStoreNotFound_throwsException() {
    User ownerUser = createUser(6L, User.UserType.ADMIN, "ownerUser");
    when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
    when(storeRepo.findById(999L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.deleteStore(999L, "ownerUser"));

    assertEquals("Store not found", ex.getMessage());
}

@Test
void deleteStore_whenUserNotFound_throwsException() {
    when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.deleteStore(400L, "unknown"));

    assertEquals("User not found", ex.getMessage());
}

// @Test
// void deleteStore_whenStoreHasLogo_deletesLogoFromStorage() throws IOException {
//     User ownerUser = createUser(6L, User.UserType.ADMIN, "ownerUser");
//     Store_Owners owner = createStoreOwner(40L, ownerUser);
//     Stores existing = createStore(400L, owner);
//     existing.setStoreLogo("http://cdn/logo.png");

//     when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
//     when(storeRepo.findById(400L)).thenReturn(Optional.of(existing));
//     doNothing().when(fileStorageService).deleteStoreImage(400L);  // ✅ Long, not "400"
//     doNothing().when(storeRepo).delete(existing);

//     storeService.deleteStore(400L, "ownerUser");

//     verify(fileStorageService).deleteStoreImage(400L);  // ✅ Long
// }

@Test
void uploadStoreLogo_whenUserNotOwner_throwsException() {
    User otherUser = createUser(9L, User.UserType.ADMIN, "otherUser");
    User ownerUser = createUser(10L, User.UserType.ADMIN, "ownerUser");
    Store_Owners owner = createStoreOwner(60L, ownerUser);
    Stores existing = createStore(600L, owner);
    MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());

    when(userRepo.findByUsername("otherUser")).thenReturn(Optional.of(otherUser));
    when(storeRepo.findById(600L)).thenReturn(Optional.of(existing));

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.uploadStoreLogo(600L, file, "otherUser"));

    assertEquals("You do not have permission to update this store", ex.getMessage());
}

@Test
void uploadStoreLogo_whenStoreNotFound_throwsException() {
    User ownerUser = createUser(9L, User.UserType.ADMIN, "ownerUser");
    when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
    when(storeRepo.findById(999L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.uploadStoreLogo(999L, mock(MultipartFile.class), "ownerUser"));

    assertEquals("Store not found", ex.getMessage());
}

@Test
void uploadStoreLogo_whenStorageFails_throwsException() throws IOException {
    User ownerUser = createUser(9L, User.UserType.ADMIN, "ownerUser");
    Store_Owners owner = createStoreOwner(60L, ownerUser);
    Stores existing = createStore(600L, owner);
    MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "data".getBytes());

    when(userRepo.findByUsername("ownerUser")).thenReturn(Optional.of(ownerUser));
    when(storeRepo.findById(600L)).thenReturn(Optional.of(existing));
    when(fileStorageService.storeStoreLogo(file, "600")).thenThrow(new IOException("Disk full"));

    IOException ex = assertThrows(IOException.class,
            () -> storeService.uploadStoreLogo(600L, file, "ownerUser"));

    assertEquals("Disk full", ex.getMessage());   // or contains("Disk full")
}

@Test
void getStoresByUserId_whenUserNotFound_throwsException() {
    when(userRepo.findByUserId(999L)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.getStoresByUserId(999L));

    assertEquals("User not found", ex.getMessage());
}

@Test
void getStoresByUserId_whenUserHasNoOwner_throwsException() {
    User user = createUser(8L, User.UserType.ADMIN, "ownerUser");
    when(userRepo.findByUserId(8L)).thenReturn(Optional.of(user));
    when(storeOwnerRepo.findByUser(user)).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> storeService.getStoresByUserId(8L));

    assertEquals("This user has no stores", ex.getMessage());
}
}
