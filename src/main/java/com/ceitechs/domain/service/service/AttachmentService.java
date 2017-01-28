package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author iddymagohe on 1/28/17.
 */
public interface AttachmentService {

    Optional<Attachment> storeAttachmentFor(User user, Attachment attachment);

    Optional<Attachment> retrieveAttachmentBy(String referenceId);

    Optional<Attachment> removeAttachmentBy(String referenceId);

    List<Attachment> retrieveAttachmentsBy(User user);

    List<Attachment> retrieveAttachmentsBy(String parentReferenceId, String category);

    List<Attachment> retrieveThumbnailAttachmentsBy(List<String> parentReferenceIds, String category);

}

@Service
class AttachmentServiceImpl implements AttachmentService {

    @Override
    public Optional<Attachment> storeAttachmentFor(User user, Attachment attachment) {
        return null;
    }

    @Override
    public Optional<Attachment> retrieveAttachmentBy(String referenceId) {
        return null;
    }


    @Override
    public Optional<Attachment> removeAttachmentBy(String referenceId) {
        return null;
    }

    @Override
    public List<Attachment> retrieveAttachmentsBy(User user) {
        return null;
    }

    @Override
    public List<Attachment> retrieveAttachmentsBy(String parentReferenceId, String category) {
        return null;
    }

    @Override
    public List<Attachment> retrieveThumbnailAttachmentsBy(List<String> parentReferenceIds, String category) {
        return null;
    }


}
