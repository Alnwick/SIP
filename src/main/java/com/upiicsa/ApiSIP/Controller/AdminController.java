package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.User.NewUserDto;
import com.upiicsa.ApiSIP.Service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/registerOperator")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    public ResponseEntity<Void> registerOperator(@RequestBody NewUserDto userDto){

        adminService.createOperator(userDto);
        return ResponseEntity.ok().build();
    }
}
