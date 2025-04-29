package com.epam.learning.storage.api;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class StorageExceptionWithDetailsResponse extends StorageExceptionResponse {
	private Map<String, String> details;

	public StorageExceptionWithDetailsResponse(@NonNull Integer errorCode, @NonNull String errorMessage, Map<String, String> details) {
		super(errorCode, errorMessage);
		this.details = details;
	}
}
