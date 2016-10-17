package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.PropertyHoldingHistory;
import com.ceitechs.domain.service.domain.PropertyRentalHistory;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.PropertyHoldingHistoryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.events.PangoEventsPublisher;
import com.ceitechs.domain.service.service.events.PropertyHoldingEvent;
import com.ceitechs.domain.service.util.PangoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author iddymagohe on 8/29/16.
 */

// deals with holding and renting operations Pango's properties

public interface PangoPropertyLeasingService {
    /**
     * @param user                interested in this property
     * @param propertyReferenceId
     * @return
     * @throws EntityExists if there is an outstanding Holding to the property.
     */
    Optional<PropertyHoldingHistory> createPropertyHoldingRequestBy(User user, String propertyReferenceId) throws EntityExists, EntityNotFound;

    /**
     * user/owner updates to an existing holding request, holding start-date is not updatable
     *
     * @param holdingHistory updated with the respective updates
     * @param user           making the updates (Owner makes decision Accept/Reject), Requester cancels if allowed.
     * @param isOwner        indicates whether user is the owner
     * @return
     */
    Optional<PropertyHoldingHistory> updatePropertyHoldingRequest(PropertyHoldingHistory holdingHistory, User user, boolean isOwner) throws EntityNotFound;

    /**
     * retrieves all an expired holding requests made by the user or made to the owners properties.
     *
     * @param user
     * @param isOwner indicates whether user is the owner
     * @return
     */
    List<PropertyHoldingHistory> retrievesHoldingHistoryBy(User user, boolean isOwner) throws EntityNotFound;

    /**
     *  creates a new rental contract for a pango property- initiated by Owner's.
     * @param owner
     * @param rentalHistory
     * @return
     * @throws EntityNotFound
     * @throws EntityExists
     */
    Optional<PropertyRentalHistory> createPropertyRentalContract(User owner, PropertyRentalHistory rentalHistory) throws EntityNotFound, EntityExists;
}

@Service
class PropertyLeasingServiceImpl implements PangoPropertyLeasingService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyLeasingServiceImpl.class);

    private final UserRepository userRepository;
    private final PropertyUnitRepository propertyUnitRepository;
    private final PropertyHoldingHistoryRepository propertyHoldingHistoryRepository;

    @Autowired
    PangoEventsPublisher eventsPublisher;

    @Value("${property.holding.hours}")
    private int holdingHours;

    @Autowired
    public PropertyLeasingServiceImpl(UserRepository userRepository, PropertyUnitRepository propertyUnitRepository, PropertyHoldingHistoryRepository propertyHoldingHistoryRepository) {
        this.userRepository = userRepository;
        this.propertyUnitRepository = propertyUnitRepository;
        this.propertyHoldingHistoryRepository = propertyHoldingHistoryRepository;
    }

    /**
     * @param user                interested in this property
     * @param propertyReferenceId
     * @return
     * @throws EntityExists if there is an outstanding Holding to the property.
     */
    @Override
    public Optional<PropertyHoldingHistory> createPropertyHoldingRequestBy(User user, String propertyReferenceId) throws EntityExists, EntityNotFound {
        Assert.notNull(user, "User initiating a holding request can not be null ");
        Assert.hasText(user.getUserReferenceId(), "userId initiating a holding request can not be null or empty ");
        Assert.hasText(propertyReferenceId, "property referenceId can not be null or empty");
        PropertyUnit property = propertyUnitRepository.findByPropertyIdAndActiveTrue(propertyReferenceId); // consider available property
        if (property == null)
            throw new EntityNotFound(String.format("Property : %s does not exist or can not be held.", propertyReferenceId), new IllegalArgumentException(String.format("Property : %s does not exist or can not be held.", propertyReferenceId)));
        // Asynchronous get the a verified user by Id
        CompletableFuture<User> userCompletableFuture = CompletableFuture.supplyAsync(() -> userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId()));
        // Check for any outstanding holding on this property.
        PropertyHoldingHistory holdingHistory = propertyHoldingHistoryRepository.findByPropertyUnitAndPhaseNotInOrderByCreatedDateDesc(property, Arrays.asList(PropertyHoldingHistory.HoldingPhase.CANCELLED, PropertyHoldingHistory.HoldingPhase.EXPIRED));
        if (holdingHistory != null)
            throw new EntityExists(String.format("Property : %s has an outstanding holding", propertyReferenceId), new IllegalArgumentException(String.format("Property : %s has an outstanding holding", propertyReferenceId)));

        try {
            User savedUser = userCompletableFuture.get();
            if (savedUser == null)
                throw new EntityNotFound(String.format("Unknown User : %s ", user.getUserReferenceId()), new IllegalArgumentException(String.format("Unknown User : %s ", user.getUserReferenceId())));
            // creates a new holding request
            PropertyHoldingHistory propertyHoldingHistory = new PropertyHoldingHistory();
            propertyHoldingHistory.setHoldingReferenceId(PangoUtility.generateIdAsString());
            propertyHoldingHistory.setPropertyUnit(property);
            propertyHoldingHistory.setUser(savedUser);
            propertyHoldingHistory.setStartDate(LocalDateTime.now());
            propertyHoldingHistory.setEndDate(propertyHoldingHistory.getStartDate().plusHours(holdingHours));
            propertyHoldingHistory.setOwnerReferenceId(property.getOwner().getUserReferenceId());
            propertyHoldingHistory.setPhase(PropertyHoldingHistory.HoldingPhase.INITIATED);
            logger.info(String.format("saving a new holding request for user : %s on property : %s  ", user.getUserReferenceId(), propertyReferenceId));
            PropertyHoldingHistory savedHoldingHistory = propertyHoldingHistoryRepository.save(propertyHoldingHistory);
            // trigger a holding event
            CompletableFuture.runAsync(() -> {
                logger.info(String.format("publishing holding INITIATED event : %s ", savedHoldingHistory.getHoldingReferenceId()));
                eventsPublisher.publishPangoEvent(new PropertyHoldingEvent(savedHoldingHistory, property.getOwner()));
            });

            return Optional.ofNullable(savedHoldingHistory);

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e.getCause());
        }

        return Optional.empty();
    }

    /**
     * user/owner updates to an existing holding request
     *
     * @param holdingHistory updated with the respective updates
     * @param user           making the updates (Owner makes decision Accept/Reject), Requester cancels if allowed.
     * @param isOwner        indicates whether user is the owner
     * @return
     */
    @Override
    public Optional<PropertyHoldingHistory> updatePropertyHoldingRequest(PropertyHoldingHistory holdingHistory, User user, boolean isOwner) throws EntityNotFound {
        Assert.notNull(holdingHistory, "holding history can not be null");
        Assert.hasText(holdingHistory.getHoldingReferenceId(), "Holding referenceId can not be null or empty");
        Assert.notNull(user, "user can not be null");
        Assert.hasText(user.getUserReferenceId(), "User can not be null or empty");
        User savedUser = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
        if (savedUser == null)
            throw new EntityNotFound(String.format("Unknown/Unverified User with Id : %s ", user.getUserReferenceId()), new IllegalArgumentException(String.format("Unknown/Unverified User with Id : %s ", user.getUserReferenceId())));

        PropertyHoldingHistory savedHoldingHistory = propertyHoldingHistoryRepository.findOne(holdingHistory.getHoldingReferenceId());
        if (savedHoldingHistory == null)
            throw new EntityNotFound(String.format("Unknown HoldingHistory with Id : %s ", holdingHistory.getHoldingReferenceId()), new IllegalArgumentException(String.format("Unknown HoldingHistory with Id : %s ", holdingHistory.getHoldingReferenceId())));

        Assert.isTrue(savedHoldingHistory.getUser().equals(savedUser) || savedHoldingHistory.getOwnerReferenceId().equals(savedUser.getUserReferenceId()), "Holding History can only be updated by the initiator or property owner");

        Assert.isTrue(savedHoldingHistory.getPhase() == PropertyHoldingHistory.HoldingPhase.INITIATED, "Can not update holding history in status : " + savedHoldingHistory.getPhase().name());

        if (isOwner && savedHoldingHistory.getOwnerReferenceId().equals(savedUser.getUserReferenceId())) { // Owner updated Accept/Reject
            savedHoldingHistory.setPhase(PropertyHoldingHistory.HoldingPhase.DECIDED);
            savedHoldingHistory.setHoldingRequestAccepted(holdingHistory.isHoldingRequestAccepted());
            savedHoldingHistory.setDecisionDetails(holdingHistory.getDecisionDetails());
            propertyHoldingHistoryRepository.save(savedHoldingHistory);
            CompletableFuture.runAsync(() -> {
                logger.info(String.format("publishing holding DECIDED event : %s ", savedHoldingHistory.getHoldingReferenceId()));
                eventsPublisher.publishPangoEvent(new PropertyHoldingEvent(savedHoldingHistory, savedUser));
            });
        } else {  // user updates - Cancel
            savedHoldingHistory.setPhase(PropertyHoldingHistory.HoldingPhase.CANCELLED);
            propertyHoldingHistoryRepository.save(savedHoldingHistory);
        }

        return Optional.ofNullable(savedHoldingHistory);
    }

    /**
     * retrieves all un expired holding requests made by the user or made to the owner properties.
     *
     * @param user
     * @param isOwner indicates whether user is the owner
     * @return
     */
    @Override
    public List<PropertyHoldingHistory> retrievesHoldingHistoryBy(User user, boolean isOwner) throws EntityNotFound {
        Assert.notNull(user, "user can not be null ");
        Assert.hasText(user.getUserReferenceId(), "User referenceId can not be null or empty");
        User savedUser = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
        if (savedUser == null)
            throw new EntityNotFound(String.format("Unknown/Unverified User with Id : %s ", user.getUserReferenceId()), new IllegalArgumentException(String.format("Unknown/Unverified User with Id : %s ", user.getUserReferenceId())));

        List<PropertyHoldingHistory> propertyHoldingHistories = isOwner ? propertyHoldingHistoryRepository.findByOwnerReferenceIdAndAndPhaseNotInOrderByStartDateDesc(user.getUserReferenceId(), Arrays.asList(PropertyHoldingHistory.HoldingPhase.EXPIRED, PropertyHoldingHistory.HoldingPhase.CANCELLED)) :
                propertyHoldingHistoryRepository.findByUserAndAndPhaseNotInOrderByStartDateDesc(savedUser, Arrays.asList(PropertyHoldingHistory.HoldingPhase.EXPIRED, PropertyHoldingHistory.HoldingPhase.CANCELLED), new PageRequest(0, 50)).getContent();

        return propertyHoldingHistories;
    }

    /**
     * creates a new rental contract for a pango property- initiated by Owner's.
     *
     * @param owner
     * @param rentalHistory
     * @return
     * @throws EntityNotFound
     * @throws EntityExists
     */
    @Override
    public Optional<PropertyRentalHistory> createPropertyRentalContract(User owner, PropertyRentalHistory rentalHistory) throws EntityNotFound, EntityExists {
        return null; //TODO impls
    }

    //TODO mark holdings expired, rentals.
}
