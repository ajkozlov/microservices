package com.epam.lerning.resource.service;

import com.epam.lerning.resource.api.SongDTO;
import com.epam.lerning.resource.domain.Resource;
import com.epam.lerning.resource.domain.ResourceRepository;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Service
public class ResourceService {

	private final ResourceRepository repository;
	private final RestClient restClient;

	public ResourceService(ResourceRepository resourceRepository, RestClient.Builder restClientBuilder, String songUrl) {
		repository = resourceRepository;
		this.restClient = restClientBuilder.baseUrl(songUrl).build();
	}

	private void saveMetadata(byte[] file, Long id) throws TikaException, IOException, SAXException {
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
		songDTO.setDuration(period.toMinutesPart() +":"+ period.toSecondsPart());
		songDTO.setName(Optional.ofNullable(metadata.get("dc:title")).orElse("Default"));
		songDTO.setArtist(Optional.ofNullable(metadata.get("xmpDM:artist")).orElse(""));
		songDTO.setAlbum(Optional.ofNullable(metadata.get("xmpDM:album")).orElse(""));
		restClient.post()
				  .contentType(APPLICATION_JSON)
				  .body(songDTO)
				  .retrieve()
				  .onStatus(status -> status.value() == 500, (request, response) -> {
					  throw new ResponseStatusException(response.getStatusCode(), response.getStatusText());
				  })
				  .toBodilessEntity();
	}

	public Long crateResource(byte[] file) throws IOException, TikaException, SAXException {
		Resource resource = new Resource(file);
		repository.save(resource);
		saveMetadata(file, resource.getId());
		return resource.getId();
	}



	public List<Long> deleteResources(String ids) {
		List<Long> idList;
		if (ids.length() > 200) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV string is too long: received " + ids.length()
					+ " characters, maximum allowed length is 200 characters");
		}
		try {
			idList = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
		} catch (NumberFormatException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id should be numbers");
		}
		repository.deleteAllById(idList);
		return idList;
	}

	public Resource findById(Long id) {
		if (id < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Id");
		}
		Optional<Resource> resource = repository.findById(id);
		return resource.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource has not found"));
	}
}
