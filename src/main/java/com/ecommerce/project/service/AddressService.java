package com.ecommerce.project.service;

import com.ecommerce.project.model.Users;
import com.ecommerce.project.payload.AddressDTO;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressService {
  AddressDTO createAddress(AddressDTO addressDTO, Users user);

  List<AddressDTO> getAllAddresses();

  AddressDTO getAddressById(Long addressId);

  List<AddressDTO> getAddressByUser(Users user);

  AddressDTO updateAddressById(Long addressId, @Valid AddressDTO addressDTO);

  AddressDTO deleteAddressById(Long addressId);
}
