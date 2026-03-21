package com.lms.service.impl;

import com.lms.service.SmsService;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendOtpSms(String phoneNumber, String otp) {
        System.out.println("⚠️ SMS skipped - Twilio not configured");
    }
}