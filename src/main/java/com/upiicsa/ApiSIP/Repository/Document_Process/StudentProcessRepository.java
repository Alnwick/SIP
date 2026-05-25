package com.upiicsa.ApiSIP.Repository.Document_Process;

import com.upiicsa.ApiSIP.Dto.Data.ExcelTableStudentsDto;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProcessRepository extends JpaRepository<StudentProcess, Integer> {

    Optional<StudentProcess> findByStudentIdAndReasonLeavingIsNull(Integer studentId);

    Optional<StudentProcess> findByStudentEnrollmentAndReasonLeavingIsNull(String enrollment);

    @Query("SELECT new com.upiicsa.ApiSIP.Dto.Data.ExcelTableStudentsDto(" +
            "  s.enrollment, " +
            "  CONCAT(s.fLastName, ' ', s.mLastName, ' ', s.name), " +
            "  s.email, s.phone, " +
            "  o.career.name, o.syllabus.code, s.semester.description, " +
            "  CASE WHEN s.graduate = true THEN 'SÍ' ELSE 'NO' END, " +
            "  sp.processStatus.description) " +
            "FROM StudentProcess sp " +
            "JOIN sp.student s " +
            "JOIN s.semester sem " +
            "JOIN s.offer o " +
            "JOIN sp.processStatus ps " +
            "WHERE sp.startDate BETWEEN :startDate AND :endDate " +
            "AND o.career.acronym IN :careers " +
            "ORDER BY sp.startDate DESC")
    List<ExcelTableStudentsDto> getStudentsReport(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("careers") List<String> careers
    );
}
