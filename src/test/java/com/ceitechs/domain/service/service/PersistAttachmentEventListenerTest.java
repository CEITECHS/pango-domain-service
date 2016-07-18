package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PersistAttachmentEventListenerTest extends AbstractPangoDomainServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PersistAttachmentEventListenerTest.class);


    @Autowired
    private GridFsOperations operations;

    @Autowired
    PangoEventsPublisher pangoEventsPublisher;

    @Test
    public void publishAnEventToStoreTest() throws IOException {
        operations.delete(null);
        assertTrue(operations.find(null).size() == 0);

        OnAttachmentEvent<List<AttachmentToUpload>> listSupplier = () -> Arrays.asList(new AttachmentToUpload("1",ReferenceIdFor.PROPERTY,buildAttachment(), ""));
        pangoEventsPublisher.publishAttachmentUploadedEvent(listSupplier);

        List<GridFSDBFile> files = operations.find(null);
        assertTrue(files.size() > 0);
    }

    private static Attachment buildAttachment()  {
        Attachment attachment = new Attachment();
        attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
        attachment.setFileName(resource.getFilename());
        try {
            attachment.setFileSize(resource.getFile().length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        attachment.setFileDescription("profile_picture");
        attachment.setProfilePicture(true);
        return attachment;
    }

}
