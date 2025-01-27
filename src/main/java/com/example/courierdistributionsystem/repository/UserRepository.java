package com.example.courierdistributionsystem.repository;
import org.springframework.data.domain.Pageable;
import com.example.courierdistributionsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRoleIn(Collection<User.UserRole> roles, Pageable pageable);
}
