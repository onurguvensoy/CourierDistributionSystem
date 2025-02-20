package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.model.Admin;
import com.example.courierdistributionsystem.model.Customer;
import com.example.courierdistributionsystem.model.Courier;
import com.example.courierdistributionsystem.dto.UserDto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    Optional<User> findByUsername(@NotBlank String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    Admin saveAdmin(Admin admin);
    Customer saveCustomer(Customer customer);
    Courier saveCourier(Courier courier);
    
    Map<String, String> signup(Map<String, String> signupRequest);
    List<User> getAllUsers();
    List<Customer> getAllCustomers();
    List<Courier> getAllCouriers();
    List<Admin> getAllAdmins();
    
    Optional<Courier> getCourierById(Long id);
    Optional<Customer> getCustomerById(Long id);
    Optional<Admin> getAdminById(Long id);
    Optional<User> getUserById(Long id);
    
    Map<String, Object> getUserStats();
    void deleteUser(User user);
    void deleteByUsername(String username);
    User editUser(Long id, Map<String, String> updates);
    Map<String, Object> updateUserSettings(String username, Map<String, String> settings);
    Map<String, Object> changePassword(String username, String currentPassword, String newPassword);
    User updateUser(User user);
    
    // Additional methods based on implementation
    void deleteUser(Long id);
    UserDto getUserProfile(String username);
    Map<String, Object> updateProfile(String username, Map<String, String> updates);
    void validateUser(String username, String password);
    boolean isValidPassword(String password);
    void checkUserExists(String username);
    void checkEmailExists(String email);
} 