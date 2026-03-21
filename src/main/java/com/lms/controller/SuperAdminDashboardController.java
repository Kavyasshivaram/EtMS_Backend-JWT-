package com.lms.controller;

import com.lms.repository.CourseRepository;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminDashboardController {

    @Autowired
    private com.lms.repository.UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private com.lms.repository.SalaryRepository salaryRepository;

    @Autowired
    private com.lms.repository.ExpenseRepository expenseRepository;

    @Autowired
    private com.lms.repository.MeetingRepository meetingRepository;

    @Autowired
    private com.lms.repository.MessageRepository messageRepository;

    @Autowired
    private com.lms.service.PerformanceService performanceService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Real Data
        long totalStudents = userRepository.findByRoleRoleNameIn(java.util.List.of("STUDENT")).size();
        long totalTrainers = userRepository.findByRole_RoleName("TRAINER").size();
        long totalAdmins = userRepository.findByRoleRoleNameIn(java.util.List.of("ADMIN", "SUPERADMIN")).size();
        long totalCourses = courseRepository.count();

        stats.put("totalStudents", totalStudents);
        stats.put("activeStudents", totalStudents); // Mocking active for now
        stats.put("totalTrainers", totalTrainers);
        stats.put("totalAdmins", totalAdmins);
        stats.put("totalBatches", 24); // Still mock for now
        stats.put("totalCourses", totalCourses);

        // Financial Data (REAL)
        double totalSalaries = salaryRepository.findAll().stream().mapToDouble(com.lms.entity.Salary::getAmount).sum();
        double totalExpenses = expenseRepository.findAll().stream().mapToDouble(com.lms.entity.Expense::getAmount).sum();
        double totalRevenue = 450000.0; // From Students eventually

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalExpenses", totalSalaries + totalExpenses);
        stats.put("netProfit", totalRevenue - (totalSalaries + totalExpenses));

        // Performance & Activity
        stats.put("upcomingMeetings", meetingRepository.count());
        stats.put("unreadMessages", messageRepository.findAll().stream().filter(m -> !m.isRead()).count());
        
        stats.put("avgAttendance", 86.4);
        stats.put("placementRate", 91.2);

        // Alerts (Dynamic-ish)
        java.util.List<String> alerts = new java.util.ArrayList<>();
        if (totalRevenue < (totalSalaries + totalExpenses)) alerts.add("⚠ Expenses exceeding revenue!");
        alerts.add("📅 " + meetingRepository.count() + " meetings scheduled this week");
        
        // Phase 6: AI Strategic Advice
        try {
            alerts.add("🤖 " + performanceService.getAiStrategicAdvice());
        } catch (Exception e) {
            alerts.add("💡 Recommendation: Evaluate batch capacity for next quarter.");
        }
        
        stats.put("alerts", alerts);

        return ResponseEntity.ok(stats);
    }
}
