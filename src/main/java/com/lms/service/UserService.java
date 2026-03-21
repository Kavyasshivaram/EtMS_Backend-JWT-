package com.lms.service;

import com.lms.dto.RegisterRequest;
import com.lms.entity.User;

public interface UserService {
	  User register(RegisterRequest request);
	  User login(String email, String password);
	  User createAdmin(RegisterRequest request);
	}

