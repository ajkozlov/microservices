package com.epam.lerning.song.web;

import com.epam.lerning.song.api.SongExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.stream.Collectors;


@RestControllerAdvice
@Log4j2
public class SongExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<SongExceptionResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
		log.error(ex);
		return ResponseEntity.status(500).body(new SongExceptionResponse(1001, "An unexpected error occurred"));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<SongExceptionResponse> handleResourceNotFoundException(ResponseStatusException ex, HttpServletRequest request) {
		log.error(ex);
		return ResponseEntity.badRequest().body(new SongExceptionResponse(ex.getStatusCode().value(), ex.getReason()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<SongExceptionResponse> handleNotValidException(MethodArgumentNotValidException ex,
																		 HttpServletRequest request) {
		log.error(ex);
		return ResponseEntity.badRequest()
							 .body(new SongExceptionResponse(ex.getStatusCode().value(), ex.getAllErrors().stream().map(
									 DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "))));
	}
}