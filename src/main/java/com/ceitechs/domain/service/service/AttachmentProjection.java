package com.ceitechs.domain.service.service;

/**
 * @author iddymagohe on 1/29/17.
 */
public interface AttachmentProjection {
    String getReferenceId();
    String getParentReferenceId();
    String getUrl();
    boolean isThumbnail();
    String getCategory();
    String getDescription();
}
