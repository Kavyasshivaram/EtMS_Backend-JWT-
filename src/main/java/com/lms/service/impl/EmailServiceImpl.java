package com.lms.service.impl;

import com.lms.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);  // dynamic, not hardcoded
            message.setSubject("Password Reset OTP - LMS");
            message.setText(
                "Dear User,\n\n" +
                "Your OTP for password reset is: " + otp + "\n\n" +
                "This OTP will expire in 15 minutes.\n\n" +
                "Do not share this OTP with anyone.\n\n" +
                "Regards,\nLMS Team"
            );
            mailSender.send(message);
            System.out.println("✅ Email sent to " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ MAIL ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }
}