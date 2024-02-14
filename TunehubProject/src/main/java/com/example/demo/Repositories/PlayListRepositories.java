package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entities.Playlist;

public interface PlayListRepositories extends JpaRepository <Playlist,Integer>{
	
	public Playlist findByName(String name);
	
	

}
