package com.epam.lerning.song.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;


@Data
@AllArgsConstructor
public class SongExceptionResponse {
	
	@NonNull
	private Integer errorCode;
	@NonNull
	private String errorMessage;
}
