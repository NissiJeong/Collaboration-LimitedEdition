package com.project.userservice.address.service;

import com.project.userservice.address.dto.AddressDto;
import com.project.userservice.address.entity.Address;
import com.project.userservice.address.repository.AddressRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressDto saveAddress(AddressDto addressDto, HttpServletRequest request) {
        // X-Claim-email 헤더 값을 가져오기
        long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        Address savedAddress = addressRepository.save(new Address(addressDto, userId));

        return AddressDto.builder()
                .addressId(savedAddress.getId())
                .zipcode(savedAddress.getZipCode())
                .city(savedAddress.getCity())
                .firstAddress(savedAddress.getFirstAddress())
                .secondAddress(savedAddress.getSecondAddress())
                .defaultAddressYn(savedAddress.getDefaultAddressYn()).build();
    }

    @Transactional
    public AddressDto updateAddress(AddressDto addressDto, HttpServletRequest request) {
        // X-Claim-email 헤더 값을 가져오기
        long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        Address address = addressRepository.findByIdAndUserId(addressDto.getAddressId(), userId).orElseThrow(()->
                new NullPointerException("해당 주소가 존재하지 않습니다.")
        );

        address.updateAddress(addressDto);

        return AddressDto.builder()
                .addressId(address.getId())
                .zipcode(address.getZipCode())
                .city(address.getCity())
                .firstAddress(address.getFirstAddress())
                .secondAddress(address.getSecondAddress())
                .defaultAddressYn(address.getDefaultAddressYn()).build();
    }

    public boolean deleteAddress(Long addressId) {
        if(addressRepository.existsById(addressId)) {
            addressRepository.deleteById(addressId);
        }

        return true;
    }

    public List<AddressDto> getAddressList(HttpServletRequest request) {
        // X-Claim-email 헤더 값을 가져오기
        long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        List<Address> addressList = addressRepository.findAllByUserId(userId);

        return addressList.stream().map(address -> AddressDto.builder()
                .addressId(address.getId())
                .zipcode(address.getZipCode())
                .city(address.getCity())
                .firstAddress(address.getFirstAddress())
                .secondAddress(address.getSecondAddress())
                .defaultAddressYn(address.getDefaultAddressYn()).build()).toList();
    }
}
