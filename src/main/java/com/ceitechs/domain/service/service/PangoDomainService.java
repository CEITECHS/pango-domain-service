package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.*;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author iddymagohe
 * @since 1.0 -
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

    /**
     *
     * @param propertyReferenceId
     * @param user
     * @return
     */
    Optional<PropertyUnit> retrievePropertyBy(String propertyReferenceId, User user);

    /**
     *
     * @param user
     * @return
     */
    Optional<UserProjection> registerUser(User user) throws EntityExists;

    /**
     * Used for login type of activity
     * @param username
     * @return
     */
    User retrieveVerifiedUserByUsername(String username);

    /**
     * retrieve user associated with a profile picture if one exists.
     *
     * @param userId
     * @param userName
     * @return
     */
    Optional<UserProjection> retrieveUserByIdOrUserName(String userId, String userName);

    /**
     * retrieve user set preference , limit to 10 active and most recent preferences
     * @param userId
     * @return
     */
    List<UserPreference> retrievePreferencesByUserId(String userId);

    /**
     *  Add user's preference
     * @param userPreference
     * @param user
     * @return
     */
    Optional<UserProjection> addUserPreference(UserPreference userPreference, User user);

    /**
     *  removes a particular user's preference
     * @param preferenceId
     * @param user
     * @return
     */
    Optional<UserProjection> removeUserPreferenceBy(String preferenceId, User user);


    /**
     *  Update a particular user-preference
     * @param userPreference
     * @param user
     * @return
     */
    Optional<UserProjection> updateUserPreference(UserPreference userPreference, User user);

}

@Service
class PangoDomainServiceImpl implements PangoDomainService {

    private static final Logger logger = LoggerFactory.getLogger(PangoDomainServiceImpl.class);

    private final PangoEventsPublisher eventsPublisher;
    private final PropertyUnitRepository propertyUnitRepository;
    private final UserRepository userRepository;
    private final GridFsService gridFsService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


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
                logger.info("published event to store attachments");
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

        //record user search history
        recordUserSearchHistory(new UserSearchHistory(searchCriteria, propertyUnitGeoResults.getContent().size()), user);

        if(!propertyUnitGeoResults.getContent().isEmpty()){
            // associate cover photos
           List<GridFSDBFile> coverPhotos = gridFsService.getPropertiesCoverPhotos(propertyUnitGeoResults.getContent().parallelStream()
                    .map(propertyUnitGeoResult -> propertyUnitGeoResult.getContent().getPropertyUnitId())
                    .collect(Collectors.toList()));
            Map<String, FileMetadata> propertyCoverPhoto = FileMetadata.getFileMetaFromGridFSDBFileAsMap(coverPhotos);

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

    /**
     * @param propertyReferenceId
     * @param user
     * @return
     */
    @Override
    public Optional<PropertyUnit> retrievePropertyBy(String propertyReferenceId, User user) {
        Optional<PropertyUnit> propertyUnit = Optional.of(propertyUnitRepository.findOne(propertyReferenceId));

        PropertySearchCriteria propertySearchCriteria = new PropertySearchCriteria();
        propertySearchCriteria.setPropertyReferenceId(propertyReferenceId);
        recordUserSearchHistory(new UserSearchHistory(propertySearchCriteria, propertyUnit.isPresent()?1:0), user);
        propertyUnit.ifPresent(p -> {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setReferenceId(p.getPropertyUnitId());
            fileMetadata.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            List<GridFSDBFile> attachments = gridFsService.getAllAttachments(fileMetadata, ReferenceIdFor.PROPERTY);
            p.setAttachments(FileMetadata.getFileMetaFromGridFSDBFileAsList(attachments).parallelStream().map(Attachment::new).collect(Collectors.toList()));
        });

        return propertyUnit;
    }

    /**
     * @param user
     * @return
     */
    @Override
    public Optional<UserProjection> registerUser(User user) throws EntityExists {
        Assert.notNull(user, "User to register can not be null");
        Assert.hasText(user.getEmailAddress(), "user email address can not be null or empty");
        Assert.hasText(user.getProfile().getPassword(), "user password can not be null or Empty");
        Assert.notEmpty(user.getProfile().getRoles(), "User roles can not be null");
        // check that user by email doesn't exists
        User existingUser = userRepository.findByEmailAddressIgnoreCaseAndProfileVerifiedTrue(user.getEmailAddress());
        if (existingUser != null && StringUtils.hasText(existingUser.getEmailAddress())) {
            if (!existingUser.getProfile().isVerified())
                triggerUserInteractionEvent(user, UserInteraction.VERIFICATION);
            throw new EntityExists("User with Email address exists");

        }
        user.getProfile().setVerified(false);
        User savedUser = userRepository.save(user);
        savedUser.getProfile().setPassword("**********");
        if(savedUser != null) triggerUserInteractionEvent(savedUser, UserInteraction.REGISTRATION);
        return Optional.of(savedUser);
    }

    @Override
    public User retrieveVerifiedUserByUsername(String userId) {
        return userRepository.findByEmailAddressIgnoreCaseAndProfileVerifiedTrue(userId);
    }

    /**
     *
     * @param userId
     * @param userName
     * @return
     */
    @Override
    public Optional<UserProjection> retrieveUserByIdOrUserName(String userId, String userName) {
        Assert.isTrue(StringUtils.hasText(userId) || StringUtils.hasText(userName), "UserId or UserName can not be empty or null");
        User user = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCase(userName, userId);
        // associate user's profile picture
        if (user != null) {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setReferenceId(user.getUserReferenceId());
            Attachment userProfilePicture = new Attachment(FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(gridFsService.getProfilePicture(fileMetadata, ReferenceIdFor.USER)), ReferenceIdFor.USER));
            user.getProfile().setProfilePicture(userProfilePicture);
        }

        return Optional.of(user);
    }

    @Override
    public List<UserPreference> retrievePreferencesByUserId(String userId) {
        return userRepository.retrievePreferencesBy(userId);
    }

    @Override
    public Optional<UserProjection> addUserPreference(UserPreference userPreference, User user) {
        userPreference.setCategory(UserPreference.PreferenceCategory.USERSET);
        if (userPreference.getPreferenceType() == null)
            userPreference.setPreferenceType(UserPreference.PreferenceType.Notification);
        Optional<User> resp = userRepository.addUserPreferences(userPreference, user);
        return resp.isPresent() ? Optional.of(resp.get()) : Optional.empty();
    }

    /**
     * removes a particular user's preference
     *
     * @param preferenceId
     * @param user
     * @return
     */
    @Override
    public Optional<UserProjection> removeUserPreferenceBy(String preferenceId, User user) {
        Optional<User> removeResponse = userRepository.removePreferenceBy(preferenceId, user);
        return removeResponse.isPresent() ? Optional.of(removeResponse.get()) : Optional.empty();
    }

    @Override
    public Optional<UserProjection> updateUserPreference(UserPreference userPreference, User user) {
        // 1. Remove preference from user
         removeUserPreferenceBy(userPreference.getPreferenceId(),user);
        // 2. Add new preference to User
        return addUserPreference(userPreference, user);
    }

    private void recordUserSearchHistory(UserSearchHistory searchCriteria, User user){
        if (user == null || !StringUtils.hasText(user.getUserReferenceId())) return;
        executorService.submit(() -> {
            if( user != null) {
                OnPropertySearchEvent onPropertySearchEvent = new OnPropertySearchEvent(searchCriteria, user);
                logger.info("publishing Property Search event for user " + user.getUserReferenceId());
                eventsPublisher.publishAttachmentEvent(onPropertySearchEvent);
            }
        });
        return;
    }

    private void triggerUserInteractionEvent(User user, UserInteraction userInteraction){
        if (user == null || !StringUtils.hasText(user.getEmailAddress())) return;
        executorService.submit(() -> {
             if (userInteraction == UserInteraction.VERIFICATION){
                 UserVerificationEvent verificationEvent = new UserVerificationEvent(user);
                 logger.info("publishing User email verification for user " + user.getUserReferenceId());
                 eventsPublisher.publishAttachmentEvent(verificationEvent);
             }
        });
        return;
    }

}