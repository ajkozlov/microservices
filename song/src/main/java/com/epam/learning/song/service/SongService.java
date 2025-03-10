package com.epam.learning.song.service;

import com.epam.learning.song.api.SongConverter;
import com.epam.learning.song.api.SongCreationResponse;
import com.epam.learning.song.api.SongDTO;
import com.epam.learning.song.domain.Song;
import com.epam.learning.song.domain.SongRepository;
import jakarta.transaction.Transactional;
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
		if (songDTO.getId() != null && songRepository.existsById(songDTO.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Song with id "+songDTO.getId()+" already exists");
		}
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

	@Transactional
	public List<Long> deleteSongsByResource(Long id) {
		return List.of(songRepository.deleteByResourceId(id));
	}
}
