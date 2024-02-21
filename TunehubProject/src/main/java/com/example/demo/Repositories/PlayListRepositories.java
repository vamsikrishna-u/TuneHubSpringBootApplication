package com.example.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.Entities.Playlist;

public interface PlayListRepositories extends JpaRepository <Playlist,Integer>{
	
	public Playlist findByName(String name);

	public List<Playlist> findById(int userId);
	
	

}
