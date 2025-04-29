package com.epam.learning.storage.service;

import com.epam.learning.storage.api.StorageConverter;
import com.epam.learning.storage.api.StorageCreationResponse;
import com.epam.learning.storage.api.StorageDTO;
import com.epam.learning.storage.domain.Storage;
import com.epam.learning.storage.domain.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class StorageService {
	@Autowired
	private StorageRepository storageRepository;

	public List<StorageDTO> getStorages() {
		List<Storage> storages = storageRepository.findAll();
		return storages.stream().map(s -> {
			StorageDTO storageDTO = new StorageDTO();
			StorageConverter.convertToDTO(storageDTO, s);
			return storageDTO;
		}).toList();
	}

	public StorageCreationResponse saveStorage(StorageDTO storageDTO) {
		if (storageDTO.getId() != null && storageRepository.existsById(storageDTO.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Storage with id "+storageDTO.getId()+" already exists");
		}
		Storage storage = new Storage();
		StorageConverter.convertToEntity(storageDTO, storage);
		storageRepository.save(storage);
		return new StorageCreationResponse(String.valueOf(storage.getId()));
	}

	public List<Long> deleteStorages(String id) {
		List<Long> idList;
		if (id.length() > 200) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV string is too long: received "+id.length()+" characters, maximum allowed length is 200 characters");
		}
		try {
			idList = Arrays.stream(id.split(",")).map(Long::valueOf).collect(Collectors.toList());
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id should be numbers");
		}
		storageRepository.deleteAllById(idList);
		return idList;
	}
}
