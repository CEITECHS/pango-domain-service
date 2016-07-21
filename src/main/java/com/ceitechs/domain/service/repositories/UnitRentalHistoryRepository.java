package com.ceitechs.domain.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.UnitRentalHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface UnitRentalHistoryRepository extends MongoRepository<UnitRentalHistory, String> {

    Page<UnitRentalHistory> findByUserOrderByIsActiveDescStartDateDesc(User user, Pageable page);

    Page<UnitRentalHistory> findByPropertyUnitOrderByIsActiveDescStartDateDesc(PropertyUnit propertyUnit,
            Pageable page);
}
