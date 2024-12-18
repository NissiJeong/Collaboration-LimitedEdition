package com.project.collaboration.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    public UserDto(Builder builder) {
        this.userName = builder.userName;
        this.password = builder.password;
        this.email = builder.email;
        this.mobileNumber = builder.mobileNumber;
        this.address = builder.address;
    }

    public static class Builder {
        private String userName;
        private String password;
        private String email;
        private String mobileNumber;;
        private String address;

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public UserDto build() {
            return new UserDto(this);
        }
    }
}
