package com.ceitechs.domain.service.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.UnitHoldingHistory;
import com.ceitechs.domain.service.domain.User;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface UnitHoldingHistoryRepositoryCustom {

    List<UnitHoldingHistory> getUnitHoldingHistory(String ownerId, LocalDate date);
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
    public List<UnitHoldingHistory> getUnitHoldingHistory(String ownerId, LocalDate date) {
        User user = new User();
        user.setUserReferenceId(ownerId);
        List<PropertyUnit> propertyUnitList = propertyUnitRepository.findByOwner(user);
        List<String> propertyUnitIds = new ArrayList<>();
        propertyUnitList.forEach(propertyUnit -> {
            propertyUnitIds.add(propertyUnit.getPropertyUnitId());
        });
        Criteria criteria = Criteria.where("propertyUnit.$id").in(propertyUnitIds).and("startDate").lte(date)
                .and("endDate").gte(date);
        return mongoTemplate.find(Query.query(criteria), UnitHoldingHistory.class);
    }
}
