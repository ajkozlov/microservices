package com.epam.learning.resource;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testcontainers.utility.DockerImageName.parse;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceServiceE2ETest {
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("resources")
			.withUsername("resource")
			.withPassword("resource");

	@Container
	static LocalStackContainer localstack =
			new LocalStackContainer(parse("localstack/localstack"))
					.withServices(LocalStackContainer.Service.S3)
					.withCopyFileToContainer(MountableFile.forClasspathResource("init-aws.sh"), "/etc/localstack/init/ready.d/")
					.withExposedPorts(4566);


	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq")
			.withExposedPorts(5672, 15672);

	@BeforeAll
	static void initContainers() {
		System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
	}

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("aws.s3.endpoint", localstack::getEndpoint);
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(2)
	void testUploadAndSaveFileLocation() throws Exception {
		byte[] fileContent = getTestMp3Bytes();

		mockMvc.perform(post("/resources")
								.content(fileContent)
								.contentType(MediaType.valueOf("audio/mpeg"))
								.header("Content-Type", MediaType.valueOf("audio/mpeg")))

			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.id").value(1L));
	}

	@Test
	@Order(3)
	void testGetResourceBytes() throws Exception {
		byte[] expectedContent = getTestMp3Bytes();

		mockMvc.perform(post("/resources")
								.content(expectedContent)
								.contentType("audio/mpeg"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.id").exists())
			   .andDo(result -> {
				   String responseBody = result.getResponse().getContentAsString();
				   Integer id = JsonPath.read(responseBody, "$.id");

				   byte[] actualContent = mockMvc.perform(MockMvcRequestBuilders.get("/resources/" + id))
												 .andExpect(status().isOk())
												 .andExpect(content().contentType("audio/mpeg"))
												 .andReturn()
												 .getResponse()
												 .getContentAsByteArray();

				   assertThat(actualContent).isEqualTo(expectedContent);
			   });
	}

	@Test
	@Order(4)
	void testDeleteResource() throws Exception {
		byte[] expectedContent = getTestMp3Bytes();
		List<Integer> createdIds = new ArrayList<>();

		mockMvc.perform(post("/resources")
								.content(expectedContent)
								.contentType("audio/mpeg"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.id").exists())
			   .andDo(result -> {
				   String responseBody = result.getResponse().getContentAsString();
				   createdIds.add(JsonPath.read(responseBody, "$.id"));
			   });

		mockMvc.perform(post("/resources")
								.content(expectedContent)
								.contentType("audio/mpeg"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.id").exists())
			   .andDo(result -> {
				   String responseBody = result.getResponse().getContentAsString();
				   createdIds.add(JsonPath.read(responseBody, "$.id"));
			   });


		mockMvc.perform(delete("/resources")
								.param("id", "1,2"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(jsonPath("$.ids[0]").value(createdIds.get(0)))
			   .andExpect(jsonPath("$.ids[1]").value(createdIds.get(1)));
	}

	@Test
	@Order(5)
	void testGetResourceNotFound() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/resources/1"))
			   .andExpect(status().isNotFound());
	}



	private byte[] getTestMp3Bytes() throws IOException {
		return new ClassPathResource("sample3.mp3").getInputStream().readAllBytes();
	}
}
