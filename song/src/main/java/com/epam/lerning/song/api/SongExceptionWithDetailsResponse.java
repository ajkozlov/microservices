package com.epam.lerning.song.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class SongExceptionWithDetailsResponse extends SongExceptionResponse {
	private Map<String, String> details;

	public SongExceptionWithDetailsResponse(@NonNull Integer errorCode, @NonNull String errorMessage, Map<String, String> details) {
		super(errorCode, errorMessage);
		this.details = details;
	}
}
