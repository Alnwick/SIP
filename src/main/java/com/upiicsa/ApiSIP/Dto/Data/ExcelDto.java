package com.upiicsa.ApiSIP.Dto.Data;

public record ExcelDto(
        String generateDate,
        String startDate,
        String endDate,
        Long totalStudents,
        Long activeProcess,
        Long finishProcess
) {
}
