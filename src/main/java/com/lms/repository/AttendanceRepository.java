package com.lms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.lms.entity.TrainerMarkedAttendance;
import com.lms.dto.AttendanceHistoryDTO;

@Repository
public interface AttendanceRepository extends JpaRepository<TrainerMarkedAttendance, Integer> {

    // Attendance history with student name and course name
    @Query(value = "SELECT " +
                   "  a.attendance_date AS attendanceDate, " +
                   "  u.name AS studentName, " +
                   "  c.course_name AS courseName, " +
                   "  a.topic AS topic, " +
                   "  a.status AS status " +
                   "FROM trainer_marked_attendance a " +
                   "JOIN users u ON a.student_id = u.id " +
                   "JOIN student_course sc ON u.id = sc.student_id " +
                   "JOIN course_master c ON sc.course_id = c.id " +
                   "WHERE a.batch_id = :batchId " +
                   "AND a.attendance_date BETWEEN :fromDate AND :toDate " +
                   "ORDER BY a.attendance_date DESC", 
           nativeQuery = true)
    List<AttendanceHistoryDTO> findAttendanceHistory(
            @Param("batchId") Integer batchId, 
            @Param("fromDate") LocalDate fromDate, 
            @Param("toDate") LocalDate toDate
    );

    // Existing attendance for marking, including course name
    @Query(value = "SELECT a.id AS id, a.student_id AS studentId, " +
                   "u.name AS studentName, u.email AS studentEmail, " +
                   "c.course_name AS courseName, a.status AS status, a.topic AS topic " +
                   "FROM trainer_marked_attendance a " +
                   "JOIN users u ON a.student_id = u.id " +
                   "JOIN student_course sc ON u.id = sc.student_id " +
                   "JOIN course_master c ON sc.course_id = c.id " +
                   "WHERE a.batch_id = :batchId AND a.attendance_date = :date", 
           nativeQuery = true)
    List<Map<String, Object>> findExistingAttendanceWithDetails(
            @Param("batchId") Integer batchId, 
            @Param("date") LocalDate date
    );

    List<TrainerMarkedAttendance> findByStudentId(Integer studentId);
    
    boolean existsByStudentIdAndBatchIdAndAttendanceDate(Integer studentId, Integer batchId, LocalDate date);
    List<TrainerMarkedAttendance> findByStudentIdAndBatchId(Integer studentId, Integer batchId);
    List<TrainerMarkedAttendance> findByBatchId(Integer batchId);
    List<TrainerMarkedAttendance> findByBatchIdAndAttendanceDate(Integer batchId, LocalDate date);
    List<TrainerMarkedAttendance> findByBatchIdAndAttendanceDateBetween(Integer batchId, LocalDate fromDate, LocalDate toDate);
    
}