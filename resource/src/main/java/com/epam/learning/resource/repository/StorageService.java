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
import org.springframework.web.server.ResponseStatusException;


@Log4j2
@Service
public class StorageService {
	private final DiscoveryClient discoveryClient;
	private final RestClient restClient;

	@Autowired
	public StorageService(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
		this.discoveryClient = discoveryClient;
		this.restClient = restClientBuilder.build();
	}

	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000), retryFor = Exception.class)
	@CircuitBreaker(name = "storageServiceCircuitBreaker", fallbackMethod = "getStorageFallback")
	public StorageDTO getStorage(StorageType storageType) {
		ServiceInstance serviceInstance = discoveryClient.getInstances("storage").get(0);
		log.info("Calling storage service at: {}", serviceInstance.getUri()+"/storages");
		StoragesDTO storagesDTO = restClient.get().uri(serviceInstance.getUri()+"/storages")
												 .retrieve()
												 .onStatus(status -> status.value() == 500, (request, response) -> {
													throw new ResponseStatusException(response.getStatusCode(), response.toString());
												}).body(StoragesDTO.class);
		log.info("Success call to storage service for storage type: {}", storageType);
		return storagesDTO.getStorages().stream().filter(storage -> storage.getStorageType().equals(storageType.toString())).findFirst()
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
