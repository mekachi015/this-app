package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.dto.StoresDTO.StoreDTO;
import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.service.StoreService;

import jakarta.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;




@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class StoreController {
    
    @Autowired
    private StoreService storeService;

    // //create store
    // @PostMapping("/create")
    // public Stores createStore(@RequestBody Stores store,
    //  @RequestParam Integer adminUserId){
    //     return storeService.createStore(store, adminUserId);
    // }

    // //Update store
    // @PutMapping("/{storeId}/update")
    // public Stores updateStore (@PathVariable String storeId, 
    // @RequestBody Stores updatedStores, 
    // @RequestParam Integer adminUserId){
    //     return storeService.updateStore(storeId, updatedStores, adminUserId);
    // }

    // //delete store
    // @DeleteMapping("/{storeId}/delete")
    // public String deleteStore(@PathVariable String storeId,
    // @RequestParam Integer adminUserId){
    //     storeService.deletStore(storeId, adminUserId);
    //     return "Store with ID: "+ storeId + "deleted successfully";
    // }

    // //Get all stores
    // @GetMapping("/all")
    // public List<Stores> getAllStores() {
    //     return storeService.getAllStores();
    // }


    // @GetMapping("/{storeId}")
    // public Stores getStoreById(@PathVariable String storeId) {
    //     return storeService.getStoreById(storeId)
    //     .orElseThrow(() -> new RuntimeException("store not found with Id"));
    // }
    

    @PostMapping
    public ResponseEntity<Stores> createStore(@RequestBody StoreDTO storeDTO, Principal principal) {
        Stores store = storeService.createStore(storeDTO, principal.getName());
        return ResponseEntity.ok(store);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stores> updateStore(@PathVariable Long id, @RequestBody StoreDTO storeDTO) {
        Stores store = storeService.updateStore(id, storeDTO);
        return ResponseEntity.ok(store);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Stores>> getAllStores() {
        return ResponseEntity.ok(storeService.getAllStores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stores> getStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<String> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String logoUrl = storeService.uploadStoreLogo(id, file);
        return ResponseEntity.ok(logoUrl);
    }

    

}
