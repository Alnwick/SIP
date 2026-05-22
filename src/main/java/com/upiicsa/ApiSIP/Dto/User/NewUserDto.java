package com.upiicsa.ApiSIP.Dto.User;

public record NewUserDto(
        String name,
        String fLastName,
        String mLastName,
        String email,
        String password,
        String confirmPassword
) {
}
