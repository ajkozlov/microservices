package com.epam.learning.resource.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static com.epam.learning.resource.repository.StorageType.PERMANENT;
import static com.epam.learning.resource.repository.StorageType.STAGING;


public class S3Service {
	private final S3Client s3;
	private final StorageService storageService;
	private static final Logger LOGGER = LoggerFactory.getLogger(S3Service.class);

	@Value("${aws.s3.bucket}")
	private String bucketName;

	@Autowired
	public S3Service(S3Client s3, StorageService storageService) {
		this.s3 = s3;
		this.storageService = storageService;
	}

	public void saveToStage(String fileName, byte[] file) throws IOException {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
															.bucket(getStageBucket())
															.key(fileName)
															.build();

		try {

			s3.putObject(putObjectRequest, RequestBody.fromBytes(file));
			LOGGER.info("File {} saved to S3 at {}", fileName, "path");
		} catch (SdkException e) {
			LOGGER.error("Failed to save file to S3: {}", e.getMessage());
			throw new IOException("Failed to save file to S3", e);
		}
	}

	public void moveToPermanent(String fileName) throws IOException {
		CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
															   .sourceBucket(getStageBucket())
															   .destinationBucket(getPermanentBucket())
															   .sourceKey(fileName)
															   .destinationKey(fileName)
															   .build();

		try {
			s3.copyObject(copyObjectRequest);
			s3.deleteObject(DeleteObjectRequest.builder()
											   .bucket(getStageBucket())
											   .key(fileName)
											   .build());
			LOGGER.info("File {} moved to S3 at {}", fileName, "path");
		} catch (SdkException e) {
			LOGGER.error("Failed to move file to S3: {}", e.getMessage());
			throw new IOException("Failed to move file to S3", e);
		}
	}

	public byte[] download(String key, StorageType type) throws IOException {
		String bucketName = STAGING == type ? getStageBucket() : getPermanentBucket();
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
															.bucket(bucketName)
															.key(key)
															.build();

		try {
			return s3.getObject(getObjectRequest).readAllBytes();
		} catch (NoSuchKeyException e) {
			LOGGER.warn("Could not find object: {}", key);
			return new byte[0]; // Return empty byte array when object is not found
		} catch (SdkException e) {
			LOGGER.error("Failed to download file from S3: {}", e.getMessage());
			throw new IOException("Failed to download file from S3", e);
		}
	}

	public String getStageBucket() {
		StorageDTO storage = storageService.getStorage(STAGING);
		return storage.getBucket();
	}

	private String getPermanentBucket() {
		StorageDTO storage = storageService.getStorage(PERMANENT);
		return storage.getBucket();
	}
}
