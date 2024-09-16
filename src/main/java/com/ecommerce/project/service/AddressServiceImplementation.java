package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Users;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImplementation implements AddressService{

  @Autowired
  ModelMapper modelMapper;

  @Autowired
  AddressRepository addressRepository;

  @Autowired
  private UserRepository userRepository;

  @Override
  public AddressDTO createAddress(AddressDTO addressDTO, Users user) {
    Address address = modelMapper.map(addressDTO, Address.class);
    List<Address> addresses = user.getAddressesInUsers();
    addresses.add(address);
    user.setAddressesInUsers(addresses);

    address.setUsers(user);
    addressRepository.save(address);

    return modelMapper.map(address, AddressDTO.class);
  }

  @Override
  public List<AddressDTO> getAllAddresses() {
    List<Address> addresses = addressRepository.findAll();

    if(addresses.isEmpty()){
      throw new APIException("No addresses found");
    }

    return addresses.stream().map(address -> modelMapper.map(
      address, AddressDTO.class)).toList();
  }

  @Override
  public AddressDTO getAddressById(Long addressId) {
    Address address = addressRepository.findById(addressId).orElseThrow(
      () -> new APIException("No address found with id: " + addressId)
    );

    return modelMapper.map(address, AddressDTO.class);
  }

  @Override
  public List<AddressDTO> getAddressByUser(Users user) {
    Users savedUser = userRepository.findById(user.getId()).orElseThrow(
      () -> new APIException("No user found with id: " + user.getId())
    );

    List<Address> addresses = savedUser.getAddressesInUsers();

    if(addresses.isEmpty()){
      throw new APIException("No addresses found");
    }

    return addresses.stream().map(
      address -> modelMapper.map(address, AddressDTO.class)
    ).toList();
  }

  @Override
  public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
    Address fetchedAddress = addressRepository.findById(addressId).orElseThrow(
      ()-> new APIException("No address found with id: " + addressId)
    );

    fetchedAddress.setStreet(addressDTO.getStreet());
    fetchedAddress.setBuildingName(addressDTO.getBuildingName());
    fetchedAddress.setCity(addressDTO.getCity());
    fetchedAddress.setState(addressDTO.getState());
    fetchedAddress.setStreet(addressDTO.getStreet());
    fetchedAddress.setCountry(addressDTO.getCountry());
    fetchedAddress.setZipCode(addressDTO.getZipCode());

    Address updatedAddress = addressRepository.save(fetchedAddress);

    Users user = updatedAddress.getUsers();
    user.getAddressesInUsers().removeIf(address -> address.getId().equals(addressId));
    user.getAddressesInUsers().add(updatedAddress);
    userRepository.save(user);

    return modelMapper.map(updatedAddress, AddressDTO.class);
  }

  @Override
  public AddressDTO deleteAddressById(Long addressId) {
    Address address = addressRepository.findById(addressId).orElseThrow(
      ()-> new APIException("Address Not Found with "+addressId)
    );

    Users user = address.getUsers();
    user.getAddressesInUsers().removeIf(addresses -> addresses.getId().equals(addressId));
    userRepository.save(user);

    AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);

    addressRepository.delete(address);

    return addressDTO;
  }
}
