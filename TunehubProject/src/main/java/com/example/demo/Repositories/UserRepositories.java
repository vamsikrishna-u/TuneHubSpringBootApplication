package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entities.Songs;
import com.example.demo.Entities.Users;

public interface UserRepositories extends JpaRepository<Users, Integer> {
    Users findByEmailId(String emailId);
}




