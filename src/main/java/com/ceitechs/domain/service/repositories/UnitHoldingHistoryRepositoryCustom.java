package com.ceitechs.domain.service.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyHoldingHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface UnitHoldingHistoryRepositoryCustom {

    Optional<List<PropertyHoldingHistory>> getUnitHoldingHistory(String ownerId);
}

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
@Service
class UnitHoldingHistoryRepositoryImpl implements UnitHoldingHistoryRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PropertyUnitRepository propertyUnitRepository;

    @Override
    public Optional<List<PropertyHoldingHistory>> getUnitHoldingHistory(String ownerId) {
        User user = new User();
        user.setUserReferenceId(ownerId);
        List<PropertyUnit> propertyUnitList = propertyUnitRepository.findByOwner(user);
        List<String> propertyUnitIds = propertyUnitList.stream().map(PropertyUnit::getPropertyUnitId)
                .collect(Collectors.toList());
        if (propertyUnitIds.size() > 0) {
            LocalDateTime dateTime = LocalDateTime.now();
            Criteria criteria = Criteria.where("propertyUnit.$id").in(propertyUnitIds).and("startDate").lte(dateTime)
                    .and("endDate").gte(dateTime);
            return Optional.of(mongoTemplate.find(Query.query(criteria), PropertyHoldingHistory.class));
        } else {
            return Optional.empty();
        }
    }
}
