package com.example.courierdistributionsystem.dto;

import com.example.courierdistributionsystem.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String defaultAddress;
    private User.UserRole role;
} 