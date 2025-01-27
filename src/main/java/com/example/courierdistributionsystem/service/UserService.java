package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.model.User;
import com.example.courierdistributionsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(Map<String, String> userRequest) {
        String username = userRequest.get("username");
        String email = userRequest.get("email");
        String password = userRequest.get("password");
        String roleType = userRequest.get("role");

        if (username == null || email == null || password == null || roleType == null) {
            throw new IllegalArgumentException("Missing required fields");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(password)
                .role(User.UserRole.valueOf(roleType))
                .build();

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, Map<String, String> userRequest) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        String username = userRequest.get("username");
        String email = userRequest.get("email");
        String roleType = userRequest.get("role");

        if (username != null) {
            if (!username.equals(existingUser.getUsername()) && userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already exists");
            }
            existingUser.setUsername(username);
        }

        if (email != null) {
            if (!email.equals(existingUser.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            existingUser.setEmail(email);
        }

        if (roleType != null) {
            existingUser.setRole(User.UserRole.valueOf(roleType));
        }
        
        return userRepository.save(existingUser);
    }

    public List<User> getUsersByRole(String roleType, Pageable pageable) {
        try {
            User.UserRole role = User.UserRole.valueOf(roleType.toUpperCase());
            return userRepository.findByRoleIn(List.of(role), pageable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role type: " + roleType);
        }
    }
}
