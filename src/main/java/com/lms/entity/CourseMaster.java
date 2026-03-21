package com.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "course_master")
public class CourseMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_name")
    private String courseName;

    private String duration;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String syllabusFileName;

    private String syllabusFilePath;



    @Column(nullable = false)
    private String status = "ACTIVE";

    // ================= GETTERS & SETTERS =================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSyllabusFileName() { return syllabusFileName; }
    public void setSyllabusFileName(String syllabusFileName) { this.syllabusFileName = syllabusFileName; }

    public String getSyllabusFilePath() { return syllabusFilePath; }
    public void setSyllabusFilePath(String syllabusFilePath) { this.syllabusFilePath = syllabusFilePath; }

   

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}