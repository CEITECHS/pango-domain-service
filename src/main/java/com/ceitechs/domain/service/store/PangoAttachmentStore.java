package com.ceitechs.domain.service.store;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ceitechs.domain.service.domain.AttachmentResource;
import com.ceitechs.domain.service.domain.AttachmentStoreMetaData;
import com.ceitechs.domain.service.exception.PangoServiceException;

/**
 * Defines contracts for AttachmentStore implementations.
 * 
 * <p> AttachmentStore provides contracts for storing and retrieving PangoAttachments
 * from the store, it hides the underlining implementation of the store.
 * 
 * @author uvsd1
 *
 */
public interface PangoAttachmentStore {
	
    /**
     * @param content
     * @param location
     * @param dbObject
     */
	AttachmentStoreMetaData storeAttachment(AttachmentResource attachmentToUpload, String attachmentName);
    
    AttachmentStoreMetaData getAttachmentLocation(String attachmentName);
}

@Repository
class S3AttachmentStore implements PangoAttachmentStore{
	
    private static final Logger logger = LoggerFactory.getLogger(S3AttachmentStore.class);
    
    private static final String ATTACHMENT_REFRENCE_ID="attachmentRefrenceId";
	
	private AmazonS3Client s3Client;
	
	@Value("${s3.signedurl.timeout.milliseconds:3600000}")
    private  long signedUrlTimeout;
	
	@Value("${s3.pango.bucketname}")
    private  String bucketName;
	
	public S3AttachmentStore(AmazonS3Client s3Clinet) {
		this.s3Client = s3Clinet;
	}
	
	
	@Override
	public AttachmentStoreMetaData storeAttachment(AttachmentResource attachmentToUpload) {		
		try {
			logger.debug("storing attachment: {} to s3 bucket: {}",attachmentToUpload.getAttachment().getName(), bucketName);
			ObjectMetadata metaData = new ObjectMetadata();
			metaData.setContentLength(attachmentToUpload.getAttachment().getSize());
			metaData.addUserMetadata(ATTACHMENT_REFRENCE_ID, attachmentToUpload.getAttachmentReferenceId());
            s3Client.putObject(new PutObjectRequest(bucketName,keyName,attachmentToUpload.getAttachment().getInputStream(),metaData));            
		} catch (AmazonServiceException ase) {
        	 logger.error("AmazonServiceException while storing the attachment: {}",attachmentToUpload.getAttachmentReferenceId(),ase);
        	 //TODO re-factor the below to come up with a standard for exception handling
        	 throw new PangoServiceException("StoreAttachementError",ase.getErrorMessage(),ase);
         } catch (AmazonClientException ace) {
        	 logger.error("AmazonClientException while storing the attachment: {}",attachmentToUpload.getAttachmentReferenceId(),ace);
        	 //TODO re-factor the below to come up with a standard for exception handling
        	 throw new PangoServiceException("StoreAttachementError",ace.getMessage(),ace);
          } catch (IOException io) {
        	  logger.error("IOException while storing the attachment: {}",attachmentToUpload.getAttachmentReferenceId(),io);
         	 //TODO re-factor the below to come up with a standard for exception handling
         	 throw new PangoServiceException("StoreAttachementError",io.getMessage(),io);
		}
	}


	@Override
	public AttachmentStoreMetaData getAttachmentLocation(String attachmentName) {
		// TODO Auto-generated method stub
		return null;
	}

	
}

