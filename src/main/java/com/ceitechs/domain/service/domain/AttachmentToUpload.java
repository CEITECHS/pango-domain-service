package com.ceitechs.domain.service.domain;

import com.ceitechs.domain.service.util.ReferenceIdFor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author iddymagohe
 * @since 1.0
 */
@Getter
@ToString
public class AttachmentToUpload {
    private  String referenceId;
    private ReferenceIdFor referenceIdFor;
    private AttachmentOld attachment;
    private String parentReferenceId;

    public AttachmentToUpload(String referenceId, ReferenceIdFor referenceIdFor, AttachmentOld attachment, String parentReferenceId) {
        this.referenceId = referenceId;
        this.referenceIdFor = referenceIdFor;
        this.attachment = attachment;
        this.parentReferenceId = parentReferenceId;
    }
}
