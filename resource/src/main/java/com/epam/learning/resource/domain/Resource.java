package com.epam.learning.resource.domain;

import com.epam.learning.resource.repository.StorageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.epam.learning.resource.repository.StorageType.STAGING;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
	
	public Resource(String mp3){
		this(mp3, STAGING);
	}
	public Resource(String mp3, StorageType storageType){
		this.mp3 = mp3;
		this.storageType = storageType;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String mp3;
	
	@Column
	private StorageType storageType;
}
