package com.ceitechs.domain.service.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.ceitechs.domain.service.domain.Attachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;

/**
 * @author iddymagohe on 1/28/17.
 */
public interface FileStorageService {

    Attachment storeFile(Attachment attachment) throws Exception;

    String resolveUrl(Attachment attachment) throws Exception;

    void removeFile(Attachment attachment) throws Exception;
}

@Service
class AWSS3FileStorageService implements FileStorageService {

    private static final String PATH_DELIMITER ="/";
    private static final String ATTACHMENT_REFRENCE_ID="referenceId";
    private static final String ATTACHMENT_CATEGORY="category";
    private static final String PARENT_REFERENCE_ID="parentReferenceId";
    private static final String ATTACHMENT_DESCRIPTION="description";

    private final AmazonS3 s3Client;

    private final TransferManager transferManager;

    @Value("${s3.signedurl.timeout.milliseconds:3600000}")
    private  long signedUrlTimeout;

    @Value("${s3.attachments.bucketname:pango-attachmentse}")
    private  String bucketName;

    @Autowired
    public AWSS3FileStorageService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
        this.transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    @Override
    public Attachment storeFile(Attachment attachment) throws Exception {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(attachment.getAttachment().getSize());
        metaData.setContentType(attachment.getAttachment().getContentType());
        metaData.addUserMetadata(ATTACHMENT_REFRENCE_ID, attachment.getReferenceId());
        metaData.addUserMetadata(ATTACHMENT_CATEGORY, attachment.getCategory());
        metaData.addUserMetadata(PARENT_REFERENCE_ID, attachment.getParentReferenceId());
        metaData.addUserMetadata(ATTACHMENT_DESCRIPTION, attachment.getDescription());
        Upload upload = transferManager.upload(new PutObjectRequest(bucketName, resolveKeyName(attachment), attachment.getAttachment().getInputStream(), metaData));
        upload.waitForCompletion();
        attachment.setBucket(bucketName);
        return attachment;
    }

    @Override
    public String resolveUrl(Attachment attachment) throws Exception {
        java.util.Date expiration = new java.util.Date();
        long milliSeconds = expiration.getTime();
        milliSeconds += signedUrlTimeout; // Add 1 hour.
        expiration.setTime(milliSeconds);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, resolveKeyName(attachment));
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    private String resolveKeyName(Attachment attachment){
        StringBuilder keyName = new StringBuilder(attachment.getCategory());
        keyName.append(PATH_DELIMITER);
        keyName.append(attachment.getReferenceId());
        return keyName.toString();
    }

    @Override
    public void removeFile(Attachment attachment) throws Exception {
        s3Client.deleteObject(new DeleteObjectRequest(attachment.getBucket(), resolveKeyName(attachment)));
    }


}
