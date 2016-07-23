package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.OnAttachmentUploadEvent;
import com.ceitechs.domain.service.service.events.OnPropertySearchEvent;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

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
    List<GeoResult<PropertyUnit>> searchForProperties(PropertySearchCriteria searchCriteria, User user);

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
        User owner = userRepository.findOne(userId);
        propertyUnit.setOwner(owner);
        if (!StringUtils.hasText(propertyUnit.getPropertyUnitId())){
            propertyUnit.setPropertyUnitId(PangoUtility.generateIdAsString());
            PropertyUnit savedUnit = propertyUnitRepository.save(propertyUnit);
            logger.info("Saved Property "+ savedUnit);

            if(savedUnit !=null && !propertyUnit.getAttachments().isEmpty()){
                OnAttachmentUploadEvent attachmentEvent = new OnAttachmentUploadEvent(
                       propertyUnit.getAttachments() .stream().map((attachment) ->  new AttachmentToUpload(savedUnit.getPropertyUnitId(), ReferenceIdFor.PROPERTY, attachment,"")).collect(Collectors.toList()));
                        eventsPublisher.publishAttachmentEvent(attachmentEvent);
                logger.info("published event to store attachmnets");
                return savedUnit;
            }

        }
        //TODO : Existing or firstTime update by Coordinator do update
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
    public List<GeoResult<PropertyUnit>> searchForProperties(PropertySearchCriteria searchCriteria, User user) {
        Assert.notNull(searchCriteria,"Search criteria can not be null ");
        GeoResults<PropertyUnit> propertyUnitGeoResults = propertyUnitRepository.findAllPropertyUnits(searchCriteria);
        if(!propertyUnitGeoResults.getContent().isEmpty() && user != null){
            OnPropertySearchEvent onPropertySearchEvent = new OnPropertySearchEvent(new UserSearchHistory(searchCriteria, propertyUnitGeoResults.getContent().size()),user);
            logger.info("publishing Property Search event for user "+ user.getUserReferenceId());
            eventsPublisher.publishAttachmentEvent(onPropertySearchEvent);
        }
        return propertyUnitGeoResults.getContent();
    }
}