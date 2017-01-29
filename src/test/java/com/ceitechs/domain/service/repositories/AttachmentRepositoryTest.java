package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * @author iddymagohe on 1/28/17.
 */
public class AttachmentRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Test
    public void saveTest(){
        attachmentRepository.deleteAll();
        Attachment attachment = createAttachment();
        attachmentRepository.save(attachment);
        Attachment savedAttachment = attachmentRepository.findOne(attachment.getReferenceId());
        assertEquals(attachment, savedAttachment);

    }

    @Test
    public void retrieveThumbnailsByParentReferenceIdsTest(){
        attachmentRepository.deleteAll();
        List<Attachment> attachments = createAttachments();
        attachmentRepository.save(attachments);
        List<Attachment> savedAttachments = attachmentRepository.findByParentReferenceIdInAndCategoryAndThumbnailTrueAndActiveTrue(attachments.stream()
                .map(Attachment::getParentReferenceId).distinct().collect(Collectors.toList()),Attachment.attachmentCategoryType.PROPERTY.name());
        assertNotNull(savedAttachments);
        assertThat("retrieved size should match saved saved size", savedAttachments,hasSize(attachments.stream()
                .filter(Attachment::isThumbnail).filter(Attachment::isActive).collect(Collectors.toList()).size()));
    }

    @Test
    public void retrieveByParentReferenceIdTest(){
        attachmentRepository.deleteAll();
        List<Attachment> localAttachments = createAttachments();
        attachmentRepository.save(localAttachments);
        Map<String, List<Attachment>> localAttachmentGrouping= localAttachments.stream().filter(Attachment::isActive).collect(Collectors.groupingBy(Attachment::getParentReferenceId));
        localAttachmentGrouping.forEach((k,v) -> {
            List<Attachment> retrievedList = attachmentRepository.findByParentReferenceIdAndCategoryAndActiveTrue(k,Attachment.attachmentCategoryType.PROPERTY.name());
            assertNotNull(retrievedList);
            assertThat("retrieved size should match saved saved size", retrievedList, hasSize(v.size()));
        });
    }

    public static Attachment createAttachment(){
        Attachment attachment = new Attachment();
        attachment.setBucket("test");
        attachment.setCategory(Attachment.attachmentCategoryType.PROPERTY.name());
        attachment.setDescription("test - attachment");
        attachment.setReferenceId(PangoUtility.generateIdAsString());
        attachment.setParentReferenceId(PangoUtility.generateIdAsString());
        return attachment;
    }

    /**
     * creates 10 attachments under 3 distinct parent of the same category
     * @return
     */
    private static List<Attachment> createAttachments() {
        String[] parents = {PangoUtility.generateIdAsString(), PangoUtility.generateIdAsString(), PangoUtility.generateIdAsString()};
        List<Attachment> attachments = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Attachment attachment = createAttachment();
            if (i % 2 == 0) {
                attachment.setParentReferenceId(parents[0]);
                if (i > 6) attachment.setActive(false);
                if (i == 6) attachment.setThumbnail(true);
            } else if (i % 3 == 0 && i % 2 != 0) {
                attachment.setParentReferenceId(parents[1]);
                if (i == 9) attachment.setThumbnail(true);
            }
            else attachment.setParentReferenceId(parents[2]);

            attachments.add(attachment);
        }

        return attachments;
    }
}
