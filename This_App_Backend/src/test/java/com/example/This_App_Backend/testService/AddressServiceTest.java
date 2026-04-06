package com.example.This_App_Backend.testService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.This_App_Backend.dto.AddressDTO.AddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.CreateAddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.UpdateDTO;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import com.example.This_App_Backend.service.AddressService;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
    @Mock
    private UserAddressesRepository addressesRepos;

    @Mock
    private UserRepository userRepos;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private User_Addresses testAddress;
    private CreateAddressDTO createDTO;
    private UpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");

        testAddress = new User_Addresses();
        testAddress.setAddressId(100L);
        testAddress.setUser(testUser);
        testAddress.setStreetNumber("123");
        testAddress.setStreetName("Main St");
        testAddress.setSuburb("Downtown");
        testAddress.setCity("Metropolis");
        testAddress.setProvince("State");
        testAddress.setPostalCode("12345");
        testAddress.setAddressType("SHIPPING");
        testAddress.setDefault(false);
        testAddress.setCreatedAt(LocalDateTime.now());
        testAddress.setUpdatedAt(LocalDateTime.now());

        createDTO = new CreateAddressDTO();
        createDTO.setStreetNumber("456");
        createDTO.setStreetName("Oak Ave");
        createDTO.setSuburb("Uptown");
        createDTO.setCity("Gotham");
        createDTO.setProvince("Gotham Province");
        createDTO.setPostalCode("67890");
        createDTO.setAddressType("BILLING");
        createDTO.setIsDefault(true);

        updateDTO = new UpdateDTO();
        updateDTO.setStreetNumber("789");
        updateDTO.setIsDefault(true);
    }

    // ---------- createAddress ----------
    @Test
    void createAddress_successWithoutDefault() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        createDTO.setIsDefault(false);
        when(addressesRepos.save(any(User_Addresses.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressDTO result = addressService.createAddress(1L, createDTO);

        assertNotNull(result);
        assertEquals("456", result.getStreetNumber());
        assertEquals("BILLING", result.getAddressType());
        assertFalse(result.getIsDefault());
        verify(addressesRepos, never()).findByUser(any());
        verify(addressesRepos, times(1)).save(any());
    }

    @Test
    void createAddress_successWithDefaultClearsOthers() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        User_Addresses existingDefault = new User_Addresses();
        existingDefault.setAddressId(200L);
        existingDefault.setUser(testUser);
        existingDefault.setDefault(true);
        when(addressesRepos.findByUser(testUser)).thenReturn(List.of(existingDefault));

        // Stub save for any User_Addresses (both existing and new)
        when(addressesRepos.save(any(User_Addresses.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressDTO result = addressService.createAddress(1L, createDTO);

        assertTrue(result.getIsDefault());
        assertFalse(existingDefault.isDefault());
        // Expect 2 saves: one for the existing default (unset), one for the new address
        verify(addressesRepos, times(2)).save(any(User_Addresses.class));
    }

    @Test
    void createAddress_userNotFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.createAddress(1L, createDTO));
        assertEquals("User not found", ex.getMessage());
        verify(addressesRepos, never()).save(any());
    }

    // ---------- getUserAddress ----------
    @Test
    void getUserAddress_success() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressesRepos.findByUser(testUser)).thenReturn(List.of(testAddress));

        List<AddressDTO> result = addressService.getUserAddress(1L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getAddressId());
    }

    @Test
    void getUserAddress_userNotFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.getUserAddress(1L));
        assertEquals("user not found", ex.getMessage());
    }

    // ---------- getAddressById ----------
    @Test
    void getAddressById_success() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));

        AddressDTO result = addressService.getAddressById(1L, 100L);

        assertEquals(100L, result.getAddressId());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getAddressById_addressNotFound_throwsException() {
        when(addressesRepos.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.getAddressById(1L, 99L));
        assertEquals("Address not found", ex.getMessage());
    }

    @Test
    void getAddressById_addressBelongsToDifferentUser_throwsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        testAddress.setUser(otherUser);
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.getAddressById(1L, 100L));
        assertEquals("Address does not belong to this user", ex.getMessage());
    }

    // ---------- deleteAddress ----------
    @Test
    void deleteAddress_success() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));
        addressService.deleteAddress(1L, 100L);
        verify(addressesRepos, times(1)).delete(testAddress);
    }

    @Test
    void deleteAddress_addressNotFound_throwsException() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.deleteAddress(1L, 100L));
        assertEquals("address not found", ex.getMessage());
    }

    @Test
    void deleteAddress_addressBelongsToDifferentUser_throwsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        testAddress.setUser(otherUser);
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.deleteAddress(1L, 100L));
        assertEquals("Address does not belong to this user", ex.getMessage());
        verify(addressesRepos, never()).delete(any());
    }

    // ---------- updateAddress ----------
    @Test
    void updateAddress_successWithoutDefault() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));
        updateDTO.setIsDefault(false);
        when(addressesRepos.save(any(User_Addresses.class))).thenReturn(testAddress);

        AddressDTO result = addressService.updateAddress(1L, 100L, updateDTO);

        assertEquals("789", result.getStreetNumber());
        assertFalse(result.getIsDefault());
        verify(addressesRepos, never()).findByUser(any());
        verify(addressesRepos, times(1)).save(testAddress);
    }

    @Test
    void updateAddress_successWithDefaultClearsOthers() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));
        User_Addresses otherAddress = new User_Addresses();
        otherAddress.setAddressId(200L);
        otherAddress.setUser(testUser);
        otherAddress.setDefault(true);
        when(addressesRepos.findByUser(testUser)).thenReturn(List.of(otherAddress));
        when(addressesRepos.save(any(User_Addresses.class))).thenReturn(testAddress);

        AddressDTO result = addressService.updateAddress(1L, 100L, updateDTO);

        assertTrue(result.getIsDefault());
        assertFalse(otherAddress.isDefault());
        verify(addressesRepos, times(1)).save(otherAddress);
        verify(addressesRepos, times(1)).save(testAddress);
    }

    @Test
    void updateAddress_addressNotFound_throwsException() {
        when(addressesRepos.findById(100L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.updateAddress(1L, 100L, updateDTO));
        assertEquals("Address not found", ex.getMessage());
    }

    @Test
    void updateAddress_addressBelongsToDifferentUser_throwsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        testAddress.setUser(otherUser);
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.updateAddress(1L, 100L, updateDTO));
        assertEquals("Address does not belong to this user", ex.getMessage());
        verify(addressesRepos, never()).save(any());
    }

    // ---------- setDefaultAddress ----------
    @Test
    void setDefaultAddress_success() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));

        User_Addresses existingDefault = new User_Addresses();
        existingDefault.setAddressId(200L);
        existingDefault.setUser(testUser);
        existingDefault.setDefault(true);
        when(addressesRepos.findByUser(testUser)).thenReturn(List.of(existingDefault));

        // Stub save for any User_Addresses (both the existing default and the new
        // default)
        when(addressesRepos.save(any(User_Addresses.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressDTO result = addressService.setDefaultAddress(1L, 100L);

        assertTrue(result.getIsDefault());
        assertFalse(existingDefault.isDefault());
        // Expect 2 saves: one for the existing default (unset), one for the new default
        verify(addressesRepos, times(2)).save(any(User_Addresses.class));
    }

    @Test
    void setDefaultAddress_userNotFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.setDefaultAddress(1L, 100L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void setDefaultAddress_addressNotFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressesRepos.findById(100L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.setDefaultAddress(1L, 100L));
        assertEquals("Address not found", ex.getMessage());
    }

    @Test
    void setDefaultAddress_addressBelongsToDifferentUser_throwsException() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        testAddress.setUser(otherUser);
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressesRepos.findById(100L)).thenReturn(Optional.of(testAddress));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.setDefaultAddress(1L, 100L));
        assertEquals("Address does not beling to this user", ex.getMessage());
    }

    // ---------- getDefaultAddress ----------
    @Test
    void getDefaultAddress_success() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        testAddress.setDefault(true);
        when(addressesRepos.findFirstByUserAndIsDefault(testUser, true)).thenReturn(Optional.of(testAddress));

        AddressDTO result = addressService.getDefaultAddress(1L);

        assertEquals(100L, result.getAddressId());
        assertTrue(result.getIsDefault());
    }

    @Test
    void getDefaultAddress_userNotFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.getDefaultAddress(1L));
        assertEquals("user not found", ex.getMessage());
    }

    @Test
    void getDefaultAddress_noDefaultAddressFound_throwsException() {
        when(userRepos.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressesRepos.findFirstByUserAndIsDefault(testUser, true)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> addressService.getDefaultAddress(1L));
        assertEquals("No default address found", ex.getMessage());
    }
}
