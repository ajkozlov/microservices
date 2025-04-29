package com.epam.learning.storage.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;


@Data
@AllArgsConstructor
public class StorageExceptionResponse {
	
	@NonNull
	private Integer errorCode;
	@NonNull
	private String errorMessage;
}
