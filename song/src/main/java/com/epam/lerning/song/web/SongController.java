package com.epam.lerning.song.web;

import com.epam.lerning.song.api.DeleteResponse;
import com.epam.lerning.song.api.SongCreationResponse;
import com.epam.lerning.song.api.SongDTO;
import com.epam.lerning.song.domain.Song;
import com.epam.lerning.song.service.SongService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/songs")
public class SongController {
	@Autowired
	private SongService songService;

	@GetMapping
	public List<Song> getAllSongs() {
		return songService.getAllSongs();
	}

	@GetMapping(value = "/{id}", produces = "application/json")
	public ResponseEntity<SongDTO> getSongById(@PathVariable Long id) {
		SongDTO song = songService.getSongById(id);
		return ResponseEntity.ok(song);
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	public SongCreationResponse createSong(@Valid @RequestBody SongDTO song) {
		return songService.saveSong(song);
	}

	@DeleteMapping(produces = "application/json")
	public ResponseEntity<DeleteResponse> deleteSong(@RequestParam("id") String id) {
		List<Long> idList = songService.deleteSongs(id);
		return ResponseEntity.ok(new DeleteResponse(idList));
	}
	
	@DeleteMapping(value = "/by-resource/{id}", produces = "application/json")
	public ResponseEntity<DeleteResponse> deleteSongByResource(@PathVariable("id") Long id) {
		List<Long> idList = songService.deleteSongsByResource(id);
		return ResponseEntity.ok(new DeleteResponse(idList));
	}
}
