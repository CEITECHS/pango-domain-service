package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.Attachment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author iddymagohe on 1/28/17.
 */
public interface AttachmentRepository extends MongoRepository<Attachment, String> {

    List<Attachment> findByUserReferenceIdAndActiveTrueOrderByCreatedDateDesc(String userReferenceId);

    List<Attachment> findByParentReferenceIdInAndCategoryAndThumbnailTrueAndActiveTrue(Collection<String> parentReferenceIds, String Category);

    List<Attachment> findByParentReferenceIdAndCategoryAndActiveTrue(String referenceId, String category);
}
