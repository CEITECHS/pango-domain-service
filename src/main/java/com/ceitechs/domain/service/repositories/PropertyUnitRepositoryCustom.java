package com.ceitechs.domain.service.repositories;


import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.PropertyUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

/**
 * @author iddymagohe
 * @since 0.1
 */
public interface PropertyUnitRepositoryCustom {

    GeoResults<PropertyUnit> findAllPropertyUnits(PropertySearchCriteria searchCriteria);
}

@Service
class PropertyUnitRepositoryImpl implements PropertyUnitRepositoryCustom{

    @Autowired
    MongoOperations mongoOperations;

    @Override
    public GeoResults<PropertyUnit> findAllPropertyUnits(PropertySearchCriteria searchCriteria) {

        return null;
    }
}