package com.example.demo.Services;

import java.util.List;

import com.example.demo.Entities.Songs;

public interface SongServices {

	public String addSong(Songs song);

	public List<Songs> fetchSongs();

	public boolean SongExist(String name);

	public void updateSong(Songs song);

}
