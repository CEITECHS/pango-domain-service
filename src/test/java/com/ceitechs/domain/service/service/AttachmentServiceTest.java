package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.AttachmentRepository;
import com.ceitechs.domain.service.repositories.AttachmentRepositoryTest;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author iddymagohe on 1/28/17.
 */
public class AttachmentServiceTest extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    AttachmentRepository attachmentRepository;

    @Test(expected = RuntimeException.class)
    public void storeAttachmentWithoutActualAttachmentTest() {
        Attachment attachment = AttachmentRepositoryTest.createAttachment();
        attachment.setAttachment(null);
        User user = new User();
        user.setUserReferenceId(PangoUtility.generateIdAsString());
        attachmentService.storeAttachment(user, attachment);
    }
    @Test
    public void storeAttachmentWithActualAttachmentTest() throws IOException {
        attachmentRepository.deleteAll();
        Attachment attachment = AttachmentRepositoryTest.createAttachment();
        User user = new User();
        user.setUserReferenceId(PangoUtility.generateIdAsString());
        attachment.setAttachment(new MockMultipartFile(resource.getFilename(), resource.getInputStream()));
        Optional<Attachment> attachmentOptional = attachmentService.storeAttachment(user, attachment);
        assertTrue(attachmentOptional.isPresent());
        assertNotNull(attachmentOptional.get().getUrl());
        System.out.println(attachmentOptional.get().getUrl());
    }
}
