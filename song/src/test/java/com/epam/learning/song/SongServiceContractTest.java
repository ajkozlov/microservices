package com.epam.learning.song;

import com.epam.learning.song.api.SongCreationResponse;
import com.epam.learning.song.api.SongDTO;
import com.epam.learning.song.domain.Song;
import com.epam.learning.song.service.SongService;
import com.epam.learning.song.web.SongController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.epam.learning.song.api.SongConverter.convertToDTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(SongController.class)
class SongServiceContractTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SongService songService;

	@BeforeEach
	void setup() throws Exception {
		Song song = new Song(1L, "Name", "Artist", "Album", "125", 2020, 1L);
		SongDTO songDTO = new SongDTO(); 
		convertToDTO(songDTO, song);
		SongCreationResponse songCreationResponse = new SongCreationResponse(String.valueOf(song.getId()));

		when(songService.getSongById(1L)).thenReturn(songDTO);
		when(songService.saveSong(any(SongDTO.class))).thenReturn(songCreationResponse);
		when(songService.deleteSongs("1,2")).thenReturn(List.of(1L, 2L));
	}

	@Test
	void getSongByIdContractValidatesResponseStructure() throws Exception {
		mockMvc.perform(get("/songs/1")
								.accept(MediaType.APPLICATION_JSON))

			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))

			   .andExpect(jsonPath("$.name").value("Name"))
			   .andExpect(jsonPath("$.artist").value("Artist"))
			   .andExpect(jsonPath("$.album").value("Album"))
			   .andExpect(jsonPath("$.year").value(2020));
	}

	@Test
	void saveSongContractValidatesResponseStructure() throws Exception {
		String requestBody = """
                {
                    "name": "Name",
                    "artist": "Artist",
                    "album": "Album",
                    "year": 2020
                }
                """;

		mockMvc.perform(post("/songs")
								.contentType(MediaType.APPLICATION_JSON)
								.content(requestBody))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void deleteSongsContractValidatesResponseStructure() throws Exception {
		mockMvc.perform(delete("/songs")
								.param("id", "1,2")
								.accept(MediaType.APPLICATION_JSON))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(jsonPath("$.ids[0]").value(1))
			   .andExpect(jsonPath("$.ids[1]").value(2));
	}
}