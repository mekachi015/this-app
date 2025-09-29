package com.example.This_App_Backend.controller;

import com.example.This_App_Backend.entity.Stores;
import com.example.This_App_Backend.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("/api/stores")
public class StoreController {
    
    @Autowired
    private StoreService storeService;

    //create store
    @PostMapping("/create")
    public Stores createStore(@RequestBody Stores store,
     @RequestParam Integer adminUserId){
        return storeService.createStore(store, adminUserId);
    }

    //Update store
    @PutMapping("/{storeId}/update")
    public Stores updateStore (@PathVariable String storeId, 
    @RequestBody Stores updatedStores, 
    @RequestParam Integer adminUserId){
        return storeService.updateStore(storeId, updatedStores, adminUserId);
    }

    //delete store
    @DeleteMapping("/{storeId}/delete")
    public String deleteStore(@PathVariable String storeId,
    @RequestParam Integer adminUserId){
        storeService.deletStore(storeId, adminUserId);
        return "Store with ID: "+ storeId + "deleted successfully";
    }

    //Get all stores
    @GetMapping("/all")
    public List<Stores> getAllStores() {
        return storeService.getAllStores();
    }


    @GetMapping("/{storeId}")
    public Stores getStoreById(@PathVariable String storeId) {
        return storeService.getStoreById(storeId)
        .orElseThrow(() -> new RuntimeException("store not found with Id"));
    }
    
    


    

}
