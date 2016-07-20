package com.ceitechs.domain.service.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.PropertyUnit;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PropertyUnitRepository extends MongoRepository<PropertyUnit, String> {

}
