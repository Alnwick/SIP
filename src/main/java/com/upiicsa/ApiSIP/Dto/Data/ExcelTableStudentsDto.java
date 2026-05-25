package com.upiicsa.ApiSIP.Dto.Data;

public record ExcelTableStudentsDto(
        String enrollment,
        String fullName,
        String email,
        String phone,
        String career,
        String syllabus,
        String semester,
        String isGraduate,
        String processStatus
) {
}
