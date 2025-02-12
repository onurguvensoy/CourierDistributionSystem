package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.jpa.UserRepository;
import com.example.courierdistributionsystem.repository.jpa.AdminRepository;
import com.example.courierdistributionsystem.repository.jpa.CustomerRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.utils.PasswordEncoderService;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Service
@Validated
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository;
    private final PasswordEncoderService passwordEncoder;
    private final Counter userSignupCounter;
    private final Counter userSignupFailureCounter;

    @Autowired
    public UserService(UserRepository userRepository,
                      AdminRepository adminRepository,
                      CustomerRepository customerRepository,
                      CourierRepository courierRepository,
                      PasswordEncoderService passwordEncoder,
                      MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.courierRepository = courierRepository;
        this.passwordEncoder = passwordEncoder;
        
        this.userSignupCounter = Counter.builder("user.signup.total")
                .description("Total number of user signups")
                .register(meterRegistry);
        
        this.userSignupFailureCounter = Counter.builder("user.signup.failures")
                .description("Number of failed user signups")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(@NotBlank String username) {
        logger.debug("Looking up user by username: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            user.ifPresent(u -> initializeLazyCollections(u));
            return user;
        } catch (Exception e) {
            logger.error("Error looking up user: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("Error looking up user", e);
        }
    }

    private void initializeLazyCollections(User user) {
        try {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                Hibernate.initialize(customer.getPackages());
            } else if (user instanceof Courier) {
                Hibernate.initialize(((Courier) user).getDeliveries());
            } else if (user instanceof Admin) {
                Hibernate.initialize(((Admin) user).getReports());
            }
        } catch (Exception e) {
            logger.warn("Error initializing lazy collections for user: {} - {}", user.getUsername(), e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email: {}", email);
        try {
            Optional<User> user = userRepository.findByEmail(email);
            user.ifPresent(u -> initializeLazyCollections(u));
            return user;
        } catch (Exception e) {
            logger.error("Error looking up user by email: {} - {}", email, e.getMessage(), e);
            throw new RuntimeException("Error looking up user by email", e);
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Admin saveAdmin(Admin admin) {
        logger.info("Saving admin user: {}", admin.getUsername());
        try {
            validatePassword(admin.getPassword());
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            Admin savedAdmin = adminRepository.save(admin);
            logger.info("Successfully saved admin user: {}", admin.getUsername());
            return savedAdmin;
        } catch (Exception e) {
            logger.error("Error saving admin user: {} - {}", admin.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving admin user", e);
        }
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Customer saveCustomer(Customer customer) {
        logger.info("Saving customer user: {}", customer.getUsername());
        try {
            validatePassword(customer.getPassword());
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Successfully saved customer user: {}", customer.getUsername());
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Error saving customer user: {} - {}", customer.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving customer user", e);
        }
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Courier saveCourier(Courier courier) {
        logger.info("Saving courier user: {}", courier.getUsername());
        try {
            validatePassword(courier.getPassword());
            courier.setPassword(passwordEncoder.encode(courier.getPassword()));
            Courier savedCourier = courierRepository.save(courier);
            logger.info("Successfully saved courier user: {}", courier.getUsername());
            return savedCourier;
        } catch (Exception e) {
            logger.error("Error saving courier user: {} - {}", courier.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving courier user", e);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException.InvalidUserDataException("Password cannot be null or empty");
        }
        if (password.length() < 6) {
            throw new AuthenticationException.InvalidUserDataException("Password must be at least 6 characters long");
        }
    }

    @Transactional
    public Map<String, String> signup(Map<String, String> signupRequest) {
        String username = signupRequest.get("username");
        logger.info("Processing signup request for username: {}", username);
        
        try {
            // Validate username and email uniqueness
            if (existsByUsername(username)) {
                logger.warn("Username already exists: {}", username);
                throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
            }

            String email = signupRequest.get("email");
            if (existsByEmail(email)) {
                logger.warn("Email already exists: {}", email);
                throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
            }

            // Process signup based on role
            String role = signupRequest.get("role").toUpperCase();
            User.UserRole userRole = User.UserRole.valueOf(role);
            
            switch (userRole) {
                case CUSTOMER -> processCustomerSignup(signupRequest);
                case COURIER -> processCourierSignup(signupRequest);
                case ADMIN -> processAdminSignup(signupRequest);
            };

            userSignupCounter.increment();
            logger.info("User registered successfully: {}", username);
            
            return Map.of("message", "User registered successfully");
            
        } catch (IllegalArgumentException e) {
            userSignupFailureCounter.increment();
            logger.error("Invalid role specified for user: {} - {}", username, e.getMessage());
            throw new AuthenticationException.InvalidUserDataException("Invalid role specified");
        } catch (Exception e) {
            userSignupFailureCounter.increment();
            logger.error("Error during user registration: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("Error during user registration", e);
        }
    }

    private Customer processCustomerSignup(Map<String, String> signupRequest) {
        String rawPassword = signupRequest.get("password");
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Customer customer = Customer.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(encodedPassword)
                .role(User.UserRole.CUSTOMER)
                .phoneNumber(signupRequest.get("phoneNumber"))
                .build();
        return saveCustomer(customer);
    }

    private Courier processCourierSignup(Map<String, String> signupRequest) {
        String rawPassword = signupRequest.get("password");
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Courier courier = Courier.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(encodedPassword)
                .role(User.UserRole.COURIER)
                .phoneNumber(signupRequest.get("phoneNumber"))
                .vehicleType(signupRequest.get("vehicleType"))
                .available(true)
                .build();
        return saveCourier(courier);
    }

    private Admin processAdminSignup(Map<String, String> signupRequest) {
        String rawPassword = signupRequest.get("password");
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Admin admin = Admin.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(encodedPassword)
                .role(User.UserRole.ADMIN)
                .build();
        return saveAdmin(admin);
    }

    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Cacheable(value = "users", key = "'customers'")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Cacheable(value = "users", key = "'couriers'")
    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    @Cacheable(value = "users", key = "'admins'")
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Cacheable(value = "users", key = "'courier_' + #id")
    public Optional<Courier> getCourierById(Long id) {
        return courierRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'customer_' + #id")
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'admin_' + #id")
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'user_' + #id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Cacheable(value = "users", key = "'stats'")
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalCouriers", courierRepository.count());
        stats.put("totalAdmins", adminRepository.count());
        return stats;
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public void deleteUser(User user) {
        logger.info("Deleting user: {} with role: {}", user.getUsername(), user.getRole());
        try {
            switch (user.getRole()) {
                case ADMIN -> adminRepository.delete((Admin) user);
                case CUSTOMER -> customerRepository.delete((Customer) user);
                case COURIER -> courierRepository.delete((Courier) user);
            }
            userRepository.delete(user);
            logger.info("User deleted successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteByUsername(String username) {
        logger.info("Attempting to delete user: {}", username);
        try {
            Optional<User> userOpt = findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found for deletion: {}", username);
                throw new UserNotFoundException("User not found: " + username);
            }

            User user = userOpt.get();
            deleteUser(user);
            logger.info("User deleted successfully: {}", username);
        } catch (Exception e) {
            logger.error("Error deleting user: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User editUser(Long id, Map<String, String> updates) {
        User user = getUserById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phoneNumber")) {
            if (user instanceof Customer) {
                ((Customer) user).setPhoneNumber(updates.get("phoneNumber"));
            } else if (user instanceof Courier) {
                ((Courier) user).setPhoneNumber(updates.get("phoneNumber"));
            }
        }
        if (updates.containsKey("password")) {
            user.setPassword(passwordEncoder.encode(updates.get("password")));
        }

        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Map<String, Object> updateUserSettings(String username, Map<String, String> settings) {
        logger.info("Updating settings for user: {}", username);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (settings.containsKey("email")) {
                String newEmail = settings.get("email");
                if (!user.getEmail().equals(newEmail) && existsByEmail(newEmail)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(newEmail);
            }

            if (settings.containsKey("phoneNumber")) {
                if (user instanceof Customer) {
                    ((Customer) user).setPhoneNumber(settings.get("phoneNumber"));
                } else if (user instanceof Courier) {
                    ((Courier) user).setPhoneNumber(settings.get("phoneNumber"));
                }
            }

            userRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "Settings updated successfully");
            logger.info("Successfully updated settings for user: {}", username);
        } catch (Exception e) {
            logger.error("Error updating user settings: {} - {}", username, e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to update settings: " + e.getMessage());
        }
        
        return response;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Map<String, Object> changePassword(String username, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", username);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                logger.warn("Invalid current password for user: {}", username);
                response.put("status", "error");
                response.put("message", "Current password is incorrect");
                return response;
            }

            validatePassword(newPassword);
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            response.put("status", "success");
            response.put("message", "Password changed successfully");
            logger.info("Successfully changed password for user: {}", username);
        } catch (Exception e) {
            logger.error("Error changing password: {} - {}", username, e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to change password: " + e.getMessage());
        }
        
        return response;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
} 