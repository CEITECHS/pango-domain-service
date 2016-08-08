package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.*;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.service.GridFsService;
import com.ceitechs.domain.service.util.PangoUtility;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private GridFsService gridFsService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Listen for <code>OnPangoEvent<List<AttachmentToUpload>> types of events</code> and fires to stores the respective attachments;
     * @param onPangoEvent
     */

    @Async
    @EventListener
    public void handleAttachmentsUpload(OnPangoEvent<List<AttachmentToUpload>> onPangoEvent){
        List<AttachmentToUpload> attachmentToUploadList = onPangoEvent.get();
        if (!CollectionUtils.isEmpty(attachmentToUploadList)){
            attachmentToUploadList.stream()
                    .filter(attachmentToUpload -> attachmentToUpload.getAttachment() != null && (StringUtils.hasText(attachmentToUpload.getAttachment().getFileName()) && StringUtils.hasText(attachmentToUpload.getAttachment().getContentBase64())))
                    .forEach(attachmentToUpload -> {
                        Map<String, String> metadata = PangoUtility.attachmentMetadataToMap(attachmentToUpload);
                        try {
                            PangoUtility.Base64ToInputStream(Optional.of(attachmentToUpload.getAttachment().getContentBase64()))
                                    .ifPresent(inputStream -> {
                                        gridFsService.storeFiles(inputStream, metadata, BasicDBObject::new);
                                        logger.info("stored attachment" + attachmentToUpload);
                                    });
                        } catch (Exception e) {
                            logger.error("An error has Occured while trying to save " + attachmentToUpload, e.getCause());
                        }
                    });
        }
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
            logger.info("Updated search history for user : " + searchHistoryOnPangoEvent.getUser().getUserReferenceId());
        else
            logger.info("Could not update search history for user : " + searchHistoryOnPangoEvent.getUser().getUserReferenceId());

    }

    @Async
    @EventListener
    public void HandleUserInteractionEvents(OnPangoEvent<User> userOnPangoEvent){
        if (userOnPangoEvent instanceof UserVerificationEvent){
            //TODO - generate verification code (check for existing of expired ) and trigger email to user
            logger.info("generating verfication code for user");
        }
    }
}
