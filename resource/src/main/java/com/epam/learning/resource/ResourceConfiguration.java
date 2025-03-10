package com.epam.learning.resource;

import com.epam.learning.resource.domain.ResourceRepository;
import com.epam.learning.resource.service.ResourceService;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestClient;


@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
public class ResourceConfiguration {
	
	@Bean
	@ConfigurationProperties(prefix = "song.service")
	public ResourceService resourceService(ResourceRepository resourceRepository, RestClient.Builder restClientBuilder, DiscoveryClient discoveryClient) {
		return new ResourceService(resourceRepository, restClientBuilder, discoveryClient);
	}
}
