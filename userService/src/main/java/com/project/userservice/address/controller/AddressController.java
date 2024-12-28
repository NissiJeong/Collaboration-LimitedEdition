package com.project.userservice.address.controller;

import com.project.userservice.address.service.AddressService;
import com.project.userservice.address.dto.AddressDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<?> registerAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        AddressDto resultDto = addressService.saveAddress(addressDto, request);
        return ResponseEntity.ok(resultDto);
    }

    @PutMapping
    public ResponseEntity<?> updateAddress(@RequestBody AddressDto addressDto, HttpServletRequest request) {
        AddressDto resultDto = addressService.updateAddress(addressDto, request);
        return ResponseEntity.ok(resultDto);
    }

    @DeleteMapping(value = "/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        boolean result = addressService.deleteAddress(addressId);
        return ResponseEntity.ok(result ? "정상적으로 삭제되었습니다." : "삭제 시 오류가 발생했습니다.");
    }

    @GetMapping
    public ResponseEntity<?> getAddressList(HttpServletRequest request) {
        List<AddressDto> addressDtos = addressService.getAddressList(request);
        return ResponseEntity.ok(addressDtos);
    }
}
