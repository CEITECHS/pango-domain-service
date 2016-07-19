package com.ceitechs.domain.service.repositories;


import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.PropertyUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
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

        Criteria criteria = Criteria.where("active").is(true);

        criteria.and("purpose").is(PropertyUnit.PropertyPurpose.valueOf(searchCriteria.getPropertyPupose()));

        criteria.and("nextAvailableDate").lt(searchCriteria.getMoveInDate().get());

        criteria.and("features.nbrOfRooms").gte(searchCriteria.getRoomsCount());

        criteria.orOperator(Criteria.where("features.nbrOfBedRooms").gte(searchCriteria.getBedRoomsCount()),
                Criteria.where("features.nbrOfBaths").gte(searchCriteria.getBathCount()));

        Point location = new Point(searchCriteria.getLatitude(), searchCriteria.getLongitude());

        NearQuery near = NearQuery.near(location).maxDistance(new Distance(searchCriteria.getRadius(), Metrics.KILOMETERS));
        near.query(new Query(criteria));

        return mongoOperations.geoNear(near,PropertyUnit.class);
    }
}