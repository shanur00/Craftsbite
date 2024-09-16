package com.ecommerce.project.controller;

import com.ecommerce.project.model.Users;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.utils.AuthUtil;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

  @Autowired
  private AddressService addressService;

  @Autowired
  private AuthUtil authUtil;
  @Autowired
  private ModelMapper modelMapper;


  @PostMapping("/addresses")
  public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
    Users user = authUtil.loggedInUser();
    AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);
    return new ResponseEntity<AddressDTO>(savedAddressDTO, HttpStatus.CREATED);
  }

  @GetMapping("/addresses")
  public ResponseEntity<List<AddressDTO>> getAllAddresses() {
    List<AddressDTO> addressDTOS = addressService.getAllAddresses();

    return ResponseEntity.ok(addressDTOS);
  }

  @GetMapping("/addresses/{addressId}")
  public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId) {
    AddressDTO addressDTO = addressService.getAddressById(addressId);

    return ResponseEntity.ok(addressDTO);
  }

  @GetMapping("/addresses/user")
  public ResponseEntity<List<AddressDTO>> getAddressByUser() {
    Users user = authUtil.loggedInUser();
    List<AddressDTO> addressDTOS = addressService.getAddressByUser(user);

    return ResponseEntity.ok(addressDTOS);
  }

  @PutMapping("/addresses/{addressId}")
  public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @Valid @RequestBody AddressDTO addressDTO) {
    AddressDTO savedAddressDTO = addressService.updateAddressById(addressId, addressDTO);
    return ResponseEntity.ok(savedAddressDTO);
  }

  @DeleteMapping("/addresses/{addressId}")
  public ResponseEntity<AddressDTO> deleteAddress(@PathVariable Long addressId){
    AddressDTO addressDTO = addressService.deleteAddressById(addressId);
    return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
  }
}
