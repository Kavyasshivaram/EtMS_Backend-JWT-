package com.lms.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lms.dto.CourseFullDetailsDTO;
import com.lms.dto.DashboardResponse;
import com.lms.entity.*;
import com.lms.enums.Status;
import com.lms.repository.*;
import com.lms.service.AdminService;

import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private CourseRepository         courseRepository;
    @Autowired private UserRepository           userRepository;
    @Autowired private BatchRepository          batchRepository;
    @Autowired private StudentBatchesRepository studentBatchesRepository;
    @Autowired private StudentCourseRepository  studentCourseRepository;

    // ================= DASHBOARD =================
    @Override
    public DashboardResponse getDashboardData() {
        long totalCourses  = courseRepository.count();
        long totalTrainers = userRepository.countByRole_RoleNameAndStatus("TRAINER", Status.ACTIVE);
        long totalStudents = userRepository.countByRole_RoleNameAndStatus("STUDENT", Status.ACTIVE);
        long activeBatches = batchRepository.countByStatus("ONGOING");
        return new DashboardResponse(totalCourses, totalTrainers, totalStudents, activeBatches, Collections.emptyList());
    }

    // ================= COURSE =================
    @Override
    public void createCourse(CourseMaster course) { courseRepository.save(course); }

    @Override
    public List<CourseMaster> getAllCourses() { return courseRepository.findAll(); }

    @Override
    public void updateCourse(Long id, CourseMaster updatedCourse) {
        CourseMaster course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setCourseName(updatedCourse.getCourseName());
        course.setDuration(updatedCourse.getDuration());
        course.setDescription(updatedCourse.getDescription());
        courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long id) {
        CourseMaster course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        course.setStatus("INACTIVE");
        courseRepository.save(course);
    }

    @Override
    public List<CourseFullDetailsDTO> getAllCoursesWithDetails() {
        return courseRepository.findAll().stream().map(course ->
                new CourseFullDetailsDTO(course.getId(), course.getCourseName(),
                        course.getDuration(), course.getDescription(), Collections.emptyList())
        ).toList();
    }

    // ================= STUDENT → COURSE MAPPING =================
    @Override
    public List<Map<String, Object>> getStudentCourseMappings() {
        return studentCourseRepository.findAll().stream().map(sc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("mappingId",    sc.getId());
            map.put("studentId",    sc.getStudent().getId());
            map.put("studentName",  sc.getStudent().getName());
            map.put("studentEmail", sc.getStudent().getEmail());
            map.put("phone",        sc.getStudent().getPhone());

            // ── KEY FIX: return the MAPPING's own status, not the student account status ──
            // sc.getStatus() is StudentCourse.Status enum (ACTIVE / INACTIVE)
            map.put("status", sc.getStatus() != null
                    ? sc.getStatus().name()
                    : "ACTIVE");

            map.put("courseId",     sc.getCourse().getId());
            map.put("courseName",   sc.getCourse().getCourseName());
            map.put("courseStatus", sc.getCourse().getStatus());
            return map;
        }).toList();
    }

    // ================= STUDENT → BATCH MAPPING =================
    @Override
    public List<Map<String, Object>> getStudentBatchMappings() {
        return studentBatchesRepository.findAll().stream().map(sb -> {
            Map<String, Object> map = new HashMap<>();
            map.put("mappingId",    sb.getId());
            map.put("studentId",    sb.getStudent().getId());
            map.put("studentName",  sb.getStudent().getName());
            map.put("studentEmail", sb.getStudent().getEmail());
            map.put("phone",        sb.getStudent().getPhone());

            // ── KEY FIX: return the MAPPING's own status, not the student account status ──
            // sb.getStatus() is StudentBatches.Status enum (ACTIVE / INACTIVE / REMOVED / UPDATED)
            map.put("status", sb.getStatus() != null
                    ? sb.getStatus().name()
                    : "ACTIVE");

            map.put("batchId",     sb.getBatch().getId());
            map.put("batchName",   sb.getBatch().getBatchName());
            map.put("batchStatus", sb.getBatch().getStatus());
            return map;
        }).toList();
    }

    // ================= MAP STUDENT → COURSE =================
    @Override
    public void mapStudentToCourse(Long studentId, Long courseId) {
        Optional<StudentCourse> existing =
                studentCourseRepository.findByStudent_IdAndCourse_Id(studentId, courseId);

        // If a mapping exists but was previously deactivated → reactivate it (no duplicate row)
        if (existing.isPresent()) {
            StudentCourse sc = existing.get();
            if (sc.getStatus() == StudentCourse.Status.INACTIVE) {
                sc.setStatus(StudentCourse.Status.ACTIVE);
                studentCourseRepository.save(sc);
                return;
            }
            throw new RuntimeException("Student is already enrolled in this course.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        CourseMaster course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StudentCourse mapping = new StudentCourse();
        mapping.setStudent(student);
        mapping.setCourse(course);
        mapping.setStatus(StudentCourse.Status.ACTIVE);
        studentCourseRepository.save(mapping);
    }

    // ================= MAP STUDENT → BATCH =================
    @Override
    public void mapStudentToBatch(Long studentId, Long batchId) {
        Optional<StudentBatches> existing =
                studentBatchesRepository.findByStudent_IdAndBatch_Id(studentId, batchId);

        // If a mapping exists but was previously deactivated → reactivate it (no duplicate row)
        if (existing.isPresent()) {
            StudentBatches sb = existing.get();
            if (sb.getStatus() == StudentBatches.Status.INACTIVE) {
                sb.setStatus(StudentBatches.Status.ACTIVE);
                studentBatchesRepository.save(sb);
                return;
            }
            throw new RuntimeException("Student is already assigned to this batch.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Batches batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        StudentBatches mapping = new StudentBatches();
        mapping.setStudent(student);
        mapping.setBatch(batch);
        mapping.setStatus(StudentBatches.Status.ACTIVE);
        studentBatchesRepository.save(mapping);
    }

    // ================= UPDATE COURSE MAPPING STATUS =================
    @Override
    public void updateCourseMappingStatus(Long studentId, Long courseId, String status) {
        StudentCourse mapping = studentCourseRepository
                .findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Course mapping not found"));
        // status param is "ACTIVE" or "INACTIVE" — matches StudentCourse.Status enum exactly
        mapping.setStatus(StudentCourse.Status.valueOf(status.toUpperCase()));
        studentCourseRepository.save(mapping);
    }

    // ================= UPDATE BATCH MAPPING STATUS =================
    @Override
    public void updateBatchMappingStatus(Long studentId, Long batchId, String status) {
        StudentBatches mapping = studentBatchesRepository
                .findByStudent_IdAndBatch_Id(studentId, batchId)
                .orElseThrow(() -> new RuntimeException("Batch mapping not found"));
        // status param is "ACTIVE" or "INACTIVE" — INACTIVE is now valid after entity + DB fix
        mapping.setStatus(StudentBatches.Status.valueOf(status.toUpperCase()));
        studentBatchesRepository.save(mapping);
    }
}