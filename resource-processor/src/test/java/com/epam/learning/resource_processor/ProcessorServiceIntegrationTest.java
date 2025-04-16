package com.epam.learning.resource_processor;

import com.epam.learning.resource_processor.api.SongDTO;
import com.epam.learning.resource_processor.service.ProcessorService;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


@SpringBootTest
class ProcessorServiceIntegrationTest {

	@Autowired
	private ProcessorService processorService;

	@MockBean
	private RestClient restClient;

	@MockBean
	private DiscoveryClient discoveryClient;
	
	@BeforeEach
	void setUp() {
		
	}

	@Test
	void processResource_processesResourceSuccessfully() throws IOException, TikaException, SAXException {
		long id = 1L;
		byte[] file = "test file".getBytes();
		ServiceInstance resourceServiceInstance = mock(ServiceInstance.class);
		ServiceInstance songServiceInstance = mock(ServiceInstance.class);
		RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
		RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
		RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

		when(discoveryClient.getInstances("resource")).thenReturn(Collections.singletonList(resourceServiceInstance));
		when(resourceServiceInstance.getUri()).thenReturn(URI.create("http://localhost"));
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(anyString(), anyLong())).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
		when(responseSpec.toEntity(byte[].class)).thenReturn(ResponseEntity.ok(file));
		when(discoveryClient.getInstances("song")).thenReturn(Collections.singletonList(songServiceInstance));
		when(songServiceInstance.getUri()).thenReturn(URI.create("http://localhost"));
		when(restClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
		when(requestBodyUriSpec.body(any(SongDTO.class))).thenReturn(requestBodyUriSpec);
		when(responseSpec.toBodilessEntity()).thenReturn(null);

		processorService.processResource(id);

		verify(requestHeadersUriSpec).uri("http://localhost/resources/{id}", id);
		verify(responseSpec, times(2)).onStatus(any(), any());
		verify(responseSpec).toEntity(byte[].class);
		verify(requestBodyUriSpec).uri("http://localhost/songs");
		verify(requestBodyUriSpec).contentType(APPLICATION_JSON);
		verify(requestBodyUriSpec).body(any(SongDTO.class));
		verify(responseSpec).toBodilessEntity();
	}

	@Test
	void processResource_throwsExceptionOn500Status() {
		long id = 1L;
		ServiceInstance serviceInstance = mock(ServiceInstance.class);
		RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
		RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

		when(discoveryClient.getInstances("resource")).thenReturn(Collections.singletonList(serviceInstance));
		when(serviceInstance.getUri()).thenReturn(URI.create("http://localhost"));
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(anyString(), anyLong())).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.onStatus(any(), any())).thenAnswer(invocation -> {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
		});

		assertThrows(ResponseStatusException.class, () -> processorService.processResource(id));

		verify(requestHeadersUriSpec).uri("http://localhost/resources/{id}", id);
		verify(responseSpec).onStatus(any(), any());
	}
}