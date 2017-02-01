package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.EmailModel;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.service.PangoMailService;
import com.ceitechs.domain.service.service.UserProjection;
import com.ceitechs.domain.service.util.PangoUtility;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author iddymagohe
 * @since 1.0
 */

@Service
public class PangoEventsListener{
    private static final Logger logger = LoggerFactory.getLogger(PangoEventsListener.class);

    @Value("${user.verification.uri}")
    private String verificationUri;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    PangoMailService mailService;

    /**
     * Listen for <code>OnPangoEvent<List<AttachmentToUpload>> types of events</code> and fires to stores the respective attachments;
     * @param onPangoEvent
     */

    @Async
    @EventListener
    public void handleAttachmentsUpload(OnPangoEvent<List<AttachmentToUpload>> onPangoEvent){
       //TODO IMPL
    }

    @Async
    @EventListener
    public void handleUserSearchEvent(OnPangoEvent<UserSearchHistory> searchHistoryOnPangoEvent) {
        UserPreference userPreference = new UserPreference();
        userPreference.setCategory(UserPreference.PreferenceCategory.SEARCH);
        userPreference.setPreferenceType(UserPreference.PreferenceType.NotforDisplay);
        userPreference.setUserSearchHistory(searchHistoryOnPangoEvent.get());
        Optional<User> resp = userRepository.addUserPreferences(userPreference, searchHistoryOnPangoEvent.getUser());
        if (resp.isPresent())
            logger.info("Updated search rentingHistory for user : " + searchHistoryOnPangoEvent.getUser().getUserReferenceId());
        else
            logger.info("Could not update search rentingHistory for user : " + searchHistoryOnPangoEvent.getUser().getUserReferenceId());

    }

    @Async
    @EventListener
    public void handleUserInteractionEvents(OnPangoEvent<User> userOnPangoEvent) {
        if (userOnPangoEvent instanceof UserVerificationEvent) {
            logger.info("generating verification code for user: " + userOnPangoEvent.get().getEmailAddress());

            User user = userOnPangoEvent.get();

            user.setVerificationPathParam(verificationUri + user.getVerificationPathParam());
            UserProjection usr = user;
            //initiate email verification to user.

            EmailModel<UserProjection> emailModel = new EmailModel<>();
            emailModel.setTemplate("registration-confirmation");//TODO: externalize this and subject-line && Capture whether emails were sent
            emailModel.setRecipients(new String[]{usr.getEmailAddress()});
            emailModel.setSubject("Pango - Registration confirmation"); //
            emailModel.setModel(usr);
            logger.info("sending registration confirmation email to User :" + usr.getEmailAddress());
            mailService.sendEmail(emailModel);
            logger.info("Completed - sending registration confirmation email to User :" + usr.getEmailAddress());

        }

    }

    @Async
    @EventListener
    public void handlePropertyHoldingEvents(OnPangoEvent<PropertyHoldingHistory> holdingHistoryOnPangoEvent){
        //TODO holding events implementation
        PropertyHoldingHistory propertyHoldingHistory = holdingHistoryOnPangoEvent.get();
        User owner = holdingHistoryOnPangoEvent.getUser(); // for notification purposes.
        if (propertyHoldingHistory == null ) return;
        switch(propertyHoldingHistory.getPhase()){
            case INITIATED:
                //1 . notify (email, push )the property owner
                logger.info("initiating holding event for : " + propertyHoldingHistory.getHoldingReferenceId());
                //TODO send email to owner on this
                break;
            case DECIDED:
                logger.info("decided holding event for : " + propertyHoldingHistory.getHoldingReferenceId());
                //2. if accepted initiate payment for the duration (X) days this must go through.
                //2.1.0 update the holding with payments, holding start/end date.
                //2.1.1 notify the users about the accepted/declined holding decision
                break;
            default:
                break;
        }
    }


    @Async
    @EventListener
    public void handlePropertyRentingEvents(OnPangoEvent<PropertyRentalHistory> rentalHistoryOnPangoEvent){
        //TODO holding events implementation
        PropertyRentalHistory propertyRentalHistory = rentalHistoryOnPangoEvent.get();
        User owner = rentalHistoryOnPangoEvent.getUser(); // for notification purposes.
        if (propertyRentalHistory == null ) return;
        switch(propertyRentalHistory.getPhase()){
            case INITIATED:
                //1 . notify (email, push ) to the property owner/renter to initiate payment, user registration for unregistered users
                logger.info("initiating holding event for : " + propertyRentalHistory.getRentalReferenceId());
                //TODO send email to owner on this
                break;
            case PROCESSING:
                logger.info("PROCESSING rental event for : " + propertyRentalHistory.getRentalReferenceId());
                break;
            case PROCESSED:
                logger.info("PROCESSED rental event for : " + propertyRentalHistory.getRentalReferenceId());
                //2. payment processed and completed, notify the respective parties
                //2.1.0 update the rental with payments, holding start/end date.
                //2.1.1 notify the users about the accepted/declined payment decision
                break;
            case FULFILLED:
                logger.info("FULFILLED rental event for : " + propertyRentalHistory.getRentalReferenceId());
                //3. payed for, pending acceptance by owner and renter
                break;
            case ACCEPTED:
                logger.info("ACCEPTED rental event for : " + propertyRentalHistory.getRentalReferenceId());
                //accepted for all parties (owner and renter)
                break;
            case CANCELLED:
                logger.info("CANCELLED rental event for : " + propertyRentalHistory.getRentalReferenceId());
                break;
            default:
                break;
        }
    }


}
