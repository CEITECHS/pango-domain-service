package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.ceitechs.domain.service.util.DistanceCalculator;
import com.ceitechs.domain.service.util.PangoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author by iddymagohe on 8/9/16.
 */
public interface PangoEnquiryService {

    /**
     * @param user
     * @param propertyReferenceId
     * @param enquiry
     * @return
     */
    Optional<EnquiryProjection> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists, EntityNotFound;

    /**
     * adds a {@link EnquiryCorrespondence}  to an existing {@link PropertyUnitEnquiry}
     *
     * @param user               prospective tenant or property owner
     * @param enquiryReferenceId
     * @param correspondence
     * @return
     * @throws EntityExists if there is 60days old open enquiry - otherwise.
     */
    Optional<EnquiryProjection> addEnquiryCorrespondence(User user, String enquiryReferenceId, EnquiryCorrespondence correspondence) throws EntityNotFound;

    /**
     * retrieves recent  enquiries made by the user to various properties
     *
     * @param prospectiveTenant
     * @param count             total enquiries to return
     * @return EnquiryProjection
     */
    List<EnquiryProjection> retrieveEnquiriesBy(User prospectiveTenant, int count) throws EntityNotFound;

    /**
     * retrieve all enquiries made to a property optionalPropertyReferenceId when passed,
     * Otherwise returns all enquiries made to properties owned by the by Owner.
     *
     * @param owner
     * @param optionalPropertyReferenceId
     * @param count                       recent enquiries count to return per property in-case of optionalPropertyReferenceId is not passed
     * @return
     */
    List<EnquiryProjection> retrieveEnquiriesBy(User owner, Optional<String> optionalPropertyReferenceId, int count);

    /**
     * retrieves all  enquires made by a user to property
     *
     * @param user
     * @param property
     * @return
     */
    Optional<EnquiryProjection> retrieveEnquiriesBy(User user, PropertyUnit property);

    /**
     * retrieves enquiry details with correspondence and urls to get to attachments if available
     *
     * @param user
     * @param enquiryReferenceId
     * @return
     */
    Optional<EnquiryProjection> retrieveEnquiryBy(User user, String enquiryReferenceId);


}

    @Service
    class PangoEnquiryServiceImpl implements PangoEnquiryService {

        private final PangoEventsPublisher eventsPublisher;

        private static final Logger logger = LoggerFactory.getLogger(PangoEnquiryServiceImpl.class);


        private final UserRepository userRepository;


        private final PropertyUnitRepository propertyUnitRepository;


        private final PropertyUnitEnquiryRepository enquiryRepository;

        @Autowired
        private AttachmentService attachmentService;


        @Autowired
        public PangoEnquiryServiceImpl(UserRepository userRepository, PropertyUnitRepository propertyUnitRepository, PropertyUnitEnquiryRepository enquiryRepository, PangoEventsPublisher eventsPublisher) {
            this.userRepository = userRepository;
            this.propertyUnitRepository = propertyUnitRepository;
            this.enquiryRepository = enquiryRepository;
            this.eventsPublisher = eventsPublisher;
        }

        /**
         * @param user
         * @param propertyReferenceId
         * @param enquiry
         * @return
         * @throws EntityNotFound for property or user
         * @throws EntityExists   for an open 60 days old enquiry by the user to the property
         */
        @Override
        public Optional<EnquiryProjection> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists, EntityNotFound {
            Assert.notNull(user, " for creating an enquiry user can not be null");
            Assert.hasText(user.getUserReferenceId(), "for creating an enquiry user reference id can not be null or empty");
            Assert.hasText(propertyReferenceId, "for creating an enquiry to a property: propertyReferenceId can not be null or empty");
            Assert.notNull(enquiry, "Enquiry can not be null");
            Assert.notNull(enquiry.getEnquiryType(), "Enquiry type can not be null or empty");
            Assert.notNull(enquiry.getMessage(), "Enquiry message can not be null or empty");

            // 1. check that a user exists and has verified their account
            User savedUser = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
            if (savedUser == null)
                throw new EntityNotFound(String.format("User : %s  does not exist or account has not been verified", user.getUserReferenceId()), new IllegalArgumentException(String.format("User : %s  does not exist or account has not been verified", user.getUserReferenceId())));

            //2. Check that a property exists
            PropertyUnit propertyUnit = propertyUnitRepository.findOne(propertyReferenceId);
            if (propertyUnit == null)
                throw new EntityNotFound(String.format("Property : %s  does not exist", propertyReferenceId), new IllegalArgumentException(String.format("Property : %s  does not exist", propertyReferenceId)));

            //3. check that the user doesn't have an open enquiry to this property
            PropertyUnitEnquiry existingEnquiry = enquiryRepository.findByProspectiveTenantAndPropertyUnitOrderByEnquiryDateDesc(savedUser, propertyUnit);
            if (existingEnquiry != null) {
                LocalDateTime sixtyDays = LocalDateTime.of(existingEnquiry.getEnquiryDate().toLocalDate(), existingEnquiry.getEnquiryDate().toLocalTime()).plusDays(60);
                if (existingEnquiry.getEnquiryDate().isBefore(sixtyDays))
                    throw new EntityExists(String.format("There is an open Enquiry : %s for this user on this property : %s", existingEnquiry.getEnquiryReferenceId(), existingEnquiry.getPropertyUnit().getPropertyId()), new IllegalStateException("there is an open Enquiry"));
            }
            //4. Create a new enquiry to a property
            enquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());
            enquiry.setPropertyUnit(propertyUnit);
            enquiry.setProspectiveTenant(savedUser);
            enquiry.setOwnerReferenceId(propertyUnit.getOwner().getUserReferenceId());

            PropertyUnitEnquiry savedEnquiry = enquiryRepository.save(enquiry);
            //TODO Trigger a push notification event to the owners
            return Optional.ofNullable(savedEnquiry);
        }

        /**
         * adds a {@link EnquiryCorrespondence}  to an existing {@link PropertyUnitEnquiry}
         *
         * @param user               prospective tenant or property owner
         * @param enquiryReferenceId
         * @param correspondence
         * @return
         */
        @Override
        public Optional<EnquiryProjection> addEnquiryCorrespondence(User user, String enquiryReferenceId, EnquiryCorrespondence correspondence) throws EntityNotFound {
            Assert.notNull(user, "user can not be null for adding correspondence to an exiting Enquiry");
            Assert.hasText(user.getUserReferenceId(), "User referenceId can not be null");
            Assert.hasText(enquiryReferenceId, "enquiryReferenceId can not be null or empty");
            Assert.notNull(correspondence, "correspondence can not be null");
            Assert.hasText(correspondence.getMessage(), "correspondence message can not be empty");
            Assert.notNull(correspondence.getCorrespondenceType(), "Correspondence type can not be null");
            PropertyUnitEnquiry propertyUnitEnquiry = enquiryRepository.findOne(enquiryReferenceId);

            //1. check that Enquiry with exists.
            if (propertyUnitEnquiry == null)
                throw new EntityNotFound(String.format("Enquiry with ID: %s not found", enquiryReferenceId), new IllegalArgumentException(String.format("Enquiry with ID: %s not found", enquiryReferenceId)));

            //2. Ensure that correspondence can only be added by property owner or a user who initiated the enquiry
            Assert.isTrue(PangoUtility.isEnquiryParticipant(user, propertyUnitEnquiry), String.format("User : %s not allowed to modify the  Enquiry", user.getUserReferenceId()));


            //3. add correspondence to the enquiry
            correspondence.setCorrespondenceReferenceId(PangoUtility.generateIdAsLong());
            correspondence.setOwner(propertyUnitEnquiry.getPropertyUnit().getOwner().getUserReferenceId().equals(user.getUserReferenceId()));
            Optional<PropertyUnitEnquiry> enquiryOptional = enquiryRepository.updateEnquiryWith(enquiryReferenceId, correspondence);

            //TODO trigger notification

            return enquiryOptional.isPresent() ? Optional.of(enquiryOptional.get()) : Optional.empty();
        }

        /**
         * retrieves recent  enquiries made by the user to various properties
         *
         * @param prospectiveTenant
         * @param count             total enquiries to return
         * @return
         */
        @Override
        public List<EnquiryProjection> retrieveEnquiriesBy(User prospectiveTenant, int count) throws EntityNotFound {
            Assert.notNull(prospectiveTenant, "User can not be null");
            Assert.hasText(prospectiveTenant.getUserReferenceId(), "User Identifier can not be null or empty");

            User savedUsr = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", prospectiveTenant.getUserReferenceId());
            if (savedUsr == null)
                throw new EntityNotFound(String.format("Unknown user %s  ", prospectiveTenant.toString()), new IllegalArgumentException(String.format("Unkown user %s  ", prospectiveTenant.toString())));
            Page<PropertyUnitEnquiry> result = enquiryRepository.findByProspectiveTenantOrderByEnquiryDateDesc(savedUsr, new PageRequest(0, count > 0 ? count : 50));

              //retrieve cover photos to properties.
            if (result.getContent() != null || !result.getContent().isEmpty()) {
                CompletableFuture<Map<String, List<Attachment>>> completableFuturePropertiesThumbnails = CompletableFuture.supplyAsync(() -> {
                    List<Attachment> attachments = attachmentService.retrieveThumbnailAttachmentsBy(result.getContent().stream()
                            .map(enquiry -> enquiry.getPropertyUnit().getPropertyId()).collect(toList()), Attachment.attachmentCategoryType.PROPERTY.name());
                    return attachments.stream().collect(groupingBy(Attachment::getParentReferenceId));
                });

                // calculate and associate distance btn a user and properties they're enquired
                result.getContent().forEach(enquiry -> {
                    if (prospectiveTenant.getLatitude() != 0.0 && prospectiveTenant.getLongitude() != 0.0) { // calculate distance from user
                        double distance = DistanceCalculator.distance(prospectiveTenant.getLatitude(), prospectiveTenant.getLongitude(), enquiry.getPropertyUnit().getLocation()[1], enquiry.getPropertyUnit().getLocation()[0], "K");
                        enquiry.getPropertyUnit().setDistance(distance);
                    }
                });

                //associate cover photos to properties when available
                try {
                    Map<String, List<Attachment>> retrievedAttachments = completableFuturePropertiesThumbnails.get(1500, TimeUnit.MILLISECONDS);
                    result.getContent().stream().forEach(enquiry -> {
                        if (retrievedAttachments.containsKey(retrievedAttachments.get(enquiry.getPropertyUnit().getPropertyId())))
                            enquiry.getPropertyUnit().setCoverPhoto(retrievedAttachments.get(enquiry.getPropertyUnit().getPropertyId()).get(0));
                    });

                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error(" Un-exception has occurred while retrieving property thumbnail to associate to Enqury");

                }

            }

            return new ArrayList<>(result.getContent());
        }

        /**
         * retrieve all enquiries made to a property optionalPropertyReferenceId when passed,
         * Otherwise returns all enquiries made to properties owned by the by Owner.
         *
         * @param owner
         * @param optionalPropertyReferenceId
         * @param count                       recent enquiries count to return per property in-case of optionalPropertyReferenceId is not passed
         * @return
         */
        @Override
        public List<EnquiryProjection> retrieveEnquiriesBy(User owner, Optional<String> optionalPropertyReferenceId, int count) {
            Assert.notNull(owner, "Owner can not be null");
            Assert.hasText(owner.getUserReferenceId(), " Owner referenceId can not be null or empty");

            List<PropertyUnitEnquiry> enquiries = new ArrayList<>();
            // Enquiries made to a property
            if (optionalPropertyReferenceId.isPresent()) {
                PropertyUnit propertyUnit = propertyUnitRepository.findOne(optionalPropertyReferenceId.get());
                if (propertyUnit != null && propertyUnit.getOwner().getUserReferenceId().equals(owner.getUserReferenceId()))
                    enquiries = enquiryRepository.findByPropertyUnitOrderByEnquiryDateDesc(propertyUnit, new PageRequest(0, count > 0 ? count : 50)).getContent();

            } else {
                User savedOwner = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", owner.getUserReferenceId());
                if (savedOwner != null)
                    enquiries = enquiryRepository.findByOwnerReferenceIdOrderByEnquiryDateDesc(savedOwner.getUserReferenceId(), new PageRequest(0, count > 0 ? count : 50)).getContent();

            }
            return new ArrayList<>(enquiries);
        }

        @Override
        public Optional<EnquiryProjection> retrieveEnquiriesBy(User user, PropertyUnit property) {
            Assert.notNull(user, "User can not be null");
            Assert.notNull(property, "Property can not be null");
            Assert.hasText(user.getUserReferenceId(), "User referenceId can not be null or empty");
            Assert.hasText(property.getPropertyId(), "property referenceId can not be null ot empty");

            CompletableFuture<User> prospectiveTenantFuture = CompletableFuture.supplyAsync(() -> userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId()));
            CompletableFuture<PropertyUnit> propertyUnitFuture = CompletableFuture.supplyAsync(() -> propertyUnitRepository.findOne(property.getPropertyId()));
            if (propertyUnitFuture.isDone() && propertyUnitFuture.isDone()) {
                try {
                    User usr = prospectiveTenantFuture.get();
                    PropertyUnit prt = propertyUnitFuture.get();
                    Optional<EnquiryProjection> enquiryProjection = Optional.ofNullable(enquiryRepository.findByProspectiveTenantAndPropertyUnitOrderByEnquiryDateDesc(user, prt));

                    if (user.getLatitude() != 0.0 && user.getLongitude() != 0.0) {
                        double distance = DistanceCalculator.distance(user.getLatitude(), user.getLongitude(), prt.getLocation()[1], prt.getLocation()[0], "K");
                        enquiryProjection.ifPresent(enquiryProjection1 -> enquiryProjection1.getPropertyUnit().setDistance(distance));

                    }
                    return enquiryProjection;
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("while retrieval   a user and property" + e.getMessage(), e.getCause());
                }
            }

            return Optional.empty();
        }

        /**
         * retrieves enquiry details with correspondence and urls to get to attachments if available
         *
         * @param user
         * @param enquiryReferenceId
         * @return
         */
        @Override
        public Optional<EnquiryProjection> retrieveEnquiryBy(User user, String enquiryReferenceId) {
            Assert.notNull(user, "user can not be null");
            Assert.hasText(user.getUserReferenceId(), "user reference Id can not be null");
            Assert.hasText(enquiryReferenceId, " Enquiry referenceId can not be null or empty");

            PropertyUnitEnquiry enquiry = enquiryRepository.findOne(enquiryReferenceId);

            if (enquiry != null) {
                try {
                    List<Attachment> attachments = attachmentService.retrieveAttachmentsBy(Arrays.asList(enquiryReferenceId), Attachment.attachmentCategoryType.CORRESPONDENCE.name());
                    enquiry.setAttachments(new ArrayList<>(attachments));
                    enquiry.getAttachments().sort(Comparator.comparing(AttachmentProjection::getCreatedDate).reversed());
                } catch (Exception ex) {
                    logger.error("Error occurred while retrieving attachments associated with Enquiry: " + enquiryReferenceId);
                }

                //ensure that a right user is accessing the enquiry
                Assert.isTrue(PangoUtility.isEnquiryParticipant(user, enquiry), String.format("User : %s not allowed to retrieve the  Enquiry Details", user.getUserReferenceId()));
                //enquiry.getCorrespondences().forEach(correspondence -> correspondence.setAttachmentId(enquiry.getEnquiryReferenceId() + "-" + correspondence.getCorrespondenceReferenceId())); // associate attachmentId
                enquiry.getCorrespondences().sort(Comparator.comparing(EnquiryCorrespondence::getCorrespondenceDate).reversed()); // sort by most recent


                if (user.getLatitude() != 0.0 && user.getLongitude() != 0.0) { // calculate distance from user
                    double distance = DistanceCalculator.distance(user.getLatitude(), user.getLongitude(), enquiry.getPropertyUnit().getLocation()[1], enquiry.getPropertyUnit().getLocation()[0], "K");
                    enquiry.getPropertyUnit().setDistance(distance);

                }
            }
            return Optional.ofNullable(enquiry);
        }

    }
