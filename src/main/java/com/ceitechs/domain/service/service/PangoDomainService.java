package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.AttachmentRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.OnPropertySearchEvent;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.ceitechs.domain.service.service.events.UserInteraction;
import com.ceitechs.domain.service.service.events.UserVerificationEvent;
import com.ceitechs.domain.service.util.DistanceCalculator;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.TokensUtil;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


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
     *  modify and existing property
     * @param propertyUnit
     * @param user
     * @return
     */
    Optional<PropertyUnit> updateProperty(PropertyUnit propertyUnit, User user);

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
     * verify that property does exist or remove it from the Pango.
     * @param user
     * @param property
     * @return
     */
    Optional<PropertyUnit> verifyProperty(User user, PropertyUnit property);

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
     *  Add user profile-picture or property-picture or correspondence-attachment
     * @param user
     * @param attachment
     * @return
     */
    Optional<Attachment> saveAttachment(User user, Attachment attachment);


    /**
     *  replace attachment or update attachment metadata user profile-picture or property-picture
     * @param user
     * @param attachment
     * @return
     */
    Optional<Attachment> updateAttachment(User user, Attachment attachment);



    /**
     * Removes the attachment by it's reference-id
     * @param user
     * @param attachmentReferenceId
     * @return
     */
    Optional<Attachment> deleteAttachment(User user, String attachmentReferenceId);



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

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private PropertyUnitEnquiryRepository enquiryRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Autowired
    PangoMailService mailService;

    @Autowired
    AttachmentService attachmentService;


    @Autowired
    public PangoDomainServiceImpl(PangoEventsPublisher eventsPublisher, PropertyUnitRepository propertyUnitRepository, UserRepository userRepository,AttachmentService attachmentService) {
        this.eventsPublisher = eventsPublisher;
        this.propertyUnitRepository = propertyUnitRepository;
        this.userRepository = userRepository;
        this.attachmentService = attachmentService;
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
        Assert.isTrue(PangoUtility.isPropertyOwner(user, propertyUnit) || PangoUtility.isCoordinator(user), "Property can only be added by it's owner or coordinator");

        String userId = propertyUnit.getOwner().getUserReferenceId();
        User owner = userRepository.findOne(userId);
        propertyUnit.setOwner(owner);
        if (!StringUtils.hasText(propertyUnit.getPropertyId())) {
            propertyUnit.setPropertyId(PangoUtility.generateIdAsString());
            propertyUnit.setVerified(PangoUtility.isCoordinator(user)); // verified only when added by a coordinator.
            PropertyUnit savedUnit = propertyUnitRepository.save(propertyUnit);
            logger.info("Saved Property " + savedUnit);
            if ( !PangoUtility.isCoordinator(user) && !owner.getProfile().getRoles().contains(PangoUserRole.RENTAL_OWNER)) {
                owner.getProfile().getRoles().add(PangoUserRole.RENTAL_OWNER);
                CompletableFuture.runAsync(() -> userRepository.save(owner));
            }

            return Optional.of(savedUnit);
        }
        //TODO : check for Existing
        return Optional.empty();
    }


    /**
     * verify that property does exist or remove it from the Pango.
     *
     * @param user
     * @param property
     * @return
     */
    @Override
    public Optional<PropertyUnit> verifyProperty(User user, PropertyUnit property) {
        throw new UnsupportedOperationException("yet to be implemented"); //TODO implementation
    }

    /**
     * modify and existing property
     *
     * @param propertyUnit
     * @param user
     * @return
     */
    @Override
    public Optional<PropertyUnit> updateProperty(PropertyUnit propertyUnit, User user) {
        throw new UnsupportedOperationException("yet to be implemented"); //TODO implementation
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
        Assert.notNull(searchCriteria, "Search criteria can not be null ");
        GeoResults<PropertyUnit> propertyUnitGeoResults = propertyUnitRepository.findAllPropertyUnits(searchCriteria);

        //record user search rentingHistory
        recordUserSearchHistory(new UserSearchHistory(searchCriteria, propertyUnitGeoResults.getContent().size()), user);

        if (!propertyUnitGeoResults.getContent().isEmpty()) {
            // associate cover photos
            List<String> propertyIds = propertyUnitGeoResults.getContent().stream().map(g -> g.getContent()).map(PropertyUnit::getPropertyId).collect(toList());
            List<Attachment> thumbnails = attachmentService.retrieveThumbnailAttachmentsBy(propertyIds, Attachment.attachmentCategoryType.PROPERTY.name());

            if (thumbnails != null && !thumbnails.isEmpty()) {
                Map<String, List<Attachment>> thumbnailsMap = thumbnails.stream().collect(groupingBy(Attachment::getParentReferenceId));
                return propertyUnitGeoResults.getContent().parallelStream()
                        .map(propertyUnitGeoResult -> {
                            PropertyUnit propertyUnit = propertyUnitGeoResult.getContent();
                            propertyUnit.setCoverPhoto(thumbnailsMap.get(propertyUnit.getPropertyId()).get(0));
                            return new GeoResult<>(propertyUnit, propertyUnitGeoResult.getDistance());
                        }).collect(toList());
            }

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

            List<Attachment> thumbnails = attachmentService.retrieveThumbnailAttachmentsBy(new ArrayList<>(savedUsr.getFavouredProperties()), Attachment.attachmentCategoryType.PROPERTY.name());
            Map<String, List<Attachment>> thumbnailsMap = thumbnails!=null && !thumbnails.isEmpty() ? thumbnails.stream().collect(groupingBy(Attachment::getParentReferenceId)): new HashMap<>();

            List<PropertyUnit> units = propertyUnits.parallelStream().map(propertyUnit -> {
                if (user.getLatitude() != 0.0 && user.getLongitude() != 0.0) { // calculate distance from user
                    double distance = DistanceCalculator.distance(user.getLatitude(), user.getLongitude(), propertyUnit.getLocation()[1], propertyUnit.getLocation()[0], "K");
                    propertyUnit.setDistance(distance);
                }
                //associate cover photos
                    propertyUnit.setCoverPhoto((AttachmentProjection) thumbnailsMap.get(propertyUnit.getPropertyId()));
                return propertyUnit;
            }).collect(toList());
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
        recordUserSearchHistory(new UserSearchHistory(propertySearchCriteria, propertyUnit.isPresent() ? 1 : 0), user);
        propertyUnit.ifPresent(p -> {
            List<Attachment> images = attachmentService.retrieveAttachmentsBy(Arrays.asList(p.getPropertyId()), Attachment.attachmentCategoryType.PROPERTY.name());
            p.setAttachments(new ArrayList<>(images));
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
        String verificationToken;
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
            Optional<Attachment> userProfilePicture = attachmentService.retrieveProfilePictureBy(user.getUserReferenceId(), Attachment.attachmentCategoryType.PROFILE_PICTURE.name());
            if (userProfilePicture.isPresent())
                user.getProfile().setProfilePicture(userProfilePicture.get());
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
                    //updateUserProfilePicture(user); TODO update this differently
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


    /**
     * Add user profile-picture or property-picture or correspondence
     *
     * @param user
     * @param attachment
     * @return
     */
    @Override
    public Optional<Attachment> saveAttachment(User user, Attachment attachment) {
        Assert.notNull(user, "user can not be null or Empty");
        Assert.notNull(attachment, "Attachment can not be null or empty");
        Assert.hasText(attachment.getCategory(), "Attachment-category can not be null or empty");
        User userSaved = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue(user.getEmailAddress(), "");

        if (userSaved != null) {
            if (PangoUtility.isAttachmentCategoty(attachment, Attachment.attachmentCategoryType.PROFILE_PICTURE)) {
                Assert.isTrue(PangoUtility.isProfilePictureOwner(userSaved, attachment), "profile picture can only be added or modified by it's owner ");
                return attachmentService.storeAttachment(userSaved, attachment);
            }

            if (PangoUtility.isAttachmentCategoty(attachment, Attachment.attachmentCategoryType.PROPERTY)) {
                PropertyUnit propertyUnit = propertyUnitRepository.findOne(attachment.getParentReferenceId());
                Assert.isTrue(PangoUtility.isPropertyOwner(userSaved, propertyUnit) || PangoUtility.isCoordinator(userSaved), " property pictures can only be added by owner or coordinator");
                return  attachmentService.storeAttachment(userSaved, attachment);
            }

            if (PangoUtility.isAttachmentCategoty(attachment, Attachment.attachmentCategoryType.CORRESPONDENCE)){
                PropertyUnitEnquiry propertyUnitEnquiry = enquiryRepository.findOne(attachment.getParentReferenceId());
                Assert.isTrue(PangoUtility.isEnquiryParticipant(userSaved, propertyUnitEnquiry), "Enquiry attachment can only be added or modified by it's respective participants.");
                return attachmentService.storeAttachment(userSaved, attachment);
            }
        } else {
            logger.error("User: " + user.getEmailAddress() + " does not exist or has not been verified, can no perform the operation");
        }

        return Optional.empty();
    }

    /**
     * replace attachment or update attachment metadata user profile-picture or property-picture
     *
     * @param user
     * @param attachment
     * @return
     */
    @Override
    public Optional<Attachment> updateAttachment(User user, Attachment attachment) {
        throw new UnsupportedOperationException(" yet to be implemented");
    }

    /**
     * Removes the attachment by it's reference-id
     * Attachment can only be removed by it's owner or coordinator for property attachments
     *
     * @param user
     * @param attachmentReferenceId
     * @return
     */
    @Override
    public Optional<Attachment> deleteAttachment(User user, String attachmentReferenceId) {
        Assert.hasText(attachmentReferenceId, "ReferenceId can not be null or empty");
        Assert.notNull(user, "user can not be null or empty");
        User userSaved = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue(user.getEmailAddress(), "");
        Assert.notNull(user, "user has not been verified or does not exists");
        Attachment attachment = attachmentRepository.findOne(attachmentReferenceId);
        Assert.notNull(attachment, "Attachment with reference-Id does not exists");
        if (userSaved != null && attachment != null) {
            if (attachment.getCategory().toUpperCase().equalsIgnoreCase(Attachment.attachmentCategoryType.PROFILE_PICTURE.name())) {
                Assert.isTrue(PangoUtility.isProfilePictureOwner(userSaved, attachment), "profile picture can only be removed by it's owner");
                return attachmentService.removeAttachmentBy(attachmentReferenceId);
            }
            if (attachment.getCategory().toUpperCase().equalsIgnoreCase(Attachment.attachmentCategoryType.PROPERTY.name())) {
                PropertyUnit propertyUnit = propertyUnitRepository.findOne(attachment.getParentReferenceId());
                Assert.isTrue(PangoUtility.isPropertyOwner(userSaved, propertyUnit) || PangoUtility.isCoordinator(userSaved), " property picture can only be removed by owner or coordinator");
                return attachmentService.removeAttachmentBy(attachmentReferenceId);
            }
        }
        return Optional.empty();
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