package com.epam.learning.resource.repository;

import lombok.Data;


@Data
public class StorageDTO {
	private Long id;
	private String storageType;
	private String bucket;
	private String path;
}
