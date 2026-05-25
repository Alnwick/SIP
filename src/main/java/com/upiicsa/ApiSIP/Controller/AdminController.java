package com.upiicsa.ApiSIP.Controller;

import com.upiicsa.ApiSIP.Dto.User.NewUserDto;
import com.upiicsa.ApiSIP.Service.AdminService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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

    @GetMapping("generate-report")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> downloadExcel(@RequestParam String startDate,
                                                @RequestParam String endDate,
                                                @RequestBody List<String> careersInclude) throws IOException {
        byte[] excelBytes = adminService.generateExcel(startDate, endDate, careersInclude);

        String filename = "Reporte SIP(" + startDate + " : " + endDate + ").xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
