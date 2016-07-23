package com.ceitechs.domain.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.PropertyRemoved;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PropertyRepository extends MongoRepository<PropertyRemoved, String>{

    public Page<PropertyRemoved> findByOwnerOrderByCreatedDateDesc(String userId, Pageable pageable);
    
    public Page<PropertyRemoved> findByListingForOrderByCreatedDateDesc(Enum listingFor, Pageable pageable);
}
