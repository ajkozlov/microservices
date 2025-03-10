package com.epam.learning.song.web;

import com.epam.learning.song.api.DeleteResponse;
import com.epam.learning.song.api.SongCreationResponse;
import com.epam.learning.song.api.SongDTO;
import com.epam.learning.song.domain.Song;
import com.epam.learning.song.service.SongService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
