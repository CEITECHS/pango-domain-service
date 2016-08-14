package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.EnquiryCorrespondence;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author iddymagohe  on 8/13/16.
 */
public interface EnquiryProjection {
    String getEnquiryReferenceId();
    PropertyProjection getPropertyUnit();
    UserProjection getProspectiveTenant();
    String getMessage();
    String getSubject();
    int getCorrespondenceCount();
    List<EnquiryCorrespondence> getCorrespondences();
    LocalDateTime getEnquiryDate();

}
