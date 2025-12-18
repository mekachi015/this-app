package com.example.This_App_Backend.service;

import com.example.This_App_Backend.dto.AddressDTO.AddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.CreateAddressDTO;
import com.example.This_App_Backend.dto.AddressDTO.UpdateDTO;
import com.example.This_App_Backend.entity.User;
import com.example.This_App_Backend.entity.User_Addresses;
import com.example.This_App_Backend.repository.UserAddressesRepository;
import com.example.This_App_Backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService {

    @Autowired
    private UserAddressesRepository addressesRepos;

    @Autowired
    private UserRepository userRepos;

    @Transactional
    public AddressDTO createAddress(Long userId, CreateAddressDTO dto){
        User user = userRepos.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //check if user customer
        if (dto.getIsDefault() != null && dto.getIsDefault()){
            addressesRepos.findByUser(user).forEach(addr -> {
                addr.setDefault(false);
                addressesRepos.save(addr);
            });
        }

        User_Addresses addresses = new User_Addresses();
        addresses.setUser(user);
        addresses.setAddressLine1(dto.getAddressLine1());
        addresses.setAddressLine2(dto.getAddressLine2());
        addresses.setAddressLine3(dto.getAddressLine3());
        addresses.setCity(dto.getCity());
        addresses.setState(dto.getProvince());
        addresses.setPostalCode(dto.getPostalCode());
        addresses.setAddressType(dto.getAddressType() != null ? dto.getAddressType() : "SHIPPING");
        addresses.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        addresses.setCreatedAt(LocalDateTime.now());
        addresses.setUpdatedAt(LocalDateTime.now());

        User_Addresses saved = addressesRepos.save(addresses);
        return  convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getUserAddress(Long userId){
        User user = userRepos.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        return addressesRepos.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long userId, Long addressId) {
        User_Addresses address = addressesRepos.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to this user");
        }

        return convertToDTO(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId){
        User_Addresses addresses = addressesRepos.findById(addressId)
                .orElseThrow(() -> new RuntimeException("address not found"));


        if (!addresses.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to this user");
        }

        addressesRepos.delete(addresses);
    }

    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, UpdateDTO dto) {
        User_Addresses address = addressesRepos.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to this user");
        }

        // If setting as default, unset other defaults
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            addressesRepos.findByUser(address.getUser()).forEach(addr -> {
                if (!addr.getAddressId().equals(addressId)) {
                    addr.setDefault(false);
                    addressesRepos.save(addr);
                }
            });
        }

        if (dto.getAddressLine1() != null) address.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) address.setAddressLine2(dto.getAddressLine2());
        if (dto.getAddressLine3() != null) address.setAddressLine3(dto.getAddressLine3());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getProvince() != null) address.setState(dto.getProvince());
        if (dto.getPostalCode() != null) address.setPostalCode(dto.getPostalCode());
        if (dto.getAddressType() != null) address.setAddressType(dto.getAddressType());
        if (dto.getIsDefault() != null) address.setDefault(dto.getIsDefault());

        address.setUpdatedAt(LocalDateTime.now());

        User_Addresses updated = addressesRepos.save(address);
        return convertToDTO(updated);
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long userId, Long addressId){
        User user = userRepos.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User_Addresses address = addressesRepos.findById(addressId)
                .orElseThrow(()-> new RuntimeException("Address not found"));

        if(!address.getUser().getUserId().equals(userId)){
            throw new RuntimeException("Address does not beling to this user");
        }

        //Unset all defaults
        addressesRepos.findByUser(user).forEach(
                add -> {
                    add.setDefault(false);
                    addressesRepos.save(add);
                });

        User_Addresses updated = addressesRepos.save(address);
        return convertToDTO(updated);
    }

    @Transactional(readOnly = true)
    public AddressDTO getDefaultAddress (Long userId){
        User user = userRepos.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        User_Addresses addresses = addressesRepos.findFirstByUserAndIsDefault(user, true)
                .orElseThrow(() -> new RuntimeException("No default address found"));

        return convertToDTO(addresses);
    }

    private AddressDTO convertToDTO(User_Addresses addresses){
        AddressDTO dto = new AddressDTO();

        dto.setAddressId(addresses.getAddressId());
        dto.setUserId(addresses.getUser().getUserId());
        dto.setAddressLine1(addresses.getAddressLine1());
        dto.setAddressLine2(addresses.getAddressLine2());
        dto.setAddressLine3(addresses.getAddressLine3());
        dto.setCity(addresses.getCity());
        dto.setProvince(addresses.getState());
        dto.setPostalCode(addresses.getPostalCode());
        dto.setAddressType(addresses.getAddressType());
        dto.setIsDefault(addresses.isDefault());
        dto.setCreateAt(addresses.getCreatedAt());
        dto.setUpdateAt(addresses.getUpdatedAt());
        return dto;


    }

}
