package com.epam.lerning.resource.web;

import com.epam.lerning.resource.api.DeleteResponse;
import com.epam.lerning.resource.api.ResourceResponse;
import com.epam.lerning.resource.domain.Resource;
import com.epam.lerning.resource.service.ResourceService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/resources")
public class ResourceController {
	
	@Autowired
	private ResourceService resourceService;

	@GetMapping(value = "/{id}", produces = "audio/mpeg")
	public ResponseEntity<byte[]> getResource(@PathVariable Long id) {
		Resource resource = resourceService.findById(id);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Disposition", "attachment; filename=\"" + id +".mp3\"");
		return new ResponseEntity<>(resource.getMp3(), headers, HttpStatus.OK);
	}

	@PostMapping(consumes = "audio/mpeg")
	public ResponseEntity<ResourceResponse> createResource(@RequestBody byte[] file) throws FileNotFoundException {
		Long id;
		try {
			id = resourceService.crateResource(file);
		} catch (TikaException | IOException | SAXException e) {
			throw new RuntimeException(e);
		}
		return ResponseEntity.ok(new ResourceResponse(id));
	} 
	
	@DeleteMapping(produces = "application/json")
	public ResponseEntity<DeleteResponse> deleteResources(@RequestParam("id") String id) {
		List<Long> idList = resourceService.deleteResources(id);
		DeleteResponse deleteResponse = new DeleteResponse(idList);
		return ResponseEntity.ok(deleteResponse);
	}
}
