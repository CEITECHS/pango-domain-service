package com.ceitechs.domain.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.Property;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PangoDomainServicePropertyRepository extends MongoRepository<Property, String>{

    public Page<Property> findByOwnerOrderByCreatedDateDesc(String userId, Pageable pageable);
    
    public Page<Property> findByListingForOrderByCreatedDateDesc(Enum listingFor, Pageable pageable);
}
