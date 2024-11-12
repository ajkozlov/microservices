package com.epam.lerning.song.api;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class SongExceptionResponse {
	
	private int errorCode;
	private String errorMessage;
}
