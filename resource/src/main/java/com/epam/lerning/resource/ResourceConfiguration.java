package com.epam.lerning.resource;

import com.epam.lerning.resource.domain.ResourceRepository;
import com.epam.lerning.resource.service.ResourceService;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${song.service.url}") 
	private String songUrl;
	
	@Bean
	@ConfigurationProperties(prefix = "song.service")
	public ResourceService resourceService(ResourceRepository resourceRepository, RestClient.Builder restClientBuilder) {
		return new ResourceService(resourceRepository, restClientBuilder, songUrl);
	}
}