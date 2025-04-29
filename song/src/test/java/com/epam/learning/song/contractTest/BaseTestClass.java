package com.epam.learning.song.contractTest;

import com.epam.learning.song.SongApplication;
import com.epam.learning.song.api.SongCreationResponse;
import com.epam.learning.song.api.SongDTO;
import com.epam.learning.song.domain.Song;
import com.epam.learning.song.web.SongController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static com.epam.learning.song.api.SongConverter.convertToDTO;


@SpringBootTest(classes = SongApplication.class)
public class BaseTestClass {
	@BeforeEach
	void setup() throws Exception {
		Song song = new Song(1L, "Name", "Artist", "Album", "125", 2020, 1L);
		SongDTO songDTO = new SongDTO();
		convertToDTO(songDTO, song);
		SongCreationResponse songCreationResponse = new SongCreationResponse(String.valueOf(song.getId()));
		SongController controller = new SongController() {
			@Override
			public ResponseEntity<SongDTO> getSongById(Long id) {
				return ResponseEntity.ok(songDTO);
			}
		};
		RestAssuredMockMvc.standaloneSetup(controller);
	}
}
