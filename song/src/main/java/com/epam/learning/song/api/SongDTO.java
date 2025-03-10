package com.epam.learning.song.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class SongDTO {
	private Long id;
	@NotBlank(message = "Name must not be blank")
	private String name;
	private String artist;
	private String album;
	@Pattern(regexp = "[0-5][0-9]:[0-5][0-9]", message = "Duration must be in the format MM:SS")
	private String duration;
	@Pattern(regexp = "[0-9]{4}", message = "Year must be in a YYYY format")
	private String year;
	private Long resourceId;
}
