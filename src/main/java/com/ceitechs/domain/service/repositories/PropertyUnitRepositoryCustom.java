package com.ceitechs.domain.service.repositories;


import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.PropertyUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @author iddymagohe
 * @since 0.1
 */
public interface PropertyUnitRepositoryCustom {

    /**
     * @param searchCriteria
     * @return
     */
    GeoResults<PropertyUnit> findAllPropertyUnits(PropertySearchCriteria searchCriteria);
}

@Service
class PropertyUnitRepositoryImpl implements PropertyUnitRepositoryCustom {

    @Autowired
    MongoOperations mongoOperations;

    @Override
    public GeoResults<PropertyUnit> findAllPropertyUnits(PropertySearchCriteria searchCriteria) {
        /**
         * Number of Rooms
         *  - exact match for studios and rooms (bed or offices )less than 4.
         */

        PropertyUnit.PropertyPurpose propertyPurpose = PropertyUnit.PropertyPurpose.valueOf(searchCriteria.getPropertyPupose());


        Criteria criteria = Criteria.where("active").is(true);
        Query query = new Query(criteria);

        criteria.and("purpose").is(propertyPurpose);

        if (propertyPurpose == PropertyUnit.PropertyPurpose.BUSINESS)
            criteria.and("features.nbrOfRooms").gte(searchCriteria.getRoomsCount()); // Offices

        if (propertyPurpose == PropertyUnit.PropertyPurpose.HOME) {
            if (searchCriteria.getBedRoomsCount() > 0 && searchCriteria.getBedRoomsCount() < 1.0) { // studio
                criteria.and("features.studio").is(true);
            } else if (searchCriteria.getRoomsCount() > 0 && searchCriteria.getBedRoomsCount() < 4.0) { //Bed rooms
                criteria.and("features.nbrOfBedRooms").is(Double.valueOf(searchCriteria.getBedRoomsCount()).intValue());
            } else {
                criteria.and("features.nbrOfBedRooms").gte(Double.valueOf(searchCriteria.getBedRoomsCount()).intValue()); //0-4+
            }
        }

        criteria.and("features.nbrOfBaths").gte(searchCriteria.getBathCount());

        criteria.and("nextAvailableDate").lte(searchCriteria.getMoveInDate().get()); //amount

        if (searchCriteria.getMaxPrice() >= searchCriteria.getMinPrice()) {
            //query.addCriteria(Criteria.where("rent.amount").gte(searchCriteria.getMinPrice()).lt(searchCriteria.getMaxPrice()));
            criteria.and("rent.amount").gte(searchCriteria.getMinPrice()).lte(searchCriteria.getMaxPrice());
        } else {
            criteria.and("rent.amount").gte(searchCriteria.getMinPrice());
        }

        if (StringUtils.hasText(searchCriteria.getAmenities())) {
            query.addCriteria(Criteria.where("features.additionalFeatures")
                    .elemMatch(Criteria.where("name").in(Arrays.asList(searchCriteria.getAmenities().toUpperCase().split(",")))
                            .and("available").is(true)
                    )
            );
        }

        Point location = new Point(searchCriteria.getLongitude(), searchCriteria.getLatitude());

        NearQuery near = NearQuery.near(location).maxDistance(new Distance(searchCriteria.getRadius(), Metrics.KILOMETERS));
        near.query(query);

        mongoOperations.indexOps(PropertyUnit.class).ensureIndex(new GeospatialIndex("location"));

        return mongoOperations.geoNear(near, PropertyUnit.class);
    }
}