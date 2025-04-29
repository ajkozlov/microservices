package com.epam.learning.storage.api;

import com.epam.learning.storage.domain.Storage;


public class StorageConverter {
	public static void convertToEntity(StorageDTO dto, Storage storage) {
		storage.setId(dto.getId());
		storage.setStorageType(dto.getStorageType());
		storage.setBucket(dto.getBucket());
		storage.setPath(dto.getPath());
	}

	public static void convertToDTO(StorageDTO dto, Storage storage) {
		dto.setId(storage.getId());
		dto.setStorageType(storage.getStorageType());
		dto.setBucket(storage.getBucket());
		dto.setPath(storage.getPath());
	}
}
