package com.epam.learning.resource;

import com.epam.learning.resource.domain.ResourceRepository;
import com.epam.learning.resource.messages.Producer;
import com.epam.learning.resource.repository.S3Service;
import com.epam.learning.resource.repository.StorageService;
import com.epam.learning.resource.service.ResourceService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;


@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
public class ResourceConfiguration {

	@Value("${aws.credentials.access-key}")
	private String awsAccessKey;

	@Value("${aws.credentials.secret-key}")
	private String awsSecretKey;

	@Value("${aws.region}")
	private String awsRegion;

	@Value("${aws.s3.endpoint}")
	private String awsS3EndPoint;
	
	@Value("${rabbitMQ.queueName}")
	private String queueName;

	@Bean
	@ConfigurationProperties(prefix = "song.service")
	public ResourceService resourceService(ResourceRepository resourceRepository, RestClient.Builder restClientBuilder,
										   DiscoveryClient discoveryClient, S3Service s3s, Producer producer) {
		return new ResourceService(resourceRepository, restClientBuilder, discoveryClient, s3s, producer);
	}
	
	@Bean
	public S3Service s3Service(S3Client s3Client, StorageService storageService) {
		return new S3Service(s3Client, storageService);
	}

	@Bean
	public S3Client s3() {
		var credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey));
		return S3Client.builder()
					   .region(Region.of(awsRegion))
					   .credentialsProvider(credentialsProvider)
					   .endpointOverride(URI.create(awsS3EndPoint))
					   .build();
	}

	@Bean
	public Queue exampleQueue() {
		return new Queue(queueName, false);
	}
	
	@Bean
	public Producer messageProducer(RabbitTemplate rabbitTemplate) {
		return new Producer(rabbitTemplate);
	}
}
