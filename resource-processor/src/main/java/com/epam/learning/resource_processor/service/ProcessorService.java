package com.epam.learning.resource_processor.service;

import com.epam.learning.resource_processor.api.SongDTO;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Log4j2
@Service
public class ProcessorService {
	
	private final RestClient restClient;
	private final DiscoveryClient discoveryClient;
	
	public ProcessorService(RestClient restClient,
							DiscoveryClient discoveryClient1) {
		this.restClient = restClient;
		this.discoveryClient = discoveryClient1;
	}
	
	public void processResource(long id) {
		byte[] file = getFile(id);
		try {
			saveMetadata(file, id);
		} catch (TikaException | IOException | SAXException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] getFile(long id) {
		ServiceInstance serviceInstance = discoveryClient.getInstances("resource").get(0);
		ResponseEntity<byte[]> file = restClient.get().uri(serviceInstance.getUri()+"/resources/{id}", id)
												  .retrieve()
												  .onStatus(status -> status.value() == 500, (request, response) -> {
			throw new ResponseStatusException(response.getStatusCode(), response.toString());
		}).toEntity(byte[].class);
		log.info(file.getStatusCode());
		return file.getBody();
	}

	private void saveMetadata(byte[] file, long id) throws TikaException, IOException, SAXException {
		//detecting the file type
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		ParseContext pcontext = new ParseContext();

		//Mp3 parser
		InputStream stream = new ByteArrayInputStream(file);
		Mp3Parser Mp3Parser = new Mp3Parser();
		Mp3Parser.parse(stream, handler, metadata, pcontext);

		SongDTO songDTO = new SongDTO();
		songDTO.setResourceId(id);
		songDTO.setYear(Optional.ofNullable(metadata.get("xmpDM:releaseDate")).orElse("1900"));
		Duration period = Duration.ofMillis((long)(Float.parseFloat(Optional.ofNullable(metadata.get("xmpDM:duration")).orElse("100")) * 1000L));
		songDTO.setDuration(String.format("%02d", period.toMinutesPart()) +":"+ String.format("%02d", period.toSecondsPart()));
		songDTO.setName(Optional.ofNullable(metadata.get("dc:title")).orElse("Default"));
		songDTO.setArtist(Optional.ofNullable(metadata.get("xmpDM:artist")).orElse(""));
		songDTO.setAlbum(Optional.ofNullable(metadata.get("xmpDM:album")).orElse(""));
		log.info(songDTO);
		ServiceInstance serviceInstance = discoveryClient.getInstances("song").get(0);
		restClient.post()
				  .uri(serviceInstance.getUri()+"/songs")
				  .contentType(APPLICATION_JSON)
				  .body(songDTO)
				  .retrieve()
				  .onStatus(status -> status.value() == 500, (request, response) -> {
					  throw new ResponseStatusException(response.getStatusCode(), response.toString());
				  })
				  .toBodilessEntity();
	}
}
