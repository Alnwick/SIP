package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Data.ExcelDto;
import com.upiicsa.ApiSIP.Dto.Data.ExcelTableCareerDto;
import com.upiicsa.ApiSIP.Dto.Data.ExcelTableStudentsDto;
import com.upiicsa.ApiSIP.Dto.User.NewUserDto;
import com.upiicsa.ApiSIP.Model.UserSIP;
import com.upiicsa.ApiSIP.Service.Document.StudentProcessService;
import com.upiicsa.ApiSIP.Service.Infrastructure.GeneratorExcelService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserService userService;
    private final CatalogsService catalogsService;
    private final StudentProcessService processService;
    private final GeneratorExcelService generatorService;

    public AdminService(UserService userService,  CatalogsService catalogsService,
                        StudentProcessService processService, GeneratorExcelService generatorService) {
        this.userService = userService;
        this.catalogsService = catalogsService;
        this.processService = processService;
        this.generatorService = generatorService;
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

    public byte[] generateExcel(String startDate, String endDate, List<String> careersInclude) throws IOException {
        LocalDate localStart = LocalDate.parse(startDate);
        LocalDate localEnd = LocalDate.parse(endDate);

        LocalDateTime start = localStart.atStartOfDay();
        LocalDateTime end = localEnd.atTime(LocalTime.MAX);

        long activeCount;
        long finishCount;

        List<ExcelTableStudentsDto> tableStudents = processService.getStudentReport(start, end, careersInclude);
        List<ExcelTableCareerDto> tableCareers;

        Map<String, List<ExcelTableStudentsDto>> studentsForCareer = tableStudents.stream()
                .collect(Collectors.groupingBy(ExcelTableStudentsDto::career));

        tableCareers =  studentsForCareer.entrySet().stream()
                .map(entry -> {
                    String acronym = entry.getKey();
                    List<ExcelTableStudentsDto> students = entry.getValue();

                    Integer registered = (int) students.stream()
                            .filter(a -> "REGISTRADO".equalsIgnoreCase(a.processStatus()))
                            .count();
                    Integer released = (int) students.stream()
                            .filter(a -> "LIBERADO".equalsIgnoreCase(a.processStatus()))
                            .count();
                    Integer inProcess = (int) students.stream()
                            .filter(a -> !"REGISTRADO".equalsIgnoreCase(a.processStatus())
                                    && !"LIBERADO".equalsIgnoreCase(a.processStatus())
                                    && !"BAJA".equalsIgnoreCase(a.processStatus()))
                            .count();
                    return new ExcelTableCareerDto(acronym, registered, inProcess, released);
                }).toList();

        activeCount = tableStudents.stream()
                .filter(a -> !"REGISTRADO".equalsIgnoreCase(a.processStatus())
                        && !"LIBERADO".equalsIgnoreCase(a.processStatus())
                        && !"BAJA".equalsIgnoreCase(a.processStatus()))
                .count();
        finishCount = tableStudents.stream()
                .filter(a -> "LIBERADO".equalsIgnoreCase(a.processStatus()))
                .count();

        ExcelDto excelDto = new ExcelDto(String.valueOf(LocalDate.now()), startDate, endDate,
                (long) tableStudents.size(), activeCount, finishCount);

        return generatorService.generateReport(excelDto, tableCareers, tableStudents);
    }
}
