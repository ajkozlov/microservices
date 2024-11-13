package com.epam.lerning.song.api;

import com.epam.lerning.song.domain.Song;


public class SongConverter {
	public static void convertToEntity(SongDTO dto, Song song) {
		song.setId(dto.getId());
		song.setYear(Integer.parseInt(dto.getYear()));
		song.setLength(dto.getYear());
		song.setName(dto.getName());
		song.setArtist(dto.getArtist());
		song.setAlbum(dto.getAlbum());
		song.setResourceId(dto.getResourceId());
	}

	public static void convertToDTO(SongDTO dto, Song song) {
		dto.setId(song.getId());
		dto.setYear(String.valueOf(song.getYear()));
		dto.setDuration(song.getLength());
		dto.setName(song.getName());
		dto.setArtist(song.getArtist());
		dto.setAlbum(song.getAlbum());
		dto.setResourceId(song.getResourceId());
	}
}
