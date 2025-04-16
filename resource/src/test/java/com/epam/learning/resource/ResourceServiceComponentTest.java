package com.epam.learning.resource;

import com.epam.learning.resource.domain.Resource;
import com.epam.learning.resource.service.ResourceService;
import com.epam.learning.resource.domain.ResourceRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testcontainers.utility.DockerImageName.parse;


@SpringBootTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class ResourceServiceComponentTest {

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private DiscoveryClient discoveryClient;

	@Container
	static LocalStackContainer localstack =
			new LocalStackContainer(parse("localstack/localstack"))
					.withServices(LocalStackContainer.Service.S3)
					.withCopyFileToContainer(MountableFile.forClasspathResource("init-aws.sh"), "/etc/localstack/init/ready.d/")
					.withExposedPorts(4566);

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management")
			.withExposedPorts(5672, 15672);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("resources")
			.withUsername("resource")
			.withPassword("resource");

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("aws.s3.endpoint", localstack::getEndpoint);
	}

	@BeforeAll
	static void initContainers() {
		System.setProperty("rabbitmq.host", "localhost");
		System.setProperty("rabbitmq.port", String.valueOf(rabbitmq.getHttpPort()));
		System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
	}

	@Test
	public void rabbitmq_shouldBeUp() {
		assertTrue(rabbitmq.isRunning());
	}

	@Test
	@Order(1)
	void createResourceShouldStoreResourceAndSendMessage() throws Exception {
		byte[] mp3Data = getTestMp3Bytes();

		Long resourceId = resourceService.createResource(mp3Data);

		assertThat(resourceId).isNotNull();

		Resource persisted = resourceRepository.findById(resourceId).orElseThrow();
		assertNotNull(persisted.getMp3());
	}

	@Test
	@Order(2)
	void deleteResourceShouldRemoveFilesAndDatabaseEntries() throws Exception {
		Resource resource = resourceRepository.findAll().iterator().next();
		Long id = resource.getId();

		ServiceInstance eurekaServiceInstance = mock(EurekaServiceInstance.class);
		when(eurekaServiceInstance.getUri()).thenReturn(URI.create("http://localhost:8080"));
		when(discoveryClient.getInstances("song")).thenReturn(List.of(eurekaServiceInstance));

		List<Long> deletedIds = resourceService.deleteResources(id.toString());

		assertThat(deletedIds).contains(id);
		assertThat(resourceRepository.findById(id)).isEmpty();
	}

	private byte[] getTestMp3Bytes() throws IOException {
		return new ClassPathResource("sample3.mp3").getInputStream().readAllBytes();
	}
}
