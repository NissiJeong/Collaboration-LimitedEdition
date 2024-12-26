package com.project.orderservice.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDto {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String mobileNumber;;
    @NotBlank
    private String address;

    private String verifyCode;

    @Builder
    public UserDto(String userName, String password, String email, String mobileNumber, String address, String verifyCode) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.verifyCode = verifyCode;
    }
}
