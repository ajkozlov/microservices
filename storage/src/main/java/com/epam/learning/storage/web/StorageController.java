package com.epam.learning.storage.web;

import com.epam.learning.storage.api.DeleteResponse;
import com.epam.learning.storage.api.StorageCreationResponse;
import com.epam.learning.storage.api.StorageDTO;
import com.epam.learning.storage.api.StoragesDTO;
import com.epam.learning.storage.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/storages")
public class StorageController {
	@Autowired
	private StorageService storageService;

	@GetMapping(produces = "application/json")
	public ResponseEntity<StoragesDTO> getStorages() {
		StoragesDTO storagesDTO = new StoragesDTO();
		storagesDTO.setStorages(storageService.getStorages());
		return ResponseEntity.ok(storagesDTO);
	}

	@PostMapping(consumes = "application/json", produces = "application/json")
	public StorageCreationResponse createStorage(@Valid @RequestBody StorageDTO storage) {
		return storageService.saveStorage(storage);
	}

	@DeleteMapping(produces = "application/json")
	public ResponseEntity<DeleteResponse> deleteStorage(@RequestParam("id") String id) {
		List<Long> idList = storageService.deleteStorages(id);
		return ResponseEntity.ok(new DeleteResponse(idList));
	}
}
