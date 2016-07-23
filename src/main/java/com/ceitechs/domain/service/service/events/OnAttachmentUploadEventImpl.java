package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.AttachmentToUpload;

import java.util.Arrays;
import java.util.List;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class OnAttachmentUploadEventImpl implements OnPangoEvent<List<AttachmentToUpload>> {
    private final List<AttachmentToUpload> attachmentToUpload;

    public OnAttachmentUploadEventImpl(List<AttachmentToUpload> attachmentToUpload) {
        this.attachmentToUpload = attachmentToUpload;
    }

    public OnAttachmentUploadEventImpl(AttachmentToUpload attachmentToUpload) {
        this.attachmentToUpload = Arrays.asList(attachmentToUpload);
    }

    @Override
    public List<AttachmentToUpload> get() {
        return attachmentToUpload;
    }
}
