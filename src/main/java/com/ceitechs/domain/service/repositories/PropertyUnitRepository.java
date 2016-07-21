package com.ceitechs.domain.service.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PropertyUnitRepository extends MongoRepository<PropertyUnit, String>, PropertyUnitRepositoryCustom {

    public List<PropertyUnit> findByOwner(User user);
}
