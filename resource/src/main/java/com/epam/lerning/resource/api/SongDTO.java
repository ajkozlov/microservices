package com.epam.lerning.resource.api;

import lombok.Data;


@Data
public class SongDTO {
	private String name;
	private String artist;
	private String album;
	private String duration;
	private String year;
	private Long resourceId;
}
