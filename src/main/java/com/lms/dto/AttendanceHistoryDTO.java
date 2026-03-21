package com.lms.dto;

import java.time.LocalDate;

public interface AttendanceHistoryDTO {
    LocalDate getAttendanceDate();
    String getStudentName();
    String getTopic();
    String getStatus();
}