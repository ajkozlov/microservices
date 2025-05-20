package com.epam.learning.storage;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	//	@Bean
	//	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	//		return http
	//				.csrf(ServerHttpSecurity.CsrfSpec::disable)
	//				.authorizeExchange(exchange -> exchange
	//						.pathMatchers(HttpMethod.GET, "/storage/**").hasAnyAuthority("SCOPE_read", "SCOPE_write")
	//						.pathMatchers(HttpMethod.POST, "/storage/**").hasAuthority("SCOPE_write")
	//						.pathMatchers(HttpMethod.DELETE, "/storage/**").hasAuthority("SCOPE_write")
	//						.pathMatchers("/oauth2/token").permitAll()
	//						.anyExchange().authenticated()
	//				)
	//				.oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
	//				.build();
	//	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.securityMatcher("/storage**")
			.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.GET).hasAuthority("SCOPE_storage.read"))
			.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.POST).hasAuthority("SCOPE_storage.write"))
			.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.DELETE).hasAuthority("SCOPE_storage.write"))
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}