package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.AttachmentToUpload;
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

    /**
     * Listen for <code>OnAttachmentEvent<List<AttachmentToUpload>> types of events</code> and fires to stores the respective attachments;
     * @param onAttachmentEvent
     */

    @Async
    @EventListener
    public void handleAttachmentsUpload(OnAttachmentEvent<List<AttachmentToUpload>> onAttachmentEvent){
        List<AttachmentToUpload> attachmentToUploadList = onAttachmentEvent.get();
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
}
