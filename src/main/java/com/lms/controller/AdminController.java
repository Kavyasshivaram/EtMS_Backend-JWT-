package com.lms.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.lms.service.AdminService;

import com.lms.dto.*;
import com.lms.entity.*;
import com.lms.enums.Status;
import com.lms.repository.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	@Autowired
	private AttendanceRepository attendanceRepository;

	
    @Autowired
    private AdminService adminService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseTrainerRepository courseTrainerRepository;
    
    @Autowired
    private ScheduledClassRepository scheduledClassRepository;
    @Autowired
    private RoleRepository roleRepository;

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {
        return adminService.getDashboardData();
    }

    @PostMapping("/course")
    public ResponseEntity<?> createCourse(
            @RequestParam("courseName") String courseName,
            @RequestParam("duration") String duration,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {

            CourseMaster course = new CourseMaster();
            course.setCourseName(courseName);
            course.setDuration(duration);
            course.setDescription(description);
            course.setStatus("ACTIVE");

            // ===== UPDATED FILE UPLOAD LOGIC =====
            if (file != null && !file.isEmpty()) {

                String uploadDir = System.getProperty("user.dir") + "/uploads/syllabus/";

                java.io.File directory = new java.io.File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = uploadDir + fileName;

                file.transferTo(new java.io.File(filePath));

                course.setSyllabusFileName(fileName);
                course.setSyllabusFilePath(filePath);
            }

            courseRepository.save(course);

            return ResponseEntity.ok("Course Created Successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
//
//    @GetMapping("/course-full-details/{courseId}")
//    public ResponseEntity<?> getCourseFullDetails(@PathVariable Long courseId) {
//
//        try {
//
//            CourseMaster course = courseRepository.findById(courseId)
//                    .orElseThrow(() -> new RuntimeException("Course not found"));
//
//            Map<String, Object> response = new HashMap<>();
//
//            response.put("id", course.getId());
//            response.put("courseName", course.getCourseName());
//            response.put("description", course.getDescription());
//            response.put("duration", course.getDuration());
//            response.put("syllabusFileName", course.getSyllabusFileName());
//
//            // ✅ FIXED STUDENT DATA
//            List<Map<String, Object>> students = adminService.getStudentCourseMappings()
//                    .stream()
//                    .filter(m -> Long.valueOf(m.get("courseId").toString()).equals(courseId))
//                    .map(m -> {
//
//                        Long studentId = Long.valueOf(m.get("studentId").toString());
//
//                        User student = userRepository.findById(studentId).orElse(null);
//
//                        Map<String, Object> map = new HashMap<>();
//                        map.put("studentId", studentId);
//                        map.put("studentName", m.get("studentName"));
//                        map.put("studentEmail", m.get("studentEmail"));
//
//                        // ✅ ADD THESE (VERY IMPORTANT)
//                        map.put("phone", student != null ? student.getPhone() : "");
//                        map.put("status", student != null ? student.getStatus().name() : "ACTIVE");
//
//                        return map;
//
//                    })
//                    .collect(Collectors.toList());
//
//            response.put("students", students);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    @GetMapping("/courses")
    public List<CourseMaster> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .filter(course -> "ACTIVE".equals(course.getStatus()))
                .toList();
    }

    // ================= USERS =================
    @GetMapping("/students")
    public List<User> getAllStudents() {
        return userRepository.findByRole_RoleNameAndStatus("STUDENT", Status.ACTIVE);
    }

    @GetMapping("/trainers")
    public List<User> getAllActiveTrainers() {
        return userRepository.findByRole_RoleNameAndStatus("TRAINER", Status.ACTIVE);
    }

    @GetMapping("/all-users")
    public List<User> getAllUsers() {
        return userRepository.findByRole_RoleName("STUDENT");
    }

    @PutMapping("/approve-user/{id}")
    public String approveUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == Status.ACTIVE) {
            return "User is already approved";
        }

        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        return "User Approved Successfully";
    }
    // ================= BATCH =================
    @PostMapping("/create-batch")
    public ResponseEntity<?> createBatch(@RequestBody Map<String, Object> payload) {

        try {

            Batches batch = new Batches();

            batch.setBatchName(payload.get("batchName").toString());
            batch.setStatus("ONGOING");
            batch.setMeetingLink(payload.get("meetingLink") != null ? payload.get("meetingLink").toString() : "");

            Long trainerId = Long.valueOf(payload.get("trainerId").toString());

            User trainer = userRepository.findById(trainerId)
                    .orElseThrow(() -> new RuntimeException("Trainer not found"));

            batch.setTrainer(trainer);

            batch.setStartDate(LocalDate.parse(payload.get("startDate").toString()));
            batch.setEndDate(LocalDate.parse(payload.get("endDate").toString()));

            batchRepository.save(batch);

            return ResponseEntity.ok("Batch created successfully");

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }
    @GetMapping("/batches")
    public List<Batches> getAllBatches() {
        return batchRepository.findAll();
    }

 

    // ================= ASSIGNMENTS =================
    @GetMapping("/batch-assignments")
    public List<Map<String, Object>> getBatchAssignments() {

        return batchRepository.findAll().stream().map(batch -> {

            Map<String, Object> map = new HashMap<>();
            map.put("batchId", batch.getId());
            map.put("batchName", batch.getBatchName());
           

            if (batch.getTrainer() != null) {
                map.put("trainerName", batch.getTrainer().getName());
                map.put("trainerEmail", batch.getTrainer().getEmail());
            } else {
                map.put("trainerName", "Not Assigned");
                map.put("trainerEmail", "-");
            }

            return map;

        }).collect(Collectors.toList());
    }

    @GetMapping("/all-assignments")
    public List<Map<String, Object>> getAllAssignments() {

        return courseTrainerRepository.findAll().stream().map(ct -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ct.getId());
            map.put("courseName", ct.getCourse().getCourseName());
            map.put("trainerName", ct.getTrainer().getName());
            map.put("trainerEmail", ct.getTrainer().getEmail());
            return map;
        }).collect(Collectors.toList());
    }


   
    	//================= SCHEDULED CLASSES (UPDATED) =================
    @GetMapping("/schedule-classes")
    public List<Map<String, Object>> getAllScheduledClasses() {

     return scheduledClassRepository.findAll().stream().map(schedule -> {

         Map<String, Object> map = new HashMap<>();

         map.put("id", schedule.getId());

         map.put("startDate", schedule.getClassDate());

         /* FIXED */
         map.put("endDate", schedule.getEndDate());

         map.put("startTime", schedule.getStartTime());
         map.put("endTime", schedule.getEndTime());

         map.put("status", schedule.getStatus());

         Batches batch = batchRepository.findById(schedule.getBatchId()).orElse(null);
         User trainer = userRepository.findById(schedule.getTrainerId()).orElse(null);

         map.put("batch", Map.of(
                 "id", batch != null ? batch.getId() : 0,
                 "batch_name", batch != null ? batch.getBatchName() : "N/A"
         ));

         map.put("trainer", Map.of(
                 "id", trainer != null ? trainer.getId() : 0,
                 "trainer_name", trainer != null ? trainer.getName() : "N/A"
         ));

         return map;

     }).collect(Collectors.toList());
    }
 // ================= ASSIGN TRAINER TO BATCH =================
    @PostMapping("/assign-trainer-batch")
    public ResponseEntity<?> assignTrainerToBatch(@RequestBody Map<String, Long> payload) {
        try {

            Long batchId = payload.get("batchId");
            Long trainerId = payload.get("trainerId");

            Batches batch = batchRepository.findById(batchId)
                    .orElseThrow(() -> new RuntimeException("Batch not found"));

            User trainer = userRepository.findById(trainerId)
                    .orElseThrow(() -> new RuntimeException("Trainer not found"));

            batch.setTrainer(trainer);
            batchRepository.save(batch);

            return ResponseEntity.ok("Trainer assigned successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/reject-user/{id}")
    public String rejectUser(@PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == Status.REJECTED) {
            return "User is already rejected";
        }

        user.setStatus(Status.REJECTED);
        userRepository.save(user);

        return "User Rejected Successfully";
    }

    @PutMapping("/deactivate-user/{id}")
    public String deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == Status.INACTIVE) {
            return "User is already deactivated";
        }

        user.setStatus(Status.INACTIVE);
        userRepository.save(user);

        return "User Deactivated Successfully";
    }
 // ================= BATCH CRUD OPERATIONS =================


    @PutMapping("/schedule-classes/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id,
                                            @RequestBody Map<String, Object> payload) {

        try {

            ScheduledClass schedule = scheduledClassRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            Long batchId = Long.parseLong(payload.get("batchId").toString());

            LocalDate startDate = LocalDate.parse(payload.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(payload.get("endDate").toString());

            LocalTime startTime = LocalTime.parse(payload.get("startTime").toString());
            LocalTime endTime = LocalTime.parse(payload.get("endTime").toString());

            // ✅ Check conflict using batchId only
            List<ScheduledClass> existingSchedules =
                    scheduledClassRepository.findByBatchId(batchId);

            for (ScheduledClass existing : existingSchedules) {

                if (existing.getId().equals(id)) continue;

                boolean dateOverlap =
                        !(endDate.isBefore(existing.getClassDate())
                        || startDate.isAfter(existing.getEndDate()));

                boolean timeOverlap =
                        !(endTime.isBefore(existing.getStartTime())
                        || startTime.isAfter(existing.getEndTime()));

                if (dateOverlap && timeOverlap) {

                    return ResponseEntity.badRequest()
                            .body("Schedule conflict: Class already exists for this batch.");

                }
            }

            schedule.setBatchId(batchId);

            if (payload.get("trainerId") != null) {
                schedule.setTrainerId(Long.parseLong(payload.get("trainerId").toString()));
            }

            schedule.setClassDate(startDate);
            schedule.setEndDate(endDate);
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);

            String newStatus = payload.get("status").toString().toUpperCase();
            schedule.setStatus(newStatus);

            scheduledClassRepository.save(schedule);

            return ResponseEntity.ok("Schedule updated successfully");

        } catch (Exception e) {

            return ResponseEntity.badRequest().body(e.getMessage());

        }
    }
 // 3. TOGGLE STATUS (PATCH)
 @PatchMapping("/batches/{id}/status")
 public ResponseEntity<?> updateBatchStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
     try {
         Batches batch = batchRepository.findById(id)
                 .orElseThrow(() -> new RuntimeException("Batch not found"));
         
         batch.setStatus(payload.get("status"));
         batchRepository.save(batch);
         return ResponseEntity.ok("Status updated");
     } catch (Exception e) {
         return ResponseEntity.badRequest().body(e.getMessage());
     }
 }
//================= BATCH CRUD OPERATIONS =================
 @PutMapping("/batches/{id}")
 public ResponseEntity<?> updateBatch(@PathVariable Long id,
                                      @RequestBody Map<String, Object> payload) {

     try {

         Batches batch = batchRepository.findById(id)
                 .orElseThrow(() -> new RuntimeException("Batch not found"));

         batch.setBatchName(payload.get("batchName").toString());

         String statusInput = payload.get("status").toString().toUpperCase();
         batch.setStatus(statusInput);

         batch.setStartDate(LocalDate.parse(payload.get("startDate").toString()));
         batch.setEndDate(LocalDate.parse(payload.get("endDate").toString()));

         Long trainerId = Long.valueOf(payload.get("trainerId").toString());

         User trainer = userRepository.findById(trainerId)
                 .orElseThrow(() -> new RuntimeException("Trainer not found"));
         batch.setMeetingLink(payload.get("meetingLink") != null ? payload.get("meetingLink").toString() : "");

         batch.setTrainer(trainer);

         batchRepository.save(batch);

         return ResponseEntity.ok("Batch updated successfully");

     } catch (Exception e) {

         return ResponseEntity.badRequest().body("Error: " + e.getMessage());

     }
 }
//2. SOFT DELETE BATCH (Mark as INACTIVE)
@DeleteMapping("/batches/{id}")
public ResponseEntity<?> softDeleteBatch(@PathVariable Long id) {
  try {
      Batches batch = batchRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Batch not found"));
      
      // Ensure INACTIVE is added to your MySQL ENUM list
      batch.setStatus("INACTIVE"); 
      batchRepository.save(batch);
      return ResponseEntity.ok("Batch marked as Inactive");
  } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error: " + e.getMessage());
  }
}



//================= SOFT DELETE SCHEDULED CLASS (Mark as INACTIVE) =================
@DeleteMapping("/schedule-classes/{id}")
public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
 try {
     ScheduledClass schedule = scheduledClassRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Schedule not found"));

     // Soft Delete Logic: Change status to INACTIVE instead of removing from DB
     schedule.setStatus("INACTIVE");
     
     scheduledClassRepository.save(schedule);
     return ResponseEntity.ok("Schedule marked as Inactive successfully");
 } catch (Exception e) {
     return ResponseEntity.badRequest().body("Error updating status: " + e.getMessage());
 }
}
@PostMapping("/create-trainer")
public ResponseEntity<?> createTrainer(@RequestBody Map<String, String> payload) {
    try {

        String email = payload.get("email");

        // ✅ CHECK DUPLICATE EMAIL
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email already exists"));
        }

        User trainer = new User();
        String plainPass = payload.get("password");
        trainer.setName(payload.get("name"));
        trainer.setEmail(email);
        trainer.setPhone(payload.get("phone"));
        trainer.setPassword(passwordEncoder.encode(plainPass)); // hashed
        trainer.setStatus(Status.ACTIVE);

        RoleMaster role = roleRepository.findByRoleName("TRAINER");

        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "TRAINER role missing in DB"));
        }

        trainer.setRole(role);

        userRepository.save(trainer);

        return ResponseEntity.ok(Map.of(
                "message", "Trainer created successfully",
                "password", plainPass   // ✅ admin can see plaintext initially
        ));

    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}



@PutMapping("/reset-trainer-password/{id}")
public ResponseEntity<?> resetTrainerPassword(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload) {

    try {
        User trainer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));
        String plainPass = payload.get("password");
        trainer.setPassword(passwordEncoder.encode(plainPass));
        userRepository.save(trainer);

        // Simulated SMS trigger
        System.out.println("Send SMS to: " + trainer.getPhone());
        System.out.println("New Password: " + payload.get("password"));

        return ResponseEntity.ok("Password updated and sent to trainer phone.");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
@PutMapping("/inactivate-trainer/{id}")
public ResponseEntity<?> inactivateTrainer(@PathVariable Long id) {
    try {
        User trainer = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer not found"));

        trainer.setStatus(Status.INACTIVE);
        userRepository.save(trainer);

        return ResponseEntity.ok("Trainer marked as INACTIVE");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
@GetMapping("/all-trainers")
public List<User> getAllTrainers() {
    return userRepository.findByRole_RoleName("TRAINER");
}
@PutMapping("/update-trainer/{id}")
public ResponseEntity<?> updateTrainer(@PathVariable Long id,
                                       @RequestBody User updatedTrainer) {

    User trainer = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trainer not found"));

    trainer.setName(updatedTrainer.getName());
    trainer.setEmail(updatedTrainer.getEmail());
    trainer.setPhone(updatedTrainer.getPhone());
    
    if (updatedTrainer.getPassword() != null && !updatedTrainer.getPassword().isEmpty()) {
         trainer.setPassword(passwordEncoder.encode(updatedTrainer.getPassword()));
    }

    userRepository.save(trainer);

    return ResponseEntity.ok(Map.of("message", "Trainer updated successfully"));
}
//================= TOGGLE TRAINER STATUS =================
@PutMapping("/toggle-trainer-status/{id}")
public ResponseEntity<?> toggleTrainerStatus(@PathVariable Long id) {

 try {
     User trainer = userRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Trainer not found"));

     // Toggle logic
     if (trainer.getStatus() == Status.ACTIVE) {
         trainer.setStatus(Status.INACTIVE);
     } else {
         trainer.setStatus(Status.ACTIVE);
     }

     userRepository.save(trainer);

     return ResponseEntity.ok(
             Map.of(
                     "message", "Trainer status updated successfully",
                     "newStatus", trainer.getStatus()
             )
     );

 } catch (Exception e) {
     return ResponseEntity.badRequest().body(
             Map.of("message", e.getMessage())
     );
 }
}
@PutMapping("/courses/{id}")
public ResponseEntity<?> updateCourse(
        @PathVariable Long id,
        @RequestParam("courseName") String courseName,
        @RequestParam("duration") String duration,
        @RequestParam("description") String description,
        @RequestParam(value = "file", required = false) MultipartFile file
) {

    try {

        CourseMaster course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setCourseName(courseName);
        course.setDuration(duration);
        course.setDescription(description);

        // ===== UPDATED FILE UPLOAD LOGIC =====
        if (file != null && !file.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads/syllabus/";

            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadDir + fileName;

            file.transferTo(new java.io.File(filePath));

            course.setSyllabusFileName(fileName);
            course.setSyllabusFilePath(filePath);
        }

        courseRepository.save(course);

        return ResponseEntity.ok("Course updated successfully");

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


//================= SOFT DELETE COURSE =================
@DeleteMapping("/courses/{id}")
public ResponseEntity<?> softDeleteCourse(@PathVariable Long id) {
 try {
     CourseMaster course = courseRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Course not found"));

     course.setStatus("INACTIVE");
     courseRepository.save(course);

     return ResponseEntity.ok("Course marked as INACTIVE");

 } catch (Exception e) {
     return ResponseEntity.badRequest().body(e.getMessage());
 }
}
//================= REACTIVATE COURSE =================
@PutMapping("/courses/reactivate/{id}")
public ResponseEntity<?> reactivateCourse(@PathVariable Long id) {
 try {
     CourseMaster course = courseRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Course not found"));

     course.setStatus("ACTIVE");
     courseRepository.save(course);

     return ResponseEntity.ok("Course reactivated successfully");

 } catch (Exception e) {
     return ResponseEntity.badRequest().body(e.getMessage());
 }
}
@GetMapping("/courses/inactive")
public List<CourseMaster> getInactiveCourses() {
    return courseRepository.findAll()
            .stream()
            .filter(course -> "INACTIVE".equals(course.getStatus()))
            .toList();
}
@GetMapping("/courses/download/{id}")
public ResponseEntity<?> downloadSyllabus(@PathVariable Long id) {

    try {
        CourseMaster course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (course.getSyllabusFilePath() == null) {
            return ResponseEntity.badRequest().body("No syllabus available");
        }

        java.io.File file = new java.io.File(course.getSyllabusFilePath());

        if (!file.exists()) {
            return ResponseEntity.badRequest().body("File not found on server");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + course.getSyllabusFileName() + "\"")
                .body(new org.springframework.core.io.FileSystemResource(file));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}



@GetMapping("/student-course-mappings")
public List<Map<String, Object>> getStudentCourseMappings() {
    return adminService.getStudentCourseMappings();
}



@GetMapping("/student-batch-mappings")
public List<Map<String, Object>> getStudentBatchMappings() {
    return adminService.getStudentBatchMappings();
}



@PostMapping("/student-course-mappings")
public ResponseEntity<?> mapStudentToCourse(
        @RequestParam Long studentId,
        @RequestParam Long courseId) {

    try {
        adminService.mapStudentToCourse(studentId, courseId);
        return ResponseEntity.ok("Mapped successfully");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@PostMapping("/student-batch-mappings")
public ResponseEntity<?> mapStudentToBatch(
        @RequestParam Long studentId,
        @RequestParam Long batchId) {

    try {
        adminService.mapStudentToBatch(studentId, batchId);
        return ResponseEntity.ok("Mapped successfully");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}


@PutMapping("/students/{id}/contact")
public ResponseEntity<?> updateStudentContact(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload) {
    try {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        String newEmail = payload.get("email");
        String newPhone = payload.get("phone");

        if (newEmail != null && !newEmail.equals(student.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email already in use by another account"));
            }
            student.setEmail(newEmail.trim());
        }

        if (newPhone != null) {
            student.setPhone(newPhone.trim());
        }

        userRepository.save(student);

        // ✅ RETURN UPDATED DATA
        return ResponseEntity.ok(Map.of(
                "message", "Contact updated successfully",
                "studentId", student.getId(),
                "studentEmail", student.getEmail(),
                "phone", student.getPhone()
        ));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

//================= ATTENDANCE MARKING =================

//1. Fetch Students by Batch for Marking
@GetMapping("/attendance/students/{batchId}")
public ResponseEntity<?> getStudentsByBatch(@PathVariable Long batchId) {
 try {
     Batches batch = batchRepository.findById(batchId)
             .orElseThrow(() -> new RuntimeException("Batch not found"));

     // Assuming StudentBatch entity connects students to batches
     List<Map<String, Object>> students = batch.getStudentBatches().stream()
             .map(sb -> {
                 Map<String, Object> map = new HashMap<>();
                 map.put("studentId", sb.getStudent().getId());
                 map.put("studentName", sb.getStudent().getName());
                 return map;
             }).collect(Collectors.toList());

     return ResponseEntity.ok(students);
 } catch (Exception e) {
     return ResponseEntity.badRequest().body("Error: " + e.getMessage());
 }
}


@GetMapping("/courses/details")
public List<Map<String, Object>> getAllCoursesDetails() {

    List<Map<String, Object>> studentMappings = adminService.getStudentCourseMappings();

    return courseRepository.findAll().stream().map(course -> {

        Map<String, Object> courseMap = new HashMap<>();

        courseMap.put("id", course.getId());
        courseMap.put("courseName", course.getCourseName());
        courseMap.put("description", course.getDescription());
        courseMap.put("duration", course.getDuration());
        courseMap.put("syllabusFileName", course.getSyllabusFileName());

        // ✅ Fetch students mapped to this course
        List<Map<String, Object>> students =
                studentMappings.stream()
                        .filter(m -> Long.valueOf(m.get("courseId").toString())
                                .equals(course.getId()))
                        .collect(Collectors.toList());

        courseMap.put("students", students);

        return courseMap;

    }).collect(Collectors.toList());
}
@PostMapping("/schedule-classes")
public ResponseEntity<?> scheduleClass(@RequestBody Map<String, String> payload) {

    try {

        Long batchId = Long.parseLong(payload.get("batchId"));
        Long trainerId = Long.parseLong(payload.get("trainerId"));

        Batches batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        // ✅ Allow only ACTIVE batches
        if (!"ONGOING".equalsIgnoreCase(batch.getStatus())) {
            return ResponseEntity.badRequest().body("Classes can only be scheduled for ACTIVE batches");
        }

        LocalDate startDate = LocalDate.parse(payload.get("startDate"));
        LocalDate endDate = LocalDate.parse(payload.get("endDate"));

        LocalTime startTime = LocalTime.parse(payload.get("startTime"));
        LocalTime endTime = LocalTime.parse(payload.get("endTime"));

        List<ScheduledClass> existingSchedules =
                scheduledClassRepository.findByBatchId(batchId);

        for (ScheduledClass existing : existingSchedules) {

            boolean dateOverlap =
                    !(endDate.isBefore(existing.getClassDate())
                            || startDate.isAfter(existing.getEndDate()));

            boolean timeOverlap =
                    !(endTime.isBefore(existing.getStartTime())
                            || startTime.isAfter(existing.getEndTime()));

            if (dateOverlap && timeOverlap) {

                return ResponseEntity.badRequest()
                        .body("Schedule conflict: Class already exists for this batch.");

            }
        }

        ScheduledClass schedule = new ScheduledClass();

        schedule.setBatchId(batchId);
        schedule.setTrainerId(trainerId);
        schedule.setClassDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setStatus("ACTIVE");

        scheduledClassRepository.save(schedule);

        return ResponseEntity.ok("Class scheduled successfully");

    } catch (Exception e) {

        return ResponseEntity.badRequest().body(e.getMessage());

    }
}

@Autowired
private StudentBatchesRepository studentBatchesRepository;



// 3. GET STUDENTS OF A BATCH (FOR INITIAL DISPLAY)
@GetMapping("/attendance/batch/{batchId}/students")
public ResponseEntity<?> getBatchStudents(@PathVariable Long batchId) {
    try {
        // Get all students mapped to this batch
        List<StudentBatches> studentBatches = studentBatchesRepository.findByBatch_Id(batchId);
        
        List<Map<String, Object>> students = studentBatches.stream().map(sb -> {
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", sb.getStudent().getId());
            map.put("studentName", sb.getStudent().getName());
            map.put("studentEmail", sb.getStudent().getEmail());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(students);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}

// 4. DOWNLOAD ATTENDANCE REPORT (CSV)
@GetMapping("/attendance/download/{batchId}")
public ResponseEntity<?> downloadAdminAttendanceReport(
        @PathVariable Integer batchId,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate) {

    try {
        Batches batch = batchRepository.findById(Long.valueOf(batchId))
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        List<TrainerMarkedAttendance> attendanceList;

        if (fromDate != null && toDate != null) {
            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);
            attendanceList = attendanceRepository.findByBatchIdAndAttendanceDateBetween(batchId, from, to);
        } else {
            attendanceList = attendanceRepository.findByBatchId(batchId);
        }

        // Generate CSV content
        StringBuilder csv = new StringBuilder();
        csv.append("Student Name,Email,Date,Status,Topic,Created At\n");

        for (TrainerMarkedAttendance attendance : attendanceList) {
            User student = userRepository.findById(Long.valueOf(attendance.getStudentId())).orElse(null);
            
            if (student != null) {
                csv.append(student.getName()).append(",");
                csv.append(student.getEmail()).append(",");
            } else {
                csv.append("Unknown,N/A,");
            }
            
            csv.append(attendance.getAttendanceDate()).append(",");
            csv.append(attendance.getStatus()).append(",");
            csv.append(attendance.getTopic() != null ? attendance.getTopic() : "N/A").append(",");
            csv.append(attendance.getCreatedAt()).append("\n");
        }

        String filename = batch.getBatchName() + "_Attendance_Report.csv";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "text/csv")
                .body(csv.toString());

    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
//================= ADMIN ATTENDANCE =================




@GetMapping("/attendance/batch/{batchId}")
public ResponseEntity<?> getAdminAttendanceByBatch(
        @PathVariable Long batchId,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate) {

    try {

        List<TrainerMarkedAttendance> attendanceList;

        if (date != null && !date.isEmpty()) {

            LocalDate targetDate = LocalDate.parse(date);

            attendanceList = attendanceRepository
                    .findByBatchIdAndAttendanceDate(batchId.intValue(), targetDate);

        } else if (fromDate != null && toDate != null
                && !fromDate.isEmpty() && !toDate.isEmpty()) {

            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);

            attendanceList = attendanceRepository
                    .findByBatchIdAndAttendanceDateBetween(batchId.intValue(), from, to);

        } else {

            attendanceList = attendanceRepository
                    .findByBatchId(batchId.intValue());
        }

        List<Map<String, Object>> response = new ArrayList<>();

        for (TrainerMarkedAttendance attendance : attendanceList) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", attendance.getId());
            map.put("studentId", attendance.getStudentId());
            map.put("batchId", attendance.getBatchId());
            map.put("date", attendance.getAttendanceDate());
            map.put("status", attendance.getStatus());
            map.put("topic", attendance.getTopic());

            // Fetch student details
            User student = userRepository
                    .findById(Long.valueOf(attendance.getStudentId()))
                    .orElse(null);

            if (student != null) {
                map.put("studentName", student.getName());
                map.put("studentEmail", student.getEmail());
            } else {
                map.put("studentName", "Unknown");
                map.put("studentEmail", "N/A");
            }

            response.add(map);
        }

        return ResponseEntity.ok(response);

    } catch (Exception e) {

        return ResponseEntity.badRequest()
                .body("Error fetching attendance: " + e.getMessage());
    }
}
@PutMapping("/attendance/update")
public ResponseEntity<?> updateAdminAttendance(
        @RequestBody List<Map<String, Object>> updates) {

    try {

        for (Map<String, Object> update : updates) {

            Integer id = update.get("id") != null
                    ? Integer.valueOf(update.get("id").toString())
                    : null;

            String status = update.get("status").toString();

            TrainerMarkedAttendance attendance;

            // UPDATE EXISTING RECORD
            if (id != null) {

                attendance = attendanceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Attendance record not found"));

                attendance.setStatus(status);

            } else {

                attendance = new TrainerMarkedAttendance();

                attendance.setStudentId(
                        Integer.valueOf(update.get("studentId").toString())
                );

                attendance.setBatchId(
                        Integer.valueOf(update.get("batchId").toString())
                );

                attendance.setAttendanceDate(
                        LocalDate.parse(update.get("date").toString())
                );

                attendance.setStatus(status);

                attendance.setTopic(
                        update.get("topic") != null
                                ? update.get("topic").toString()
                                : ""
                );

                attendance.setCreatedAt(LocalDate.now().atStartOfDay());
            }

            attendanceRepository.save(attendance);
        }

        return ResponseEntity.ok("Attendance updated successfully");

    } catch (Exception e) {

        return ResponseEntity.badRequest()
                .body("Update failed: " + e.getMessage());
    }
}
////================= UPDATE STUDENT CONTACT (email + phone) =================
//@PutMapping("/students/{id}/contact")
//public ResponseEntity<?> updateStudentContact(
//     @PathVariable Long id,
//     @RequestBody Map<String, String> payload) {
// try {
//     User student = userRepository.findById(id)
//             .orElseThrow(() -> new RuntimeException("Student not found"));
//
//     String newEmail = payload.get("email");
//     String newPhone = payload.get("phone");
//
//     // If email changed, check it's not taken by another user
//     if (newEmail != null && !newEmail.equals(student.getEmail())) {
//         if (userRepository.findByEmail(newEmail).isPresent()) {
//             return ResponseEntity.badRequest()
//                     .body(Map.of("message", "Email already in use by another account"));
//         }
//         student.setEmail(newEmail.trim());
//     }
//
//     if (newPhone != null) {
//         student.setPhone(newPhone.trim());
//     }
//
//     userRepository.save(student);
//
//     return ResponseEntity.ok(Map.of("message", "Contact updated successfully"));
//
// } catch (Exception e) {
//     return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
// }
//}
//
//@PutMapping("/students/{id}/toggle-status")
//public ResponseEntity<?> toggleStudentStatus(@PathVariable Long id) {
//    try {
//        User student = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Student not found"));
// 
//        if (student.getStatus() == Status.ACTIVE) {
//            student.setStatus(Status.INACTIVE);
//        } else {
//            student.setStatus(Status.ACTIVE);
//        }
// 
//        userRepository.save(student);
// 
//        return ResponseEntity.ok(Map.of(
//                "message", "Student status updated successfully",
//                "newStatus", student.getStatus().name()
//        ));
// 
//    } catch (Exception e) {
//        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//    }
//}
@PutMapping("/students/{id}/toggle-status")
public ResponseEntity<?> toggleStudentStatus(@PathVariable Long id) {
    try {
        User student = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getStatus() == Status.ACTIVE) {
            student.setStatus(Status.INACTIVE);
        } else {
            student.setStatus(Status.ACTIVE);
        }

        userRepository.save(student);

        // ✅ IMPORTANT: return "newStatus"
        return ResponseEntity.ok(Map.of(
                "message", "Student status updated successfully",
                "newStatus", student.getStatus().name()
        ));

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}




//── 2. Update Student-Batch Mapping Status ───────────────────
@PutMapping("/student-batch-mappings/status")
public ResponseEntity<?> updateBatchMappingStatus(
     @RequestBody Map<String, Object> payload) {
 try {
     Long studentId = Long.valueOf(payload.get("studentId").toString());
     Long batchId   = Long.valueOf(payload.get("batchId").toString());
     String statusStr = payload.get("status").toString().toUpperCase(); // "ACTIVE" or "INACTIVE"

     StudentBatches mapping = studentBatchesRepository
             .findByStudent_IdAndBatch_Id(studentId, batchId)
             .orElseThrow(() -> new RuntimeException("Batch mapping not found"));

     // Use StudentBatches.Status enum — INACTIVE is now a valid value
     mapping.setStatus(StudentBatches.Status.valueOf(statusStr));
     studentBatchesRepository.save(mapping);

     return ResponseEntity.ok(Map.of(
             "message", "Batch mapping status updated to " + statusStr,
             "studentId", studentId,
             "batchId", batchId,
             "status", statusStr
     ));
 } catch (Exception e) {
     return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
 }
}
// ── Update Student-Course Mapping Status (ACTIVE ↔ INACTIVE) ──
@PutMapping("/student-course-mappings/status")
public ResponseEntity<?> updateCourseMappingStatus(
        @RequestBody Map<String, Object> payload) {
    try {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        Long courseId  = Long.valueOf(payload.get("courseId").toString());
        String statusStr = payload.get("status").toString().toUpperCase();

        adminService.updateCourseMappingStatus(studentId, courseId, statusStr);

        return ResponseEntity.ok(Map.of(
                "message",   "Course mapping status updated to " + statusStr,
                "studentId", studentId,
                "courseId",  courseId,
                "status",    statusStr
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

@PutMapping("/student-course-mappings/deactivate")
public ResponseEntity<?> deactivateCourseMapping(
        @RequestBody Map<String, Object> payload) {
    try {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        Long courseId  = Long.valueOf(payload.get("courseId").toString());

        // Deactivate only the course mapping — student account is untouched
        adminService.updateCourseMappingStatus(studentId, courseId, "INACTIVE");

        return ResponseEntity.ok(Map.of(
                "message",   "Course mapping deactivated",
                "studentId", studentId,
                "courseId",  courseId,
                "newStatus", "INACTIVE"
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
@GetMapping("/course-full-details/{courseId}")
public ResponseEntity<?> getCourseFullDetails(@PathVariable Long courseId) {
    try {
        CourseMaster course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id",               course.getId());
        response.put("courseName",        course.getCourseName());
        response.put("description",       course.getDescription());
        response.put("duration",          course.getDuration());
        response.put("syllabusFileName",  course.getSyllabusFileName());

        // ── Return ALL mappings for this course (active + inactive) ──
        List<Map<String, Object>> students = adminService.getStudentCourseMappings()
                .stream()
                .filter(m -> Long.valueOf(m.get("courseId").toString()).equals(courseId))
                .map(m -> {
                    Long studentId = Long.valueOf(m.get("studentId").toString());
                    User student = userRepository.findById(studentId).orElse(null);

                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId",    studentId);
                    map.put("studentName",  m.get("studentName"));
                    map.put("studentEmail", m.get("studentEmail"));
                    map.put("phone",        student != null ? student.getPhone() : "");

                    // ── FIX: use MAPPING status from adminService (not student account status) ──
                    // m.get("status") is the StudentCourse.Status → "ACTIVE" or "INACTIVE"
                    map.put("status", m.get("status") != null ? m.get("status") : "ACTIVE");

                    return map;
                })
                .collect(Collectors.toList());

        response.put("students", students);
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
@PutMapping("/student-course-mappings/activate")
public ResponseEntity<?> activateCourseMappingAndStudent(
        @RequestBody Map<String, Object> payload) {
    try {
        Long studentId = Long.valueOf(payload.get("studentId").toString());
        Long courseId  = Long.valueOf(payload.get("courseId").toString());

        // 1. Activate the course mapping
        adminService.updateCourseMappingStatus(studentId, courseId, "ACTIVE");

        // 2. Also activate the student account if it was INACTIVE
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (student.getStatus() != Status.ACTIVE) {
            student.setStatus(Status.ACTIVE);
            userRepository.save(student);
        }

        return ResponseEntity.ok(Map.of(
                "message",   "Student activated successfully",
                "studentId", studentId,
                "courseId",  courseId,
                "newStatus", "ACTIVE"
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
}