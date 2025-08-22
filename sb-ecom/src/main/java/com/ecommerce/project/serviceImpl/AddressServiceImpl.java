package com.ecommerce.project.serviceImpl;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.services.AddressService;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        // Get List of Addresses
        List<Address> addressList = user.getAddresses();
        // save current address in the old list
        addressList.add(address);
        // set address in userList
        user.setAddresses(addressList);

        // add/set user to/against the address
        address.setUser(user);

        // save address in db
        Address savedAddress = addressRepository.save(address);
        // we have to return DTO so use modelMapper
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddresses() {
        List<Address> addresses = addressRepository.findAll();
        if(addresses.isEmpty()){
            throw new APIException("No address exists");
        }

        return addresses.stream()
                .map(address -> {
                    return modelMapper.map(address, AddressDTO.class);
                }).collect(Collectors.toList());
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address"
                       ,"addressId",addressId));

        return modelMapper.map(address, AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = user.getAddresses();
        return addresses.stream()
                .map(address -> {
                    return modelMapper.map(address, AddressDTO.class);
                }).collect(Collectors.toList());

    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "address", addressId));

        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setState(addressFromDatabase.getState());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setStreet(addressDTO.getStreet());
        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());

        Address updatedAddress = addressRepository.save(addressFromDatabase);

        User user = addressFromDatabase.getUser();
        // Remove old address
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        // update with new address
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address"
                        ,"addressId",addressId));
        User user = addressFromDatabase.getUser();
        // Remove old address
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));

        addressRepository.delete(addressFromDatabase);
        return "Address deleted successfully with addressId:" + addressId;

    }


}
