package com.example.courierdistributionsystem.repository;

import com.example.courierdistributionsystem.model.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
} 