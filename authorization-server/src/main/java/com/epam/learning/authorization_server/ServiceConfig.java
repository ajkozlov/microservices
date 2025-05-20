package com.epam.learning.authorization_server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


@Configuration
@EnableWebSecurity
public class ServiceConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withUsername("user")
							   .password("{noop}password")
									  .roles("USER")
									  .build();
		UserDetails admin = User.withUsername("admin")
								.password("{noop}password")
								.roles("ADMIN")
								.build();

		return new InMemoryUserDetailsManager(user, admin);
	}

	@Bean
	public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

		RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

		http.securityMatcher(endpointsMatcher)
				.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
				.csrf(csrf -> csrf
						.ignoringRequestMatchers(
								endpointsMatcher,
								new AntPathRequestMatcher("/oauth2/token", "POST"),
								new AntPathRequestMatcher("/oauth2/token", "OPTIONS")
						)
				)
				.with(authorizationServerConfigurer, Customizer.withDefaults());

		return http.build();
	}
}
