package com.lms.controller;

import com.lms.entity.RoleMaster;
import com.lms.entity.User;
import com.lms.enums.Status;
import com.lms.repository.RoleRepository;
import com.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin/users")
public class SuperAdminUserManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String roleName = payload.get("roleName");

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        RoleMaster role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role " + roleName + " not found"));
        }

        User user = new User();
        user.setName(payload.get("name"));
        user.setEmail(email);
        user.setPhone(payload.get("phone"));
        user.setPassword(passwordEncoder.encode(payload.get("password")));
        user.setStatus(Status.ACTIVE);
        user.setRole(role);

        return ResponseEntity.ok(userRepository.save(user));
    }

    @PatchMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setStatus(user.getStatus() == Status.ACTIVE ? Status.INACTIVE : Status.ACTIVE);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Status updated to " + user.getStatus()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
