package com.epam.learning.resource.repository;

import lombok.extern.log4j.Log4j2;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;


@Log4j2
@Service
public class StorageService {
	private final DiscoveryClient discoveryClient;
	private final WebClient webClient;

	@Autowired
	public StorageService(DiscoveryClient discoveryClient, WebClient webClient) {
		this.discoveryClient = discoveryClient;
		this.webClient = webClient;
	}

	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
	@CircuitBreaker(name = "storageServiceCircuitBreaker", fallbackMethod = "getStorageFallback")
	public StorageDTO getStorage(StorageType storageType) {
		ServiceInstance serviceInstance = discoveryClient.getInstances("storage").get(0);
		log.info("Calling storage service at: {}", serviceInstance.getUri() + "/storages");
		StoragesDTO storagesDTO = webClient.get().uri(serviceInstance.getUri() + "/storages")
										   .retrieve()
										   .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals,
													 response -> {
														 throw new ResponseStatusException(response.statusCode(),
																						   response.toString());
													 }).bodyToMono(StoragesDTO.class).block();
		log.info("Success call to storage service for storage type: {}", storageType);
		return storagesDTO.getStorages()
						  .stream()
						  .filter(storage -> storage.getStorageType().equals(storageType.toString()))
						  .findFirst()
						  .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Storage not found"));
	}
	
	public StorageDTO getStorageFallback(StorageType storageType, Exception e) {
		log.error("Error calling storage service: {}", e.getMessage());
		StorageDTO storageDTO = new StorageDTO();
		storageDTO.setStorageType(storageType.toString());
		storageDTO.setBucket("fallback-bucket");
		return storageDTO;
	}
}
