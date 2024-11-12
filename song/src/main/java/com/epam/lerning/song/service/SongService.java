package com.epam.lerning.song.service;

import com.epam.lerning.song.api.SongConverter;
import com.epam.lerning.song.api.SongCreationResponse;
import com.epam.lerning.song.api.SongDTO;
import com.epam.lerning.song.domain.Song;
import com.epam.lerning.song.domain.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SongService {
	@Autowired
	private SongRepository songRepository;

	public List<Song> getAllSongs() {
		return songRepository.findAll();
	}

	public SongDTO getSongById(Long id) {
		Optional<Song> song = songRepository.findById(id);
		SongDTO songDTO = new SongDTO();
		SongConverter.convertToDTO(songDTO, song.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource has not found")));
		return songDTO;
	}

	public SongCreationResponse saveSong(SongDTO songDTO) {
		Song song = new Song();
		SongConverter.convertToEntity(songDTO, song);
		songRepository.save(song);
		return new SongCreationResponse(String.valueOf(song.getId()));
	}

	public List<Long> deleteSongs(String id) {
		List<Long> idList;
		if (id.length() > 200) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV string is too long: received "+id.length()+" characters, maximum allowed length is 200 characters");
		}
		try {
			idList = Arrays.stream(id.split(",")).map(Long::valueOf).collect(Collectors.toList());
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id should be numbers");
		}
		songRepository.deleteAllById(idList);
		return idList;
	}
}