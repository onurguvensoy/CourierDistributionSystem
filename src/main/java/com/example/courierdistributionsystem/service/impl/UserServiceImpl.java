package com.example.courierdistributionsystem.service.impl;

import com.example.courierdistributionsystem.dto.UserDto;
import com.example.courierdistributionsystem.exception.AuthenticationException;
import com.example.courierdistributionsystem.exception.ResourceNotFoundException;
import com.example.courierdistributionsystem.model.*;
import com.example.courierdistributionsystem.repository.jpa.UserRepository;
import com.example.courierdistributionsystem.repository.jpa.AdminRepository;
import com.example.courierdistributionsystem.repository.jpa.CustomerRepository;
import com.example.courierdistributionsystem.repository.jpa.CourierRepository;
import com.example.courierdistributionsystem.service.IUserService;
import com.example.courierdistributionsystem.mapper.UserMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository;
    private final Counter userSignupCounter;
    private final Counter userSignupFailureCounter;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                      AdminRepository adminRepository,
                      CustomerRepository customerRepository,
                      CourierRepository courierRepository,
                      MeterRegistry meterRegistry,
                      UserMapper userMapper) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.courierRepository = courierRepository;
        this.userMapper = userMapper;
        
        this.userSignupCounter = Counter.builder("user.signup.total")
                .description("Total number of user signups")
                .register(meterRegistry);
        
        this.userSignupFailureCounter = Counter.builder("user.signup.failures")
                .description("Total number of failed user signups")
                .register(meterRegistry);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(@NotBlank String username) {
        logger.debug("Looking up user by username: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            user.ifPresent(this::initializeLazyCollections);
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
                // Admin has no collections to initialize
            }
        } catch (Exception e) {
            logger.warn("Error initializing lazy collections for user: {} - {}", user.getUsername(), e.getMessage());
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Looking up user by email: {}", email);
        try {
            Optional<User> user = userRepository.findByEmail(email);
            user.ifPresent(this::initializeLazyCollections);
            return user;
        } catch (Exception e) {
            logger.error("Error looking up user by email: {} - {}", email, e.getMessage(), e);
            throw new RuntimeException("Error looking up user by email", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Admin saveAdmin(Admin admin) {
        logger.info("Saving admin user: {}", admin.getUsername());
        try {
            validatePassword(admin.getPassword());
            Admin savedAdmin = adminRepository.save(admin);
            logger.info("Successfully saved admin user: {}", admin.getUsername());
            return savedAdmin;
        } catch (Exception e) {
            logger.error("Error saving admin user: {} - {}", admin.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving admin user", e);
        }
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Customer saveCustomer(Customer customer) {
        logger.info("Saving customer user: {}", customer.getUsername());
        try {
            validatePassword(customer.getPassword());
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Successfully saved customer user: {}", customer.getUsername());
            return savedCustomer;
        } catch (Exception e) {
            logger.error("Error saving customer user: {} - {}", customer.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving customer user", e);
        }
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Courier saveCourier(Courier courier) {
        logger.info("Saving courier user: {}", courier.getUsername());
        try {
            validatePassword(courier.getPassword());
            Courier savedCourier = courierRepository.save(courier);
            logger.info("Successfully saved courier user: {}", courier.getUsername());
            return savedCourier;
        } catch (Exception e) {
            logger.error("Error saving courier user: {} - {}", courier.getUsername(), e.getMessage(), e);
            throw new RuntimeException("Error saving courier user", e);
        }
    }

    @Override
    @Transactional
    public Map<String, String> signup(Map<String, String> signupRequest) {
        String username = signupRequest.get("username");
        logger.info("Processing signup request for username: {}", username);
        
        try {
            if (existsByUsername(username)) {
                logger.warn("Username already exists: {}", username);
                throw new AuthenticationException.UserAlreadyExistsException("Username already exists");
            }

            String email = signupRequest.get("email");
            if (existsByEmail(email)) {
                logger.warn("Email already exists: {}", email);
                throw new AuthenticationException.UserAlreadyExistsException("Email already exists");
            }

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
        Customer customer = Customer.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(signupRequest.get("password"))
                .role(User.UserRole.CUSTOMER)
                .phoneNumber(signupRequest.get("phoneNumber"))
                .build();
        return saveCustomer(customer);
    }

    private Courier processCourierSignup(Map<String, String> signupRequest) {
        Courier courier = Courier.builder()
                .username(signupRequest.get("username"))
                .email(signupRequest.get("email"))
                .password(signupRequest.get("password"))
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

    @Override
    @Cacheable(value = "users", key = "'all'")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "'customers'")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "'couriers'")
    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "'admins'")
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    @Cacheable(value = "users", key = "'courier_' + #id")
    public Optional<Courier> getCourierById(Long id) {
        return courierRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'customer_' + #id")
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'admin_' + #id")
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'user_' + #id")
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'stats'")
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalCouriers", courierRepository.count());
        stats.put("totalAdmins", adminRepository.count());
        return stats;
    }

    @Override
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

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteByUsername(String username) {
        logger.info("Attempting to delete user: {}", username);
        try {
            Optional<User> userOpt = findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found for deletion: {}", username);
                throw new RuntimeException("User not found: " + username);
            }

            User user = userOpt.get();
            deleteUser(user);
            logger.info("User deleted by username successfully: {}", username);
        } catch (Exception e) {
            logger.error("Error deleting user: {} - {}", username, e.getMessage(), e);
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User editUser(Long id, Map<String, String> updates) {
        User user = getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

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
            user.setPassword(updates.get("password"));
        }

        return userRepository.save(user);
    }

    @Override
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

    @Override
    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public Map<String, Object> changePassword(String username, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", username);
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!currentPassword.equals(user.getPassword())) {
                logger.warn("Invalid current password for user: {}", username);
                response.put("status", "error");
                response.put("message", "Current password is incorrect");
                return response;
            }

            validatePassword(newPassword);
            user.setPassword(newPassword);
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

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void validateUser(String username, String password) {
        User user = findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
    }

    @Override
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        // Check for at least one special character
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    @Override
    public void checkUserExists(String username) {
        logger.debug("Checking if user exists: {}", username);
        if (!existsByUsername(username)) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
    }

    @Override
    public void checkEmailExists(String email) {
        logger.debug("Checking if email exists: {}", email);
        if (!existsByEmail(email)) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
    }

    @Override
    public void deleteUser(Long id) {
        logger.debug("Deleting user with ID: {}", id);
        User user = getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        deleteUser(user);
    }

    @Override
    public UserDto getUserProfile(String username) {
        logger.debug("Getting user profile for username: {}", username);
        return findByUsername(username)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public Map<String, Object> updateProfile(String username, Map<String, String> updates) {
        logger.debug("Updating profile for user: {}", username);
        User user = findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        if (updates.containsKey("phoneNumber")) {
            user.setPhoneNumber(updates.get("phoneNumber"));
        }

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Profile updated successfully");
        response.put("user", userMapper.toDto(user));
        return response;
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException.InvalidUserDataException("Password cannot be null or empty");
        }
        if (password.length() < 8) {
            throw new AuthenticationException.InvalidUserDataException("Password must be at least 8 characters long");
        }
    }
} 