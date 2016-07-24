package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.OnAttachmentUploadEvent;
import com.ceitechs.domain.service.service.events.OnPropertySearchEvent;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
     Optional<PropertyUnit> createProperty(PropertyUnit propertyUnit, User user);

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
    private final GridFsService gridFsService;
    private ExecutorService executorService = Executors.newCachedThreadPool();


    @Autowired
    public PangoDomainServiceImpl(PangoEventsPublisher eventsPublisher, PropertyUnitRepository propertyUnitRepository, UserRepository userRepository,GridFsService gridFsService) {
        this.eventsPublisher = eventsPublisher;
        this.propertyUnitRepository = propertyUnitRepository;
        this.userRepository = userRepository;
        this.gridFsService = gridFsService;
    }

    /**
     * Creates a unit property
     *
     * @param propertyUnit
     * @param user
     * @return
     */
    @Override
    public Optional<PropertyUnit> createProperty(PropertyUnit propertyUnit, User user) {
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
                return Optional.of(savedUnit);
            }

        }
        //TODO : Existing or firstTime update by Coordinator do update
        return Optional.empty();
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
        if(!propertyUnitGeoResults.getContent().isEmpty()){
            // associate cover photos
           List<GridFSDBFile> coverPhotos = gridFsService.getPropertiesCoverPhotos(propertyUnitGeoResults.getContent().parallelStream()
                    .map(propertyUnitGeoResult -> propertyUnitGeoResult.getContent().getPropertyUnitId())
                    .collect(Collectors.toList()));
            Map<String, FileMetadata> propertyCoverPhoto = FileMetadata.getFileMetaFromGridFSDBFileAsMap(coverPhotos);

            executorService.submit(() ->{
                if( user != null) {
                    OnPropertySearchEvent onPropertySearchEvent = new OnPropertySearchEvent(new UserSearchHistory(searchCriteria, propertyUnitGeoResults.getContent().size()), user);
                    logger.info("publishing Property Search event for user " + user.getUserReferenceId());
                    eventsPublisher.publishAttachmentEvent(onPropertySearchEvent);
                }
            });

            return propertyCoverPhoto.isEmpty()? propertyUnitGeoResults.getContent() : propertyUnitGeoResults.getContent().stream()
                    .map(propertyUnitGeoResult -> {
                        PropertyUnit propertyUnit = propertyUnitGeoResult.getContent();
                        if(propertyCoverPhoto.containsKey(propertyUnit.getPropertyUnitId()))
                           propertyUnit.setCoverPhoto(new Attachment(propertyCoverPhoto.get(propertyUnit.getPropertyUnitId())));
                        return new GeoResult<>(propertyUnit,propertyUnitGeoResult.getDistance());
                    }).collect(Collectors.toList());
        }
        return propertyUnitGeoResults.getContent();
    }
}