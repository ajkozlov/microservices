package com.epam.learning.resource.messages;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Log4j2
public class Producer {

	@Value("${rabbitMQ.queueName}")
	private String queueName;

	private final RabbitTemplate rabbitTemplate;

	public Producer(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public void sendMessage(String message) {
		log.info("Sending message: {}", message);
		rabbitTemplate.convertAndSend(queueName, message);
		log.info("Message is sent: {}", message);
	}
}
