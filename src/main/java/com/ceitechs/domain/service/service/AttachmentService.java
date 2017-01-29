package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.AttachmentRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

/**
 * @author iddymagohe on 1/28/17.
 */
public interface AttachmentService {

    Optional<Attachment> storeAttachment(User user, Attachment attachment);

    Optional<Attachment> retrieveAttachmentBy(String referenceId);

    Optional<Attachment> removeAttachmentBy(String referenceId);

    List<Attachment> retrieveAttachmentsBy(User user);

    List<Attachment> retrieveAttachmentsBy(String parentReferenceId, String category);

    List<Attachment> retrieveThumbnailAttachmentsBy(List<String> parentReferenceIds, String category);

}

@Service
class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    private  final Logger logger = LoggerFactory.getLogger(getClass());

    //Size in KB
    private static final long MAX_SIZE = 1024;

    private final Executor executor = Executors.newFixedThreadPool(100,
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true); // use daemon threads, - they don't prevent termination of program
                return t;
            });

    @Autowired
    public AttachmentServiceImpl(AttachmentRepository attachmentRepository, FileStorageService fileStorageService) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Optional<Attachment> storeAttachment(User user, Attachment attachment) {
        Assert.notNull(attachment , "Attachment; can not be null or empty");
        Assert.notNull(user, "user uploading an attachment can not be null");
        Assert.hasText(user.getUserReferenceId(), "user-reference-id : can not be null or empty");
        Assert.hasText(attachment.getCategory(),"attachment-category can not be null or empty");
        Assert.hasText(attachment.getParentReferenceId(), "attachment-parent-id can not be null or empty");
        Assert.notNull(attachment.getAttachment(), "attachment-to-upload can not be null or empty");
        Assert.isTrue(Arrays.stream(Attachment.attachmentCategoryType.values()).map(Enum::name).anyMatch(cat -> cat.equals(attachment.getCategory().toUpperCase())),"un-supported-category");
        Assert.isTrue(MAX_SIZE >= (attachment.getAttachment().getSize()/MAX_SIZE) ,"attachment-size can not be greater than 1MB");
        attachment.setUserReferenceId(user.getUserReferenceId());
        // prepare Id
        StringBuilder keyName = new StringBuilder(attachment.getCategory().toUpperCase());
        keyName.append("/");
        keyName.append(PangoUtility.generateIdAsUUID());
        //set Id
        attachment.setReferenceId(keyName.toString());

        attachment.setCategory(attachment.getCategory().toUpperCase());

         Attachment attachmentToLoad = null;
         try {
             //1. store actual file
             attachmentToLoad = fileStorageService.storeFile(attachment);
             //2. store metadata
             attachmentToLoad = attachmentRepository.save(attachmentToLoad);
             //3. resolve urls
             attachmentToLoad.setUrl(fileStorageService.resolveUrl(attachmentToLoad));
         }catch (Exception ex){
             logger.error("Error occurred during attachment upload " + ex.getMessage(), ex);
             throw new RuntimeException("Error occurred during attachment upload ", ex);
         }
        return Optional.ofNullable(attachmentToLoad);
    }

    @Override
    public Optional<Attachment> retrieveAttachmentBy(String referenceId) {
        Assert.hasText(referenceId, "ReferenceId can not be null or empty");
        Attachment attachment = attachmentRepository.findByReferenceIdAndActiveTrue(referenceId);
        try {
            attachment.setUrl(fileStorageService.resolveUrl(attachment));
        } catch (Exception ex) {
            logger.error("Error occurred during attachment-url resolve " + ex.getMessage(), ex);
        }
        return Optional.ofNullable(attachment);
    }


    @Override
    public Optional<Attachment> removeAttachmentBy(String referenceId) {
        Assert.hasText(referenceId, "ReferenceId can not be null or empty");
        Attachment attachment = attachmentRepository.findOne(referenceId);
        Assert.notNull(attachment, "reference-id doesn't exists ");
        try{
            fileStorageService.removeFile(attachment);
            attachmentRepository.delete(referenceId);
        }catch (Exception ex){
            logger.error("Error occurred during attachment upload " + ex.getMessage(), ex);
            throw new RuntimeException("Error occurred during attachment upload ", ex);
        }

        return Optional.ofNullable(attachment);
    }

    @Override
    public List<Attachment> retrieveAttachmentsBy(User user) {
        Assert.notNull(user, "user can not be null");
        Assert.hasText(user.getUserReferenceId(), "User-referenceId can not be null or empty");
        List<Attachment> attachments = attachmentRepository.findByUserReferenceIdAndActiveTrueOrderByCreatedDateDesc(user.getUserReferenceId());
        return CollectionUtils.isNotEmpty(attachments) ? resolveUrls(attachments) : attachments;
    }

    @Override
    public List<Attachment> retrieveAttachmentsBy(String parentReferenceId, String category) {
        Assert.hasText(parentReferenceId, "attachment-parent-reference-id: can not be null or empty");
        Assert.hasText(category, "category: can not be null or empty");
        List<Attachment> attachments = attachmentRepository.findByParentReferenceIdAndCategoryAndActiveTrue(parentReferenceId, category);
        return CollectionUtils.isNotEmpty(attachments) ? resolveUrls(attachments) : attachments;
    }

    @Override
    public List<Attachment> retrieveThumbnailAttachmentsBy(List<String> parentReferenceIds, String category) {
        Assert.notEmpty(parentReferenceIds, "parent-reference-ids; can not be null or empty");
        Assert.hasText(category, "category: can not be null or empty");
        List<Attachment> attachments = attachmentRepository.findByParentReferenceIdInAndCategoryAndThumbnailTrueAndActiveTrue(parentReferenceIds,category);
        return CollectionUtils.isNotEmpty(attachments)? resolveUrls(attachments):attachments;
    }

    private List<Attachment> resolveUrls(List<Attachment> attachments) {
        Assert.notEmpty(attachments, "attachment-list can not be null or empty");
        List<CompletableFuture<Attachment>> attachmentsCompletableFutures =
                attachments.stream()
                        .map(attachment -> CompletableFuture.supplyAsync(
                                () -> {
                                    try {
                                        attachment.setUrl(fileStorageService.resolveUrl(attachment));
                                    } catch (Exception ex) {
                                        logger.error("Error occurred during attachment-url resolve " + ex.getMessage(), ex);
                                    }
                                    return attachment;

                                }, executor
                        )).collect(toList());
        return attachmentsCompletableFutures.stream()
                .map(CompletableFuture::join)
                .collect(toList());
    }


}
