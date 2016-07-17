package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author iddymagohe
 * @since 0.1
 */
public interface PangoDomainServiceReviewRepository extends MongoRepository<Review, String>{
    Page<Review> findByTenantReferenceIdOrderByCreatedDateDesc(String tenantReferenceId, Pageable pegiable);
    Page<Review> findByPropertyUnitReferenceIdOrderByCreatedDateDesc(String propertyUnitReferenceId,Pageable pegiable);
}
