package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.PropertyUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.PropertyHoldingHistory;
import com.ceitechs.domain.service.domain.User;

import java.util.List;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PropertyHoldingHistoryRepository
        extends MongoRepository<PropertyHoldingHistory, String>, PropertyHoldingHistoryRepositoryCustom {

    Page<PropertyHoldingHistory> findByUserOrderByStartDateDesc(User user, Pageable page); //filter out the expired ones

    PropertyHoldingHistory findByPropertyUnitAndPhaseNotInOrderByCreatedDateDesc(PropertyUnit propertyUnit, List<PropertyHoldingHistory.HoldingPhase> phases);

}
