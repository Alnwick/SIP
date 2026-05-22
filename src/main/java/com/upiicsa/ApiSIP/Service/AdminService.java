package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.User.NewUserDto;
import com.upiicsa.ApiSIP.Model.UserSIP;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminService {

    private final UserService userService;
    private final CatalogsService catalogsService;

    public AdminService(UserService userService,  CatalogsService catalogsService) {
        this.userService = userService;
        this.catalogsService = catalogsService;
    }

    public void createOperator(NewUserDto userDto) {

        String pass = userDto.password().equals(userDto.confirmPassword())  ?
                userDto.password() : null;


        UserSIP user = UserSIP.builder()
                .name(userDto.name()).fLastName(userDto.fLastName()).mLastName(userDto.mLastName())
                .email(userDto.email()).password(pass)
                .enabled(true).registrationDate(LocalDateTime.now())
                .userType(catalogsService.getType("OPERADOR"))
                .status(catalogsService.getStatus("ACTIVO"))
                .build();
        userService.createUser(user);
    }
}
