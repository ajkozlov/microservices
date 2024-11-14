package com.epam.lerning.resource.web;

import com.epam.lerning.resource.api.ResourceExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.HttpMediaTypeNotSupportedException;


@RestControllerAdvice
@Log4j2
public class ResourceExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResourceExceptionResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
		log.error(ex);
		log.error(ex.getStackTrace());
		return ResponseEntity.status(500).body(new ResourceExceptionResponse(1001, "An unexpected error occurred"));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ResourceExceptionResponse> handleResourceNotFoundException(ResponseStatusException ex,
																					 HttpServletRequest request) {
		log.info(ex);
		return ResponseEntity.status(ex.getStatusCode()).body(new ResourceExceptionResponse(ex.getStatusCode().value(), ex.getReason()));
	}
	
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ResourceExceptionResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex,
																					 HttpServletRequest request) {
		log.info(ex);
		return ResponseEntity.status(ex.getStatusCode()).body(new ResourceExceptionResponse(ex.getStatusCode().value(), ex.getMessage()));
	}
	
	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<ResourceExceptionResponse> handleHttpClientErrorException(HttpClientErrorException ex,
																						  HttpServletRequest request) {
		log.info(ex);
		return ResponseEntity.status(ex.getStatusCode()).body(new ResourceExceptionResponse(ex.getStatusCode().value(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResourceExceptionResponse> handleArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
																					HttpServletRequest request) {
		log.info(ex);
		return ResponseEntity.badRequest().body(new ResourceExceptionResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}
}
