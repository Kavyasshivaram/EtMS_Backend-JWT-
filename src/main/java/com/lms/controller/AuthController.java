package com.lms.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.lms.dto.LoginRequest;
import com.lms.dto.LoginResponse;
import com.lms.dto.RegisterRequest;
import com.lms.entity.User;
import com.lms.repository.UserRepository;
import com.lms.service.UserService;
import com.lms.security.JwtService;
import com.lms.security.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        User user = userService.register(request);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Registration failed"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Registration successful",
                "userId", String.valueOf(user.getId())
        ));
    }

    // =========================
    // LOGIN (SESSION CREATION)
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        try {

            User user = userService.login(
                    request.getEmail(),
                    request.getPassword()
            );

            // Generate JWT Token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtService.generateToken(userDetails);

            LoginResponse response = new LoginResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().getRoleName(),
                    jwt
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // =========================
    // LOGOUT
    // =========================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
    @Autowired
    private com.lms.service.EmailService emailService;

 
    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {

        String email = payload.get("email");
        System.out.println("🔍 STEP 1: Received forgot-password for: " + email);

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = optionalUser.get();
        System.out.println("🔍 STEP 2: User found: " + user.getEmail());

        String otp = String.valueOf((int)((Math.random() * 900000) + 100000));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        System.out.println("🔍 STEP 3: OTP saved to DB: " + otp);

        // Use EmailService (not direct mailSender) - keeps controller clean
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
            System.out.println("✅ STEP 4: Email sent successfully to " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ EMAIL FAILED: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            // Still return OK — OTP is in DB, don't block user
        }

        return ResponseEntity.ok(Map.of("message", "OTP sent to your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));

        User user = optionalUser.get();
        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid OTP"));
        }

        if (user.getResetOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "OTP Expired"));
        }

        return ResponseEntity.ok(Map.of("message", "OTP Verified. Proceed to reset."));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));

        User user = optionalUser.get();
        
        // Final verification
        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Unauthorized reset attempt"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null); // Clear OTP after use
        user.setResetOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successful. You can now login."));
    }
 
}
