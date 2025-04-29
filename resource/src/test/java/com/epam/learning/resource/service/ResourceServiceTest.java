package com.epam.learning.resource.service;

import com.epam.learning.resource.domain.Resource;
import com.epam.learning.resource.domain.ResourceRepository;
import com.epam.learning.resource.messages.Producer;
import com.epam.learning.resource.repository.S3Service;
import com.epam.learning.resource.repository.StorageType;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceServiceTest {

	@Mock
	private ResourceRepository repository;

	@Mock
	private RestClient restClient;

	@Mock
	private RestClient.Builder restClientBuilder;
	
	@Mock
	private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@Mock
	private RestClient.ResponseSpec responseSpec;

	@Mock
	private DiscoveryClient discoveryClient;

	@Mock
	private S3Service s3Service;

	@Mock
	private Producer producer;

	private ResourceService resourceService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(restClientBuilder.build()).thenReturn(restClient);
		resourceService = new ResourceService(repository, restClientBuilder, discoveryClient, s3Service, producer);
	}

	@Test
	void crateResource_createsResourceSuccessfully() throws IOException, TikaException, SAXException {
		byte[] file = "test file".getBytes();
		Resource resource = new Resource("hash");
		doNothing().when(s3Service).saveToStage(anyString(), any(byte[].class));
		when(repository.save(any(Resource.class))).thenReturn(resource);

		Long resourceId = resourceService.createResource(file);

		verify(repository, times(1)).save(any(Resource.class));
		verify(producer, times(1)).sendMessage(anyString());
	}

	@Test
	void getResource_returnsResourceSuccessfully() throws IOException {
		String key = "testKey";
		byte[] file = "test file".getBytes();
		when(s3Service.download(key, StorageType.STAGING)).thenReturn(file);

		byte[] result = resourceService.getResource(key, StorageType.STAGING);

		assertArrayEquals(file, result);
	}

	@Test
	void getResource_throwsRuntimeExceptionOnIOException() throws IOException {
		String key = "testKey";
		when(s3Service.download(key, StorageType.STAGING)).thenThrow(new IOException());

		assertThrows(RuntimeException.class, () -> resourceService.getResource(key, StorageType.STAGING));
	}

	@Test
	void deleteResources_deletesResourcesSuccessfully() {
		String ids = "1,2,3";
		when(repository.existsById(anyLong())).thenReturn(true);
		when(discoveryClient.getInstances("song")).thenReturn(Collections.singletonList(mock(ServiceInstance.class)));
		when(restClient.delete()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
		when(responseSpec.toBodilessEntity()).thenReturn(null);

		List<Long> result = resourceService.deleteResources(ids);

		assertEquals(Arrays.asList(1L, 2L, 3L), result);
		verify(repository, times(3)).deleteById(anyLong());
	}

	@Test
	void deleteResources_throwsBadRequestOnInvalidIds() {
		String ids = "1,abc,3";

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> resourceService.deleteResources(ids));

		assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode().value());
	}

	@Test
	void findById_returnsResourceSuccessfully() {
		Long id = 1L;
		Resource resource = new Resource("hash");
		when(repository.findById(id)).thenReturn(Optional.of(resource));

		Resource result = resourceService.findById(id);

		assertEquals(resource, result);
	}

	@Test
	void findById_throwsNotFoundOnInvalidId() {
		Long id = 1L;
		when(repository.findById(id)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> resourceService.findById(id));

		assertEquals(HttpStatus.NOT_FOUND.value(), exception.getStatusCode().value());
	}
}