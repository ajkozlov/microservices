package com.epam.learning.resource.service;

import com.epam.learning.resource.domain.Resource;
import com.epam.learning.resource.domain.ResourceRepository;
import com.epam.learning.resource.messages.Producer;
import com.epam.learning.resource.repository.S3Service;
import com.epam.learning.resource.repository.StorageType;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.apache.tika.exception.TikaException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Log4j2
public class ResourceService {

	private final ResourceRepository repository;
	private final RestClient restClient;
	private final DiscoveryClient discoveryClient;
	private final S3Service s3Service;
	private final Producer producer;

	public ResourceService(ResourceRepository repository, RestClient.Builder restClientBuilder,
						   DiscoveryClient discoveryClient, S3Service s3Service, Producer producer) {
		this.repository = repository;
		this.discoveryClient = discoveryClient;
		this.restClient = restClientBuilder.build();
		this.s3Service = s3Service;
		this.producer = producer;
	}

	public Long createResource(byte[] file) throws IOException, TikaException, SAXException {
		Resource resource = new Resource(saveToCloud(file));
		log.info("Created: ({})", resource.getMp3());
		repository.save(resource);
		producer.sendMessage(String.valueOf(resource.getId()));
		return resource.getId();
	}
	
	public byte[] getResource(String key, StorageType storageType) {
		try {
			log.info("Getting resource: ({}), storageType ({})", key, storageType);
			return s3Service.download(key, storageType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String saveToCloud(byte[] file) {
		String hash = SHAsum(file);
		try {
			s3Service.saveToStage(hash, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return hash;
	}
	
	public void resourceProcessed(Long id) {
		log.info("Resource processed: ({})", id);
		Resource resource = repository.findById(id).orElseThrow();
		try {
			log.info("Moving resource to permanent storage: ({})", resource.getId());
			s3Service.moveToPermanent(resource.getMp3());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		resource.setStorageType(StorageType.PERMANENT);
		repository.save(resource);
	}

	private static String SHAsum(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return byteArray2Hex(md.digest(convertme));
	}

	private static String byteArray2Hex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public List<Long> deleteResources(String ids) {
		List<Long> idList;
		if (ids.length() > 200) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV string is too long: received " + ids.length()
					+ " characters, maximum allowed length is 200 characters");
		}
		try {
			idList = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id should be numbers");
		}
		return idList.stream()
					 .filter(repository::existsById)
					 .peek(repository::deleteById)
					 .peek(this::deleteMetadata)
					 .collect(Collectors.toList());
	}
	
	private void deleteMetadata(Long id) {
		ServiceInstance serviceInstance = discoveryClient.getInstances("song").get(0);
		restClient.delete()
				  .uri(serviceInstance.getUri()+"/songs/by-resource/"+id)
				  .retrieve()
				  .onStatus(status -> status.value() == 500, (request, response) -> {
					  throw new ResponseStatusException(response.getStatusCode(), response.toString());
				  })
				  .toBodilessEntity();
	}

	public Resource findById(Long id) {
		if (id < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Id");
		}
		Optional<Resource> resource = repository.findById(id);
		return resource.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource has not found"));
	}
}
