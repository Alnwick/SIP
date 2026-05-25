package com.upiicsa.ApiSIP.Dto.Data;

public record ExcelTableCareerDto(
        String career,
        Integer registered,
        Integer inProcess,
        Integer released
) {
}
