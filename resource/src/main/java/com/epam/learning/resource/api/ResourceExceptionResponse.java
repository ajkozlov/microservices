package com.epam.learning.resource.api;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ResourceExceptionResponse {
	
	private int errorCode;
	private String errorMessage;
}
