package com.example.courierdistributionsystem.service;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.*;
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

/**
 * Service class for managing user-related operations.
 */
@Service
@Validated
public class UserService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private PasswordEncoderService passwordEncoder;

    private final Counter userSignupCounter;
    private final Counter userSignupFailureCounter;
    private final Timer userLookupTimer;

    @Autowired
    public UserService(UserRepository userRepository,
                      PasswordEncoderService passwordEncoder,
                      MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        
        this.userSignupCounter = Counter.builder("user.signup.total")
                .description("Total number of user signups")
                .register(meterRegistry);
        
        this.userSignupFailureCounter = Counter.builder("user.signup.failures")
                .description("Number of failed user signups")
                .register(meterRegistry);
        
        this.userLookupTimer = Timer.builder("user.lookup.time")
                .description("Time taken to look up users")
                .register(meterRegistry);
    }
    
    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return the found user
     * @throws UserNotFoundException if no user is found with the given username
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username", unless = "#result == null")
    public User findByUsername(@NotBlank String username) {
        return userLookupTimer.record(() -> {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            
            // Initialize lazy collections if needed
            if (user instanceof com.example.courierdistributionsystem.model.Customer) {
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Customer) user).getPackages());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Customer) user).getRatings());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Customer) user).getNotifications());
            } else if (user instanceof com.example.courierdistributionsystem.model.Courier) {
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Courier) user).getDeliveries());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Courier) user).getReports());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Courier) user).getRatings());
            } else if (user instanceof com.example.courierdistributionsystem.model.Admin) {
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Admin) user).getReports());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Admin) user).getNotifications());
                Hibernate.initialize(((com.example.courierdistributionsystem.model.Admin) user).getMetrics());
            }
            
            return user;
        });
    }

    @Cacheable(value = "users", key = "#email")
    public Optional<? extends User> findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }

        return switch (user.get().getRole()) {
            case ADMIN -> adminRepository.findByEmail(email);
            case CUSTOMER -> customerRepository.findByEmail(email);
            case COURIER -> courierRepository.findByEmail(email);
        };
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Admin saveAdmin(Admin admin) {
        if (admin.getPassword() == null || admin.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return adminRepository.save(admin);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Customer saveCustomer(Customer customer) {
        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return customerRepository.save(customer);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Courier saveCourier(Courier courier) {
        if (courier.getPassword() == null || courier.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return courierRepository.save(courier);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Courier> getCourierById(Long id) {
        return courierRepository.findById(id);
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalCouriers", courierRepository.count());
        stats.put("totalAdmins", adminRepository.count());
        return stats;
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(User user) {
        LOGGER.info("Deleting user: {} with role: {}", user.getUsername(), user.getRole());
        try {
            switch (user.getRole()) {
                case ADMIN -> {
                    Admin admin = (Admin) user;
                    adminRepository.delete(admin);
                }
                case CUSTOMER -> {
                    Customer customer = (Customer) user;
                    customerRepository.delete(customer);
                }
                case COURIER -> {
                    Courier courier = (Courier) user;
                    courierRepository.delete(courier);
                }
            }
            userRepository.delete(user);
            LOGGER.info("User deleted successfully: {}", user.getUsername());
        } catch (Exception e) {
            LOGGER.error("Error deleting user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Registers a new user in the system.
     *
     * @param signupRequest the signup request containing user details
     * @return a map containing the result of the signup operation
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public Map<String, String> signup(Map<String, String> signupRequest) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = signupRequest.get("username");
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String role = signupRequest.get("role");

            // Validate required fields
            if (username == null || username.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Username is required");
                return response;
            }
            if (email == null || email.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Email is required");
                return response;
            }
            if (password == null || password.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Password is required");
                return response;
            }
            if (role == null || role.trim().isEmpty()) {
                userSignupFailureCounter.increment();
                response.put("error", "Role is required");
                return response;
            }

            // Check if username already exists
            if (existsByUsername(username)) {
                userSignupFailureCounter.increment();
                response.put("error", "Username already exists");
                return response;
            }

            // Check if email already exists
            if (existsByEmail(email)) {
                userSignupFailureCounter.increment();
                response.put("error", "Email already exists");
                return response;
            }

            String encodedPassword = passwordEncoder.encode(password);
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            
            switch (userRole) {
                case CUSTOMER -> {
                    Customer customer = Customer.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .phoneNumber(signupRequest.get("phoneNumber"))
                            .deliveryAddress(signupRequest.get("deliveryAddress"))
                            .build();
                    saveCustomer(customer);
                }
                case COURIER -> {
                    Courier courier = Courier.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .phoneNumber(signupRequest.get("phoneNumber"))
                            .vehicleType(signupRequest.get("vehicleType"))
                            .available(true)
                            .build();
                    saveCourier(courier);
                }
                case ADMIN -> {
                    Admin admin = Admin.builder()
                            .username(username)
                            .email(email)
                            .password(encodedPassword)
                            .role(userRole)
                            .build();
                    saveAdmin(admin);
                }
                default -> {
                    userSignupFailureCounter.increment();
                    throw new IllegalArgumentException("Invalid role: " + role);
                }
            }

            userSignupCounter.increment();
            response.put("message", "User registered successfully");
            return response;
            
        } catch (IllegalArgumentException e) {
            userSignupFailureCounter.increment();
            LOGGER.error("Invalid role specified: {}", e.getMessage());
            response.put("error", "Invalid role specified");
            return response;
        } catch (Exception e) {
            userSignupFailureCounter.increment();
            LOGGER.error("Error during user registration: {}", e.getMessage());
            response.put("error", "An unexpected error occurred");
            return response;
        }
    }
    
    /**
     * Custom exception for user not found scenarios.
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteByUsername(String username) {
        LOGGER.info("Attempting to delete user by username: {}", username);
        User user = findByUsername(username);
        if (user == null) {
            LOGGER.warn("User not found for deletion: {}", username);
            throw new UserNotFoundException("User not found: " + username);
        }
        deleteUser(user);
    }
} 