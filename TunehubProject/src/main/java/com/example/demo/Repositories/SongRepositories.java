package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entities.Songs;

public interface SongRepositories extends JpaRepository<Songs,Integer> {
	Songs findByName(String name);

	

}
