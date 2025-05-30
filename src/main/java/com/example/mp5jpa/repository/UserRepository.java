package com.example.mp5jpa.repository;

import com.example.mp5jpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}