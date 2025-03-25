package com.epam.learning.resource_processor.messages;

import com.epam.learning.resource_processor.service.ProcessorService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;


@Log4j2
@Component
public class Receiver {
	
	private final ProcessorService processorService;

	private CountDownLatch latch = new CountDownLatch(1);

	public Receiver(ProcessorService processorService) {
		this.processorService = processorService;
	}

	public void receiveMessage(String message) throws InterruptedException {
		log.info("Received <{}>", message);
		try {
			processorService.processResource(Long.parseLong(message));
		} catch (Exception e) {
			log.error(e);
			log.warn("sleep");
			Thread.sleep(1000);
		}
		latch.countDown();
	}

}
