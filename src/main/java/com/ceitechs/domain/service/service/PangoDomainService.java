package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.sun.tools.javac.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface PangoDomainService {

    /**
     *  Creates a unit property
     * @param propertyUnit
     * @param user
     * @return
     */
     PropertyUnit createProperty(PropertyUnit propertyUnit, User user);

    /**
     * Search for properties and updates user search criteria
     *
      * @param searchCriteria
     * @param user
     * @return
     */
    List<GeoResults<PropertyUnit>> searchForProperties(PropertySearchCriteria searchCriteria, User user);

}

@Service
class PangoDomainServiceImpl implements PangoDomainService {

    private static final Logger logger = LoggerFactory.getLogger(PangoDomainServiceImpl.class);

    private final PangoEventsPublisher eventsPublisher;

    private final PropertyUnitRepository propertyUnitRepository;

    private final UserRepository userRepository;

    @Autowired
    public PangoDomainServiceImpl(PangoEventsPublisher eventsPublisher, PropertyUnitRepository propertyUnitRepository, UserRepository userRepository) {
        this.eventsPublisher = eventsPublisher;
        this.propertyUnitRepository = propertyUnitRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a unit property
     *
     * @param propertyUnit
     * @param user
     * @return
     */
    @Override
    public PropertyUnit createProperty(PropertyUnit propertyUnit, User user) {
        Assert.notNull(propertyUnit, "Property to add can not be null or Empty");
        Assert.notNull(propertyUnit.getOwner(), "Property Owner can not be null or Empty");

        String userId = propertyUnit.getOwner().getUserReferenceId();


        if (StringUtils.hasText(propertyUnit.getPropertyUnitId())){
            //TODO : Existing or firstTime update by Coordinator do update
        }else{

        }
        return null;
    }

    /**
     * Search for properties and updates user search criteria
     *
     * @param searchCriteria
     * @param user
     * @return
     */
    @Override
    public List<GeoResults<PropertyUnit>> searchForProperties(PropertySearchCriteria searchCriteria, User user) {
        return null;
    }
}