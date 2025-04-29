package com.epam.learning.storage.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class StorageDTO {
	private Long id;
	@NotBlank(message = "Type must not be blank")
	private String storageType;
	private String bucket;
	private String path;
}
