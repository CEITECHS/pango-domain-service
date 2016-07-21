package com.ceitechs.domain.service.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.UnitHoldingHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface UnitHoldingHistoryRepository
        extends MongoRepository<UnitHoldingHistory, String>, UnitHoldingHistoryRepositoryCustom {

    Page<UnitHoldingHistory> findByUserOrderByStartDateDesc(User user, Pageable page);
}
