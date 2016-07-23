package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.AttachmentToUpload;

import java.util.Arrays;
import java.util.List;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class OnAttachmentUploadEvent implements OnPangoEvent<List<AttachmentToUpload>> {
    private final List<AttachmentToUpload> attachmentToUpload;

    public OnAttachmentUploadEvent(List<AttachmentToUpload> attachmentToUpload) {
        this.attachmentToUpload = attachmentToUpload;
    }

    public OnAttachmentUploadEvent(AttachmentToUpload attachmentToUpload) {
        this.attachmentToUpload = Arrays.asList(attachmentToUpload);
    }

    @Override
    public List<AttachmentToUpload> get() {
        return attachmentToUpload;
    }
}
