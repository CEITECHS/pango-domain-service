package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.*;
import com.ceitechs.domain.service.util.DistanceCalculator;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.ceitechs.domain.service.util.TokensUtil;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
     *  retrieve user's favorite properties
     * @param user
     * @return
     */
    List<PropertyProjection> retrieveFavoritePropertiesBy(User user) throws EntityNotFound;

    /**
     *
     * @param propertyReferenceId
     * @param user
     * @return
     */
    Optional<PropertyUnit> retrievePropertyBy(String propertyReferenceId, User user);

    /**
     * User registration, trigger verification email
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


    /**
     * updates user information based on {@link UserUpdating } value
     * @param user
     * @return {@link Optional<UserProjection>}
     */
    Optional<UserProjection> updateUserInformation(User user, UserUpdating updating);

    /**
     *  add/remove a property by referenceId as a user's favorite depending favourable flag
     * @param user
     * @param propertyReferenceId
     * @param favourable true/false for add/remove
     * @return
     */
    Optional<UserProjection> updateUserFavoriteProperties(User user, String propertyReferenceId, boolean favourable) throws EntityNotFound;

    /**
     *  Verifies user account token
     *  @see TokensUtil#createAccountVerificationToken(User) for the expected token
     * @param verificationToken
     * @return Optional<UserProjection>
     */
    Optional<UserProjection> verifyUserAccountBy(String verificationToken) throws Exception;


}

@Service
class PangoDomainServiceImpl implements PangoDomainService {

    private static final Logger logger = LoggerFactory.getLogger(PangoDomainServiceImpl.class);

    private static final String TOKEN_SEPERATOR = ":";

    private final PangoEventsPublisher eventsPublisher;
    private final PropertyUnitRepository propertyUnitRepository;
    private final UserRepository userRepository;
    private final GridFsService gridFsService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Autowired
    PangoMailService mailService;



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
        if (!StringUtils.hasText(propertyUnit.getPropertyId())){
            propertyUnit.setPropertyId(PangoUtility.generateIdAsString());
            PropertyUnit savedUnit = propertyUnitRepository.save(propertyUnit);
            logger.info("Saved Property "+ savedUnit);

            if(savedUnit !=null && !propertyUnit.getAttachments().isEmpty()){
                OnAttachmentUploadEvent attachmentEvent = new OnAttachmentUploadEvent(
                       propertyUnit.getAttachments() .stream().map((attachment) ->  new AttachmentToUpload(savedUnit.getPropertyId(), ReferenceIdFor.PROPERTY, attachment,"")).collect(Collectors.toList()));
                        eventsPublisher.publishPangoEvent(attachmentEvent);
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

        //record user search rentingHistory
        recordUserSearchHistory(new UserSearchHistory(searchCriteria, propertyUnitGeoResults.getContent().size()), user);

        if(!propertyUnitGeoResults.getContent().isEmpty()){
            // associate cover photos
           List<GridFSDBFile> coverPhotos = gridFsService.getPropertiesCoverPhotos(propertyUnitGeoResults.getContent().parallelStream()
                    .map(propertyUnitGeoResult -> propertyUnitGeoResult.getContent().getPropertyId())
                    .collect(Collectors.toList()));
            Map<String, FileMetadata> propertyCoverPhoto = FileMetadata.getFileMetaFromGridFSDBFileAsMap(coverPhotos);

            return propertyCoverPhoto.isEmpty()? propertyUnitGeoResults.getContent() : propertyUnitGeoResults.getContent().stream()
                    .map(propertyUnitGeoResult -> {
                        PropertyUnit propertyUnit = propertyUnitGeoResult.getContent();
                        if(propertyCoverPhoto.containsKey(propertyUnit.getPropertyId()))
                           propertyUnit.setCoverPhoto(new AttachmentOld(propertyCoverPhoto.get(propertyUnit.getPropertyId())));
                        return new GeoResult<>(propertyUnit,propertyUnitGeoResult.getDistance());
                    }).collect(Collectors.toList());
        }
        return propertyUnitGeoResults.getContent();
    }

    /**
     * retrieve user's favorite properties
     *
     * @param user
     * @return
     */
    @Override
    public List<PropertyProjection> retrieveFavoritePropertiesBy(User user) throws EntityNotFound {
        Assert.notNull(user, "User can not be null");
        Assert.hasText(user.getUserReferenceId(), "user referenceId can not be null");
        User savedUsr = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
        if (savedUsr == null)
            throw new EntityNotFound(String.format("User : %s  does not exist", user.getUserReferenceId()), new IllegalArgumentException(String.format("User : %s  does not exist", user.getUserReferenceId())));

        if (!savedUsr.getFavouredProperties().isEmpty()) {
            List<PropertyUnit> propertyUnits = PangoUtility.toList(propertyUnitRepository.findAll(savedUsr.getFavouredProperties()));

            List<GridFSDBFile> coverPhotos = gridFsService.getPropertiesCoverPhotos(propertyUnits.parallelStream().map(propertyUnit -> propertyUnit.getPropertyId()).collect(Collectors.toList()));
            Map<String, FileMetadata> propertyCoverPhoto = FileMetadata.getFileMetaFromGridFSDBFileAsMap(coverPhotos);

            List<PropertyUnit> units = propertyUnits.parallelStream().map(propertyUnit -> {
                if (user.getLatitude() != 0.0 && user.getLongitude() != 0.0) { // calculate distance from user
                    double distance = DistanceCalculator.distance(user.getLatitude(), user.getLongitude(), propertyUnit.getLocation()[1], propertyUnit.getLocation()[0], "K");
                    propertyUnit.setDistance(distance);
                }
                //associate cover photos
                if (propertyCoverPhoto.containsKey(propertyUnit.getPropertyId()))
                    propertyUnit.setCoverPhoto(new AttachmentOld(propertyCoverPhoto.get(propertyUnit.getPropertyId())));
                return propertyUnit;
            }).collect(Collectors.toList());
            units.sort(Comparator.comparing(PropertyUnit::getDistance));
            return new ArrayList<>(units);
        }
        return new ArrayList<>();
    }

    /**
     * @param propertyReferenceId
     * @param user
     * @return
     */
    @Override
    public Optional<PropertyUnit> retrievePropertyBy(String propertyReferenceId, User user) {
        Optional<PropertyUnit> propertyUnit = Optional.ofNullable(propertyUnitRepository.findOne(propertyReferenceId));

        PropertySearchCriteria propertySearchCriteria = new PropertySearchCriteria();
        propertySearchCriteria.setPropertyReferenceId(propertyReferenceId);
        recordUserSearchHistory(new UserSearchHistory(propertySearchCriteria, propertyUnit.isPresent()?1:0), user);
        propertyUnit.ifPresent(p -> {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setReferenceId(p.getPropertyId());
            fileMetadata.setFileType(FileMetadata.FILETYPE.PHOTO.name());
            List<GridFSDBFile> attachments = gridFsService.getAllAttachments(fileMetadata, ReferenceIdFor.PROPERTY);
            p.setAttachments(FileMetadata.getFileMetaFromGridFSDBFileAsList(attachments).parallelStream().map(AttachmentOld::new).collect(Collectors.toList()));
        });

        return propertyUnit;
    }

    /**
     * Verifies user account token
     *
     * @param verificationToken
     * @return Optional<UserProjection>
     * @see TokensUtil#createAccountVerificationToken(User) for the expected token
     */
    @Override
    public Optional<UserProjection> verifyUserAccountBy(String verificationToken) throws Exception {
        Assert.hasText(verificationToken, "Verification token can not be null or empty");
        Assert.isTrue(verificationToken.contains(TOKEN_SEPERATOR), "Invalid token");
        String[] tokens = verificationToken.split(TOKEN_SEPERATOR);
        User usr = userRepository.findOne(tokens[0]);

        if (usr.getProfile().isVerified()) throw new IllegalStateException("User has already been verified");

        Optional<User> validatedToken = TokensUtil.validateVerificationToken(tokens[1], usr);
        User savedUsr = null;
        if (validatedToken.isPresent()) {
            savedUsr = userRepository.findByEmailAddressIgnoreCase(validatedToken.get().getEmailAddress());
            if (!savedUsr.getProfile().isVerified()) {
                savedUsr.getProfile().setVerified(true);
                savedUsr.getProfile().setVerificationDate(LocalDate.now());
                savedUsr = userRepository.save(savedUsr);
            }
        }
        return Optional.ofNullable(savedUsr);
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
        User existingUser = userRepository.findByEmailAddressIgnoreCase(user.getEmailAddress());
        if (existingUser != null && StringUtils.hasText(existingUser.getEmailAddress())) {
            if (!existingUser.getProfile().isVerified()) {
                triggerUserInteractionEvent(user, UserInteraction.VERIFICATION);
            }
            throw new EntityExists(String.format("User with Email : %s  address exists", user.getEmailAddress()), new IllegalArgumentException(String.format("User with Email : %s  address exists", user.getEmailAddress())));

        }
        user.getProfile().setVerified(false);
        User savedUser = null;
        String verificationToken = null;
        try {
            // generate account verification code
            verificationToken =  TokensUtil.createAccountVerificationToken(user);
            if (StringUtils.hasText(user.getProfile().getVerificationCode())) {
                user.getProfile().setPasswordChangeDate(LocalDateTime.now());
                savedUser = userRepository.save(user); // persist user details
                savedUser.setVerificationPathParam(user.getUserReferenceId() + ":" +verificationToken); // for email template use.
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e); //TODO deal with these exception scenario.
        }

        if (savedUser != null) triggerUserInteractionEvent(savedUser, UserInteraction.REGISTRATION);
        return Optional.ofNullable(savedUser);
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
        User user = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue(userName, userId);
        // associate user's profile picture
        if (user != null) {
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setReferenceId(user.getUserReferenceId());
            AttachmentOld userProfilePicture = new AttachmentOld(FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(gridFsService.getProfilePicture(fileMetadata, ReferenceIdFor.USER)), ReferenceIdFor.USER));
            user.getProfile().setProfilePicture(userProfilePicture);
        }

        return Optional.ofNullable(user);
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

    /**
     * updates user information based on {@link UserUpdating } value
     *
     * @param user
     * @param updating
     * @return {@link Optional < UserProjection >}
     */
    @Override
    public Optional<UserProjection> updateUserInformation(User user, UserUpdating updating) {
        Assert.notNull(user, "User to update can not be null or empty");
        Assert.hasText(user.getUserReferenceId(), "User Id can not be empty or null");
        Assert.notNull(updating, "User updating can nnot be null or empty");
        // only update existing users
        if (userRepository.exists(user.getUserReferenceId())) {
            switch (updating) {
                case PROFILE_PICTURE:
                    updateUserProfilePicture(user);
                    break;
                case PASSWORD_CHANGE:
                    changeUserPassword(user);
                    break;
                case BASIC_INFO:
                    return Optional.ofNullable(updateUserBasicInfo(user));
                default:
                    break;
            }
            return Optional.ofNullable(user);
        }
        return Optional.empty();
    }

    /**
     * add/remove a property by referenceId as a user's favorite depending favourable flag
     *
     * @param user
     * @param propertyReferenceId
     * @param favourable   true/false for add/remove
     * @return
     */
    @Override
    public Optional<UserProjection> updateUserFavoriteProperties(User user, String propertyReferenceId, boolean favourable) throws EntityNotFound {
        Assert.notNull(user, "user can not be null");
        Assert.hasText(user.getUserReferenceId(), "user reference Id ca not be null or empty");
        Assert.hasText(propertyReferenceId, "property referenceId can not null or empty");
        // Ensure that user exists
        User savedUsr = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
        if (savedUsr == null)
            throw new EntityNotFound(String.format("User : %s  does not exist", user.getUserReferenceId()), new IllegalArgumentException(String.format("User : %s  does not exist", user.getUserReferenceId())));

        //Ensure that property exists
        PropertyUnit propertyUnit = propertyUnitRepository.findOne(propertyReferenceId);
        if (propertyUnit == null)
            throw new EntityNotFound(String.format("Property : %s  does not exist", propertyReferenceId), new IllegalArgumentException(String.format("Property : %s  does not exist", propertyReferenceId)));

        return Optional.ofNullable(userRepository.updateFavouredProperties(savedUsr,propertyUnit,favourable).get());
    }

    private User updateUserBasicInfo(User user) {
        if (user != null) {
            User savedUsr = userRepository.findOne(user.getUserReferenceId());

            // Extract fields to update
            List<String> fieldsForUpdate = PangoUtility.fieldNamesByAnnotation(User.class, Updatable.class);
            try {
                boolean update =  PangoUtility.updatedSomeObjectProperties(savedUsr,user,fieldsForUpdate,User.class); // perform the update
                if (update) {
                    savedUsr = userRepository.save(savedUsr); // persist the updates
                }
            } catch (IntrospectionException e) {
                logger.error(e.getMessage(),e.getCause());
            }

            return savedUsr;
        }

        return user;
    }



    private void changeUserPassword(User user){
        if(user != null && user.getProfile() !=null) {
            if (StringUtils.hasText(user.getProfile().getPassword())){
                User savedUsr = userRepository.findOne(user.getUserReferenceId());
                savedUsr.getProfile().setPassword(user.getProfile().getPassword());
                savedUsr.getProfile().setPasswordChangeDate(LocalDateTime.now());
                userRepository.save(savedUsr);
            }
        }
    }
    private void updateUserProfilePicture(User user) {
        if (user != null && user.getProfile() != null) {
            if (user.getProfile().getProfilePicture() != null) {
                // 1. Delete an existing Profile picture
                FileMetadata fileMetadata = new FileMetadata();
                fileMetadata.setReferenceId(user.getUserReferenceId());
                fileMetadata.setFileType(FileMetadata.FILETYPE.PHOTO.name());
                gridFsService.deleteAllAttachmentsFor(fileMetadata, ReferenceIdFor.USER, false);

                // 2. Publish attachment upload event to upload the new photo
                user.getProfile().getProfilePicture().setProfilePicture(true); // make sure it's a profile pic
                AttachmentToUpload attachmentToUpload = new AttachmentToUpload(user.getUserReferenceId(), ReferenceIdFor.USER, user.getProfile().getProfilePicture(), "");
                eventsPublisher.publishPangoEvent(new OnAttachmentUploadEvent(attachmentToUpload));
            }
        }
    }

    private void recordUserSearchHistory(UserSearchHistory searchCriteria, User user){
        if (user == null || !StringUtils.hasText(user.getUserReferenceId())) return;
        executorService.submit(() -> {
            if( user != null) {
                OnPropertySearchEvent onPropertySearchEvent = new OnPropertySearchEvent(searchCriteria, user);
                logger.info("publishing Property Search event for user " + user.getUserReferenceId());
                eventsPublisher.publishPangoEvent(onPropertySearchEvent);
            }
        });
        return;
    }

    private void triggerUserInteractionEvent(User user, UserInteraction userInteraction){
        if (user == null || !StringUtils.hasText(user.getEmailAddress())) return;
        executorService.submit(() -> {
             if (userInteraction == UserInteraction.REGISTRATION ){
                 UserVerificationEvent verificationEvent = new UserVerificationEvent(user);
                 logger.info("publishing User email verification for user " + user.getUserReferenceId());
                 eventsPublisher.publishPangoEvent(verificationEvent);
             }
             if (userInteraction == UserInteraction.VERIFICATION ) {
                 //do nothing
                 logger.info("User : " + user.getEmailAddress() + " can not be allowed to login before verifying his/her account " );
             }
        });
        return;
    }

}