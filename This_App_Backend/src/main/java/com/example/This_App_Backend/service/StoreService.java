package com.example.This_App_Backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.This_App_Backend.dto.StoresDTO.StoreDTO;
import com.example.This_App_Backend.entity.Store_Owners;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.repository.StoreOwnerRepository;
import com.example.This_App_Backend.repository.StoreRepository;
import com.example.This_App_Backend.repository.UserRepository;

import jakarta.transaction.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StoreService {

    @Autowired
    private StoreRepository storeRepo;

    @Autowired
    private StoreOwnerRepository storeOwnerRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private FileStorageService fileStorageService;


    // // Store Management

    // // Create store (Only admin allowed)
    // public Stores createStore(Stores store, Integer adminUserId) {
    //     User adminUser = userRepo.findById(adminUserId)
    //             .orElseThrow(() -> new RuntimeException("Admin user not found"));

    //     if (adminUser.getUserType() != User.UserType.ADMIN) {
    //         throw new RuntimeException("Only ADMIN users can create stores");
    //     }

    //     if(store.getStore_Owners() != null && store.getStore_Owners().getOwnerId() == null){
    //         Store_Owners owner = storeOwnerRepo.save(store.getStore_Owners());
    //         store.setStore_Owners(owner);
    //     }

    //     return storeRepo.save(store);
    // }

    // public Stores updateStore(String storeId, Stores updateStore, Integer adminUserId){
    //     User adminUser = userRepo.findById(adminUserId)
    //     .orElseThrow(() -> new RuntimeException("Admin User not found"));

    //     if (adminUser.getUserType() != User.UserType.ADMIN){
    //         throw new RuntimeException("Only admin users can update stores");
    //     }

    //     return storeRepo.findById(storeId).map(store -> {
    //         store.setStoreName(updateStore.getStoreName());
    //         store.setStoreDescription(updateStore.getStoreDescription());
    //         store.setStoreAddress(updateStore.getStoreAddress());
    //         store.setStorePhoneNumber(updateStore.getStorePhoneNumber());
    //         store.setStoreEmail(updateStore.getStoreEmail());
    //         store.setStoreBusinessHours(updateStore.getStoreBusinessHours());

    //         // update store manager if needed
    //         if (updateStore.getStore_Owners() != null) {
    //             store.setStore_Owners(updateStore.getStore_Owners());
    //         }

    //         return storeRepo.save(store);
    //     }).orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
    // }

    // public void deletStore (String storeId, Integer adminUserId){
    //     User adminUser = userRepo.findById(adminUserId)
    //     .orElseThrow(() -> new RuntimeException("Admin user not fount"));

    //     if(adminUser.getUserType() != User.UserType.ADMIN){
    //         throw new RuntimeException("Only ADMIN users can delete stores");
    //     }

    //     Stores store = storeRepo.findById(storeId)
    //     .orElseThrow(() -> new RuntimeException("Stores not found with ID: " + storeId));

    //     storeRepo.delete(store);
    // }

    // public List<Stores> getAllStores() {
    //     return storeRepo.findAll();
    // }

    // public Optional<Stores> getStoreById (String storeId){
    //     return storeRepo.findById(storeId);
    // }

    public Stores createStore(StoreDTO storeDTO, String ownerUsername) {
        User user = userRepo.findByUsername(ownerUsername)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Find or create store owner
        Store_Owners storeOwner = storeOwnerRepo.findByUser(user)
            .orElseGet(() -> {
                Store_Owners newOwner = new Store_Owners();
                newOwner.setUser(user);
                return storeOwnerRepo.save(newOwner);
            });

         if (user.getUserType() != User.UserType.ADMIN) {
             throw new RuntimeException("User does not have permission to create stores");
         }

        Stores store = new Stores();
        store.setStoreName(storeDTO.getStoreName());
        store.setStoreDescription(storeDTO.getStoreDescription());
        store.setStoreAddress(storeDTO.getStoreAddress());
        store.setStoreEmail(storeDTO.getStoreEmail());
        store.setStorePhoneNumber(storeDTO.getStorePhoneNumber());
        store.setStoreBusinessHours(storeDTO.getStoreBusinessHours());
        store.setStoreOwner(storeOwner);

        return storeRepo.save(store);
    }

    public Stores updateStore(Long storeId, StoreDTO storeDTO, String ownerUsername) {
        // Find the user
        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the store
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Verify ownership
        if (!store.getStoreOwner().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to update this store");
        }

        // Update store details
        if (storeDTO.getStoreName() != null) {
            store.setStoreName(storeDTO.getStoreName());
        }
        if (storeDTO.getStoreDescription() != null) {
            store.setStoreDescription(storeDTO.getStoreDescription());
        }
        if (storeDTO.getStoreAddress() != null) {
            store.setStoreAddress(storeDTO.getStoreAddress());
        }
        if (storeDTO.getStoreEmail() != null) {
            store.setStoreEmail(storeDTO.getStoreEmail());
        }
        if (storeDTO.getStorePhoneNumber() != null) {
            store.setStorePhoneNumber(storeDTO.getStorePhoneNumber());
        }
        if (storeDTO.getStoreBusinessHours() != null) {
            store.setStoreBusinessHours(storeDTO.getStoreBusinessHours());
        }

        return storeRepo.save(store);
    }

    public void deleteStore(Long storeId, String ownerUsername) {
        // Find the user
        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the store
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Verify ownership
        if (!store.getStoreOwner().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to delete this store");
        }

        storeRepo.delete(store);
    }

    public String uploadStoreLogo(Long storeId, MultipartFile file) throws IOException {
        Stores store = getStoreById(storeId);
        String logoUrl = fileStorageService.storeStoreLogo(file, String.valueOf(storeId));
        store.setStoreLogo(logoUrl);
        storeRepo.save(store);
        return logoUrl;
    }

    public List<Stores> getMyStores(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Store_Owners storeOwner = storeOwnerRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("You don't have any stores yet"));

        // Pass the entire Store_Owners entity, not just the ID
        return storeRepo.findByStoreOwner(storeOwner);
    }

    /**
     * Get all stores for a specific user by their user ID
     */
    public List<Stores> getStoresByUserId(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Store_Owners storeOwner = storeOwnerRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("This user has no stores"));

        return storeRepo.findByStoreOwner(storeOwner);
    }


    /**
     * Get all stores (public - for browsing)
     */
    public List<Stores> getAllStores() {
        return storeRepo.findAll();
    }

    /**
     * Get a specific store by ID (public - for browsing)
     */
    public Stores getStoreById(Long storeId) {
        return storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }

    /**
     * Upload store logo - only if user owns the store
     */
    public String uploadStoreLogo(Long storeId, MultipartFile file, String ownerUsername) throws IOException {
        // Find the user
        User user = userRepo.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the store
        Stores store = storeRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Verify ownership
        if (!store.getStoreOwner().getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You do not have permission to update this store");
        }

        // Upload logo
        String logoUrl = fileStorageService.storeStoreLogo(file, String.valueOf(storeId));
        store.setStoreLogo(logoUrl);
        storeRepo.save(store);

        return logoUrl;
    }
    public StoreDTO convertToDTO(Stores store) {
        StoreDTO dto = new StoreDTO();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setStoreDescription(store.getStoreDescription());
        dto.setStoreAddress(store.getStoreAddress());
        dto.setStoreEmail(store.getStoreEmail());
        dto.setStorePhoneNumber(store.getStorePhoneNumber());
        dto.setStoreBusinessHours(store.getStoreBusinessHours());
        dto.setStoreLogo(store.getStoreLogo());
        dto.setOwnerId(store.getStoreOwner().getOwnerId());
        return dto;
    }
}
