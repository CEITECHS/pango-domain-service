package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.AttachmentOld;

/**
 * @author  by iddymagohe on 7/30/16.
 * @since 1.0 -
 */
public interface UserProjection {
    String getUserReferenceId();
    String getFirstName();
    String getLastName();
    String getFullName();
    String getEmailAddress();
    String getPhoneNumber();
    String getVerificationPathParam();
    Address getAddress();
    AttachmentProjection getProfilePicture();
}
