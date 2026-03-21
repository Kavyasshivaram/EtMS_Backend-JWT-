package com.lms.entity;

import jakarta.persistence.*;
import com.lms.enums.Status;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    // ================= PRIMARY KEY =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BASIC FIELDS =================
    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    // ================= ROLE MAPPING =================
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleMaster role;

    // ================= STATUS ENUM =================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // ================= PERMISSION MAPPING =================
    @ManyToMany
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<PermissionMaster> permissions;

    @Column(length = 6)
    private String resetOtp;

    private java.time.LocalDateTime resetOtpExpiry;

    // ================= CONSTRUCTORS =================

    public User() {
    }

    // ================= GETTERS & SETTERS =================

    public String getResetOtp() { return resetOtp; }
    public void setResetOtp(String resetOtp) { this.resetOtp = resetOtp; }

    public java.time.LocalDateTime getResetOtpExpiry() { return resetOtpExpiry; }
    public void setResetOtpExpiry(java.time.LocalDateTime resetOtpExpiry) { this.resetOtpExpiry = resetOtpExpiry; }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
 
    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public RoleMaster getRole() {
        return role;
    }

    public Status getStatus() {
        return status;
    }

    public List<PermissionMaster> getPermissions() {
        return permissions;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
 
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
 
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRole(RoleMaster role) {
        this.role = role;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPermissions(List<PermissionMaster> permissions) {
        this.permissions = permissions;
    }

    // ================= TO STRING =================

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}
