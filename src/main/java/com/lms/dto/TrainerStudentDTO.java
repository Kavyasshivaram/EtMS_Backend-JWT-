package com.lms.dto;

public class TrainerStudentDTO {

    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String batchName;
    private String courseName;

    // Constructor for batch-wise students (4 params)
    public TrainerStudentDTO(Integer id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Constructor for trainer-wide students (6 params)
    public TrainerStudentDTO(Integer id, String name, String email,
                             String phone, String batchName, String courseName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.batchName = batchName;
        this.courseName = courseName;
    }

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBatchName() { return batchName; }
    public String getCourseName() { return courseName; }
}