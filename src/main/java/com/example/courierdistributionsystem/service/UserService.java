package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.exception.AuthenticationException;
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
    private final Timer userLookupTimer;

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
        
        this.userLookupTimer = Timer.builder("user.lookup.time")
                .description("Time taken to look up users")
                .register(meterRegistry);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username", unless = "#result == null")
    public Optional<User> findByUsername(@NotBlank String username) {
        logger.debug("Looking up user by username: {}", username);
        return userLookupTimer.record(() -> {
            try {
                Optional<User> user = userRepository.findByUsername(username);
                
                user.ifPresent(u -> {
                    logger.debug("Initializing lazy collections for user: {}", username);
                    initializeLazyCollections(u);
                });

                if (user.isEmpty()) {
                    logger.debug("User not found: {}", username);
                }
                
                return user;
            } catch (Exception e) {
                logger.error("Error looking up user: {} - {}", username, e.getMessage(), e);
                throw new RuntimeException("Error looking up user", e);
            }
        });
    }

    private void initializeLazyCollections(User user) {
        try {
            if (user instanceof Customer) {
                Customer customer = (Customer) user;
                Hibernate.initialize(customer.getPackages());
                Hibernate.initialize(customer.getNotifications());
                Hibernate.initialize(customer.getRatings());
            } else if (user instanceof Courier) {
                Hibernate.initialize(((Courier) user).getDeliveries());
                Hibernate.initialize(((Courier) user).getRatings());
            } else if (user instanceof Admin) {
                Hibernate.initialize(((Admin) user).getReports());
                Hibernate.initialize(((Admin) user).getNotifications());
                Hibernate.initialize(((Admin) user).getMetrics());
            }
        } catch (Exception e) {
            logger.warn("Error initializing lazy collections for user: {} - {}", user.getUsername(), e.getMessage());
        }
    }

    @Cacheable(value = "users", key = "#email")
    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email: {}", email);
        try {
            Optional<User> user = userRepository.findByEmail(email);
            
            user.ifPresent(u -> {
                logger.debug("Found user by email: {}", email);
                initializeLazyCollections(u);
            });

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

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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
    @CacheEvict(value = "users", allEntries = true)
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
            
            User savedUser = switch (userRole) {
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
        Admin admin = Admin.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(signupRequest.get("password"))
                .role(User.UserRole.ADMIN)
                .build();
        return saveAdmin(admin);
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
        logger.info("Deleting user: {} with role: {}", user.getUsername(), user.getRole());
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

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
} 