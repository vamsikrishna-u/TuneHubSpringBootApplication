package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.Users;

public interface UserRepositories extends JpaRepository<Users, Integer> {
	@Transactional(readOnly = true)
    Users findByEmailId(String emailId);
}




